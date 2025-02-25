import CloseableTab, { TabItem } from '@/components/CloseableTab';
import CodeEditor from '@/components/CodeEditor';
import { DatabaseOutlined } from '@ant-design/icons';
import { useIntl, useModel } from '@umijs/max';
import { Button, Col, Divider, Row, Segmented, Tooltip } from 'antd';
import classNames from 'classnames';
import React, { useEffect, useRef, useState } from 'react';
import Split from 'react-split';
import DbCatalogTreeView from './components/DbCatalogTree';
import DbSelectModal from './components/DbSelectModal';
import FileCatalogTreeView from './components/FileCatalogTree';
import './index.less';

const DataQueryView: React.FC<{ workspaceId: string | number }> = ({
  workspaceId,
}) => {
  const intl = useIntl();
  const [showLeft, setShowLeft] = useState<boolean>(true);
  const [horizontalSplitSizes, setHorizontalSplitSizes] = useState<number[]>([20, 80,]);
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
  const newTabIndex = useRef(0);
  const newTabList = useRef<TabItem[] | undefined>([]);
  const { tabsList, tabsKey } = useModel('global');

  const handleWindowResize = () => {
    setWindowSize({ height: window.innerHeight, width: window.innerWidth });
  };

  useEffect(() => {
    if (!datasourceId) {
      setDbSelectData({ open: true, data: { workspaceId: workspaceId } });
    }
    const handleBeforeUnload = (event: { preventDefault: () => void; returnValue: string; }) => {
      // 阻止默认对话框显示
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

  // useEffect(() => {
  //   //TODO 页面加载时候，获取上次打开的文件列表，如果列表有变动，则将其存储到localstore中

  // }, [datasourceId]);

  const tabBarOperations = (
    <Tooltip
      title={intl.formatMessage({
        id: 'dms.console.workspace.dataquery.select',
      })}
    >
      <Button
        icon={<DatabaseOutlined />}
        type="text"
        style={{ marginTop: 3, marginBottom: 3 }}
        onClick={() => {
          const d = dbSelectData;
          setDbSelectData({
            open: true,
            data: { ...d.data, workspaceId: workspaceId },
          });
        }}
      />
    </Tooltip>
  );
  const newEditorTab = (
    tabName: string,
    node: DMS.FileTreeNode<string> | DMS.CatalogTreeNode<string>
  ) => {
    //TODO 创建tab时候，判断是代码文件还是查询结果，分别初始化内容进行渲染
    if (
      tabItems?.some((item) => {
        if (!item?.parentId) return false;
        if (item?.label == tabName && item?.parentId == node?.parentId) {
          setActiveKey(item.key);
          tabsKey.current = item.key
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
          theme='clouds'
          unSave={unSave}
          language='sql'
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
    newTabList.current = tabItems ? [...tabItems, newTabItem] : [newTabItem]
    setTabItems(tabItems ? [...tabItems, newTabItem] : [newTabItem]);
    setActiveKey(newTabItem.key);
    tabsKey.current = newTabItem.key
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
      currentTabList.forEach(element => updateElement(element, unsaveStyle, closable));
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
        item.keyId === tabsList.id
          ? { ...item, label: tabsList.newTitle }
          : item
      );
      newTabList.current = updatedTabItems
      setTabItems(updatedTabItems);
    }
  }, [tabsList]);
  const tabBarRender = (
    <>
      <CloseableTab
        size="small"
        items={tabItems}
        onTabChange={(item) => {
          setActiveKey(item);
          tabsKey.current = item
        }}
        saveType={true}
        tabBarExtraContent={tabBarOperations}
        defaultActiveKey={activeKey}
        onTabClose={(items: TabItem[] | undefined, activeKey: string) => {
          newTabList.current = items
          setTabItems(items);
          setActiveKey(activeKey);
          tabsKey.current = activeKey
        }}
      />
    </>
  );
  return (
    <>
      <Split
        className={classNames('split-horizontal')}
        direction="horizontal"
        gutterSize={4}
        sizes={horizontalSplitSizes}
        minSize={[0, 0]}
        maxSize={[680, Infinity]}
        snapOffset={100}
        onDrag={(sizes: number[]) => {
          if (sizes[0] <= 6) {
            setShowLeft(false);
          } else {
            setShowLeft(true);
          }
          setHorizontalSplitSizes(sizes);
        }}
      >
        <div>
          <div
            className="container-left"
            style={{ display: showLeft ? 'block' : 'none' }}
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
                  size="small"
                  value={segmentedValue}
                  onChange={(value) => {
                    setSegmentedValue(value);
                  }}
                />
              </Col>
            </Row>
            <Divider style={{ marginTop: 7, marginBottom: 7 }} />
            <div
              style={{
                display: segmentedValue === 'database' ? 'block' : 'none',
              }}
            >
              <DbCatalogTreeView
                workspaceId={workspaceId}
                datasourceId={datasourceId as string}
                height={windowSize.height - 125}
                onCallback={(action: string, node: DMS.CatalogTreeNode<string>) => {
                  newEditorTab(action, node);
                }}
              />
            </div>
            <div
              style={{ display: segmentedValue === 'file' ? 'block' : 'none' }}
            >
              <FileCatalogTreeView
                workspaceId={workspaceId}
                datasourceId={datasourceId as string}
                height={windowSize.height - 125}
                onCallback={(action: string, node: DMS.FileTreeNode<string>) => {
                  newEditorTab(action, node);
                }}
              />
            </div>
          </div>
        </div>
        <div>
          <div className={classNames('container-right')}>{tabBarRender}</div>
        </div>
      </Split>
      {dbSelectData.open && (
        <DbSelectModal
          open={dbSelectData.open}
          data={dbSelectData.data}
          handleOk={(isOpen: boolean, value: any) => {
            setDataSourceId(value);
            setDbSelectData({
              open: false,
              data: { workspaceId: workspaceId, defaultValue: value },
            });
          }}
          handleCancel={() => {
            setDbSelectData({ open: false });
          }}
        />
      )}
    </>
  );
};
export default DataQueryView;
