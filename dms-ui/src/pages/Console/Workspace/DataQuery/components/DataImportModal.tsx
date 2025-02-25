import { DataTaskService } from "@/services/workspace/data.task";
import { DataSourceService } from "@/services/workspace/datasource.service";
import { UploadOutlined } from "@ant-design/icons";
import { ProFormInstance, StepsForm } from "@ant-design/pro-components";
import { useIntl, useModel } from "@umijs/max";
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
} from "antd";
import { useEffect, useRef, useState } from "react";

export interface DataImportModalProps {
  workspaceId: number | string;
  datasourceId: number | string;
  schema: string;
  table: string;
  tableIdentifier: string;
}

const DataImportModal: React.FC<DMS.ModalProps<DataImportModalProps>> = (
  props
) => {
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
  const { setMenuKey } = useModel("global");

  useEffect(() => {
    // DataSourceService.listByWorkspace(data?.workspaceId as string).then(
    //   (resp) => {
    //     if (resp.success) {
    //       setDbList(resp.data as DMS.Dict[]);
    //     }
    //   }
    // );
  }, []);

  const descItems = () => {
    const items: DescriptionsProps["items"] = [
      {
        key: "schema",
        label: "模式",
        children: formValues.schema,
      },
      {
        key: "tableName",
        label: "表名",
        children: formValues.tableName,
      },
      {
        key: "file",
        label: "文件名",
        children: formValues.file?.file?.name,
      },
      {
        key: "fileType",
        label: "文件类型",
        children: formValues.fileType,
      },
      {
        key: "fileEncoding",
        label: "文件编码",
        children: formValues.fileEncoding,
      },

      {
        key: "isTruncate",
        label: "清空目标表",
        children: formValues.isTruncate?.toString(),
      },
    ];

    if (formValues.fileType == "CSV") {
      items.push({
        key: "separator",
        label: "分隔符",
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
          catalog: data?.tableIdentifier.split(".")[0],
          schema: values.schema,
          tableName: values.tableName,
          isTruncate:
            values.isTruncate == undefined ? false : values.isTruncate,
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
                  id: "dms.console.workspace.dataquery.import.success.info",
                })}
                <a
                  onClick={() => {
                    setMenuKey("import");
                  }}
                >
                  &nbsp;
                  {intl.formatMessage({
                    id: "dms.console.workspace.dataquery.import.success.view",
                  })}
                </a>
              </>
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
              id: "dms.console.workspace.import",
            })}
            width={860}
            open={open}
            footer={submitter}
            onCancel={handleCancel}
            destroyOnClose={true}
          >
            <div style={{ marginBottom: 16 }}>
              <Alert
                message="仅支持200M以下的CSV、EXCEL文件上传，文件中的数据列需是对应表数据列的子集，自动兼容常见日期时间格式，对于数据量较大的文件建议使用数据同步工具导入。"
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
        title="目标表"
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
            isTruncate:
              values.isTruncate == undefined ? false : values.isTruncate,
          });
          return true;
        }}
      >
        <Form.Item name="schema" label="模式" rules={[{ required: true }]}>
          <Input disabled />
        </Form.Item>
        <Form.Item name="tableName" label="表名" rules={[{ required: true }]}>
          <Input disabled />
        </Form.Item>
        <Form.Item
          name="isTruncate"
          label="清空目标表"
          tooltip="勾选后会先清空目标表再导入数据"
          valuePropName="checked"
        >
          <Checkbox />
        </Form.Item>
      </StepsForm.StepForm>
      <StepsForm.StepForm
        name="two"
        title="源文件"
        layout="horizontal"
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 16 }}
        initialValues={{ separator: ",", fileEncoding: "UTF-8" }}
        onFinish={async (values) => {
          setFormValues({
            ...formValues,
            file: values.file,
            fileType: values.fileType == "csv" ? "CSV" : "EXCEL",
            fileEncoding: values.fileEncoding,
            separator: values.separator,
          });
          return true;
        }}
      >
        <Form.Item name="file" label="文件" rules={[{ required: true }]}>
          <Upload
            name="file"
            maxCount={1}
            accept=".csv,.xlsx,.xls"
            beforeUpload={(fileInfo) => {
              let flag = true;
              if (fileInfo.size > 1024 * 1024 * 200) {
                flag = false;
                message.error(`文件大小不能超过200M`);
              } else {
                setUploadFile(fileInfo);
              }
              return flag || Upload.LIST_IGNORE;
            }}
            onChange={(info) => {
              const fileType = info.file.name.split(".").pop();
              formRef.current?.setFieldValue("fileType", fileType=='csv'?'csv':'xlsx');
              setSeparatorStatus(fileType === "csv");
            }}
          >
            <Button icon={<UploadOutlined />}>选择文件</Button>
          </Upload>
        </Form.Item>
        <Form.Item
          name="fileType"
          label="文件类型"
          rules={[{ required: true }]}
        >
          <Radio.Group
            onChange={(value) => {
              const type = value.target.value;
              setSeparatorStatus(type === "csv");
            }}
          >
            <Radio value="csv">CSV</Radio>
            <Radio value="xlsx">EXCEL</Radio>
          </Radio.Group>
        </Form.Item>
        <Form.Item
          name="fileEncoding"
          label="文件编码"
          rules={[{ required: true }]}
        >
          <Select>
            <Select.Option value="UTF-8">UTF-8</Select.Option>
            <Select.Option value="GBK">GBK</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="separator" label="分隔符" rules={[{ required: true }]}>
          <Select disabled={!separatorStatus}>
            <Select.Option value=",">,</Select.Option>
            <Select.Option value=";">;</Select.Option>
            <Select.Option value="|">|</Select.Option>
            <Select.Option value="\t">TAB</Select.Option>
            <Select.Option value=" ">SPACE</Select.Option>
          </Select>
        </Form.Item>
      </StepsForm.StepForm>
      <StepsForm.StepForm name="three" title="导入总览">
        <Descriptions
          items={descItems()}
          style={{ marginLeft: 80 }}
        ></Descriptions>
      </StepsForm.StepForm>
    </StepsForm>
  );
};

export default DataImportModal;
