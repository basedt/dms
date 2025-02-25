import CloseableTab from "@/components/CloseableTab";
import { useIntl } from "@umijs/max";
import React, { useEffect, useState } from "react";
import { Button, Tooltip, Space, Dropdown } from "antd";
import DmsAgGrid from "./DmsGrid";
import styles from "./style/DmsGrid.less";
import { DmsGridProps, TabItem } from "./DmsGrid.type";
import { exportDataToExcel } from "@/utils/ExcelUtil";
import DataExportModal from "./DataExportModal";

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
  >([
    { jobID: intl.formatMessage({ id: "dms.common.tabs.runLog" }), key: "0" },
  ]); // 数据列表
  const [items, setItems] = useState<TabItem[]>([]); // 标签项列表
  const [activeKey, setActiveKey] = useState<string | undefined>(); // 控制当前激活的tab

  const [dataExportData, setDataExportData] = useState<
    DMS.ModalProps<DMS.DataTask>
  >({ open: false });

  useEffect(() => {
    const initialDataList = [
      { jobID: intl.formatMessage({ id: "dms.common.tabs.runLog" }), key: "0" },
      ...dataColumns?.map(
        (data: { key: any; label: string; sql: string }, index: number) => ({
          jobID: data.label,
          key: data.key,
          tooltipTitle: data?.sql,
          data,
        })
      ),
    ];
    setDataList(initialDataList);
  }, [dataColumns]);

  useEffect(() => {
    const handleTabs = (
      result: { jobID: string; key: string | null | undefined; data?: any }[]
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
          item.jobID ===
            intl.formatMessage({ id: "dms.common.tabs.runLog" }) ? (
            <div style={{ height: "100%", overflow: "scroll" }}>
              {consoleList.map((res: any, index) => {
                return (
                  <span key={index}>
                    {res.type === "error" ? (
                      <span style={{ color: "red" }}>{res.value}</span>
                    ) : (
                      <span>{res.value}</span>
                    )}
                    <br />
                  </span>
                );
              })}
            </div>
          ) : (
            <>
              <Space
                style={{
                  width: "100%",
                  justifyContent: "flex-end",
                  padding: "4px 6px 4px 0px",
                }}
              >
                {exportMenu(item)}
              </Space>
              <DmsAgGrid item={item} key={item.jobID} />
            </>
          ),
        key: item.key,
      }));
    };
    const tabs = handleTabs(dataList);
    setItems(tabs);
    setActiveKey(dataList.at(-1)?.key || "");
  }, [dataList]);

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
                id: "dms.console.workspace.dataquery.export.current",
              }),
              key: "1",
              onClick: () => {
                exportDataToExcel(
                  { data: item?.data?.data ?? [], columns: item?.data?.columns ?? [] },
                  item.jobID
                );
              },
            },
            {
              label: intl.formatMessage({
                id: "dms.console.workspace.dataquery.export.all",
              }),
              key: "2",
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
          <Space>
            {intl.formatMessage({ id: "dms.common.operate.export" })}
          </Space>
        </Button>
      </Dropdown>
    );
  };

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
              item.key !== "0" ? (
                <Tooltip
                  title={
                    <div
                      style={{
                        color: "#000",
                        maxHeight: "300px",
                        overflow: "scroll",
                      }}
                    >
                      {item.tooltipTitle}
                    </div>
                  }
                  color="#f5f7f7"
                >
                  <span>{item.label}</span>
                </Tooltip>
              ) : (
                <span>{item.label}</span>
              ),
            key: item.key,
            closable: item.key === "0" ? false : true,
            children: item.children,
          } as any)
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
        ></DataExportModal>
      )}
    </div>
  );
};
export default React.memo<React.FC<DmsGridProps>>(DmsGrid);
