import { DataTaskService } from '@/services/workspace/data.task';
import { UploadOutlined } from '@ant-design/icons';
import { ProFormInstance, StepsForm } from '@ant-design/pro-components';
import { useIntl, useModel } from '@umijs/max';
import {
  Alert,
  Button,
  Checkbox,
  Descriptions,
  DescriptionsProps,
  Form,
  Input,
  message,
  Modal,
  Radio,
  Select,
  Upload,
  UploadFile,
} from 'antd';
import { useRef, useState } from 'react';

export interface DataImportModalProps {
  workspaceId: number | string;
  datasourceId: number | string;
  schema: string;
  table: string;
  tableIdentifier: string;
}

const DataImportModal: React.FC<DMS.ModalProps<DataImportModalProps>> = (props) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const formRef = useRef<ProFormInstance>();
  const [uploadFile, setUploadFile] = useState<UploadFile>();
  const [separatorStatus, setSeparatorStatus] = useState<boolean>(true);
  const [formValues, setFormValues] = useState<DMS.ImportDataTaskParam>({
    workspaceId: data?.workspaceId as string,
    datasourceId: data?.datasourceId as string,
  });
  const { setMenuKey } = useModel('global');

  const descItems = () => {
    const items: DescriptionsProps['items'] = [
      {
        key: 'schema',
        label: intl.formatMessage({
          id: 'dms.console.workspace.import.schema',
        }),
        children: formValues.schema,
      },
      {
        key: 'tableName',
        label: intl.formatMessage({
          id: 'dms.console.workspace.import.tableName',
        }),
        children: formValues.tableName,
      },
      {
        key: 'file',
        label: intl.formatMessage({ id: 'dms.console.workspace.import.file' }),
        children: formValues.file?.file?.name,
      },
      {
        key: 'fileType',
        label: intl.formatMessage({
          id: 'dms.console.workspace.import.fileType',
        }),
        children: formValues.fileType,
      },
      {
        key: 'fileEncoding',
        label: intl.formatMessage({
          id: 'dms.console.workspace.import.fileEncoding',
        }),
        children: formValues.fileEncoding,
      },

      {
        key: 'isTruncate',
        label: intl.formatMessage({
          id: 'dms.console.workspace.import.truncate',
        }),
        children: formValues.isTruncate?.toString(),
      },
    ];

    if (formValues.fileType == 'CSV') {
      items.push({
        key: 'separator',
        label: intl.formatMessage({
          id: 'dms.console.workspace.import.separator',
        }),
        children: formValues.separator,
      });
    }
    return items;
  };

  return (
    <StepsForm
      formRef={formRef}
      onFinish={async (values) => {
        setLoading(true);
        let d: DMS.ImportDataTaskParam = {
          workspaceId: data?.workspaceId as string,
          datasourceId: data?.datasourceId as string,
          catalog: data?.tableIdentifier.split('.')[0],
          schema: values.schema,
          tableName: values.tableName,
          isTruncate: values.isTruncate == undefined ? false : values.isTruncate,
          file: uploadFile,
          fileType: values.fileType,
          fileEncoding: values.fileEncoding,
          separator: values.separator,
        };
        DataTaskService.newImportTask(d).then((resp) => {
          if (resp.success) {
            message.info(
              <>
                {intl.formatMessage({
                  id: 'dms.console.workspace.dataquery.import.success.info',
                })}
                <a
                  onClick={() => {
                    setMenuKey('import');
                  }}
                >
                  &nbsp;
                  {intl.formatMessage({
                    id: 'dms.console.workspace.dataquery.import.success.view',
                  })}
                </a>
              </>,
            );
            handleOk ? handleOk(false) : null;
          }
        });
        setLoading(false);
      }}
      stepsFormRender={(dom, submitter) => {
        return (
          <Modal
            title={intl.formatMessage({
              id: 'dms.console.workspace.import',
            })}
            width={860}
            open={open}
            footer={submitter}
            onCancel={handleCancel}
            destroyOnHidden={true}
          >
            <div style={{ marginBottom: 16 }}>
              <Alert
                message={intl.formatMessage({
                  id: 'dms.console.workspace.import.toolTip',
                })}
                type="info"
                showIcon
              />
            </div>
            {dom}
          </Modal>
        );
      }}
    >
      <StepsForm.StepForm
        name="one"
        title={intl.formatMessage({ id: 'dms.console.workspace.import.step1' })}
        layout="horizontal"
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 16 }}
        initialValues={{
          schema: data?.schema,
          tableName: data?.table,
        }}
        onFinish={async (values) => {
          setFormValues({
            ...formValues,
            schema: values.schema,
            tableName: values.tableName,
            isTruncate: values.isTruncate == undefined ? false : values.isTruncate,
          });
          return true;
        }}
      >
        <Form.Item
          name="schema"
          label={intl.formatMessage({
            id: 'dms.console.workspace.import.schema',
          })}
          rules={[{ required: true }]}
        >
          <Input disabled />
        </Form.Item>
        <Form.Item
          name="tableName"
          label={intl.formatMessage({
            id: 'dms.console.workspace.import.tableName',
          })}
          rules={[{ required: true }]}
        >
          <Input disabled />
        </Form.Item>
        <Form.Item
          name="isTruncate"
          label={intl.formatMessage({
            id: 'dms.console.workspace.import.truncate',
          })}
          tooltip={intl.formatMessage({
            id: 'dms.console.workspace.import.truncate.toolTip',
          })}
          valuePropName="checked"
        >
          <Checkbox />
        </Form.Item>
      </StepsForm.StepForm>
      <StepsForm.StepForm
        name="two"
        title={intl.formatMessage({ id: 'dms.console.workspace.import.step2' })}
        layout="horizontal"
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 16 }}
        initialValues={{ separator: ',', fileEncoding: 'UTF-8' }}
        onFinish={async (values) => {
          setFormValues({
            ...formValues,
            file: values.file,
            fileType: values.fileType == 'csv' ? 'CSV' : 'EXCEL',
            fileEncoding: values.fileEncoding,
            separator: values.separator,
          });
          return true;
        }}
      >
        <Form.Item
          name="file"
          label={intl.formatMessage({
            id: 'dms.console.workspace.import.file',
          })}
          rules={[{ required: true }]}
        >
          <Upload
            name="file"
            maxCount={1}
            accept=".csv,.xlsx,.xls"
            beforeUpload={(fileInfo) => {
              if (fileInfo.size > 1024 * 1024 * 200) {
                message.error(
                  intl.formatMessage({
                    id: 'dms.console.workspace.import.file.sizeLimit',
                  }),
                );
                return Upload.LIST_IGNORE;
              } else {
                setUploadFile(fileInfo);
              }
              return false; // 阻止自动上传
            }}
            onChange={(info) => {
              console.log('change info', info);
              if (info.fileList.length >= 1) {
                const fileType = info.file.name.split('.').pop();
                formRef.current?.setFieldValue('fileType', fileType == 'csv' ? 'csv' : 'xlsx');
                setSeparatorStatus(fileType === 'csv');
              } else {
                formRef.current?.setFieldValue('fileType', undefined);
                formRef.current?.setFieldValue('file', undefined);
                setSeparatorStatus(true);
              }
            }}
          >
            <Button icon={<UploadOutlined />}>
              {intl.formatMessage({
                id: 'dms.console.workspace.import.file.select',
              })}
            </Button>
          </Upload>
        </Form.Item>
        <Form.Item
          name="fileType"
          label={intl.formatMessage({
            id: 'dms.console.workspace.import.fileType',
          })}
          rules={[{ required: true }]}
        >
          <Radio.Group
            onChange={(value) => {
              const type = value.target.value;
              setSeparatorStatus(type === 'csv');
            }}
          >
            <Radio value="csv">CSV</Radio>
            <Radio value="xlsx">EXCEL</Radio>
          </Radio.Group>
        </Form.Item>
        <Form.Item
          name="fileEncoding"
          label={intl.formatMessage({
            id: 'dms.console.workspace.import.fileEncoding',
          })}
          rules={[{ required: true }]}
        >
          <Select>
            <Select.Option value="UTF-8">UTF-8</Select.Option>
            <Select.Option value="GBK">GBK</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item
          name="separator"
          label={intl.formatMessage({
            id: 'dms.console.workspace.import.separator',
          })}
          rules={[{ required: true }]}
        >
          <Select disabled={!separatorStatus}>
            <Select.Option value=",">,</Select.Option>
            <Select.Option value=";">;</Select.Option>
            <Select.Option value="|">|</Select.Option>
            <Select.Option value="\t">TAB</Select.Option>
            <Select.Option value=" ">SPACE</Select.Option>
          </Select>
        </Form.Item>
      </StepsForm.StepForm>
      <StepsForm.StepForm
        name="three"
        title={intl.formatMessage({ id: 'dms.console.workspace.import.step3' })}
      >
        <Descriptions items={descItems()} style={{ marginLeft: 80 }}></Descriptions>
      </StepsForm.StepForm>
    </StepsForm>
  );
};

export default DataImportModal;
