import { DICT_TYPE, PATTERNS } from '@/constants';
import { DictDataService } from '@/services/admin/dict.data.service';
import { UserService } from '@/services/admin/user.service';
import { useIntl } from '@umijs/max';
import { Form, Input, message, Modal, Select } from 'antd';
import { useEffect, useState } from 'react';

const UserForm: React.FC<DMS.ModalProps<DMS.SysUser>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [userStatusList, setUserStatusList] = useState<DMS.Dict[]>([]);

  useEffect(() => {
    DictDataService.listByType(DICT_TYPE.userStatus).then((resp) => {
      if (resp.success) {
        setUserStatusList(resp.data as DMS.Dict[]);
      }
    });
  }, []);

  return (
    <Modal
      title={
        data?.id
          ? intl.formatMessage({ id: 'dms.common.operate.update' }) +
            intl.formatMessage({ id: 'dms.admin.user' })
          : intl.formatMessage({ id: 'dms.common.operate.new' }) +
            intl.formatMessage({ id: 'dms.admin.user' })
      }
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          let d: DMS.SysUser = {
            userName: values.userName,
            nickName: values.nickName,
            realName: values.realName,
            email: values.email,
            mobilePhone: values.mobilePhone,
            userStatus: { value: values.userStatus },
            summary: values.summary,
          };
          data?.id
            ? UserService.update({ ...d, id: data.id }).then((resp) => {
                if (resp.success) {
                  message.success(
                    intl.formatMessage({
                      id: 'dms.common.message.operate.update.success',
                    }),
                  );
                  handleOk ? handleOk(false) : null;
                }
              })
            : UserService.add(d).then((resp) => {
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
        initialValues={{
          ...data,
          userStatus: data?.userStatus ? data?.userStatus?.value : 'normal',
        }}
      >
        <Form.Item
          label={intl.formatMessage({ id: 'dms.admin.user.userName' })}
          name="userName"
          rules={[
            { required: true, max: 30, min: 5 },
            {
              pattern: PATTERNS.characterWord,
              message: intl.formatMessage({
                id: 'dms.common.validate.characterWord',
              }),
            },
            {
              validator: (rule, value, callback) => {
                data?.id
                  ? callback()
                  : UserService.isUserExists(value).then((resp) => {
                      if (resp) {
                        callback();
                      } else {
                        callback(
                          intl.formatMessage({
                            id: 'dms.common.validate.sameUserName',
                          }),
                        );
                      }
                    });
              },
            },
          ]}
        >
          <Input disabled={data?.id ? true : false} />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: 'dms.admin.user.email' })}
          name="email"
          rules={[
            { max: 128 },
            { required: true },
            { type: 'email' },
            {
              validator: (rule, value, callback) => {
                data?.id
                  ? callback()
                  : UserService.isEmailExists(value).then((resp) => {
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
          <Input disabled={data?.id ? true : false} />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: 'dms.admin.user.nickName' })}
          name="nickName"
          rules={[{ max: 50 }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: 'dms.admin.user.realName' })}
          name="realName"
          rules={[{ max: 64 }]}
        >
          <Input />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({ id: 'dms.admin.user.mobilePhone' })}
          name="mobilePhone"
          rules={[{ max: 16 }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: 'dms.admin.user.userStatus' })}
          name="userStatus"
          rules={[{ required: true }]}
        >
          <Select
            showSearch={true}
            allowClear={true}
            optionFilterProp="label"
            filterOption={(input, option) =>
              (option!.children as unknown as string).toLowerCase().includes(input.toLowerCase())
            }
          >
            {userStatusList.map((item) => {
              return (
                <Select.Option key={item.value} value={item.value}>
                  {item.label}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: 'dms.admin.user.summary' })}
          name="summary"
          rules={[{ max: 200 }]}
        >
          <Input.TextArea />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UserForm;
