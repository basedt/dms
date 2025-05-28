import { DICT_TYPE, PATTERNS } from '@/constants';
import { DictDataService } from '@/services/admin/dict.data.service';
import { DataTaskService } from '@/services/workspace/data.task';
import { useIntl, useModel } from '@umijs/max';
import { Form, Input, InputNumber, message, Modal, Radio, Select } from 'antd';
import { useEffect, useState } from 'react';

const DataExportModal: React.FC<DMS.ModalProps<DMS.DataTask>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [fileEncodingList, setFileEncodingList] = useState<DMS.Dict[]>([]);
  const { setMenuKey } = useModel('global');

  useEffect(() => {
    DictDataService.listByType(DICT_TYPE.fileEncoding).then((resp) => {
      if (resp.success) {
        setFileEncodingList(resp.data as DMS.Dict[]);
      }
    });
  }, []);

  return (
    <Modal
      title={intl.formatMessage({ id: 'dms.common.operate.export' })}
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          let d: DMS.DataTask = {
            workspaceId: data?.workspaceId as string,
            datasourceId: data?.datasourceId as string,
            fileName: values.fileName,
            fileType: { value: values.fileType },
            splitRow: values.splitRow,
            fileEncoding: { value: values.fileEncoding },
            taskType: { value: 'e' },
            sqlScript: data?.sqlScript as string,
          };
          DataTaskService.newExportTask(d).then((resp) => {
            if (resp.success) {
              message.info(
                <>
                  {intl.formatMessage({
                    id: 'dms.console.workspace.dataquery.export.success.info',
                  })}
                  <a
                    onClick={() => {
                      setMenuKey('export');
                    }}
                  >
                    &nbsp;
                    {intl.formatMessage({
                      id: 'dms.console.workspace.dataquery.export.success.view',
                    })}
                  </a>
                </>,
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
          fileEncoding: 'UTF-8',
          splitRow: 0,
          fileName: data?.fileName,
        }}
      >
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.export.fileName',
          })}
          name="fileName"
          rules={[
            { required: true },
            { max: 32 },
            {
              pattern: PATTERNS.excludeSpecialChar,
              message: intl.formatMessage({
                id: 'dms.common.validate.excludeSpecialChar',
              }),
            },
          ]}
        >
          <Input />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.export.fileType',
          })}
          name="fileType"
          rules={[{ required: true }]}
        >
          <Radio.Group>
            <Radio value={'csv'}>CSV</Radio>
            <Radio value={'xlsx'}>EXCEL</Radio>
            <Radio value={'orc'}>ORC</Radio>
          </Radio.Group>
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.export.splitRow',
          })}
          name="splitRow"
          tooltip={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.export.splitRow.tip',
          })}
        >
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.dataquery.export.fileEncoding',
          })}
          name="fileEncoding"
        >
          <Select
            showSearch={true}
            allowClear={true}
            optionFilterProp="label"
            filterOption={(input, option) =>
              (option!.children as unknown as string).toLowerCase().includes(input.toLowerCase())
            }
          >
            {fileEncodingList.map((item) => {
              return (
                <Select.Option key={item.value} value={item.value}>
                  {item.label}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default DataExportModal;
