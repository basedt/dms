import { DICT_TYPE, PRIVILEGES } from '@/constants';
import { DictDataService } from '@/services/admin/dict.data.service';
import { UserService } from '@/services/admin/user.service';
import { FilterOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { useAccess, useIntl } from '@umijs/max';
import {
  Badge,
  Button,
  Col,
  Drawer,
  Form,
  Input,
  message,
  Modal,
  Row,
  Select,
  Space,
  Table,
  Tooltip,
} from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import UserForm from './UserForm';
import UserRoleGrant from './UserRoleGrant';

const UserView: React.FC = () => {
  const intl = useIntl();
  const access = useAccess();
  const [queryForm] = Form.useForm();
  const actionRef = useRef<ActionType>();
  const [queryFormOpen, setQueryFormOpen] = useState<boolean>(false);
  const [queryFormData, setQueryFormData] = useState<DMS.SysUserParam>({});
  const [userData, setUserData] = useState<DMS.ModalProps<DMS.SysUser>>({
    open: false,
  });
  const [userRoleGrant, setUserRoleGrant] = useState<DMS.ModalProps<DMS.SysUser>>({
    open: false,
  });
  const [userStatusList, setUserStatusList] = useState<DMS.Dict[]>([]);

  useEffect(() => {
    DictDataService.listByType(DICT_TYPE.userStatus).then((resp) => {
      if (resp.success) {
        setUserStatusList(resp.data as DMS.Dict[]);
      }
    });
  }, []);

  const columns: ProColumns<DMS.SysUser>[] = [
    {
      title: intl.formatMessage({ id: 'dms.admin.user.userName' }),
      dataIndex: 'userName',
      width: 120,
      fixed: 'left',
    },
    {
      title: intl.formatMessage({ id: 'dms.admin.user.nickName' }),
      dataIndex: 'nickName',
      width: 180,
    },
    {
      title: intl.formatMessage({ id: 'dms.admin.user.email' }),
      dataIndex: 'email',
      width: 180,
    },
    {
      title: intl.formatMessage({ id: 'dms.admin.user.userStatus' }),
      dataIndex: 'userStatus',
      width: 120,
      align: 'center',
      render: (dom, entity) => {
        return entity.userStatus?.label;
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.admin.user.mobilePhone' }),
      dataIndex: 'mobilePhone',
      width: 180,
    },
    {
      title: intl.formatMessage({ id: 'dms.common.table.field.createTime' }),
      dataIndex: 'createTime',
      valueType: 'dateTime',
      width: 180,
    },
    {
      title: intl.formatMessage({ id: 'dms.common.table.field.updateTime' }),
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      width: 180,
    },
    {
      title: intl.formatMessage({ id: 'dms.common.table.field.action' }),
      key: 'option',
      valueType: 'option',
      align: 'center',
      fixed: 'right',
      width: 120,
      render: (_, record) => (
        <Space>
          {access.canAccess(PRIVILEGES.sysUsrUliEdit) && (
            <a
              key="update"
              onClick={() => {
                setUserData({ data: record, open: true });
              }}
            >
              {intl.formatMessage({ id: 'dms.common.operate.update' })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.sysRolRliGrant) && (
            <a
              href="#"
              key="grant"
              onClick={() => {
                setUserRoleGrant({ data: record, open: true });
              }}
            >
              {intl.formatMessage({ id: 'dms.admin.user.role.grant' })}
            </a>
          )}
        </Space>
      ),
    },
  ];

  const onSearchInputChange = (item: any) => {
    queryForm.setFieldsValue({
      userName: item.currentTarget.value,
    });
    setQueryFormData({ userName: item.currentTarget.value });
    actionRef.current?.reload();
  };

  return (
    <PageContainer
      header={{
        title: intl.formatMessage({ id: 'dms.admin.user.list' }),
        breadcrumb: {},
      }}
      extra={[
        <Input
          key="search"
          prefix={<SearchOutlined />}
          allowClear
          placeholder={intl.formatMessage({
            id: 'dms.common.operate.search.placeholder',
          })}
          onChange={onSearchInputChange}
          onPressEnter={onSearchInputChange}
        />,
        <Tooltip key="filter" title={intl.formatMessage({ id: 'dms.common.operate.filter' })}>
          <Badge
            dot={
              queryFormData.userName || queryFormData.email || queryFormData.userStatus
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
        <Tooltip key="reload" title={intl.formatMessage({ id: 'dms.common.operate.refresh' })}>
          <Button
            icon={<ReloadOutlined />}
            type="text"
            onClick={() => {
              actionRef.current?.reload();
            }}
          ></Button>
        </Tooltip>,
        access.canAccess(PRIVILEGES.sysUsrUliAdd) && (
          <Button
            key="new"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setUserData({
                open: true,
                data: { userStatus: { value: 'normal' } },
              });
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.new' })}
          </Button>
        ),
      ]}
    >
      <ProTable<DMS.SysUser>
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
          return UserService.list({
            ...params,
            ...queryFormData,
          });
        }}
        rowSelection={{
          selections: [Table.SELECTION_INVERT, Table.SELECTION_ALL],
        }}
        tableAlertOptionRender={({ selectedRowKeys, selectedRows, onCleanSelected }) => {
          return (
            <Space size={16}>
              {access.canAccess(PRIVILEGES.sysUsrUliDelete) && (
                <a
                  key="deleteBatch"
                  onClick={() => {
                    Modal.confirm({
                      title: intl.formatMessage({
                        id: 'dms.admin.user.disable.title',
                      }),
                      content: intl.formatMessage({
                        id: 'dms.admin.user.disable.content',
                      }),
                      onOk: () => {
                        UserService.deleteBatch(selectedRowKeys).then((resp) => {
                          if (resp.success) {
                            message.success(
                              intl.formatMessage({
                                id: 'dms.common.message.operate.success',
                              }),
                            );
                            onCleanSelected();
                            actionRef.current?.reload();
                          }
                        });
                      },
                    });
                  }}
                >
                  {intl.formatMessage({
                    id: 'dms.common.operate.disable.batch',
                  })}
                </a>
              )}
              {access.canAccess(PRIVILEGES.sysUsrUliDelete) && (
                <a
                  key="enableBatch"
                  onClick={() => {
                    Modal.confirm({
                      title: intl.formatMessage({
                        id: 'dms.admin.user.enable.title',
                      }),
                      content: intl.formatMessage({
                        id: 'dms.admin.user.enable.content',
                      }),
                      onOk: () => {
                        UserService.enableUsers(selectedRowKeys).then((resp) => {
                          if (resp.success) {
                            message.success(
                              intl.formatMessage({
                                id: 'dms.common.message.operate.success',
                              }),
                            );
                            onCleanSelected();
                            actionRef.current?.reload();
                          }
                        });
                      },
                    });
                  }}
                >
                  {intl.formatMessage({
                    id: 'dms.common.operate.enable.batch',
                  })}
                </a>
              )}
              <a onClick={onCleanSelected} key="cancelSelect">
                {intl.formatMessage({ id: 'dms.common.operate.select.cancel' })}
              </a>
            </Space>
          );
        }}
      ></ProTable>
      <Drawer
        width={560}
        title={intl.formatMessage({ id: 'dms.common.operate.filter' })}
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
                    userName: '',
                    email: '',
                    userStatus: null,
                  });
                  setQueryFormData({
                    userName: '',
                    email: '',
                    userStatus: '',
                  });
                }}
              >
                {intl.formatMessage({ id: 'dms.common.operate.reset' })}
              </Button>
            </Col>
            <Col span={12} style={{ textAlign: 'right' }}>
              <Button
                onClick={() => {
                  setQueryFormData(queryForm.getFieldsValue());
                  actionRef.current?.reload();
                  setQueryFormOpen(false);
                }}
                type="primary"
              >
                {intl.formatMessage({ id: 'dms.common.operate.confirm' })}
              </Button>
            </Col>
          </Row>
        }
      >
        <Form layout="horizontal" labelCol={{ span: 6 }} wrapperCol={{ span: 16 }} form={queryForm}>
          <Form.Item name="userName" label={intl.formatMessage({ id: 'dms.admin.user.userName' })}>
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item name="email" label={intl.formatMessage({ id: 'dms.admin.user.email' })}>
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item
            name="userStatus"
            label={intl.formatMessage({ id: 'dms.admin.user.userStatus' })}
          >
            <Select
              showSearch={true}
              allowClear={true}
              optionFilterProp="label"
              filterOption={(input, option) =>
                (option!.children as unknown as string).toLowerCase().includes(input.toLowerCase())
              }
            >
              {userStatusList.map((item) => {
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
      {userData.open && (
        <UserForm
          open={userData.open}
          data={userData.data}
          handleOk={(isOpen: boolean) => {
            setUserData({ open: isOpen });
            actionRef.current?.reload();
          }}
          handleCancel={() => {
            setUserData({ open: false });
          }}
        ></UserForm>
      )}
      {userRoleGrant.open && (
        <UserRoleGrant
          open={userRoleGrant.open}
          data={userRoleGrant.data}
          handleOk={(isOpen: boolean) => {
            setUserRoleGrant({ open: isOpen });
            actionRef.current?.reload();
          }}
          handleCancel={() => {
            setUserRoleGrant({ open: false });
          }}
        ></UserRoleGrant>
      )}
    </PageContainer>
  );
};

export default UserView;
