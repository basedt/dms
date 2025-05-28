import { PATTERNS } from '@/constants';
import { FileCatalogService } from '@/services/workspace/file.catalog.service';
import { useIntl } from '@umijs/max';
import { Form, Input, Modal, TreeSelect, message } from 'antd';
import { useEffect, useState } from 'react';

const FileCatalogModal: React.FC<DMS.ModalProps<DMS.FileCatalog>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [catalogTreeData, setCatalogTreeData] = useState<DMS.FileTreeNode<string>[]>([]);

  useEffect(() => {
    FileCatalogService.listCatalogTree(data?.workspaceId as string, data?.id as number).then(
      (resp) => {
        if (resp.success) {
          setCatalogTreeData(resp.data as DMS.FileTreeNode<string>[]);
        }
      },
    );
  }, []);

  return (
    <Modal
      title={
        data?.id
          ? intl.formatMessage({
              id: 'dms.console.workspace.dataquery.move',
            })
          : intl.formatMessage(
              {
                id: 'dms.console.workspace.dataquery.new',
              },
              {
                type: intl.formatMessage({
                  id: 'dms.console.workspace.dataquery.file.catalog',
                }),
              },
            )
      }
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          let d: DMS.FileCatalog = {
            name: values.name,
            pid: values.pid,
            workspaceId: data?.workspaceId as string,
          };
          data?.id
            ? FileCatalogService.update({ ...d, id: data.id }).then((resp) => {
                if (resp.success) {
                  message.success(
                    intl.formatMessage({
                      id: 'dms.common.message.operate.update.success',
                    }),
                  );
                  handleOk ? handleOk(false) : null;
                }
              })
            : FileCatalogService.add(d).then((resp) => {
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
        initialValues={data?.id ? { ...data, pid: null } : { ...data }}
      >
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.file.catalog',
          })}
          name="name"
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
          <Input disabled={data?.id ? true : false}></Input>
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.file.tgtCatalog',
          })}
          name="pid"
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
      </Form>
    </Modal>
  );
};

export default FileCatalogModal;
