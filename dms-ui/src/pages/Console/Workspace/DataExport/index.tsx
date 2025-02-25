import { DICT_TYPE } from "@/constants";
import { DictDataService } from "@/services/admin/dict.data.service";
import { DataTaskService } from "@/services/workspace/data.task";
import { DataSourceService } from "@/services/workspace/datasource.service";
import { NumberUtil } from "@/utils/NumberUtil";
import {
  FilterOutlined,
  ReloadOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import {
  ActionType,
  PageContainer,
  ProColumns,
  ProTable,
} from "@ant-design/pro-components";
import { useIntl } from "@umijs/max";
import {
  Badge,
  Button,
  Col,
  DatePicker,
  Drawer,
  Form,
  Input,
  Row,
  Select,
  Space,
  Tooltip,
} from "antd";
import moment from "moment";
import { useEffect, useRef, useState } from "react";
import TaskLogModal from "../components/TaskLogModal";

const { RangePicker } = DatePicker;

const DataExportView: React.FC<{ workspaceId: string | number }> = (props) => {
  const intl = useIntl();
  const [queryForm] = Form.useForm();
  const actionRef = useRef<ActionType>();
  const { workspaceId } = props;
  const taskType = "e";
  const expiryDay: number = 10;
  const [queryFormOpen, setQueryFormOpen] = useState<boolean>(false);
  const [queryFormData, setQueryFormData] = useState<DMS.DataTaskParam>({
    workspaceId: workspaceId,
    taskType: taskType,
  });
  const [logModalData, setLogModalData] = useState<
    DMS.ModalProps<{ taskId: number | string }>
  >({ open: false });
  const [dbList, setDbList] = useState<DMS.Dict[]>([]);
  const [taskStatusList, setTaskStatusList] = useState<DMS.Dict[]>([]);

  useEffect(() => {
    DataSourceService.listByWorkspace(workspaceId as string).then((resp) => {
      if (resp.success) {
        setDbList(resp.data as DMS.Dict[]);
      }
    });
    DictDataService.listByType(DICT_TYPE.taskStatus).then((resp) => {
      if (resp.success) {
        setTaskStatusList(resp.data as DMS.Dict[]);
      }
    });
  }, []);

  const columns: ProColumns<DMS.DataTask>[] = [
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.export.datasourceId",
      }),
      dataIndex: "datasourceId",
      width: 180,
      fixed: "left",
      render: (dom, entity) => {
        const value = dbList.filter(
          (item) => item.value == entity.datasourceId
        );
        return value[0].label;
      },
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.export.fileName",
      }),
      dataIndex: "fileName",
      width: 180,
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.export.fileType",
      }),
      dataIndex: "fileType",
      width: 100,
      render: (dom, entity) => {
        return entity.fileType?.label;
      },
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.export.fileSize",
      }),
      dataIndex: "fileSize",
      width: 100,
      render: (dom, entity) => {
        return NumberUtil.byteFormat(entity.fileSize as number);
      },
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.export.fileEncoding",
      }),
      dataIndex: "fileEncoding",
      width: 100,
      render: (dom, entity) => {
        return entity.fileEncoding?.label;
      },
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.export.taskStatus",
      }),
      dataIndex: "taskStatus",
      width: 100,
      render: (dom, entity) => {
        return intl.formatMessage({ id: entity.taskStatus?.label });
      },
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.export.creator",
      }),
      dataIndex: "creator",
      width: 100,
    },
    {
      title: intl.formatMessage({ id: "dms.common.table.field.createTime" }),
      dataIndex: "createTime",
      valueType: "dateTime",
      width: 160,
    },

    {
      title: intl.formatMessage({ id: "dms.common.table.field.action" }),
      key: "option",
      valueType: "option",
      align: "center",
      fixed: "right",
      width: 120,
      render: (_, record) => (
        <Space>
          {moment().diff(moment(record.createTime), "days") <= expiryDay &&
            record.taskStatus?.value == "3" && (
              <a
                key="download"
                onClick={() => {
                  DataTaskService.download(record);
                }}
              >
                {intl.formatMessage({ id: "dms.common.operate.download" })}
              </a>
            )}
          {moment().diff(moment(record.createTime), "days") <= expiryDay && (
            <a
              key="log"
              onClick={() => {
                setLogModalData({
                  open: true,
                  data: { taskId: record.id as string },
                });
              }}
            >
              {intl.formatMessage({
                id: "dms.console.workspace.export.action.log",
              })}
            </a>
          )}
        </Space>
      ),
    },
  ];

  const onSearchInputChange = (item: any) => {
    queryForm.setFieldsValue({
      fileName: item.currentTarget.value,
    });
    setQueryFormData({
      fileName: item.currentTarget.value,
      workspaceId: workspaceId,
      taskType: taskType,
    });
    actionRef.current?.reload();
  };

  return (
    <PageContainer
      header={{
        title: intl.formatMessage({ id: "dms.console.workspace.export" }),
        breadcrumb: {},
      }}
      subTitle= {intl.formatMessage({id:'dms.console.workspace.export.subTitle'})} 
      extra={[
        <Input
          key="search"
          prefix={<SearchOutlined />}
          allowClear
          placeholder={intl.formatMessage({
            id: "dms.common.operate.search.placeholder",
          })}
          onPressEnter={onSearchInputChange}
          onChange={onSearchInputChange}
        />,
        <Tooltip
          key="filter"
          title={intl.formatMessage({ id: "dms.common.operate.filter" })}
        >
          <Badge
            dot={
              queryFormData.createTimeFrom ||
              queryFormData.taskStatus ||
              queryFormData.fileName ||
              queryFormData.creator ||
              queryFormData.datasourceId
                ? true
                : false
            }
          >
            <Button
              icon={<FilterOutlined />}
              type="text"
              onClick={() => {
                setQueryFormOpen(true);
              }}
            ></Button>
          </Badge>
        </Tooltip>,
        <Tooltip
          key="reload"
          title={intl.formatMessage({ id: "dms.common.operate.refresh" })}
        >
          <Button
            icon={<ReloadOutlined />}
            type="text"
            onClick={() => {
              actionRef.current?.reload();
            }}
          ></Button>
        </Tooltip>,
      ]}
    >
      <ProTable<DMS.DataTask>
        search={false}
        columns={columns}
        scroll={{ x: 1300 }}
        rowKey="id"
        actionRef={actionRef}
        options={{ density: false, setting: false, reload: false }}
        pagination={{
          showQuickJumper: true,
          showSizeChanger: true,
          defaultPageSize: 10,
          hideOnSinglePage: true,
        }}
        request={(params, sorter, filter) => {
          return DataTaskService.list({
            ...params,
            ...queryFormData,
          });
        }}
      ></ProTable>
      <Drawer
        width={560}
        title={intl.formatMessage({ id: "dms.common.operate.filter" })}
        onClose={() => {
          setQueryFormOpen(false);
          actionRef.current?.reload();
        }}
        styles={{ body: { marginTop: 16 } }}
        placement="right"
        open={queryFormOpen}
        footer={
          <Row>
            <Col span={12}>
              <Button
                onClick={() => {
                  let d = {
                    datasourceId: "",
                    fileName: "",
                    taskType: taskType,
                    taskStatus: "",
                    creator: "",
                    createTimeFrom: "",
                    createTimeTo: "",
                  };
                  queryForm.setFieldsValue({
                    ...d,
                    createTime: undefined,
                  });
                  setQueryFormData({
                    ...d,
                    workspaceId: workspaceId,
                  });
                }}
              >
                {intl.formatMessage({ id: "dms.common.operate.reset" })}
              </Button>
            </Col>
            <Col span={12} style={{ textAlign: "right" }}>
              <Button
                onClick={() => {
                  queryForm.validateFields().then((values) => {
                    let d: DMS.DataTaskParam = {
                      workspaceId: workspaceId,
                      taskType: taskType,
                      datasourceId: values?.datasourceId,
                      fileName: values?.fileName,
                      taskStatus: values?.taskStatus,
                      creator: values?.creator,
                      createTimeFrom: values?.createTime
                        ? moment(values?.createTime[0].$d).format(
                            "YYYY-MM-DD HH:mm:ss"
                          )
                        : undefined,
                      createTimeTo: values?.createTime
                        ? moment(values?.createTime[1].$d).format(
                            "YYYY-MM-DD HH:mm:ss"
                          )
                        : undefined,
                    };
                    setQueryFormData({ ...d });
                    actionRef.current?.reload();
                  });
                  setQueryFormOpen(false);
                }}
                type="primary"
              >
                {intl.formatMessage({ id: "dms.common.operate.confirm" })}
              </Button>
            </Col>
          </Row>
        }
      >
        <Form
          layout="horizontal"
          labelCol={{ span: 6 }}
          wrapperCol={{ span: 16 }}
          form={queryForm}
        >
          <Form.Item
            name="datasourceId"
            label={intl.formatMessage({
              id: "dms.console.workspace.export.datasourceId",
            })}
          >
            <Select
              style={{ width: "100%" }}
              allowClear={true}
              showSearch={true}
              placeholder={intl.formatMessage({
                id: "dms.console.workspace.dataquery.select",
              })}
            >
              {dbList &&
                dbList.map((item) => {
                  return (
                    <Select.Option key={item.value} value={item.value}>
                      {item.label}
                    </Select.Option>
                  );
                })}
            </Select>
          </Form.Item>
          <Form.Item
            name="fileName"
            label={intl.formatMessage({
              id: "dms.console.workspace.export.fileName",
            })}
          >
            <Input></Input>
          </Form.Item>
          <Form.Item
            name="taskStatus"
            label={intl.formatMessage({
              id: "dms.console.workspace.export.taskStatus",
            })}
          >
            <Select
              style={{ width: "100%" }}
              allowClear={true}
              showSearch={true}
              placeholder={intl.formatMessage({
                id: "dms.common.operate.select.placeholder",
              })}
            >
              {taskStatusList &&
                taskStatusList.map((item) => {
                  return (
                    <Select.Option key={item.value} value={item.value}>
                      {intl.formatMessage({ id: item.label })}
                    </Select.Option>
                  );
                })}
            </Select>
          </Form.Item>

          <Form.Item
            name="creator"
            label={intl.formatMessage({
              id: "dms.console.workspace.export.creator",
            })}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="createTime"
            label={intl.formatMessage({
              id: "dms.common.table.field.createTime",
            })}
          >
            <RangePicker showTime={true}></RangePicker>
          </Form.Item>
        </Form>
      </Drawer>
      {logModalData.open && (
        <TaskLogModal
          open={logModalData.open}
          data={logModalData.data}
          handleOk={(isOpen: boolean, value: any) => {
            setLogModalData({ open: isOpen });
          }}
          handleCancel={() => {
            setLogModalData({ open: false });
          }}
        ></TaskLogModal>
      )}
    </PageContainer>
  );
};

export default DataExportView;
