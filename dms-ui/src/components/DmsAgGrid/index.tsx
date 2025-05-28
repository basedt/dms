import CloseableTab from '@/components/CloseableTab';
import { exportDataToExcel } from '@/utils/ExcelUtil';
import { SettingOutlined } from '@ant-design/icons';
import { useIntl } from '@umijs/max';
import { Button, Checkbox, Dropdown, Popover, Space, Tooltip } from 'antd';
import React, { useEffect, useMemo, useState } from 'react';
import DataExportModal from './DataExportModal';
import DmsAgGrid from './DmsGrid';
import { DmsGridProps, TabItem } from './DmsGrid.type';
import styles from './style/DmsGrid.less';
const CheckboxGroup = Checkbox.Group;

const DmsGrid: React.FC<DmsGridProps> = ({
  dataColumns,
  clearListData,
  consoleList,
  workspaceId,
  datasourceId,
}) => {
  const intl = useIntl();
  const [dataList, setDataList] = useState<
    { jobID: string; key: string | null | undefined; data?: any }[]
  >([{ jobID: intl.formatMessage({ id: 'dms.common.tabs.runLog' }), key: '0' }]);
  const [items, setItems] = useState<TabItem[]>([]);
  const [activeKey, setActiveKey] = useState<string | undefined>();
  // 改为使用对象存储每个表格的列选择状态
  const [checkedLists, setCheckedLists] = useState<Record<string, string[]>>({});
  const [dataExportData, setDataExportData] = useState<DMS.ModalProps<DMS.DataTask>>({
    open: false,
  });

  // 使用useMemo优化列选项计算
  const columnsOptions = useMemo(() => {
    const options: Record<string, string[]> = {};
    dataList?.forEach((item) => {
      if (item?.jobID !== intl.formatMessage({ id: 'dms.common.tabs.runLog' })) {
        options[item.key || ''] =
          item?.data?.columns
            ?.filter((col: any) => col?.dataIndex)
            .map((col: any) => col.dataIndex) || [];
      }
    });
    return options;
  }, [dataList, intl]);

  // 初始化或更新每个表格的列选择状态
  useEffect(() => {
    const newCheckedLists: Record<string, string[]> = {};
    Object.keys(columnsOptions).forEach((key) => {
      newCheckedLists[key] = checkedLists[key] || [...columnsOptions[key]];
    });
    setCheckedLists(newCheckedLists);
  }, [columnsOptions]);

  const getCheckAllStatus = (key: string) => {
    const options = columnsOptions[key] || [];
    const checkedList = checkedLists[key] || [];
    return {
      checkAll: options.length === checkedList.length,
      indeterminate: checkedList.length > 0 && checkedList.length < options.length,
    };
  };

  const onCheckAllChange = (key: string, e: any) => {
    setCheckedLists((prev) => ({
      ...prev,
      [key]: e.target.checked ? [...columnsOptions[key]] : [],
    }));
  };

  const onChange = (key: string, list: string[]) => {
    setCheckedLists((prev) => ({
      ...prev,
      [key]: list,
    }));
  };

  const handleResetColumns = (key: string) => {
    setCheckedLists((prev) => ({
      ...prev,
      [key]: [...columnsOptions[key]],
    }));
  };

  useEffect(() => {
    const initialDataList = [
      { jobID: intl.formatMessage({ id: 'dms.common.tabs.runLog' }), key: '0' },
      ...dataColumns?.map((data: { key: any; label: string; sql: string }, index: number) => ({
        jobID: data.label,
        key: data.key,
        tooltipTitle: data?.sql,
        data,
      })),
    ];
    setDataList(initialDataList);
    setActiveKey(initialDataList.at(-1)?.key || '');
  }, [dataColumns]);

  const renderColumnSettings = (item: {
    jobID: string;
    key: string | null | undefined;
    data?: any;
  }) => {
    if (!item.key || item.key === '0') return null;

    const { checkAll, indeterminate } = getCheckAllStatus(item.key);
    const options = columnsOptions[item.key] || [];
    const checkedList = checkedLists[item.key] || [];

    return (
      <Popover
        placement="bottomRight"
        trigger="click"
        arrow={false}
        title={
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Checkbox
              indeterminate={indeterminate}
              onChange={(e) => onCheckAllChange(item.key!, e)}
              checked={checkAll}
            >
              {intl.formatMessage({ id: 'dms.common.operate.select.all' })}
            </Checkbox>
            <Button type="link" size="small" onClick={() => handleResetColumns(item.key!)}>
              {intl.formatMessage({ id: 'dms.common.operate.reset' })}
            </Button>
          </div>
        }
        content={
          <div style={{ maxHeight: '300px', overflow: 'auto' }}>
            <CheckboxGroup
              options={options}
              value={checkedList}
              onChange={(list) => onChange(item.key!, list)}
              style={{ display: 'flex', flexDirection: 'column' }}
            />
          </div>
        }
      >
        <SettingOutlined />
      </Popover>
    );
  };

  const exportMenu = (item: {
    jobID: string;
    key: string | null | undefined;
    data?: any;
    columns?: any;
  }) => {
    return (
      <Dropdown
        menu={{
          items: [
            {
              label: intl.formatMessage({
                id: 'dms.console.workspace.dataquery.export.current',
              }),
              key: '1',
              onClick: () => {
                exportDataToExcel(
                  { data: item?.data?.data ?? [], columns: item?.data?.columns ?? [] },
                  item.jobID,
                );
              },
            },
            {
              label: intl.formatMessage({
                id: 'dms.console.workspace.dataquery.export.all',
              }),
              key: '2',
              onClick: () => {
                setDataExportData({
                  open: true,
                  data: {
                    workspaceId: workspaceId,
                    datasourceId: datasourceId,
                    sqlScript: item.data.sql,
                  },
                });
              },
            },
          ],
        }}
      >
        <Button size="small">
          <Space>{intl.formatMessage({ id: 'dms.common.operate.export' })}</Space>
        </Button>
      </Dropdown>
    );
  };

  useEffect(() => {
    const handleTabs = (
      result: { jobID: string; key: string | null | undefined; data?: any }[],
    ): TabItem[] => {
      if (!result || result.length === 0) return [];
      return result.map((item) => ({
        ...item,
        label: (
          <div className={styles.bottomIcon}>
            <span>{item.jobID}</span>
          </div>
        ),
        children:
          item.jobID === intl.formatMessage({ id: 'dms.common.tabs.runLog' }) ? (
            <div style={{ height: '100%', overflow: 'scroll' }}>
              {consoleList.map((res: any, index) => {
                return (
                  <span key={index}>
                    {res.type === 'error' ? (
                      <span style={{ color: 'red' }}>{res.value}</span>
                    ) : (
                      <span>{res.value}</span>
                    )}
                  </span>
                );
              })}
            </div>
          ) : (
            <>
              <Space
                style={{
                  width: '100%',
                  justifyContent: 'flex-end',
                  padding: '4px 12px 4px 0px',
                }}
              >
                {exportMenu(item)}
                {renderColumnSettings(item)}
              </Space>
              <DmsAgGrid
                item={item}
                key={item.jobID}
                visibleColumns={checkedLists[item.key || ''] || []}
              />
            </>
          ),
        key: item.key,
      }));
    };
    const tabs = handleTabs(dataList);
    setItems(tabs);
  }, [dataList, checkedLists, columnsOptions]);

  return (
    <div className={styles.editorRightResult}>
      <CloseableTab
        size="small"
        defaultActiveKey={activeKey}
        onTabClose={(items: TabItem[] | undefined, activeKey: string) => {
          setItems(items || []);
          setActiveKey(activeKey);
          clearListData(items || []);
        }}
        items={items?.map(
          (item) =>
            ({
              label:
                item.key !== '0' ? (
                  <Tooltip
                    title={
                      <div
                        style={{
                          color: '#000',
                          maxHeight: '300px',
                          overflow: 'scroll',
                        }}
                      >
                        {item.tooltipTitle}
                      </div>
                    }
                    color="#f5f7f7"
                  >
                    <span
                      onClick={() => {
                        setActiveKey(item?.key);
                      }}
                    >
                      {item.label}
                    </span>
                  </Tooltip>
                ) : (
                  <span>{item.label}</span>
                ),
              key: item.key,
              closable: item.key === '0' ? false : true,
              children: item.children,
            } as any),
        )}
      />
      {dataExportData.open && (
        <DataExportModal
          open={dataExportData.open}
          data={dataExportData.data}
          handleOk={(isOpen: boolean) => {
            setDataExportData({ open: isOpen });
          }}
          handleCancel={() => {
            setDataExportData({ open: false });
          }}
        />
      )}
    </div>
  );
};
export default React.memo(DmsGrid);
