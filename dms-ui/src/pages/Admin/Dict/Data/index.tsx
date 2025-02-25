import { PRIVILEGES } from "@/constants";
import { DictDataService } from "@/services/admin/dict.data.service";
import {
  FilterOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import {
  ActionType,
  PageContainer,
  ProColumns,
  ProTable,
} from "@ant-design/pro-components";
import {
  Badge,
  Button,
  Col,
  Drawer,
  Form,
  Input,
  message,
  Modal,
  Popconfirm,
  Row,
  Space,
  Table,
  Tooltip,
} from "antd";
import { useRef, useState } from "react";
import { history, useAccess, useIntl, useLocation } from "@umijs/max";
import DictDataForm from "./DictDataForm";

const DmsDictDataView: React.FC = () => {
  const intl = useIntl();
  const access = useAccess();
  const [queryForm] = Form.useForm();
  const actionRef = useRef<ActionType>();
  const [dictData, setDictData] = useState<DMS.ModalProps<DMS.SysDictData>>({
    open: false,
  });
  const [queryFormData, setQueryFormData] = useState<DMS.SysDictDataParam>({});
  const [queryFormOpen, setQueryFormOpen] = useState(false);
  const dictTypeParams: DMS.SysDictType = useLocation()
    .state as DMS.SysDictType;

  const columns: ProColumns<DMS.SysDictData>[] = [
    {
      title: intl.formatMessage({ id: "dms.admin.dict.data.dictCode" }),
      dataIndex: "dictCode",
      width: 180,
      fixed: "left",
    },
    {
      title: intl.formatMessage({ id: "dms.admin.dict.type" }),
      dataIndex: "sysDictType",
      hideInTable: true,
    },
    {
      title: intl.formatMessage({ id: "dms.admin.dict.data.dictValue" }),
      dataIndex: "dictValue",
      width: 180,
    },
    {
      title: intl.formatMessage({ id: "dms.admin.dict.data.remark" }),
      dataIndex: "remark",
      width: 240,
    },
    {
      title: intl.formatMessage({ id: "dms.common.table.field.createTime" }),
      dataIndex: "createTime",
      valueType: "dateTime",
      width: 180,
    },
    {
      title: intl.formatMessage({ id: "dms.common.table.field.updateTime" }),
      dataIndex: "updateTime",
      valueType: "dateTime",
      width: 180,
    },
    {
      title: intl.formatMessage({ id: "dms.common.table.field.action" }),
      key: "option",
      valueType: "option",
      align: "center",
      fixed: "right",
      width: 100,
      render: (_, record) => (
        <Space>
          {access.canAccess(PRIVILEGES.sysDicDcdEdit) && (
            <a
              key="update"
              onClick={() => {
                setDictData({ data: record, open: true });
              }}
            >
              {intl.formatMessage({ id: "dms.common.operate.update" })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.sysDicDcdDelete) && (
            <Popconfirm
              key="delete"
              title={intl.formatMessage({
                id: "dms.common.operate.delete.confirm.title",
              })}
              onConfirm={() => {
                DictDataService.delete(record).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({
                        id: "dms.common.message.operate.delete.success",
                      })
                    );
                    actionRef.current?.reload();
                  }
                });
              }}
            >
              <a href="#" style={{ color: "red" }}>
                {intl.formatMessage({ id: "dms.common.operate.delete" })}
              </a>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const onSearchInputChange = (item: any) => {
    queryForm.setFieldsValue({
      dictCode: item.currentTarget.value,
    });
    setQueryFormData({ dictCode: item.currentTarget.value });
    actionRef.current?.reload();
  };

  return (
    <PageContainer
      header={{
        title:
          intl.formatMessage({ id: "dms.admin.dict.data" }) +
          " - " +
          dictTypeParams.dictTypeCode,
        breadcrumb: {},
      }}
      onBack={() => {
        history.back();
      }}
      extra={[
        <Input
          key="search"
          prefix={<SearchOutlined />}
          allowClear
          placeholder={intl.formatMessage({
            id: "dms.common.operate.search.placeholder",
          })}
          onChange={onSearchInputChange}
          onPressEnter={onSearchInputChange}
        />,
        <Tooltip
          key="filter"
          title={intl.formatMessage({ id: "dms.common.operate.filter" })}
        >
          <Badge
            dot={
              queryFormData.dictCode || queryFormData.dictValue ? true : false
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
        access.canAccess(PRIVILEGES.sysDicDcdAdd) && (
          <Button
            key="new"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setDictData({
                open: true,
                data: { sysDictType: dictTypeParams },
              });
            }}
          >
            {intl.formatMessage({ id: "dms.common.operate.new" })}
          </Button>
        ),
      ]}
    >
      <ProTable<DMS.SysDictData>
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
          return DictDataService.list({
            ...params,
            dictTypeCode: dictTypeParams.dictTypeCode,
            ...queryFormData,
          });
        }}
        rowSelection={{
          selections: [Table.SELECTION_INVERT, Table.SELECTION_ALL],
        }}
        tableAlertOptionRender={({
          selectedRowKeys,
          selectedRows,
          onCleanSelected,
        }) => {
          return (
            <Space size={16}>
              {access.canAccess(PRIVILEGES.sysDicDcdDelete) && (
                <a
                  key="deleteBatch"
                  style={{ color: "red" }}
                  onClick={() => {
                    Modal.confirm({
                      title: intl.formatMessage({
                        id: "dms.common.operate.delete.confirm.title",
                      }),
                      content: intl.formatMessage({
                        id: "dms.common.operate.delete.confirm.content",
                      }),
                      onOk: () => {
                        DictDataService.deleteBatch(selectedRowKeys).then(
                          (resp) => {
                            if (resp.success) {
                              message.success(
                                intl.formatMessage({
                                  id: "dms.common.message.operate.delete.success",
                                })
                              );
                              onCleanSelected();
                              actionRef.current?.reload();
                            }
                          }
                        );
                      },
                    });
                  }}
                >
                  {intl.formatMessage({
                    id: "dms.common.operate.delete.batch",
                  })}
                </a>
              )}

              <a onClick={onCleanSelected} key="cancelSelect">
                {intl.formatMessage({ id: "dms.common.operate.cancel.select" })}
              </a>
            </Space>
          );
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
                  queryForm.setFieldsValue({
                    dictCode: "",
                    dictValue: "",
                  });
                  setQueryFormData({ dictCode: "", dictValue: "" });
                }}
              >
                {intl.formatMessage({ id: "dms.common.operate.reset" })}
              </Button>
            </Col>
            <Col span={12} style={{ textAlign: "right" }}>
              <Button
                onClick={() => {
                  setQueryFormData(queryForm.getFieldsValue());
                  actionRef.current?.reload();
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
            name="dictCode"
            label={intl.formatMessage({ id: "dms.admin.dict.data.dictCode" })}
          >
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item
            name="dictValue"
            label={intl.formatMessage({ id: "dms.admin.dict.data.dictValue" })}
          >
            <Input allowClear></Input>
          </Form.Item>
        </Form>
      </Drawer>
      {dictData.open && (
        <DictDataForm
          open={dictData.open}
          data={dictData.data}
          handleCancel={() => {
            setDictData({ open: false });
          }}
          handleOk={(isOpen: boolean) => {
            setDictData({ open: isOpen });
            actionRef.current?.reload();
          }}
        ></DictDataForm>
      )}
    </PageContainer>
  );
};

export default DmsDictDataView;
