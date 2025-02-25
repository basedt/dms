import { UserService } from "@/services/admin/user.service";
import { Form, Input, message, Modal, Select } from "antd";
import { useState } from "react";
import { history, useIntl } from "@umijs/max";

const PasswordChangeForm: React.FC<DMS.ModalProps<DMS.SysUser>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  return (
    <Modal
      title={intl.formatMessage({
        id: "dms.user.center.security.password.edit",
      })}
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          UserService.changePassword(values).then((resp) => {
            if (resp.success) {
              message.success(
                intl.formatMessage({
                  id: "dms.common.message.operate.success",
                })
              );
              handleOk ? handleOk(false) : null;
              history.push("/user/login");
            }
          });
        });
        setLoading(false);
      }}
      destroyOnClose={true}
      confirmLoading={loading}
      onCancel={handleCancel}
      styles={{ body: { paddingTop: 8 } }}
      width="540px"
    >
      <Form
        layout="horizontal"
        form={form}
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 16 }}
      >
        <Form.Item
          label={intl.formatMessage({
            id: "dms.user.center.security.password.old",
          })}
          name="oldPassword"
          rules={[{ required: true }]}
        >
          <Input.Password />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({
            id: "dms.user.center.security.password.new",
          })}
          name="password"
          rules={[{ required: true }]}
        >
          <Input.Password />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({
            id: "dms.user.center.security.password.confirm",
          })}
          name="confirmPassword"
          rules={[{ required: true }]}
        >
          <Input.Password />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default PasswordChangeForm;
