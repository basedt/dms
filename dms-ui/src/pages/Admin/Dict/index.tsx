import { PRIVILEGES } from '@/constants';
import { DictTypeService } from '@/services/admin/dict.type.service';
import { FilterOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { history, useAccess, useIntl } from '@umijs/max';
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
} from 'antd';
import React, { useRef, useState } from 'react';
import DictTypeForm from './DictTypeForm';

const DmsDictView: React.FC = () => {
  const intl = useIntl();
  const access = useAccess();
  const [queryForm] = Form.useForm();
  const dictTypeActionRef = useRef<ActionType>();
  const [dictTypeData, setDictTypeData] = useState<DMS.ModalProps<DMS.SysDictType>>({
    open: false,
  });
  const [queryFormData, setQueryFormData] = useState<DMS.SysDictTypeParam>({});
  const [queryFormOpen, setQueryFormOpen] = useState(false);
  const columns: ProColumns<DMS.SysDictType>[] = [
    {
      title: intl.formatMessage({ id: 'dms.admin.dict.type.dictTypeCode' }),
      dataIndex: 'dictTypeCode',
      width: 180,
      fixed: 'left',
    },
    {
      title: intl.formatMessage({ id: 'dms.admin.dict.type.dictTypeName' }),
      dataIndex: 'dictTypeName',
      width: 180,
    },
    {
      title: intl.formatMessage({ id: 'dms.admin.dict.type.remark' }),
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
      width: 100,
      render: (_, record) => (
        <Space>
          {access.canAccess(PRIVILEGES.sysDicDctEdit) && (
            <a
              key="update"
              onClick={() => {
                setDictTypeData({ data: record, open: true });
              }}
            >
              {intl.formatMessage({ id: 'dms.common.operate.update' })}
            </a>
          )}
          {access.canAccess(PRIVILEGES.sysDicDctDelete) && (
            <Popconfirm
              key="delete"
              title={intl.formatMessage({
                id: 'dms.common.operate.delete.confirm.title',
              })}
              onConfirm={() => {
                DictTypeService.delete(record).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({
                        id: 'dms.common.message.operate.delete.success',
                      }),
                    );
                    dictTypeActionRef.current?.reload();
                  }
                });
              }}
            >
              <a href="#" style={{ color: 'red' }}>
                {intl.formatMessage({ id: 'dms.common.operate.delete' })}
              </a>
            </Popconfirm>
          )}
          {access.canAccess(PRIVILEGES.sysDicDctDetail) && (
            <a
              key="detail"
              onClick={() => {
                history.push('/admin/dict/data', record);
              }}
            >
              {intl.formatMessage({ id: 'dms.common.operate.detail' })}
            </a>
          )}
        </Space>
      ),
    },
  ];

  const onSearchInputChange = (item: any) => {
    queryForm.setFieldsValue({
      dictTypeCode: item.currentTarget.value,
    });
    setQueryFormData({ dictTypeCode: item.currentTarget.value });
    dictTypeActionRef.current?.reload();
  };

  return (
    <PageContainer
      header={{
        title: intl.formatMessage({ id: 'dms.admin.dict.type' }),
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
          <Badge dot={queryFormData.dictTypeCode || queryFormData.dictTypeName ? true : false}>
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
              dictTypeActionRef.current?.reload();
            }}
          ></Button>
        </Tooltip>,
        access.canAccess(PRIVILEGES.sysDicDctAdd) && (
          <Button
            key="new"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setDictTypeData({ open: true });
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.new' })}
          </Button>
        ),
      ]}
    >
      <ProTable<DMS.SysDictType>
        search={false}
        columns={columns}
        scroll={{ x: 1300 }}
        rowKey="id"
        actionRef={dictTypeActionRef}
        options={{ density: false, setting: false, reload: false }}
        pagination={{
          showQuickJumper: true,
          showSizeChanger: true,
          defaultPageSize: 10,
          hideOnSinglePage: true,
        }}
        request={(params, sorter, filter) => {
          return DictTypeService.list({
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
              {access.canAccess(PRIVILEGES.sysDicDctDelete) && (
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
                        DictTypeService.deleteBatch(selectedRowKeys).then((resp) => {
                          if (resp.success) {
                            message.success(
                              intl.formatMessage({
                                id: 'dms.common.message.operate.delete.success',
                              }),
                            );
                            onCleanSelected();
                            dictTypeActionRef.current?.reload();
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
          dictTypeActionRef.current?.reload();
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
                    dictTypeCode: '',
                    dictTypeName: '',
                  });
                  setQueryFormData({ dictTypeCode: '', dictTypeName: '' });
                }}
              >
                {intl.formatMessage({ id: 'dms.common.operate.reset' })}
              </Button>
            </Col>
            <Col span={12} style={{ textAlign: 'right' }}>
              <Button
                onClick={() => {
                  setQueryFormData(queryForm.getFieldsValue());
                  dictTypeActionRef.current?.reload();
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
            name="dictTypeCode"
            label={intl.formatMessage({
              id: 'dms.admin.dict.type.dictTypeCode',
            })}
          >
            <Input allowClear></Input>
          </Form.Item>
          <Form.Item
            name="dictTypeName"
            label={intl.formatMessage({
              id: 'dms.admin.dict.type.dictTypeName',
            })}
          >
            <Input allowClear></Input>
          </Form.Item>
        </Form>
      </Drawer>
      {dictTypeData.open && (
        <DictTypeForm
          open={dictTypeData.open}
          data={dictTypeData.data}
          handleOk={(isOpen: boolean) => {
            setDictTypeData({ open: isOpen });
            dictTypeActionRef.current?.reload();
          }}
          handleCancel={() => {
            setDictTypeData({ open: false });
          }}
        ></DictTypeForm>
      )}
    </PageContainer>
  );
};

export default DmsDictView;
