import { PATTERNS } from '@/constants';
import { FileCatalogService } from '@/services/workspace/file.catalog.service';
import { FileService } from '@/services/workspace/file.service';
import { useIntl } from '@umijs/max';
import { Form, Input, Modal, TreeSelect, message } from 'antd';
import { useEffect, useState } from 'react';

const FileModal: React.FC<DMS.ModalProps<DMS.File>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [catalogTreeData, setCatalogTreeData] = useState<DMS.FileTreeNode<string>[]>([]);

  useEffect(() => {
    FileCatalogService.listCatalogTree(data?.workspaceId as string, '').then((resp) => {
      if (resp.success) {
        setCatalogTreeData(resp.data as DMS.FileTreeNode<string>[]);
      }
    });
  }, []);

  return (
    <Modal
      title={
        data?.id
          ? intl.formatMessage({ id: 'dms.console.workspace.dataquery.move' })
          : intl.formatMessage(
              {
                id: 'dms.console.workspace.dataquery.new',
              },
              {
                type:
                  (data?.fileType?.value as string).toUpperCase() +
                  intl.formatMessage({
                    id: 'dms.console.workspace.dataquery.file',
                  }),
              },
            )
      }
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values: any) => {
          let d: DMS.File = {
            workspaceId: data?.workspaceId as string,
            datasourceId: data?.datasourceId as string,
            fileName: values.fileName,
            fileType: data?.fileType,
            fileCatalog: values.fileCatalog,
            remark: values.remark,
            content: data?.content ?? '',
          };
          data?.id
            ? FileService.moveFileCatalog({
                id: data.id,
                workspaceId: data.workspaceId,
                newFileCatalog: values.fileCatalog,
              }).then((resp) => {
                if (resp.success) {
                  message.success(
                    intl.formatMessage({
                      id: 'dms.common.message.operate.success',
                    }),
                  );
                  handleOk ? handleOk(false) : null;
                }
              })
            : FileService.save(d).then((resp) => {
                if (resp.success) {
                  message.success(
                    intl.formatMessage({
                      id: 'dms.common.message.operate.success',
                    }),
                  );
                  handleOk ? handleOk(false, d) : null;
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
        initialValues={data?.id ? { ...data, fileCatalog: null } : { ...data }}
      >
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.file.name',
          })}
          name="fileName"
          rules={[
            { required: true },
            { max: 60 },
            {
              pattern: PATTERNS.excludeSpecialChar,
              message: intl.formatMessage({
                id: 'dms.common.validate.excludeSpecialChar',
              }),
            },
          ]}
        >
          <Input disabled={data?.id ? true : false} />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.file.tgtCatalog',
          })}
          name="fileCatalog"
          rules={[{ required: true }]}
        >
          <TreeSelect
            showSearch
            style={{ width: '100%' }}
            dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
            allowClear
            treeDefaultExpandAll
            treeData={catalogTreeData}
            fieldNames={{ label: 'title', value: 'key', children: 'children' }}
          />
        </Form.Item>
        {!data?.id && (
          <Form.Item
            name="remark"
            label={intl.formatMessage({
              id: 'dms.console.workspace.dataquery.file.remark',
            })}
            rules={[{ max: 500 }]}
          >
            <Input.TextArea />
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
};

export default FileModal;
