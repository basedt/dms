import { UserService } from '@/services/admin/user.service';
import { useIntl } from '@umijs/max';
import { Button, Col, Form, Input, message, Modal, Row } from 'antd';
import { useState } from 'react';

const MailChangeForm: React.FC<DMS.ModalProps<DMS.SysUser>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [flag, setFlag] = useState<boolean>(true);

  return (
    <Modal
      title={intl.formatMessage({ id: 'dms.user.center.security.email.edit' })}
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          UserService.bindEmail(values).then((resp) => {
            if (resp.success) {
              message.success(
                intl.formatMessage({
                  id: 'dms.common.message.operate.success',
                }),
              );
              handleOk ? handleOk(false) : null;
            }
          });
        });
        setLoading(false);
      }}
      destroyOnHidden={true}
      confirmLoading={loading}
      onCancel={handleCancel}
      styles={{ body: { paddingTop: 8 } }}
      width="540px"
    >
      <Form layout="horizontal" form={form} labelCol={{ span: 6 }} wrapperCol={{ span: 16 }}>
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.user.center.security.email.mail',
          })}
          name="email"
          rules={[
            { required: true },
            { type: 'email' },
            {
              validator: (rule, value, callback) => {
                UserService.isEmailExists(value).then((resp) => {
                  if (resp) {
                    callback();
                  } else {
                    callback(
                      intl.formatMessage({
                        id: 'dms.common.validate.sameEmail',
                      }),
                    );
                  }
                });
              },
            },
          ]}
        >
          <Input
            onBlur={(item) => {
              let error = form.getFieldError('email');
              setFlag(error.length > 0);
            }}
          />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.user.center.security.email.authCode',
          })}
          name="authCode"
          rules={[{ required: true }]}
        >
          <Row>
            <Col span={16}>
              <Form.Item noStyle>
                <Input />
              </Form.Item>
            </Col>
            <Col span={8} style={{ textAlign: 'right' }}>
              <Button
                disabled={flag}
                onClick={() => {
                  UserService.getEmailAuthCode(form.getFieldValue('email')).then((resp) => {
                    if (resp.success) {
                      message.info(
                        intl.formatMessage({
                          id: 'dms.user.center.security.email.authCode.info',
                        }),
                      );
                    }
                  });
                }}
              >
                {intl.formatMessage({
                  id: 'dms.user.center.security.email.authCode.get',
                })}
              </Button>
            </Col>
          </Row>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default MailChangeForm;
