import CloseableTab, { TabItem } from '@/components/CloseableTab';
import CodeEditor from '@/components/CodeEditor';
import DmsAgent from '@/components/DmsAgent';
import AiImg from '@/icons/ai-white.svg';
import { DataSourceService } from '@/services/workspace/datasource.service';
import { history, useIntl, useModel } from '@umijs/max';
import { Button, Col, ConfigProvider, Divider, Row, Segmented, Select, Space } from 'antd';
import { createStyles } from 'antd-style';
import classNames from 'classnames';
import React, { useEffect, useRef, useState } from 'react';
import { Panel, PanelGroup, PanelResizeHandle } from 'react-resizable-panels';
import DbCatalogTreeView from './components/DbCatalogTree';
import DbSelectModal from './components/DbSelectModal';
import FileCatalogTreeView from './components/FileCatalogTree';
import './index.less';

const DataQueryView: React.FC<{ workspaceId: string | number }> = ({ workspaceId }) => {
  const intl = useIntl();
  const [tabItems, setTabItems] = useState<TabItem[] | undefined>([]);
  const [activeKey, setActiveKey] = useState<string>('');
  const [segmentedValue, setSegmentedValue] = useState<string | number>('database');
  const [windowSize, setWindowSize] = useState<{
    height: number;
    width: number;
  }>({ height: window.innerHeight, width: window.innerWidth });
  const [dbSelectData, setDbSelectData] = useState<
    DMS.ModalProps<{
      workspaceId: string | number;
      defaultValue?: number | string;
    }>
  >({ open: false });
  const [datasourceId, setDataSourceId] = useState<number | string>();
  const [datasource, setDatasource] = useState<DMS.DataSource>();
  const newTabIndex = useRef(0);
  const newTabList = useRef<TabItem[] | undefined>([]);
  const [dbList, setDbList] = useState<DMS.Dict[]>([]);
  const [selectedContent, setSelectedContent] = useState<boolean>(false);
  const { tabsList, tabsKey, tableKey, setSelectTabsActive } = useModel('global');

  const handleWindowResize = () => {
    setWindowSize({ height: window.innerHeight, width: window.innerWidth });
  };
  let dataSourceIdGlobal = JSON.parse(sessionStorage.getItem('dataSourceIdGlobal') as string);

  useEffect(() => {
    if (!datasourceId && !sessionStorage.getItem('selectOpen')) {
      setDbSelectData({ open: true, data: { workspaceId: workspaceId } });
    }
    if (dataSourceIdGlobal?.[workspaceId]) {
      setDbSelectData({ open: false, data: { workspaceId: workspaceId } });
      setDataSourceId(dataSourceIdGlobal?.[workspaceId]);
    }
    const handleBeforeUnload = (event: { preventDefault: () => void; returnValue: string }) => {
      event.preventDefault();
      return (event.returnValue = intl.formatMessage({
        id: 'dms.common.operate.delete.confirm.leavePage',
      }));
    };

    window.addEventListener('resize', handleWindowResize);
    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('resize', handleWindowResize);
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [newTabList]);

  useEffect(() => {
    if (datasourceId) {
      DataSourceService.selectOne(datasourceId as string).then((resp) => {
        if (resp.success) {
          setDatasource(resp.data);
        }
      });
    }
  }, [datasourceId]);

  useEffect(() => {
    DataSourceService.listByWorkspace(workspaceId as string).then((resp) => {
      if (resp.success) {
        setDbList(resp.data as DMS.Dict[]);
      }
    });
  }, [tableKey.current]);

  const useStyle = createStyles(({ prefixCls, css }) => ({
    linearGradientButton: css`
      &.${prefixCls}-btn-primary:not([disabled]):not(.${prefixCls}-btn-dangerous) {
        > span {
          position: relative;
        }

        &::before {
          position: absolute;
          background: linear-gradient(135deg, #6253e1, #04befe);
          border-radius: inherit;
          opacity: 1;
          transition: all 0.3s;
          content: '';
          inset: -1px;
        }

        &:hover::before {
          opacity: 0;
        }
      }
    `,
  }));
  const { styles } = useStyle();

  const tabBarOperations = (
    <div className={classNames('actionBar')}>
      <Select
        style={{ width: 220, height: 26 }}
        allowClear={false}
        showSearch={true}
        placeholder={intl.formatMessage({
          id: 'dms.console.workspace.dataquery.select',
        })}
        value={datasourceId ?? dataSourceIdGlobal?.[workspaceId]}
        onChange={(value: any) => {
          if (!value) return;
          sessionStorage.setItem(
            'dataSourceIdGlobal',
            JSON.stringify({ ...dataSourceIdGlobal, [workspaceId]: value }),
          );
          history.push(`/workspace/${workspaceId}?m=query`);
          window.location.reload();
        }}
      >
        {dbList &&
          dbList.map((item) => {
            const label: string = item.label || 'img-db';
            const dbinfo: string[] = label.split('-');
            return (
              <Select.Option key={item.value} value={item.value}>
                <Space style={{ display: 'flex', alignItems: 'center' }}>
                  <img
                    src={'/images/databases/' + dbinfo[0].toLowerCase() + '.svg'}
                    style={{ width: 16, height: 16 }}
                    alt=""
                  />
                  {dbinfo[1]}
                </Space>
              </Select.Option>
            );
          })}
      </Select>
      <div className={classNames('actionBar_aiButton')}>
        <span onClick={() => setSelectedContent(!selectedContent)}>
          <ConfigProvider
            button={{
              className: styles.linearGradientButton,
            }}
          >
            <Button
              type="primary"
              style={{ height: 26, width: 55 }}
              icon={
                <img
                  style={{ height: 18, width: 18, maxWidth: 18, marginBottom: 3 }}
                  src={AiImg}
                  alt=""
                />
              }
            >
              AI
            </Button>
          </ConfigProvider>
        </span>
      </div>
    </div>
  );
  const newEditorTab = (
    tabName: string,
    node: DMS.FileTreeNode<string> | DMS.CatalogTreeNode<string>,
  ) => {
    if (
      tabItems?.some((item) => {
        if (!item?.parentId) return false;
        if (item?.label == tabName && item?.parentId == node?.parentId) {
          setActiveKey(item.key);
          tabsKey.current = item.key;
          return true;
        }
        return false;
      })
    ) {
      return;
    }

    const newTabItem: TabItem = {
      key: `tab${++newTabIndex.current}`,
      label: tabName,
      children: (
        <CodeEditor
          theme="clouds"
          unSave={unSave}
          language="sql"
          fileId={`tab${newTabIndex.current}`}
          maxHeight={windowSize.height - 110}
          dataSourceId={datasourceId as string}
          workspaceId={workspaceId}
          fileName={node?.title}
          parentId={node?.parentId}
        />
      ),
      parentId: node?.parentId,
      keyId: node?.key,
    };
    setSelectTabsActive({
      keyId: newTabItem?.keyId,
      label: newTabItem?.label,
    });
    newTabList.current = tabItems ? [...tabItems, newTabItem] : [newTabItem];
    setTabItems(tabItems ? [...tabItems, newTabItem] : [newTabItem]);
    setActiveKey(newTabItem.key);
    tabsKey.current = newTabItem.key;
  };

  // 保存||未保存执行
  const unSave = (d: string | number | undefined, type: string): void => {
    const updateElement = (element: any, unsaveStyle: boolean, closable: boolean) => {
      if (element.key === d) {
        element['unsaveStyle'] = unsaveStyle;
        element['closable'] = closable;
      }
    };

    const updateTabList = (unsaveStyle: boolean, closable: boolean) => {
      const currentTabList = newTabList?.current ?? [];
      currentTabList.forEach((element) => updateElement(element, unsaveStyle, closable));
      setTabItems([...currentTabList]);
    };
    if (type === 'notSave') {
      updateTabList(true, false);
    } else {
      updateTabList(false, true);
    }
  };

  useEffect(() => {
    if (tabsList && tabItems) {
      const updatedTabItems = tabItems.map((item) =>
        item.keyId === tabsList.id ? { ...item, label: tabsList.newTitle } : item,
      );
      newTabList.current = updatedTabItems;
      setTabItems(updatedTabItems);
    }
  }, [tabsList]);

  const tabBarRender = (
    <>
      <CloseableTab
        size="small"
        items={tabItems}
        onTabChange={(item) => {
          tabItems?.forEach((sitem: TabItem) => {
            if (sitem.key === item) {
              setSelectTabsActive({
                keyId: sitem.keyId,
                label: sitem.label,
              });
            }
          });
          setActiveKey(item);
          tabsKey.current = item;
        }}
        saveType={true}
        tabBarExtraContent={tabBarOperations}
        defaultActiveKey={activeKey}
        onTabClose={(items: TabItem[] | undefined, activeKey: string) => {
          newTabList.current = items;
          setTabItems(items);
          setActiveKey(activeKey);
          tabsKey.current = activeKey;
          items?.forEach((sitem: TabItem) => {
            if (sitem.key === activeKey) {
              setSelectTabsActive({
                keyId: sitem.keyId,
                label: sitem.label,
              });
              return;
            }
          });
          if (!items || items.length === 0) {
            setSelectTabsActive({
              keyId: '',
              label: '',
            });
          }
        }}
      />
    </>
  );

  return (
    <>
      <PanelGroup direction="horizontal">
        <Panel
          defaultSize={24}
          minSize={0}
          style={{ padding: '1px 2px' }}
          className={classNames('panel-horizontal')}
        >
          <Row gutter={[6, 6]}>
            <Col span={24}>
              <Segmented
                block
                options={[
                  {
                    value: 'database',
                    label: intl.formatMessage({
                      id: 'dms.console.workspace.dataquery.database',
                    }),
                  },
                  {
                    value: 'file',
                    label: intl.formatMessage({
                      id: 'dms.console.workspace.dataquery.file',
                    }),
                  },
                ]}
                value={segmentedValue}
                onChange={(value: any) => {
                  setSegmentedValue(value);
                }}
              />
            </Col>
          </Row>
          <Divider style={{ marginTop: 1, marginBottom: 7 }} />
          <div
            style={{
              display: segmentedValue === 'database' ? 'block' : 'none',
            }}
          >
            <DbCatalogTreeView
              workspaceId={workspaceId}
              datasourceId={datasourceId as string}
              height={windowSize.height - 125}
              onCallback={(action, node: DMS.CatalogTreeNode<string>) => {
                newEditorTab(action, node);
              }}
            />
          </div>
          <div style={{ display: segmentedValue === 'file' ? 'block' : 'none' }}>
            <FileCatalogTreeView
              workspaceId={workspaceId}
              datasourceId={datasourceId as string}
              height={windowSize.height - 125}
              onCallback={(action, node: DMS.FileTreeNode<string>) => {
                newEditorTab(action, node);
              }}
            />
          </div>
        </Panel>
        <PanelResizeHandle className={classNames('panel_handle_hover')} />
        <Panel defaultSize={76} className={classNames('panel-horizontal')}>
          {tabBarRender}
        </Panel>
        {selectedContent && (
          <>
            <PanelResizeHandle className={classNames('panel_handle_hover')} />
            <Panel defaultSize={50} minSize={0}>
              <div className={classNames('codeEditor_aiContent')}>
                <DmsAgent
                  msgs={['database type is ' + datasource?.datasourceType?.label]}
                  onClose={() => {
                    setSelectedContent(false);
                  }}
                />
              </div>
            </Panel>
          </>
        )}
      </PanelGroup>

      {dbSelectData.open && (
        <DbSelectModal
          open={dbSelectData.open}
          data={dbSelectData.data}
          handleOk={(isOpen: boolean, value: any) => {
            setDbSelectData({
              open: false,
              data: { workspaceId: workspaceId, defaultValue: value },
            });
            if (!value) return;
            setDataSourceId(value);
            sessionStorage.setItem(
              'dataSourceIdGlobal',
              JSON.stringify({ ...dataSourceIdGlobal, [workspaceId]: value }),
            );
          }}
          handleCancel={() => {
            setDbSelectData({ open: false });
          }}
        />
      )}
    </>
  );
};
export default React.memo(DataQueryView);
