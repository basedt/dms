import { PATTERNS } from '@/constants';
import { UserService } from '@/services/admin/user.service';
import { WorkspaceService } from '@/services/workspace/workspace.service';
import { useIntl } from '@umijs/max';
import { AutoComplete, Form, Input, message, Modal } from 'antd';
import { useState } from 'react';

const WorkspaceForm: React.FC<DMS.ModalProps<DMS.Workspace>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [options, setOptions] = useState<{ value: string }[]>([]);
  return (
    <>
      <Modal
        title={
          data?.id
            ? intl.formatMessage({ id: 'dms.common.operate.update' }) +
              intl.formatMessage({ id: 'dms.workspace' })
            : intl.formatMessage({ id: 'dms.console.workspace.create' })
        }
        open={open}
        onOk={() => {
          setLoading(true);
          form.validateFields().then((values) => {
            data?.id
              ? WorkspaceService.update(values).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({
                        id: 'dms.common.message.operate.update.success',
                      }),
                    );
                    handleOk ? handleOk(false) : null;
                  }
                })
              : WorkspaceService.add(values).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({
                        id: 'dms.common.message.operate.new.success',
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
        <Form
          layout="horizontal"
          form={form}
          labelCol={{ span: 6 }}
          wrapperCol={{ span: 16 }}
          initialValues={data}
        >
          <Form.Item name="id" hidden>
            <Input />
          </Form.Item>
          <Form.Item
            name="workspaceCode"
            label={intl.formatMessage({
              id: 'dms.console.workspace.workspaceCode',
            })}
            rules={[
              { required: true },
              { max: 32 },
              {
                pattern: PATTERNS.characterWord,
                message: intl.formatMessage({
                  id: 'dms.common.validate.characterWord',
                }),
              },
            ]}
          >
            <Input disabled={data?.id ? true : false} />
          </Form.Item>
          <Form.Item
            name="workspaceName"
            label={intl.formatMessage({
              id: 'dms.console.workspace.workspaceName',
            })}
            rules={[{ required: true }, { max: 64 }]}
          >
            <Input />
          </Form.Item>
          {data?.id && (
            <Form.Item
              name="owner"
              label={intl.formatMessage({ id: 'dms.console.workspace.owner' })}
            >
              <AutoComplete
                allowClear
                options={options}
                onSearch={(value: string) => {
                  UserService.list({ userName: value }).then((resp) => {
                    let users: { value: string }[] = [];
                    setOptions([]);
                    resp.data.forEach((user) => {
                      users.push({ value: user.userName as string });
                    });
                    setOptions(users);
                  });
                }}
                placeholder={intl.formatMessage({
                  id: 'dms.console.workspace.owner.placeholder',
                })}
              ></AutoComplete>
            </Form.Item>
          )}
          <Form.Item
            name="remark"
            label={intl.formatMessage({ id: 'dms.console.workspace.remark' })}
            rules={[{ max: 500 }]}
          >
            <Input.TextArea />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default WorkspaceForm;
