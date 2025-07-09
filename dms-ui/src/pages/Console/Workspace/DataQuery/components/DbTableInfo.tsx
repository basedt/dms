import { MetaDataService } from '@/services/meta/metadata.service';
import { ActionType, EditableProTable, ProColumns } from '@ant-design/pro-components';
import { DndContext, DragEndEvent } from '@dnd-kit/core';
import { restrictToVerticalAxis } from '@dnd-kit/modifiers';
import { arrayMove, SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Editor } from '@monaco-editor/react';
import { useIntl } from '@umijs/max';
import type { AutoCompleteProps } from 'antd';
import {
  AutoComplete,
  Button,
  Checkbox,
  Form,
  Input,
  message,
  Popconfirm,
  Space,
  Spin,
  Tabs,
} from 'antd';
import { useEffect, useRef, useState } from 'react';
import { v4 as uuidv4 } from 'uuid';
import '../index.less';
import DdlConfirmModal from './DdlConfirmModal';
import { DndRow, DragHandle } from './DndRow';

export type DbTableInfoProps = {
  workspaceId: string | number;
  datasource: DMS.DataSource;
  maxHeight: number;
  node: DMS.CatalogTreeNode<string>;
  action: string; //"create"|"edit"|"view"
};

const EditFormCheckbox = (props: { value?: boolean; onChange?: (value: boolean) => void }) => {
  const { value, onChange } = props;
  const onCheckboxChange = (event: any) => {
    onChange?.(event.target.checked);
  };
  return <Checkbox checked={value} onChange={onCheckboxChange} />;
};

const DbTableInfoView: React.FC<DbTableInfoProps> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const columnActionRef = useRef<ActionType>();
  const indexActionRef = useRef<ActionType>();
  const partitionActionRef = useRef<ActionType>();
  const { workspaceId, datasource, maxHeight, node, action } = props;
  const [columnsTableKeys, setColumnsTableKeys] = useState<React.Key[]>([]);
  const [columnData, setColumnData] = useState<readonly DMS.Column[]>([]);
  const [indexTableKeys, setIndexTableKeys] = useState<React.Key[]>([]);
  const [indexData, setIndexData] = useState<readonly DMS.Index[]>([]);
  const [partitionTableKeys, setPartitionTableKeys] = useState<React.Key[]>([]);
  const [partitionData, setPartitionData] = useState<readonly DMS.Partition[]>([]);
  const [dataTypeOptions, setDataTypeOptions] = useState<AutoCompleteProps['options']>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [columnEnum, setColumnEnum] = useState<Map<string, string>>();
  const [ddlPriview, setDdlPriview] = useState<DMS.ModalProps<{ script: string }>>({
    open: false,
  });

  useEffect(() => {
    setLoading(true);
    MetaDataService.listTypeInfo(datasource.id as string).then((resp) => {
      if (resp.success) {
        let data: { label: string; value: string }[] = [];
        resp.data?.forEach((d) => {
          data.push({ label: d.typeName, value: d.typeName });
        });
        setDataTypeOptions(data);
      }
    });
    const nodeParams: string[] = node.identifier.split('.');
    if (node.type === 'TABLE' && nodeParams.length >= 3 && action !== 'create') {
      MetaDataService.getTable({
        dataSourceId: datasource.id as string,
        catalog: nodeParams[0],
        schemaName: nodeParams[1],
        tableName: nodeParams[2],
      })
        .then((resp) => {
          if (resp.success) {
            const table: DMS.Table = resp.data;
            form.setFieldsValue({
              schemaName: table.schemaName,
              tableName: table.tableName,
              tableComment: table.comment,
              columnsTable: table.columns,
              indexTable: table.indexes,
              partitionTable: table.partitions,
            });
            setColumnData(table.columns as readonly DMS.Column[]);
            setIndexData(table.indexes as readonly DMS.Index[]);
            setPartitionData(table.partitions as readonly DMS.Partition[]);
          }
          setLoading(false);
        })
        .catch((e) => {
          console.log(e);
          setLoading(false);
        });
    }
    if (action === 'create') {
      form.setFieldsValue({ schemaName: nodeParams[1], tableName: 'newTable' });
      setLoading(false);
    }
  }, []);

  const generalInfo = () => {
    return (
      <div style={{ maxWidth: '40%' }}>
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.table.general.schema',
          })}
          name="schemaName"
          rules={[{ required: true }]}
        >
          <Input disabled />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.table.general.tableName',
          })}
          name="tableName"
          rules={[{ required: true }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label={intl.formatMessage({
            id: 'dms.console.workspace.table.general.tableComment',
          })}
          name="tableComment"
        >
          <Input />
        </Form.Item>
      </div>
    );
  };

  const tableColumns: ProColumns<DMS.Column>[] = [
    {
      title: '#',
      dataIndex: 'id',
      fixed: 'left',
      width: 36,
      editable: false,
      align: 'center',
      render: () => <DragHandle />,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.columns.name' }),
      dataIndex: 'columnName',
      width: 120,
      fixed: 'left',
      formItemProps: () => {
        return {
          rules: [{ required: true }],
        };
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.columns.type' }),
      dataIndex: 'dataType',
      width: 120,
      renderFormItem: (schema, config, form, action) => {
        return (
          <AutoComplete
            options={dataTypeOptions}
            filterOption={(inputValue, option) => {
              const str: string = option?.value as string;
              return str.toUpperCase().indexOf(inputValue.toUpperCase()) !== -1;
            }}
            allowClear
          ></AutoComplete>
        );
      },
      formItemProps: () => {
        return {
          rules: [{ required: true }],
        };
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.columns.nullable' }),
      dataIndex: 'nullable',
      width: 60,
      align: 'center',
      render(_, row) {
        return <Checkbox checked={!row.nullable} disabled></Checkbox>;
      },
      renderFormItem: () => {
        return <EditFormCheckbox></EditFormCheckbox>;
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.columns.default' }),
      dataIndex: 'defaultValue',
      width: 120,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.columns.comment' }),
      dataIndex: 'comment',
      width: 120,
    },
    {
      title: intl.formatMessage({ id: 'dms.common.table.field.action' }),
      valueType: 'option',
      align: 'center',
      fixed: 'right',
      width: 80,
      render: (_, row) => (
        <Space>
          <a
            key="eidt"
            onClick={() => {
              columnActionRef.current?.startEditable(row.id, row);
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.update' })}
          </a>
          <Popconfirm
            key="delete"
            title={intl.formatMessage({
              id: 'dms.common.operate.delete.confirm.title',
            })}
            onConfirm={() => {
              const tabData: DMS.Column[] = form.getFieldValue('columnsTable');
              form.setFieldValue(
                'columnsTable',
                tabData.filter((item) => item.id !== row?.id),
              );
            }}
          >
            <a href="#" style={{ color: 'red' }}>
              {intl.formatMessage({ id: 'dms.common.operate.delete' })}
            </a>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const onDragEnd = ({ active, over }: DragEndEvent) => {
    if (active.id !== over?.id) {
      const tmpArr: DMS.Column[] = form.getFieldValue('columnsTable');
      const activeIndex = tmpArr.findIndex((record) => record.id === active?.id);
      const overIndex = tmpArr.findIndex((record) => record.id === over?.id);
      const endArray = arrayMove(tmpArr, activeIndex, overIndex);
      form.setFieldValue('columnsTable', endArray);
    }
  };

  const columnsTable = () => {
    return (
      <>
        <DndContext modifiers={[restrictToVerticalAxis]} onDragEnd={onDragEnd}>
          <SortableContext
            items={columnData.map((i) => i.id)}
            strategy={verticalListSortingStrategy}
          >
            <EditableProTable<DMS.Column>
              rowKey="id"
              name="columnsTable"
              scroll={{
                x: 1300,
                y: maxHeight - 240,
              }}
              columns={tableColumns}
              dataSource={columnData}
              actionRef={columnActionRef}
              components={{ body: { row: DndRow } }}
              onChange={(data) => {
                setColumnData(data);
              }}
              editable={{
                type: 'multiple',
                editableKeys: columnsTableKeys,
                onChange: (keys) => {
                  setColumnsTableKeys(keys);
                },
                actionRender: (row: any, config: any, defaultDom: any) => [
                  defaultDom.save,
                  defaultDom.cancel,
                ],
              }}
              recordCreatorProps={{
                position: 'bottom',
                creatorButtonText: intl.formatMessage({ id: 'dms.common.operate.new.row' }),
                record: (id, dataSource) => ({ id: uuidv4() } as DMS.Column),
              }}
            ></EditableProTable>
          </SortableContext>
        </DndContext>
      </>
    );
  };

  const indexColumns: ProColumns<DMS.Index>[] = [
    {
      title: '#',
      dataIndex: 'id',
      fixed: 'left',
      hidden: true,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.index.name' }),
      dataIndex: 'indexName',
      width: 120,
      fixed: 'left',
      formItemProps: () => {
        return {
          rules: [{ required: true }],
        };
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.index.type' }),
      dataIndex: 'indexType',
      width: 120,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.index.columns' }),
      dataIndex: 'columns',
      width: 120,
      valueType: 'select',
      valueEnum: columnEnum,
      fieldProps: () => {
        return {
          showSearch: true,
          mode: 'multiple',
        };
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.index.unique' }),
      dataIndex: 'uniqueness',
      width: 60,
      align: 'center',
      render(_, row) {
        return <Checkbox checked={row.uniqueness} disabled></Checkbox>;
      },
      renderFormItem: () => {
        return <EditFormCheckbox></EditFormCheckbox>;
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.index.pk' }),
      dataIndex: 'pk',
      width: 60,
      align: 'center',
      render(_, row) {
        return <Checkbox checked={row.pk} disabled></Checkbox>;
      },
      renderFormItem: () => {
        return <EditFormCheckbox></EditFormCheckbox>;
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.index.fk' }),
      dataIndex: 'fk',
      width: 60,
      align: 'center',
      editable: false,
      render(_, row) {
        return <Checkbox checked={row.fk} disabled></Checkbox>;
      },
      renderFormItem: () => {
        return <EditFormCheckbox></EditFormCheckbox>;
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.common.table.field.action' }),
      valueType: 'option',
      align: 'center',
      fixed: 'right',
      width: 80,
      render: (_, row) => (
        <Space>
          <a
            key="eidt"
            onClick={() => {
              indexActionRef.current?.startEditable(row.id, row);
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.update' })}
          </a>
          <Popconfirm
            key="delete"
            title={intl.formatMessage({
              id: 'dms.common.operate.delete.confirm.title',
            })}
            onConfirm={() => {
              const idxData: DMS.Index[] = form.getFieldValue('indexTable');
              form.setFieldValue(
                'indexTable',
                idxData.filter((item) => item.id !== row?.id),
              );
            }}
          >
            <a href="#" style={{ color: 'red' }}>
              {intl.formatMessage({ id: 'dms.common.operate.delete' })}
            </a>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const indexesTable = () => {
    return (
      <>
        <EditableProTable<DMS.Index>
          rowKey="id"
          name="indexTable"
          scroll={{
            x: 1300,
            y: maxHeight - 240,
          }}
          columns={indexColumns}
          actionRef={indexActionRef}
          value={indexData}
          onChange={(data) => {
            setIndexData(data);
          }}
          editable={{
            type: 'multiple',
            editableKeys: indexTableKeys,
            onChange: setIndexTableKeys,
            actionRender: (row: any, config: any, defaultDom: any) => [
              defaultDom.save,
              defaultDom.cancel,
            ],
          }}
          recordCreatorProps={{
            position: 'bottom',
            creatorButtonText: intl.formatMessage({ id: 'dms.common.operate.new.row' }),
            record: (id, dataSource) => ({ id: uuidv4() } as DMS.Index),
          }}
        ></EditableProTable>
      </>
    );
  };

  const getDdlScript = () => {
    return 'sql code here';
  };

  const ddlTab = () => {
    return (
      <Editor
        width={'100%'}
        height={maxHeight - 100}
        value={getDdlScript()}
        language="sql"
      ></Editor>
    );
  };

  const partitionColumns: ProColumns<DMS.Partition>[] = [
    {
      title: '#',
      dataIndex: 'id',
      fixed: 'left',
      hidden: true,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.partitions.name' }),
      dataIndex: 'paritionName',
      width: 120,
      fixed: 'left',
      formItemProps: () => {
        return {
          rules: [{ required: true }],
        };
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.partitions.expr' }),
      dataIndex: 'partitonExpr',
      width: 120,
      formItemProps: () => {
        return {
          rules: [{ required: true }],
        };
      },
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.partitions.rows' }),
      dataIndex: 'rows',
      editable: false,
      valueType: 'digit',
      tooltip: intl.formatMessage({ id: 'dms.console.workspace.table.partitions.rows.tooltip' }),
      width: 100,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.partitions.size' }),
      dataIndex: 'size',
      editable: false,
      valueType: 'digit',
      width: 100,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.partitions.createTime' }),
      dataIndex: 'createTime',
      editable: false,
      valueType: 'dateTime',
      width: 120,
    },
    {
      title: intl.formatMessage({ id: 'dms.console.workspace.table.partitions.updateTime' }),
      dataIndex: 'updateTime',
      editable: false,
      valueType: 'dateTime',
      width: 120,
    },
    {
      title: intl.formatMessage({ id: 'dms.common.table.field.action' }),
      valueType: 'option',
      align: 'center',
      fixed: 'right',
      width: 80,
    },

    {
      title: intl.formatMessage({ id: 'dms.common.table.field.action' }),
      valueType: 'option',
      align: 'center',
      fixed: 'right',
      width: 80,
      render: (_, row) => (
        <Space>
          <a
            key="eidt"
            onClick={() => {
              partitionActionRef.current?.startEditable(row.id, row);
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.update' })}
          </a>
          <Popconfirm
            key="delete"
            title={intl.formatMessage({
              id: 'dms.common.operate.delete.confirm.title',
            })}
            onConfirm={() => {
              const ptData: DMS.Partition[] = form.getFieldValue('partitionTable');
              form.setFieldValue(
                'partitionTable',
                ptData.filter((item) => item.id !== row?.id),
              );
            }}
          >
            <a href="#" style={{ color: 'red' }}>
              {intl.formatMessage({ id: 'dms.common.operate.delete' })}
            </a>
          </Popconfirm>
        </Space>
      ),
    },
  ];
  const partitionsTable = () => {
    return (
      <>
        <EditableProTable<DMS.Partition>
          rowKey="id"
          name="partitionTable"
          scroll={{
            x: 1300,
            y: maxHeight - 240,
          }}
          columns={partitionColumns}
          actionRef={partitionActionRef}
          value={partitionData}
          onChange={(data) => {
            setPartitionData(data);
          }}
          editable={{
            type: 'multiple',
            editableKeys: partitionTableKeys,
            onChange: setPartitionTableKeys,
            actionRender: (row: any, config: any, defaultDom: any) => [
              defaultDom.save,
              defaultDom.cancel,
            ],
          }}
          recordCreatorProps={{
            position: 'bottom',
            creatorButtonText: intl.formatMessage({ id: 'dms.common.operate.new.row' }),
            record: (index, dataSource) => ({ id: uuidv4() } as DMS.Partition),
          }}
        ></EditableProTable>
      </>
    );
  };

  return (
    <div>
      <div
        style={{
          paddingBottom: 6,
          borderBottom: '1px solid #eee',
        }}
      >
        <Space wrap>
          <Button
            size="small"
            type="primary"
            style={{ height: 22, fontSize: 12, marginLeft: 6 }}
            onClick={() => {
              setLoading(true);
              form
                .validateFields()
                .then((values) => {
                  let table: DMS.Table = {
                    schemaName: values.schemaName,
                    tableName: values.tableName,
                    comment: values.tableComment,
                    columns: values.columnsTable,
                    indexes: values.indexTable,
                  };
                  console.log('new table', values, node, table);
                  setDdlPriview({ open: true, data: { script: getDdlScript() } });
                })
                .catch((onrejected) => {
                  const errorInfo: string = onrejected.errorFields[0].errors[0];
                  message.warning(errorInfo);
                });
              setLoading(false);
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.confirm' })}
          </Button>
        </Space>
      </div>
      <div style={{ maxHeight: maxHeight - 36, overflowY: 'auto' }}>
        <div style={{ padding: '0px 6px', textAlign: 'left' }}>
          <Spin spinning={loading}>
            <Form layout="vertical" form={form}>
              <Tabs
                className="dbtabs"
                items={[
                  {
                    key: 'general',
                    label: intl.formatMessage({ id: 'dms.console.workspace.table.general' }),
                    children: generalInfo(),
                  },
                  {
                    key: 'columns',
                    label: intl.formatMessage({ id: 'dms.console.workspace.table.columns' }),
                    children: columnsTable(),
                  },
                  {
                    key: 'indexes',
                    label: intl.formatMessage({ id: 'dms.console.workspace.table.indexes' }),
                    children: indexesTable(),
                    disabled: datasource.datasourceType?.value === 'apachehive',
                  },
                  // {
                  //   key: 'partitions',
                  //   label: intl.formatMessage({ id: 'dms.console.workspace.table.partitions' }),
                  //   children: partitionsTable(),
                  //   disabled: action === 'create',
                  // },
                  {
                    key: 'ddl',
                    label: intl.formatMessage({ id: 'dms.console.workspace.table.ddl' }),
                    children: ddlTab(),
                  },
                ]}
                onChange={(activeKey: string) => {
                  if (activeKey === 'indexes') {
                    const colMap: Map<string, string> = new Map();
                    const data: DMS.Column[] = form.getFieldsValue().columnsTable;
                    data?.forEach((d) => {
                      if (
                        d.columnName != null &&
                        d.columnName != undefined &&
                        d.columnName.trim() != ''
                      ) {
                        colMap.set(d.columnName, d.columnName);
                      }
                    });
                    setColumnEnum(colMap);
                  } else if (activeKey === 'ddl') {
                    //todo generate DDL script
                  }
                }}
              ></Tabs>
            </Form>
          </Spin>
        </div>
      </div>
      {ddlPriview.open && (
        <DdlConfirmModal
          open={ddlPriview.open}
          data={ddlPriview.data}
          handleOk={(isOpen: boolean) => {
            //todo reload table info
            setDdlPriview({ open: isOpen });
          }}
          handleCancel={() => {
            setDdlPriview({ open: false });
          }}
        ></DdlConfirmModal>
      )}
    </div>
  );
};

export default DbTableInfoView;
