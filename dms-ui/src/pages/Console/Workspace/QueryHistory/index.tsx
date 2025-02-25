import { DICT_TYPE } from "@/constants";
import { DictDataService } from "@/services/admin/dict.data.service";
import { DataSourceService } from "@/services/workspace/datasource.service";
import { SqlHistoryService } from "@/services/workspace/sql.service";
import { FilterOutlined, ReloadOutlined } from "@ant-design/icons";
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
  Radio,
  Row,
  Select,
  Tooltip,
  Typography,
} from "antd";
import moment from "moment";
import { useEffect, useRef, useState } from "react";

const { Paragraph } = Typography;
const { RangePicker } = DatePicker;

const QueryHistoryView: React.FC<{ workspaceId: string | number }> = (
  props
) => {
  const intl = useIntl();
  const { workspaceId } = props;
  const [queryForm] = Form.useForm();
  const actionRef = useRef<ActionType>();
  const [queryFormOpen, setQueryFormOpen] = useState<boolean>(false);
  const [queryFormData, setQueryFormData] = useState<DMS.LogSqlHistoryParam>({
    workspaceId: workspaceId,
  });
  const [dbList, setDbList] = useState<DMS.Dict[]>([]);
  const [sqlStatusList, setSqlStatusList] = useState<DMS.Dict[]>([]);

  useEffect(() => {
    DataSourceService.listByWorkspace(workspaceId as string).then((resp) => {
      if (resp.success) {
        setDbList(resp.data as DMS.Dict[]);
      }
    });
    DictDataService.listByType(DICT_TYPE.sqlStatus).then((resp) => {
      if (resp.success) {
        setSqlStatusList(resp.data as DMS.Dict[]);
      }
    });
  }, []);

  const columns: ProColumns<DMS.LogSqlHistory>[] = [
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.sqlhistory.datasource",
      }),
      dataIndex: "datasourceName",
      width: 140,
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
        id: "dms.console.workspace.sqlhistory.sqlScript",
      }),
      dataIndex: "sqlScript",
      width: 200,
      render: (dom, entity) => {
        return (
          <Paragraph
            copyable={true}
            ellipsis={{ rows: 3 }}
            style={{ marginBottom: 0 }}
          >
            {entity.sqlScript}
          </Paragraph>
        );
      },
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.sqlhistory.startTime",
      }),
      dataIndex: "startTime",
      valueType: "dateTime",
      width: 120,
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.sqlhistory.endTime",
      }),
      dataIndex: "endTime",
      valueType: "dateTime",
      width: 120,
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.sqlhistory.sqlStatus",
      }),
      dataIndex: "sqlStatus",
      width: 100,
      align: "center",
      render: (dom, entity) => {
        return intl.formatMessage({ id: entity.sqlStatus?.label });
      },
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.sqlhistory.remark",
      }),
      dataIndex: "remark",
      width: 180,
      render: (dom, entity) => {
        return (
          <Paragraph ellipsis={{ rows: 3 }} style={{ marginBottom: 0 }}>
            {entity.remark}
          </Paragraph>
        );
      },
    },
    {
      title: intl.formatMessage({
        id: "dms.console.workspace.sqlhistory.creator",
      }),
      dataIndex: "creator",
      width: 140,
      align: "center",
    },
  ];

  return (
    <PageContainer
      header={{
        title: intl.formatMessage({ id: "dms.console.workspace.sqlhistory" }),
        breadcrumb: {},
      }}
      extra={[
        <Tooltip
          key="filter"
          title={intl.formatMessage({ id: "dms.common.operate.filter" })}
        >
          <Badge
            dot={
              queryFormData.datasourceId ||
              queryFormData.sqlStatus ||
              queryFormData.creator ||
              queryFormData.startTimeFrom ||
              queryFormData.endTimeFrom
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
      <ProTable<DMS.LogSqlHistory>
        search={false}
        columns={columns}
        scroll={{ x: 1400 }}
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
          return SqlHistoryService.list({
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
                  let d: DMS.LogSqlHistoryParam = {
                    workspaceId: workspaceId,
                    datasourceId: "",
                    sqlStatus: "",
                    creator: "",
                    startTimeFrom: "",
                    startTimeTo: "",
                    endTimeFrom: "",
                    endTimeTo: "",
                  };
                  queryForm.setFieldsValue({
                    ...d,
                    datasource: null,
                    startTime: null,
                    endTime: null,
                  });
                  setQueryFormData({ ...d });
                }}
              >
                {intl.formatMessage({ id: "dms.common.operate.reset" })}
              </Button>
            </Col>
            <Col span={12} style={{ textAlign: "right" }}>
              <Button
                onClick={() => {
                  queryForm.validateFields().then((values) => {
                    let d: DMS.LogSqlHistoryParam = {
                      workspaceId: workspaceId,
                      datasourceId: values?.datasource,
                      sqlStatus: values?.sqlStatus,
                      creator: values?.creator,
                      startTimeFrom: values?.startTime
                        ? moment(values?.startTime[0].$d).format(
                            "YYYY-MM-DD HH:mm:ss"
                          )
                        : undefined,
                      startTimeTo: values?.startTime
                        ? moment(values?.startTime[1].$d).format(
                            "YYYY-MM-DD HH:mm:ss"
                          )
                        : undefined,
                      endTimeFrom: values?.endTime
                        ? moment(values?.endTime[0].$d).format(
                            "YYYY-MM-DD HH:mm:ss"
                          )
                        : undefined,
                      endTimeTo: values?.endTime
                        ? moment(values?.endTime[1].$d).format(
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
          form={queryForm}
          labelCol={{ span: 6 }}
          wrapperCol={{ span: 16 }}
        >
          <Form.Item
            name="datasource"
            label={intl.formatMessage({
              id: "dms.console.workspace.datasource.datasourceName",
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
            name="startTime"
            label={intl.formatMessage({
              id: "dms.console.workspace.sqlhistory.startTime",
            })}
          >
            <RangePicker showTime={true}></RangePicker>
          </Form.Item>
          <Form.Item
            name="endTime"
            label={intl.formatMessage({
              id: "dms.console.workspace.sqlhistory.endTime",
            })}
          >
            <RangePicker showTime={true}></RangePicker>
          </Form.Item>
          <Form.Item
            name="sqlStatus"
            label={intl.formatMessage({
              id: "dms.console.workspace.sqlhistory.sqlStatus",
            })}
          >
            <Radio.Group>
              {sqlStatusList &&
                sqlStatusList.map((item) => {
                  return (
                    <Radio key={item.value} value={item.value}>
                      {intl.formatMessage({ id: item.label })}
                    </Radio>
                  );
                })}
            </Radio.Group>
          </Form.Item>
          <Form.Item
            name="creator"
            label={intl.formatMessage({
              id: "dms.console.workspace.sqlhistory.creator",
            })}
          >
            <Input />
          </Form.Item>
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default QueryHistoryView;
