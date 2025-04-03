import { PageContainer } from '@ant-design/pro-components';
import { useIntl } from '@umijs/max';
import { Col, Menu, Row } from 'antd';
import React, { useState } from 'react';
import EmailSetting from './EmailSetting';
import LLMSetting from './LLMSetting';

const SettingView: React.FC = () => {
  const intl = useIntl();
  const [selectedKey, setSelectKey] = useState<string>('email');

  const renderChildren = () => {
    switch (selectedKey) {
      case 'email':
        return <EmailSetting></EmailSetting>;
      case 'llm':
        return <LLMSetting></LLMSetting>;
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
                key: 'email',
                label: intl.formatMessage({ id: 'dms.admin.setting.email' }),
              },
              {
                key: 'llm',
                label: intl.formatMessage({ id: 'dms.admin.setting.llm' }),
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
