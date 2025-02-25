import { PageContainer } from "@ant-design/pro-components";
import { Col, Menu, Row } from "antd";
import React, { useState } from "react";
import { useIntl } from "@umijs/max";
import EmailSetting from "./EmailSetting";

const SettingView: React.FC = () => {
  const intl = useIntl();
  const [selectedKey, setSelectKey] = useState<string>("email");

  const renderChildren = () => {
    switch (selectedKey) {
      case "email":
        return <EmailSetting></EmailSetting>;
      default:
        return null;
    }
  };

  return (
    <PageContainer header={{ title: null, breadcrumb: {} }}>
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
                key: "email",
                label: intl.formatMessage({ id: "dms.admin.setting.email" }),
              },
            ]}
          ></Menu>
        </Col>
        <Col flex="auto" style={{ paddingLeft: 18 }}>
          {renderChildren()}
        </Col>
      </Row>
    </PageContainer>
  );
};

export default SettingView;
