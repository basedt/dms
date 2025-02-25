import { PageContainer } from "@ant-design/pro-components";
import { Col, Menu, Row } from "antd";
import React, { useEffect, useState } from "react";
import { useIntl, useLocation } from "@umijs/max";
import UserLog from "./components/UserLog";
import UserMessage from "./components/UserMessage";
import UserProfile from "./components/UserProfile";
import UserSecurity from "./components/UserSecurity";

const UserCenter: React.FC = () => {
  const intl = useIntl();
  const [selectedKey, setSelectKey] = useState<string>("profile");
  const target: { key: string } = useLocation().state as { key: string };
  const renderChildren = () => {
    switch (selectedKey) {
      case "profile":
        return <UserProfile></UserProfile>;
      case "security":
        return <UserSecurity></UserSecurity>;
      case "message":
        return <UserMessage></UserMessage>;
      case "log":
        return <UserLog></UserLog>;
      default:
        return <></>;
    }
  };

  useEffect(() => {
    if (target?.key) {
      setSelectKey(target.key);
    }
  }, []);

  return (
    <PageContainer header={{ title: null, breadcrumb: {} }}>
      <Row>
        <Col span={4}></Col>
        <Col span={16}>
          <Row>
            <Col flex="220px">
              <Menu
                mode="vertical"
                selectedKeys={[selectedKey]}
                onClick={({ key }) => {
                  setSelectKey(key);
                }}
                items={[
                  {
                    key: "profile",
                    label: intl.formatMessage({
                      id: "dms.user.center.profile",
                    }),
                  },
                  {
                    key: "security",
                    label: intl.formatMessage({
                      id: "dms.user.center.security",
                    }),
                  },
                  {
                    key: "message",
                    label: intl.formatMessage({
                      id: "dms.user.center.message",
                    }),
                  },
                  {
                    key: "log",
                    label: intl.formatMessage({
                      id: "dms.user.center.log",
                    }),
                  },
                ]}
              ></Menu>
            </Col>
            <Col flex="auto" style={{ paddingLeft: 18 }}>
              {renderChildren()}
            </Col>
          </Row>
        </Col>
        <Col span={4}></Col>
      </Row>
    </PageContainer>
  );
};

export default UserCenter;
