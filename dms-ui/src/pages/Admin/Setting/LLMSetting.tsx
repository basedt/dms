import { ConfigService } from '@/services/admin/config.service';
import { useIntl } from '@umijs/max';
import { Button, Col, Form, Input, message, Row, Select, Typography } from 'antd';
import { useEffect } from 'react';

const LLMSetting: React.FC = () => {
  const intl = useIntl();
  const [form] = Form.useForm();

  useEffect(() => {
    ConfigService.getLLMInfo().then((resp) => {
      if (resp.success) {
        form.setFieldsValue(resp.data);
      }
    });
  }, []);

  return (
    <>
      <Typography.Title level={4} style={{ marginTop: 0, marginBottom: 18 }}>
        {intl.formatMessage({ id: 'dms.admin.setting.llm' })}
      </Typography.Title>
      <Row>
        <Col span={8}>
          <Form
            name="llmSetting"
            form={form}
            layout="vertical"
            initialValues={{ port: 25 }}
            onFinish={() => {
              form.validateFields().then((values: any) => {
                ConfigService.setLLMInfo(values).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({ id: 'dms.common.message.operate.success' }),
                    );
                  }
                });
              });
            }}
          >
            <Form.Item
              label={intl.formatMessage({ id: 'dms.admin.setting.llm.type' })}
              name="type"
              rules={[{ required: true }, { max: 256 }]}
            >
              <Select>
                <Select.Option value="dashScope">DashScope</Select.Option>
                <Select.Option value="openai">OpenAI</Select.Option>
                <Select.Option value="deepseek">DeepSeek</Select.Option>
                <Select.Option value="ollama">Ollama</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: 'dms.admin.setting.llm.model' })}
              name="model"
              rules={[{ required: true }, { max: 256 }]}
            >
              <Input></Input>
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: 'dms.admin.setting.llm.baseUrl' })}
              name="baseUrl"
              rules={[{ required: true }, { max: 2048 }]}
            >
              <Input></Input>
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({
                id: 'dms.admin.setting.llm.apiKey',
              })}
              name="apiKey"
              rules={[{ required: true }, { max: 512 }]}
            >
              <Input.Password></Input.Password>
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit">
                {intl.formatMessage({ id: 'dms.common.operate.confirm' })}
              </Button>
            </Form.Item>
          </Form>
        </Col>
      </Row>
    </>
  );
};

export default LLMSetting;
