import { DICT_TYPE, PRIVILEGES } from "@/constants";
import { DictDataService } from "@/services/admin/dict.data.service";
import { RoleService } from "@/services/admin/role.service";
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
  Select,
  Space,
  Table,
  Tooltip,
} from "antd";
import React, { useEffect, useRef, useState } from "react";
import { useAccess, useIntl } from "@umijs/max";
import PrivilegeGrant from "./PrivilegeGrant";
import RoleForm from "./RoleForm";
import RoleUserGrant from "./RoleUserGrant";

const RoleView: React.FC = () => {
  const intl = useIntl();
  const access = useAccess();
  const [queryForm] = Form.useForm();
  const actionRef = useRef<ActionType>();
  const [queryFormOpen, setQueryFormOpen] = useState<boolean>(false);
  const [queryFormData, setQueryFormData] = useState<DMS.SysRoleParam>({});
  const [roleStatusList, setRoleStatusList] = useState<DMS.Dict[]>([]);
  const [roleData, setRoleData] = useState<DMS.ModalProps<DMS.SysRole>>({
    open: false,
  });
  const [roleGrant, setRoleGrant] = useState<DMS.ModalProps<DMS.SysRole>>({
    open: false,
  });
  const [privilegeGrant, setPrivilegeGrant] = useState<
    DMS.ModalProps<DMS.SysRole>
  >({
    open: false,
  });

  useEffect(() => {
    DictDataService.listByType(DICT_TYPE.roleStatus).then((resp) => {
      if (resp.success) {
        setRoleStatusList(resp.data as DMS.Dict[]);
      }
    });
  }, []);

  const columns: ProColumns<DMS.SysRole>[] = [
    {
      title: intl.formatMessage({ id: "dms.admin.role.roleCode" }),
      dataIndex: "roleCode",
      width: 120,
      fixed: "left",
      hideInTable: true,
    },
    {
      title: intl.formatMessage({ id: "dms.admin.role.roleName" }),
      dataIndex: "roleName",
      width: 180,
      fixed: "left",
    },
    {
      title: intl.formatMessage({ id: "dms.admin.role.roleType" }),
      dataIndex: "roleType",
      width: 120,
      align: "center",
      render: (dom, entity) => {
        return entity.roleType?.label;
      },
    },
    {
      title: intl.formatMessage({ id: "dms.admin.role.roleStatus" }),
      dataIndex: "roleStatus",
      width: 120,
      align: "center",
      render: (dom, entity) => {
        return entity.roleStatus?.label;
      },
    },
    {
      title: intl.formatMessage({ id: "dms.admin.role.roleDesc" }),
      dataIndex: "roleDesc",
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
      width: 220,
      render: (_, record) => (
        <Space>
          {access.canAccess(PRIVILEGES.sysRolRliEdit) && (
            <a
              key="update"
              onClick={() => {
                setRoleData({ data: record, open: true });
              }}
            >
              {intl.formatMessage({ id: "dms.common.operate.update" })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.sysRolRliGrant) && (
            <a
              key="ungrant"
              onClick={() => {
                setRoleGrant({ open: true, data: record });
              }}
            >
              {intl.formatMessage({ id: "dms.admin.role.user.grant" })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.sysRolRliGrant) && (
            <a
              key="pgrant"
              onClick={() => {
                setPrivilegeGrant({ open: true, data: record });
              }}
            >
              {intl.formatMessage({ id: "dms.admin.role.privilege.grant" })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.sysRolRliDelete) && (
            <Popconfirm
              key="delete"
              title={intl.formatMessage({
                id: "dms.common.operate.delete.confirm.title",
              })}
              onConfirm={() => {
                RoleService.delete(record).then((resp) => {
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
      roleName: item.currentTarget.value,
    });
    setQueryFormData({ roleName: item.currentTarget.value });
    actionRef.current?.reload();
  };

  return (
    <PageContainer
      header={{
        title: intl.formatMessage({ id: "dms.admin.role.list" }),
        breadcrumb: {},
      }}
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
              queryFormData.roleName || queryFormData.roleStatus ? true : false
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
        access.canAccess(PRIVILEGES.sysRolRliAdd) && (
          <Button
            key="new"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setRoleData({
                open: true,
              });
            }}
          >
            {intl.formatMessage({ id: "dms.common.operate.new" })}
          </Button>
        ),
      ]}
    >
      <ProTable<DMS.SysRole>
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
          return RoleService.list({
            ...params,
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
              {access.canAccess(PRIVILEGES.sysRolRliDelete) && (
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
                        RoleService.deleteBatch(selectedRowKeys).then(
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
                    roleName: "",
                    roleStatus: "",
                  });
                  setQueryFormData({ roleName: "", roleStatus: "" });
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
            name="roleName"
            label={intl.formatMessage({ id: "dms.admin.role.roleName" })}
          >
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item
            name="roleStatus"
            label={intl.formatMessage({ id: "dms.admin.role.roleStatus" })}
          >
            <Select
              showSearch={true}
              allowClear={true}
              optionFilterProp="label"
              filterOption={(input, option) =>
                (option!.children as unknown as string)
                  .toLowerCase()
                  .includes(input.toLowerCase())
              }
            >
              {roleStatusList.map((item) => {
                return (
                  <Select.Option key={item.value} value={item.value}>
                    {item.label}
                  </Select.Option>
                );
              })}
            </Select>
          </Form.Item>
        </Form>
      </Drawer>
      {roleData.open && (
        <RoleForm
          open={roleData.open}
          data={roleData.data}
          handleOk={(isOpen: boolean) => {
            setRoleData({ open: isOpen });
            actionRef.current?.reload();
          }}
          handleCancel={() => {
            setRoleData({ open: false });
          }}
        ></RoleForm>
      )}
      {roleGrant.open && (
        <RoleUserGrant
          open={roleGrant.open}
          data={roleGrant.data}
          handleOk={(isOpen: boolean) => {
            setRoleGrant({ open: isOpen });
            actionRef.current?.reload();
          }}
          handleCancel={() => {
            setRoleGrant({ open: false });
          }}
        ></RoleUserGrant>
      )}
      {privilegeGrant.open && (
        <PrivilegeGrant
          open={privilegeGrant.open}
          data={privilegeGrant.data}
          handleOk={(isOpen: boolean) => {
            setPrivilegeGrant({ open: isOpen });
            actionRef.current?.reload();
          }}
          handleCancel={() => {
            setPrivilegeGrant({ open: false });
          }}
        ></PrivilegeGrant>
      )}
    </PageContainer>
  );
};

export default RoleView;
