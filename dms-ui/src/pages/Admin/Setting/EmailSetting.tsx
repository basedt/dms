import { ConfigService } from "@/services/admin/config.service";
import {
  Button,
  Col,
  Form,
  Input,
  InputNumber,
  message,
  Row,
  Typography,
} from "antd";
import { useEffect } from "react";
import { useIntl } from "@umijs/max";

const EmailSetting: React.FC = () => {
  const intl = useIntl();
  const [form] = Form.useForm();

  useEffect(() => {
    ConfigService.getMailInfo().then((resp) => {
      if (resp.success) {
        form.setFieldsValue(resp.data);
      }
    });
  }, []);

  return (
    <>
      <Typography.Title level={4} style={{ marginTop: 0, marginBottom: 18 }}>
        {intl.formatMessage({ id: "dms.admin.setting.email" })}
      </Typography.Title>
      <Row>
        <Col span={8}>
          <Form
            name="emailSetting"
            form={form}
            layout="vertical"
            initialValues={{ port: 25 }}
            onFinish={() => {
              form.validateFields().then((values: any) => {
                ConfigService.setMailInfo(values).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({ id: "dms.common.message.operate.success" })
                    );
                  }
                });
              });
            }}
          >
            <Form.Item
              label={intl.formatMessage({ id: "dms.admin.setting.email.host" })}
              name="host"
              rules={[{ required: true }]}
            >
              <Input></Input>
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: "dms.admin.setting.email.port" })}
              name="port"
              rules={[{ required: true }]}
            >
              <InputNumber style={{ width: "100%" }} />
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({
                id: "dms.admin.setting.email.email",
              })}
              name="email"
              rules={[{ required: true }, { type: "email" }, { max: 100 }]}
            >
              <Input></Input>
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({
                id: "dms.admin.setting.email.password",
              })}
              name="password"
              rules={[{ required: true }]}
            >
              <Input.Password></Input.Password>
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit">
                {intl.formatMessage({ id: "dms.common.operate.confirm" })}
              </Button>
            </Form.Item>
          </Form>
        </Col>
      </Row>
    </>
  );
};

export default EmailSetting;
