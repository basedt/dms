import React, { useEffect, useState, useMemo } from "react";
import { Dropdown, MenuProps, Tabs, Typography, Modal } from "antd";
import { CloseOutlined } from "@ant-design/icons";
import { useIntl } from "@umijs/max";
import classNames from "classnames";
import "./index.less";

export type TabItem = {
  key: string;
  label: React.ReactNode;
  children: React.ReactNode;
  closable?: boolean;
  unsaveStyle?: boolean;
  parentId?: string | number;
  keyId?: string;
};

type CloseableTabProps = {
  size: "small" | "middle" | "large" | undefined;
  items?: TabItem[];
  saveType?: boolean;
  defaultActiveKey?: string;
  tabBarExtraContent?: React.ReactNode;
  onTabClose: (items: TabItem[] | undefined, activeKey: string) => void;
  onTabChange?: (activeKey: string) => void;
};

const TurnOffTans = (props: any) => {
  return <Modal {...props}>{props.content}</Modal>;
};

const CloseableTab: React.FC<CloseableTabProps> = ({
  size,
  items,
  saveType = false,
  defaultActiveKey,
  tabBarExtraContent,
  onTabClose,
  onTabChange,
}) => {
  const intl = useIntl();
  const [activeKey, setActiveKey] = useState<string | undefined>(
    defaultActiveKey
  );
  const [isModalOpen, setIsModalOpen] = useState<{
    visible: boolean;
    fnArgument: () => void;
  }>({
    visible: false,
    fnArgument: () => {},
  });

  const unsavedHint = useMemo(() => {
    return {
      title: intl.formatMessage({ id: "dms.common.operate.warn" }),
      content: intl.formatMessage({
        id: "dms.common.operate.delete.confirm.saveContent",
      }),
      visible: isModalOpen.visible,
      onCancel: () => setIsModalOpen({ visible: false, fnArgument: () => {} }),
      onClose: () => setIsModalOpen({ visible: false, fnArgument: () => {} }),
      onOk: () => {
        isModalOpen.fnArgument();
        setIsModalOpen({ visible: false, fnArgument: () => {} });
      },
      closeIcon: true,
      maskClosable: false,
      okText: intl.formatMessage({ id: "dms.common.operate.confirm" }),
      cancelText: intl.formatMessage({ id: "dms.common.operate.cancel" }),
    };
  }, [isModalOpen, intl]);

  const close = (key: string) => {
    const index = items?.findIndex((pane) => pane.key === key);
    const newPanes = items?.filter((pane) => pane.key !== key);
    let newActiveKey = "";
    if (index !== undefined && newPanes && key === activeKey) {
      if (newPanes.length === 0) {
        newActiveKey = activeKey as string;
      } else {
        newActiveKey =
          newPanes[index === newPanes.length ? index - 1 : index].key;
      }
    } else {
      newActiveKey = activeKey as string;
    }
    onTabClose(newPanes, newActiveKey);
  };

  const closeOther = (key: string) => {
    const newPanes = items?.filter((pane) => pane.key === key);
    if (items?.some((item) => item.key === "0")) {
      newPanes?.unshift(items[0]);
    }
    onTabClose(newPanes, "");
  };

  const closeAll = () => {
    if (items?.some((item) => item.key === "0")) {
      onTabClose([items[0]], "");
      return;
    }
    onTabClose([], "");
  };

  const judgment = (
    key: string,
    type: "close" | "closeOther" | "closeAll"
  ): boolean => {
    if (type === "close") {
      const item = items?.find((item) => item.key === key);
      return item?.unsaveStyle ?? false;
    } else if (type === "closeOther") {
      const hasUnsaved = items?.some(
        (pane) => pane.key !== key && pane.unsaveStyle
      );
      return hasUnsaved ?? false;
    } else {
      return items?.some((item) => item.unsaveStyle) ?? false;
    }
  };

  const menuItems = (tabKey: string): MenuProps["items"] => [
    {
      label: intl.formatMessage({ id: "dms.common.tabs.card.close" }),
      key: "close",
      onClick: () => {
        if (!saveType || !judgment(tabKey, "close")) {
          close(tabKey);
        } else {
          setIsModalOpen({ visible: true, fnArgument: () => close(tabKey) });
        }
      },
    },
    {
      label: intl.formatMessage({ id: "dms.common.tabs.card.closeOther" }),
      key: "closeOther",
      onClick: () => {
        if (!saveType || !judgment(tabKey, "closeOther")) {
          closeOther(tabKey);
        } else {
          setIsModalOpen({
            visible: true,
            fnArgument: () => closeOther(tabKey),
          });
        }
      },
    },
    {
      label: intl.formatMessage({ id: "dms.common.tabs.card.closeAll" }),
      key: "closeAll",
      onClick: () => {
        if (!saveType || !judgment(tabKey, "closeAll")) {
          closeAll();
        } else {
          setIsModalOpen({ visible: true, fnArgument: () => closeAll() });
        }
      },
    },
  ];

  useEffect(() => {
    if (!defaultActiveKey) {
      setActiveKey(items && items.length > 0 ? items[0].key : "");
    } else {
      setActiveKey(defaultActiveKey);
    }
  }, [items, defaultActiveKey]);

  const toClosableTabItem = (
    item: TabItem
  ): TabItem & { label: React.ReactNode } => {
    return {
      ...item,
      label: (
        <Dropdown
          menu={{ items: menuItems(item.key) }}
          trigger={["contextMenu"]}
          className={classNames({ "tab-notCave": item?.unsaveStyle })}
        >
          <div className={classNames("tab-card")}>
            <Typography.Text
              ellipsis={{ tooltip: { placement: "bottom" } }}
              className={classNames("tab-card-title")}
            >
              {item.label}
            </Typography.Text>
            <CloseOutlined
              className={classNames("tab-card-icon")}
              onClick={() => {
                if (!saveType || !judgment(item.key, "close")) {
                  close(item.key);
                } else {
                  setIsModalOpen({
                    visible: true,
                    fnArgument: () => close(item.key),
                  });
                }
              }}
            />
          </div>
        </Dropdown>
      ),
    };
  };

  return (
    <>
      <Tabs
        size={size}
        activeKey={activeKey}
        className={classNames("closeable-tabs")}
        tabBarGutter={4}
        hideAdd
        onChange={(key) => {
          setActiveKey(key);
          onTabChange?.(key);
        }}
        items={items?.map((item) => toClosableTabItem(item))}
        tabBarExtraContent={tabBarExtraContent}
      />
      {isModalOpen.visible && <TurnOffTans {...unsavedHint} />}
    </>
  );
};

export default CloseableTab;
