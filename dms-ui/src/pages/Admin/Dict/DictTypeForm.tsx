import { PATTERNS } from "@/constants";
import { DictTypeService } from "@/services/admin/dict.type.service";
import { Form, Input, message, Modal } from "antd";
import { useState } from "react";
import { useIntl } from "@umijs/max";

const DictTypeForm: React.FC<DMS.ModalProps<DMS.SysDictType>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);

  return (
    <Modal
      title={
        data?.id
          ? intl.formatMessage({ id: "dms.common.operate.update" }) +
          intl.formatMessage({ id: "dms.admin.dict.type" })
          : intl.formatMessage({ id: "dms.common.operate.new" }) +
          intl.formatMessage({ id: "dms.admin.dict.type" })
      }
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          let d: DMS.SysDictType = {
            dictTypeCode: values.dictTypeCode,
            dictTypeName: values.dictTypeName,
            remark: values.remark,
          };
          data?.id
            ? DictTypeService.update({ ...d, id: data.id }).then((resp) => {
              if (resp.success) {
                message.success(
                  intl.formatMessage({
                    id: "dms.common.message.operate.update.success",
                  })
                );
                handleOk ? handleOk(false) : null;
              }
            })
            : DictTypeService.add(d).then((resp) => {
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
        initialValues={data}
      >
        <Form.Item
          label={intl.formatMessage({ id: "dms.admin.dict.type.dictTypeCode" })}
          name="dictTypeCode"
          rules={[
            { required: true },
            { max: 32 },
            {
              pattern: PATTERNS.characterWord,
              message: intl.formatMessage({
                id: "dms.common.validate.characterWord",
              }),
            },
          ]}
        >
          <Input disabled={data?.id ? true : false} />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: "dms.admin.dict.type.dictTypeName" })}
          name="dictTypeName"
          rules={[{ required: true }, { max: 128 }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: "dms.admin.dict.type.remark" })}
          name="remark"
          rules={[{ max: 256 }]}
        >
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default DictTypeForm;
