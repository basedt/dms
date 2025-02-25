import CloseableTab, { TabItem } from "@/components/CloseableTab";
import CodeEditor from "@/components/CodeEditor";
import { useIntl, useModel, history } from "@umijs/max";
import { Col, Divider, Row, Segmented, Select } from "antd";
import classNames from "classnames";
import React, { useEffect, useRef, useState } from "react";
import DbCatalogTreeView from "./components/DbCatalogTree";
import DbSelectModal from "./components/DbSelectModal";
import FileCatalogTreeView from "./components/FileCatalogTree";
import { DataSourceService } from "@/services/workspace/datasource.service";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import "./index.less";

const DataQueryView: React.FC<{ workspaceId: string | number }> = ({
  workspaceId,
}) => {
  const intl = useIntl();
  const [tabItems, setTabItems] = useState<TabItem[] | undefined>([]);
  const [activeKey, setActiveKey] = useState<string>("");
  const [segmentedValue, setSegmentedValue] = useState<string | number>(
    "database"
  );
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
  const [dbList, setDbList] = useState<DMS.Dict[]>([]);
  const { tabsList, tabsKey, tableKey } = useModel("global");

  const handleWindowResize = () => {
    setWindowSize({ height: window.innerHeight, width: window.innerWidth });
  };
  let dataSourceIdGlobal = JSON.parse(
    sessionStorage.getItem("dataSourceIdGlobal") as string
  );

  useEffect(() => {
    if (!datasourceId && !sessionStorage.getItem("selectOpen")) {
      setDbSelectData({ open: true, data: { workspaceId: workspaceId } });
    }
    if (dataSourceIdGlobal?.[workspaceId]) {
      setDbSelectData({ open: false, data: { workspaceId: workspaceId } });
      setDataSourceId(dataSourceIdGlobal?.[workspaceId]);
    }
    const handleBeforeUnload = (event: {
      preventDefault: () => void;
      returnValue: string;
    }) => {
      event.preventDefault();
      return (event.returnValue = intl.formatMessage({
        id: "dms.common.operate.delete.confirm.leavePage",
      }));
    };

    window.addEventListener("resize", handleWindowResize);
    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      window.removeEventListener("resize", handleWindowResize);
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [newTabList]);

  useEffect(() => {
    DataSourceService.listByWorkspace(workspaceId as string).then((resp) => {
      if (resp.success) {
        setDbList(resp.data as DMS.Dict[]);
      }
    });
  }, [tableKey.current]);

  const tabBarOperations = (
    <div style={{ padding: "5px" }}>
      <Select
        style={{ width: 220, height: 26 }}
        allowClear={false}
        showSearch={true}
        placeholder={intl.formatMessage({
          id: "dms.console.workspace.dataquery.select",
        })}
        value={datasourceId ?? dataSourceIdGlobal?.[workspaceId]}
        onChange={(value) => {
          if (!value) return;
          sessionStorage.setItem(
            "dataSourceIdGlobal",
            JSON.stringify({ ...dataSourceIdGlobal, [workspaceId]: value })
          );
          history.push(`/workspace/${workspaceId}?m=query`);
          window.location.reload();
        }}
      >
        {dbList &&
          dbList.map((item) => {
            return (
              <Select.Option key={item.value} value={item.value}>
                {item.label}
              </Select.Option>
            );
          })}
      </Select>
    </div>
  );
  const newEditorTab = (
    tabName: string,
    node: DMS.FileTreeNode<string> | DMS.CatalogTreeNode<string>
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
    newTabList.current = tabItems ? [...tabItems, newTabItem] : [newTabItem];
    setTabItems(tabItems ? [...tabItems, newTabItem] : [newTabItem]);
    setActiveKey(newTabItem.key);
    tabsKey.current = newTabItem.key;
  };

  // 保存||未保存执行
  const unSave = (d: string | number | undefined, type: string): void => {
    const updateElement = (
      element: any,
      unsaveStyle: boolean,
      closable: boolean
    ) => {
      if (element.key === d) {
        element["unsaveStyle"] = unsaveStyle;
        element["closable"] = closable;
      }
    };

    const updateTabList = (unsaveStyle: boolean, closable: boolean) => {
      const currentTabList = newTabList?.current ?? [];
      currentTabList.forEach((element) =>
        updateElement(element, unsaveStyle, closable)
      );
      setTabItems([...currentTabList]);
    };
    if (type === "notSave") {
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
        }}
      />
    </>
  );
  return (
    <>
      <PanelGroup direction="horizontal">
        <Panel
          defaultSize={20}
          minSize={0}
          style={{ padding: "1px 2px" }}
          className={classNames("panel-horizontal")}
        >
          <Row gutter={[6, 6]}>
            <Col span={24}>
              <Segmented
                block
                options={[
                  {
                    value: "database",
                    label: intl.formatMessage({
                      id: "dms.console.workspace.dataquery.database",
                    }),
                  },
                  {
                    value: "file",
                    label: intl.formatMessage({
                      id: "dms.console.workspace.dataquery.file",
                    }),
                  },
                ]}
                value={segmentedValue}
                onChange={(value) => {
                  setSegmentedValue(value);
                }}
              />
            </Col>
          </Row>
          <Divider style={{ marginTop: 1, marginBottom: 7 }} />
          <div
            style={{
              display: segmentedValue === "database" ? "block" : "none",
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
          <div
            style={{ display: segmentedValue === "file" ? "block" : "none" }}
          >
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
        <PanelResizeHandle className={classNames("panel_handle_hover")} />
        <Panel
          defaultSize={80}
          minSize={60}
          className={classNames("panel-horizontal")}
        >
          {tabBarRender}
        </Panel>
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
              "dataSourceIdGlobal",
              JSON.stringify({ ...dataSourceIdGlobal, [workspaceId]: value })
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
