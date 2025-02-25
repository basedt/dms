import { DICT_TYPE } from "@/constants";
import { DictDataService } from "@/services/admin/dict.data.service";
import { RoleService } from "@/services/admin/role.service";
import { Form, Input, message, Modal, Select } from "antd";
import { useEffect, useState } from "react";
import { useIntl } from "@umijs/max";

const RoleForm: React.FC<DMS.ModalProps<DMS.SysRole>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [roleStatusList, setRoleStatusList] = useState<DMS.Dict[]>([]);

  useEffect(() => {
    DictDataService.listByType(DICT_TYPE.roleStatus).then((resp) => {
      if (resp.success) {
        setRoleStatusList(resp.data as DMS.Dict[]);
      }
    });
  }, []);

  return (
    <Modal
      title={
        data?.id
          ? intl.formatMessage({ id: "dms.common.operate.update" }) +
          intl.formatMessage({ id: "dms.admin.role" })
          : intl.formatMessage({ id: "dms.common.operate.new" }) +
          intl.formatMessage({ id: "dms.admin.role" })
      }
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          let d: DMS.SysRole = {
            roleName: values.roleName,
            roleStatus: { value: values.roleStatus },
            roleDesc: values.roleDesc,
          };
          data?.id
            ? RoleService.update({ ...d, id: data.id }).then((resp) => {
              if (resp.success) {
                message.success(
                  intl.formatMessage({
                    id: "dms.common.message.operate.update.success",
                  })
                );
                handleOk ? handleOk(false) : null;
              }
            })
            : RoleService.add(d).then((resp) => {
              if (resp.success) {
                message.success(
                  intl.formatMessage({
                    id: "dms.common.message.operate.new.success",
                  })
                );
                handleOk ? handleOk(false) : null;
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
        initialValues={{
          roleName: data?.roleName,
          roleStatus: data?.id ? data?.roleStatus?.value : "01",
          roleDesc: data?.roleDesc,
        }}
      >
        <Form.Item
          label={intl.formatMessage({ id: "dms.admin.role.roleName" })}
          name="roleName"
          rules={[{ required: true }, { max: 64 }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: "dms.admin.role.roleStatus" })}
          name="roleStatus"
          rules={[{ required: true }]}
        >
          <Select
            showSearch={true}
            allowClear={true}
            optionFilterProp="label"
            filterOption={(input, option) =>
              (option!.children as unknown as string)
                .toLowerCase()
                .includes(input.toLowerCase())
            }
          >
            {roleStatusList.map((item) => {
              return (
                <Select.Option key={item.value} value={item.value}>
                  {item.label}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: "dms.admin.role.roleDesc" })}
          name="roleDesc"
          rules={[{ max: 128 }]}
        >
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default RoleForm;
