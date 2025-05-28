import { FileCatalogService } from '@/services/workspace/file.catalog.service';
import { FileService } from '@/services/workspace/file.service';
import { useIntl, useModel } from '@umijs/max';
import { Form, Input, Modal, message } from 'antd';
import { useState } from 'react';

export type FileRenameModalProps = {
  type: 'file' | 'catalog';
  originNode: DMS.FileTreeNode<string>;
  originName: string;
  workspaceId: string | number;
};

const FileRenameModal: React.FC<DMS.ModalProps<FileRenameModalProps>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const { setTabsList } = useModel('global');
  return (
    <Modal
      title={intl.formatMessage({
        id: 'dms.console.workspace.dataquery.rename',
      })}
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          if (data?.type === 'catalog') {
            FileCatalogService.update({
              id: data.originNode.key,
              name: values.name,
              workspaceId: data.workspaceId,
            }).then((resp) => {
              if (resp.success) {
                message.success(
                  intl.formatMessage({
                    id: 'dms.common.message.operate.success',
                  }),
                );
                handleOk ? handleOk(false) : null;
              }
            });
          } else if (data?.type === 'file') {
            let fileId: number | string = data.originNode.key.split('.')[1];
            FileService.renameFile({
              id: fileId,
              workspaceId: data.workspaceId,
              fileName: data.originName,
              newFileName: values.name,
            }).then((resp) => {
              if (resp.success) {
                setTabsList({
                  oldTitle: data.originName,
                  newTitle: values.name,
                  id: data.originNode.key,
                });
                message.success(
                  intl.formatMessage({
                    id: 'dms.common.message.operate.success',
                  }),
                );
                handleOk ? handleOk(false) : null;
              }
            });
          }
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
        initialValues={{ name: data?.originName }}
      >
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.rename.newName',
          })}
          name="name"
          rules={[{ required: true }, { max: 128 }]}
        >
          <Input></Input>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default FileRenameModal;
