import { PRIVILEGES } from '@/constants';
import { UserService } from '@/services/admin/user.service';
import { WorkspaceService } from '@/services/workspace/workspace.service';
import {
  AppstoreOutlined,
  BarsOutlined,
  FilterOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { history, useAccess, useIntl } from '@umijs/max';
import {
  AutoComplete,
  Avatar,
  Badge,
  Button,
  Card,
  Col,
  Divider,
  Drawer,
  Form,
  Input,
  List,
  message,
  Modal,
  Popconfirm,
  Row,
  Segmented,
  Space,
  Table,
  Tooltip,
  Typography,
} from 'antd';
import React, { useEffect, useRef, useState } from 'react';
import WorkspaceForm from './Workspace/components/WorkspaceForm';

const DmsConsoleView: React.FC = () => {
  const intl = useIntl();
  const access = useAccess();
  const [queryForm] = Form.useForm();
  const actionRef = useRef<ActionType>();
  const [queryFormOpen, setQueryFormOpen] = useState<boolean>(false);
  const [queryFormData, setQueryFormData] = useState<DMS.WorkspaceParam>({});
  const [options, setOptions] = useState<{ value: string }[]>([]);
  const [workspaces, setWorkspaces] = useState<DMS.Workspace[]>([]);
  const [total, setTotal] = useState<number>(0);
  const [pagination, setPagination] = useState<DMS.QueryParam>();
  const [layout, setLayout] = useState<string>('card');
  const [workspaceData, setWorkspaceData] = useState<DMS.ModalProps<DMS.Workspace>>({
    open: false,
  });

  const columns: ProColumns<DMS.Workspace>[] = [
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.workspaceCode' }),
      dataIndex: 'workspaceCode',
      width: 180,
      fixed: 'left',
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.workspaceName' }),
      dataIndex: 'workspaceName',
      width: 180,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.owner' }),
      dataIndex: 'owner',
      width: 180,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.remark' }),
      dataIndex: 'remark',
      width: 240,
      renderText: (text, record, index, action) => {
        return (
          <Typography.Text ellipsis={{ tooltip: record.remark }}>{record.remark}</Typography.Text>
        );
      },
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
      width: 220,
      render: (_, record) => (
        <Space>
          {access.canAccess(PRIVILEGES.wsWssWpiShow) && (
            <a
              type="link"
              key="open"
              onClick={() => {
                history.push(`/workspace/${record.id}?m=query`);
              }}
            >
              {intl.formatMessage({ id: 'dms.console.workspace.open' })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.wsWssWpiEdit) && (
            <a
              key="update"
              onClick={() => {
                setWorkspaceData({ data: record, open: true });
              }}
            >
              {intl.formatMessage({ id: 'dms.common.operate.update' })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.wsWssWpiDelete) && (
            <Popconfirm
              key="delete"
              title={intl.formatMessage({
                id: 'dms.common.operate.delete.confirm.title',
              })}
              onConfirm={() => {
                WorkspaceService.delete(record).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({
                        id: 'dms.common.message.operate.delete.success',
                      }),
                    );
                    refreshData();
                  }
                });
              }}
            >
              <a href="#" style={{ color: 'red' }}>
                {intl.formatMessage({ id: 'dms.common.operate.delete' })}
              </a>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const refreshData = (params?: DMS.WorkspaceParam) => {
    WorkspaceService.list({
      ...pagination,
      ...queryFormData,
      ...params,
    }).then((resp) => {
      setWorkspaces(resp.data);
      setTotal(resp.total);
    });
  };

  const onSearchInputChange = (item: any) => {
    queryForm.setFieldsValue({
      workspaceCode: item.currentTarget.value,
    });
    setQueryFormData({ workspaceCode: item.currentTarget.value });
    refreshData({ workspaceCode: item.currentTarget.value });
  };

  useEffect(() => {
    refreshData();
  }, [pagination]);

  return (
    <PageContainer
      header={{
        title: (
          <>
            {intl.formatMessage({ id: 'dms.workspace' })}
            <Divider type="vertical" />
            <Segmented
              options={[
                {
                  value: 'card',
                  icon: (
                    <Tooltip
                      title={intl.formatMessage({
                        id: 'dms.common.layout.card',
                      })}
                    >
                      <AppstoreOutlined />
                    </Tooltip>
                  ),
                },
                {
                  value: 'list',
                  icon: (
                    <Tooltip
                      title={intl.formatMessage({
                        id: 'dms.common.layout.list',
                      })}
                    >
                      <BarsOutlined />
                    </Tooltip>
                  ),
                },
              ]}
              size="small"
              value={layout}
              onChange={(value) => {
                if (layout == 'card') {
                  setPagination({ current: 1, pageSize: 9 });
                } else {
                  setPagination({ current: 1, pageSize: 10 });
                }
                setLayout(value);
              }}
            />
          </>
        ),
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
          onPressEnter={onSearchInputChange}
          onChange={onSearchInputChange}
        />,
        <Tooltip key="filter" title={intl.formatMessage({ id: 'dms.common.operate.filter' })}>
          <Badge
            dot={
              queryFormData.workspaceCode || queryFormData.workspaceName || queryFormData.owner
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
              refreshData();
            }}
          ></Button>
        </Tooltip>,
        access.canAccess(PRIVILEGES.wsWssWpiAdd) && (
          <Button
            key="new"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setWorkspaceData({
                open: true,
              });
            }}
          >
            {intl.formatMessage({ id: 'dms.console.workspace.create' })}
          </Button>
        ),
      ]}
    >
      {layout == 'list' && (
        <ProTable<DMS.Workspace>
          search={false}
          columns={columns}
          scroll={{ x: 1300 }}
          rowKey="id"
          actionRef={actionRef}
          options={{ density: false, setting: false, reload: false }}
          pagination={{
            total: total,
            showQuickJumper: true,
            showSizeChanger: true,
            defaultPageSize: 10,
            hideOnSinglePage: true,
          }}
          dataSource={workspaces}
          onChange={(pagination, filters, sorter, extra) => {
            setPagination(pagination);
          }}
          rowSelection={{
            selections: [Table.SELECTION_INVERT, Table.SELECTION_ALL],
          }}
          tableAlertOptionRender={({ selectedRowKeys, selectedRows, onCleanSelected }) => {
            return (
              <Space size={16}>
                {access.canAccess(PRIVILEGES.wsWssWpiDelete) && (
                  <a
                    key="deleteBatch"
                    style={{ color: 'red' }}
                    onClick={() => {
                      Modal.confirm({
                        title: intl.formatMessage({
                          id: 'dms.common.operate.delete.confirm.title',
                        }),
                        content: intl.formatMessage({
                          id: 'dms.common.operate.delete.confirm.content',
                        }),
                        onOk: () => {
                          WorkspaceService.deleteBatch(selectedRowKeys as string[]).then((resp) => {
                            if (resp.success) {
                              message.success(
                                intl.formatMessage({
                                  id: 'dms.common.message.operate.delete.success',
                                }),
                              );
                              onCleanSelected();
                              refreshData();
                            }
                          });
                        },
                      });
                    }}
                  >
                    {intl.formatMessage({
                      id: 'dms.common.operate.delete.batch',
                    })}
                  </a>
                )}
                <a onClick={onCleanSelected} key="cancelSelect">
                  {intl.formatMessage({
                    id: 'dms.common.operate.select.cancel',
                  })}
                </a>
              </Space>
            );
          }}
        ></ProTable>
      )}
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
                    workspaceCode: '',
                    workspaceName: '',
                    owner: '',
                  });
                  setQueryFormData({
                    workspaceCode: '',
                    workspaceName: '',
                    owner: '',
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
                  refreshData(queryForm.getFieldsValue());
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
        <Form layout="horizontal" wrapperCol={{ span: 16 }} labelCol={{ span: 6 }} form={queryForm}>
          <Form.Item
            name="workspaceCode"
            label={intl.formatMessage({
              id: 'dms.console.workspace.workspaceCode',
            })}
          >
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item
            name="workspaceName"
            label={intl.formatMessage({
              id: 'dms.console.workspace.workspaceName',
            })}
          >
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item name="owner" label={intl.formatMessage({ id: 'dms.console.workspace.owner' })}>
            <AutoComplete
              allowClear
              options={options}
              onSearch={(value: string) => {
                UserService.list({ userName: value }).then((resp) => {
                  let users: { value: string }[] = [];
                  setOptions([]);
                  resp.data.forEach((user) => {
                    users.push({ value: user.userName as string });
                  });
                  setOptions(users);
                });
              }}
              placeholder={intl.formatMessage({
                id: 'dms.console.workspace.owner.placeholder',
              })}
            ></AutoComplete>
          </Form.Item>
        </Form>
      </Drawer>
      {layout == 'card' && (
        <List
          grid={{
            gutter: 24,
            column: 3,
          }}
          dataSource={workspaces}
          pagination={{
            size: 'small',
            showQuickJumper: true,
            showSizeChanger: true,
            defaultPageSize: 9,
            pageSizeOptions: [9],
            hideOnSinglePage: true,
            total: total,
            onChange(page, pageSize) {
              setPagination({ current: page, pageSize: pageSize });
            },
          }}
          renderItem={(item) => (
            <List.Item style={{ padding: 0 }}>
              <Card
                hoverable
                actions={[
                  access.canAccess(PRIVILEGES.wsWssWpiShow) && (
                    <Button
                      type="link"
                      onClick={() => {
                        window.open(`/workspace/${item.id}?m=query`, '_blank');
                      }}
                    >
                      {intl.formatMessage({
                        id: 'dms.console.workspace.open',
                      })}
                    </Button>
                  ),
                  access.canAccess(PRIVILEGES.wsWssWpiEdit) && (
                    <Button
                      type="link"
                      onClick={() => {
                        setWorkspaceData({ data: item, open: true });
                      }}
                    >
                      {intl.formatMessage({ id: 'dms.common.operate.update' })}
                    </Button>
                  ),
                  access.canAccess(PRIVILEGES.wsWssWpiDelete) && (
                    <Button
                      type="link"
                      danger
                      onClick={() => {
                        Modal.confirm({
                          title: intl.formatMessage({
                            id: 'dms.common.operate.delete.confirm.title',
                          }),
                          content: intl.formatMessage({
                            id: 'dms.common.operate.delete.confirm.content',
                          }),
                          onOk: () => {
                            WorkspaceService.delete(item).then((resp) => {
                              if (resp.success) {
                                message.success(
                                  intl.formatMessage({
                                    id: 'dms.common.message.operate.delete.success',
                                  }),
                                );
                                refreshData();
                              }
                            });
                          },
                        });
                      }}
                    >
                      {intl.formatMessage({ id: 'dms.common.operate.delete' })}
                    </Button>
                  ),
                ]}
              >
                <Card.Meta
                  avatar={
                    <Avatar style={{ backgroundColor: '#bae0ff', color: '#003eb3' }}>
                      {item.workspaceCode.charAt(0)}
                    </Avatar>
                  }
                  title={item.workspaceCode}
                  description={
                    <Typography.Text ellipsis={{ tooltip: item.remark }} style={{ width: '90%' }}>
                      {item.remark}
                    </Typography.Text>
                  }
                />
              </Card>
            </List.Item>
          )}
        />
      )}
      {workspaceData.open && (
        <WorkspaceForm
          open={workspaceData.open}
          data={workspaceData.data}
          handleOk={(isOpen: boolean) => {
            setWorkspaceData({ open: isOpen });
            refreshData();
          }}
          handleCancel={() => {
            setWorkspaceData({ open: false });
          }}
        ></WorkspaceForm>
      )}
    </PageContainer>
  );
};

export default DmsConsoleView;
