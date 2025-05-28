import { DICT_TYPE, PRIVILEGES } from '@/constants';
import { DictDataService } from '@/services/admin/dict.data.service';
import { DataSourceService } from '@/services/workspace/datasource.service';
import { FilterOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { useAccess, useIntl, useSearchParams } from '@umijs/max';
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
  Typography,
} from 'antd';
import { useEffect, useRef, useState } from 'react';
import DataSourceTypeSelect from './components/DataSourceTypeSelect';
import GenericDataSourceForm from './components/GenericDataSourceForm';
import HiveDataSourceForm from './components/HiveDataSourceForm';

const DataSourceView: React.FC<{ workspaceId: string | number }> = ({ workspaceId }) => {
  const intl = useIntl();
  const access = useAccess();
  const [queryForm] = Form.useForm();
  const actionRef = useRef<ActionType>();
  const [searchParams] = useSearchParams();
  const [queryFormOpen, setQueryFormOpen] = useState<boolean>(false);
  const [queryFormData, setQueryFormData] = useState<DMS.DataSourceParam>({
    workspaceId: workspaceId,
  });
  const [datasourceTypeList, setDatasourceTypeList] = useState<DMS.Dict[]>([]);
  const [dataSourceData, setDataSourceData] = useState<DMS.ModalProps<DMS.DataSource>>({
    open: false,
  });
  const [dataSourceType, setDataSourceType] = useState<DMS.ModalProps<DMS.DataSource>>({
    open: false,
  });

  useEffect(() => {
    DictDataService.listByType(DICT_TYPE.datasourceType).then((resp) => {
      if (resp.success) {
        setDatasourceTypeList(resp.data as DMS.Dict[]);
      }
    });
    if (searchParams.get('n')) {
      setDataSourceType({
        open: true,
        data: { workspaceId: workspaceId },
      });
    }
  }, []);

  const connPropsStyle = useEmotionCss(() => {
    return {
      color: '#096dd9',
    };
  });

  const columns: ProColumns<DMS.DataSource>[] = [
    {
      title: intl.formatMessage({
        id: 'dms.console.workspace.datasource.datasourceName',
      }),
      dataIndex: 'datasourceName',
      width: 140,
      fixed: 'left',
    },
    {
      title: intl.formatMessage({
        id: 'dms.console.workspace.datasource.datasourceType',
      }),
      dataIndex: 'datasourceType',
      width: 120,
      align: 'center',
      render: (dom, entity) => {
        return entity.datasourceType?.label;
      },
    },
    {
      title: intl.formatMessage({
        id: 'dms.console.workspace.datasource.info',
      }),
      width: 240,
      render: (dom, entity) => {
        return (
          <>
            <Typography.Text strong className={connPropsStyle}>
              {intl.formatMessage({
                id: 'dms.console.workspace.datasource.hostName',
              })}
            </Typography.Text>
            {' : ' + entity.hostName}
            <br />
            <Typography.Text strong className={connPropsStyle}>
              {intl.formatMessage({
                id: 'dms.console.workspace.datasource.databaseName',
              })}
            </Typography.Text>
            {' : ' + entity.databaseName}
            <br />
            <Typography.Text strong className={connPropsStyle}>
              {intl.formatMessage({
                id: 'dms.console.workspace.datasource.port',
              })}
            </Typography.Text>
            {' : ' + entity.port}
            <br />
            <Typography.Text strong className={connPropsStyle}>
              {intl.formatMessage({
                id: 'dms.console.workspace.datasource.userName',
              })}
            </Typography.Text>
            {' : ' + entity.userName}
          </>
        );
      },
    },
    {
      title: intl.formatMessage({
        id: 'dms.console.workspace.datasource.remark',
      }),
      dataIndex: 'remark',
      width: 240,
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
          {access.canAccess(PRIVILEGES.wsWsdWdlEdit) && (
            <a
              key="update"
              onClick={() => {
                setDataSourceData({ data: record, open: true });
              }}
            >
              {intl.formatMessage({ id: 'dms.common.operate.update' })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.wsWsdWdlDelete) && (
            <Popconfirm
              key="delete"
              title={intl.formatMessage({
                id: 'dms.common.operate.delete.confirm.title',
              })}
              onConfirm={() => {
                DataSourceService.delete(record).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({
                        id: 'dms.common.message.operate.delete.success',
                      }),
                    );
                    actionRef.current?.reload();
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

  const onSearchInputChange = (item: any) => {
    queryForm.setFieldsValue({
      datasourceName: item.currentTarget.value,
    });
    setQueryFormData({
      datasourceName: item.currentTarget.value,
      workspaceId: workspaceId,
    });
    actionRef.current?.reload();
  };

  return (
    <PageContainer
      header={{
        title: intl.formatMessage({ id: 'dms.console.workspace.datasource' }),
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
              queryFormData.databaseName ||
              queryFormData.datasourceName ||
              queryFormData.datasourceType ||
              queryFormData.hostName
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
        access.canAccess(PRIVILEGES.wsWsdWdlAdd) && (
          <Button
            key="new"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setDataSourceType({
                open: true,
                data: { workspaceId: workspaceId },
              });
            }}
          >
            {intl.formatMessage({ id: 'dms.console.workspace.datasource.new' }, { type: '' })}
          </Button>
        ),
      ]}
    >
      <ProTable<DMS.DataSource>
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
          return DataSourceService.list({
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
              {access.canAccess(PRIVILEGES.wsWsdWdlDelete) && (
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
                        DataSourceService.deleteBatch(selectedRowKeys).then((resp) => {
                          if (resp.success) {
                            message.success(
                              intl.formatMessage({
                                id: 'dms.common.message.operate.delete.success',
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
                    id: 'dms.common.operate.delete.batch',
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
                    datasourceName: '',
                    datasourceType: '',
                    hostName: '',
                    databaseName: '',
                  });
                  setQueryFormData({
                    datasourceName: '',
                    datasourceType: '',
                    hostName: '',
                    databaseName: '',
                    workspaceId: workspaceId,
                  });
                }}
              >
                {intl.formatMessage({ id: 'dms.common.operate.reset' })}
              </Button>
            </Col>
            <Col span={12} style={{ textAlign: 'right' }}>
              <Button
                onClick={() => {
                  queryForm.validateFields().then((values: any) => {
                    setQueryFormData({
                      ...queryForm.getFieldsValue(),
                      workspaceId: workspaceId,
                    });
                    actionRef.current?.reload();
                  });

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
          <Form.Item
            name="datasourceName"
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.datasourceName',
            })}
          >
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item
            name="datasourceType"
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.datasourceType',
            })}
          >
            <Select
              showSearch={true}
              allowClear={true}
              optionFilterProp="label"
              filterOption={(input: any, option: any) =>
                (option!.children as unknown as string).toLowerCase().includes(input.toLowerCase())
              }
            >
              {datasourceTypeList.map((item) => {
                return (
                  <Select.Option key={item.value} value={item.value}>
                    {item.label}
                  </Select.Option>
                );
              })}
            </Select>
          </Form.Item>
          <Form.Item
            name="hostName"
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.hostName',
            })}
          >
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item
            name="databaseName"
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.databaseName',
            })}
          >
            <Input allowClear></Input>
          </Form.Item>
        </Form>
      </Drawer>
      {dataSourceType.open && (
        <DataSourceTypeSelect
          open={dataSourceType.open}
          data={dataSourceType.data}
          handleOk={(isOpen: boolean, ds: DMS.DataSource) => {
            setDataSourceType({ open: isOpen });
            setDataSourceData({ open: true, data: ds });
            actionRef.current?.reload();
          }}
          handleCancel={() => {
            setDataSourceType({ open: false });
          }}
        ></DataSourceTypeSelect>
      )}

      {dataSourceData.open &&
        (dataSourceData.data?.datasourceType?.value == 'oracle' ||
          dataSourceData.data?.datasourceType?.value == 'mysql' ||
          dataSourceData.data?.datasourceType?.value == 'mssql' ||
          dataSourceData.data?.datasourceType?.value == 'postgreSQL' ||
          dataSourceData.data?.datasourceType?.value == 'doris' ||
          dataSourceData.data?.datasourceType?.value == 'hologres' ||
          dataSourceData.data?.datasourceType?.value == 'gaussdb' ||
          dataSourceData.data?.datasourceType?.value == 'clickhouse' ||
          dataSourceData.data?.datasourceType?.value == 'mariadb' ||
          dataSourceData.data?.datasourceType?.value == 'polardb_postgre' ||
          dataSourceData.data?.datasourceType?.value == 'polardb_mysql' ||
          dataSourceData.data?.datasourceType?.value == 'greenplum' ||
          dataSourceData.data?.datasourceType?.value == 'apachehive') && (
          <GenericDataSourceForm
            open={dataSourceData.open}
            data={dataSourceData.data}
            handleOk={(isOpen: boolean) => {
              setDataSourceData({ open: isOpen });
              actionRef.current?.reload();
            }}
            handleCancel={() => {
              if (!dataSourceData.data?.id) {
                setDataSourceType({
                  open: true,
                  data: { workspaceId: workspaceId },
                });
              }
              setDataSourceData({ open: false });
            }}
          ></GenericDataSourceForm>
        )}
      {dataSourceData.open && dataSourceData.data?.datasourceType?.value == 'apachehive' && (
        <HiveDataSourceForm
          open={dataSourceData.open}
          data={dataSourceData.data}
          handleOk={(isOpen: boolean) => {
            setDataSourceData({ open: isOpen });
            actionRef.current?.reload();
          }}
          handleCancel={() => {
            if (!dataSourceData.data?.id) {
              setDataSourceType({
                open: true,
                data: { workspaceId: workspaceId },
              });
            }
            setDataSourceData({ open: false });
          }}
        ></HiveDataSourceForm>
      )}
    </PageContainer>
  );
};
export default DataSourceView;
