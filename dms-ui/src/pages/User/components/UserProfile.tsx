import { AuthService } from "@/services/admin/auth.service";
import { UserService } from "@/services/admin/user.service";
import { Button, Col, Form, Input, message, Row, Typography } from "antd";
import { useEffect } from "react";
import { useIntl } from "@umijs/max";

const UserProfile: React.FC = () => {
  const intl = useIntl();
  const [form] = Form.useForm();

  const refreshUserInfo = () => {
    AuthService.getCurrentUser(true).then((resp) => {
      form.setFieldsValue(resp.data);
    });
  };
  
  useEffect(() => {
    refreshUserInfo();
  }, []);



  return (
    <>
      <Typography.Title level={4} style={{ marginTop: 0, marginBottom: 18 }}>
        {intl.formatMessage({ id: "dms.user.center.profile" })}
      </Typography.Title>
      <Row>
        <Col span={16}>
          <Form
            layout="vertical"
            form={form}
            labelCol={{ span: 6 }}
            wrapperCol={{ span: 16 }}
            onFinish={() => {
              form.validateFields().then((values: any) => {
                UserService.update(values).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({
                        id: "dms.common.message.operate.update.success",
                      })
                    );
                    refreshUserInfo();
                  }
                });
              });
            }}
          >
            <Form.Item name="id" hidden={true}>
              <Input />
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: "dms.admin.user.userName" })}
              name="userName"
            >
              <Input disabled={true} />
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: "dms.admin.user.email" })}
              name="email"
            >
              <Input disabled={true} />
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: "dms.admin.user.nickName" })}
              name="nickName"
              rules={[{ max: 50 }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: "dms.admin.user.realName" })}
              name="realName"
              rules={[{ max: 64 }]}
            >
              <Input />
            </Form.Item>

            <Form.Item
              label={intl.formatMessage({ id: "dms.admin.user.mobilePhone" })}
              name="mobilePhone"
              rules={[{ max: 16 }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: "dms.admin.user.summary" })}
              name="summary"
              rules={[{ max: 200 }]}
            >
              <Input.TextArea />
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

export default UserProfile;
