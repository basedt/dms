import { PATTERNS } from "@/constants";
import { DictDataService } from "@/services/admin/dict.data.service";
import { Form, Input, message, Modal } from "antd";
import { useState } from "react";
import { useIntl } from "@umijs/max";

const DictDataForm: React.FC<DMS.ModalProps<DMS.SysDictData>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);

  return (
    <Modal
      title={
        data?.id
          ? intl.formatMessage({ id: "dms.common.operate.update" }) +
          intl.formatMessage({ id: "dms.admin.dict.data" })
          : intl.formatMessage({ id: "dms.common.operate.new" }) +
          intl.formatMessage({ id: "dms.admin.dict.data" })
      }
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          let d: DMS.SysDictData = {
            sysDictType: {
              id: data?.sysDictType.id,
              dictTypeCode: data?.sysDictType.dictTypeCode as string,
              dictTypeName: data?.sysDictType.dictTypeName as string,
            },
            dictCode: values.dictCode,
            dictValue: values.dictValue,
            remark: values.remark,
          };
          data?.id
            ? DictDataService.update({ ...d, id: data.id }).then((resp) => {
              if (resp.success) {
                message.success(
                  intl.formatMessage({
                    id: "dms.common.message.operate.update.success",
                  })
                );
                handleOk ? handleOk(false) : null;
              }
            })
            : DictDataService.add(d).then((resp) => {
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
          label={intl.formatMessage({ id: "dms.admin.dict.data.dictCode" })}
          name="dictCode"
          rules={[
            { required: true },
            { max: 128 },
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
          label={intl.formatMessage({ id: "dms.admin.dict.data.dictValue" })}
          name="dictValue"
          rules={[{ required: true }, { max: 128 }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({ id: "dms.admin.dict.data.remark" })}
          name="remark"
          rules={[{ max: 256 }]}
        >
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default DictDataForm;
