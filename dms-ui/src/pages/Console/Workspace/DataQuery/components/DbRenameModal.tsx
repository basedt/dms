import { MetaDataService } from '@/services/meta/metadata.service';
import { useIntl } from '@umijs/max';
import { Form, Input, Modal, message } from 'antd';
import { useState } from 'react';

export type DbRenameModalProps = {
  dataSourceId: string;
  node: DMS.CatalogTreeNode<string>;
};

const DbRenameModal: React.FC<DMS.ModalProps<DbRenameModalProps>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const objectInfo: string[] = data?.node.identifier.split('.') as string[];
  return (
    <Modal
      title={intl.formatMessage({
        id: 'dms.console.workspace.dataquery.rename',
      })}
      open={open}
      onOk={() => {
        if (objectInfo.length < 3) {
          message.error('invalid object identifier');
        } else {
          setLoading(true);
          form.validateFields().then((values) => {
            if (data?.node) {
              MetaDataService.renameObject(
                data.dataSourceId,
                data.node.identifier,
                data.node.type,
                values.name,
              ).then((resp) => {
                if (resp.success) {
                  message.success(
                    intl.formatMessage({
                      id: 'dms.common.message.operate.success',
                    }),
                  );
                  handleOk?.(false, data.node);
                }
              });
            }
          });
          setLoading(false);
        }
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
        initialValues={{ name: data?.node.type === 'INDEX' ? objectInfo[3] : objectInfo[2] }}
      >
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.rename.newName',
          })}
          name="name"
          rules={[{ required: true }, { max: 512 }]}
        >
          <Input></Input>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default DbRenameModal;
