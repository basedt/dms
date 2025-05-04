import { PATTERNS } from '@/constants';
import { DataSourceService } from '@/services/workspace/datasource.service';
import { useIntl, useModel } from '@umijs/max';
import { Button, Col, Form, Input, InputNumber, message, Modal, Row, Space, Tabs } from 'antd';
import React, { useState } from 'react';

const HiveDataSourceForm: React.FC<DMS.ModalProps<DMS.DataSource>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [isPasswordChange, setPasswordChange] = useState<boolean>(false);
  const { tableKey } = useModel('global');

  const items: TabsProps['items'] = [
    {
      key: 'general',
      label: intl.formatMessage({
        id: 'dms.console.workspace.datasource.tab.general',
      }),
      children: (
        <>
          <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.datasourceName',
            })}
            name="datasourceName"
            rules={[{ required: true }, { max: 64 }, { pattern: PATTERNS.characterWord }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.hostName',
            })}
            name="hostName"
            rules={[{ max: 256 }, { required: true }]}
          >
            <Input></Input>
          </Form.Item>
          <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.databaseName',
            })}
            name="databaseName"
            rules={[{ max: 64 }, { required: true }]}
          >
            <Input></Input>
          </Form.Item>
          <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.port',
            })}
            name="port"
            rules={[{ required: true }]}
          >
            <InputNumber style={{ width: '100%' }} min={0}></InputNumber>
          </Form.Item>
          <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.userName',
            })}
            name="userName"
            rules={[{ max: 256 }, { required: true }]}
          >
            <Input></Input>
          </Form.Item>
          <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.password',
            })}
            name="password"
            rules={[{ max: 256 }, { required: true }]}
          >
            <Input.Password
              visibilityToggle={data?.id ? false : true}
              onChange={() => {
                setPasswordChange(true);
              }}
            ></Input.Password>
          </Form.Item>
          <Form.Item
            name="hmsUris"
            label="hive.metastore.uris"
            rules={[{ required: true }]}
            tooltip={intl.formatMessage({
              id: 'dms.console.workspace.datasource.attrs.hmsUris.tooltip',
            })}
          >
            <Input placeholder="thrift://ip1:port1,thrift://ip2:port2"></Input>
          </Form.Item>
          <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.remark',
            })}
            name="remark"
            rules={[{ max: 512 }]}
          >
            <Input.TextArea></Input.TextArea>
          </Form.Item>
        </>
      ),
    },
    {
      key: 'advanced',
      label: intl.formatMessage({
        id: 'dms.console.workspace.datasource.tab.advanced',
      }),
      children: (
        <>
          {/* <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.meta',
            })}
            name="meta"
            tooltip={intl.formatMessage({
              id: 'dms.console.workspace.datasource.meta.tooltip',
            })}
          >
            <Switch
              checkedChildren={intl.formatMessage({
                id: 'dms.console.workspace.datasource.meta.enable',
              })}
              unCheckedChildren={intl.formatMessage({
                id: 'dms.console.workspace.datasource.meta.disable',
              })}
              defaultChecked={false}
              checked={data?.attrs?.meta}
            />
          </Form.Item> */}

          <Form.Item
            label={intl.formatMessage({
              id: 'dms.console.workspace.datasource.attrs',
            })}
            name="jdbc"
          >
            <Input.TextArea
              placeholder={intl.formatMessage({
                id: 'dms.console.workspace.datasource.attrs.placeholder',
              })}
              rows={4}
            ></Input.TextArea>
          </Form.Item>
        </>
      ),
    },
  ];

  return (
    <Modal
      title={
        data?.id
          ? intl.formatMessage(
              { id: 'dms.console.workspace.datasource.update' },
              { type: data?.datasourceType?.label },
            )
          : intl.formatMessage(
              { id: 'dms.console.workspace.datasource.new' },
              { type: data?.datasourceType?.label },
            )
      }
      open={open}
      onCancel={() => {
        handleOk ? handleOk(false) : null;
      }}
      confirmLoading={loading}
      destroyOnClose={true}
      styles={{
        body: {
          overflowY: 'scroll',
          maxHeight: '640px',
          height: 480,
        },
      }}
      width="780px"
      footer={
        <Row gutter={[12, 12]}>
          <Col span={8} style={{ textAlign: 'left' }}>
            <Button
              type="primary"
              danger
              onClick={() => {
                let d: DMS.DataSource = {
                  id: data?.id,
                  workspaceId: data?.workspaceId as string,
                  datasourceName: form.getFieldValue('datasourceName'),
                  datasourceType: data?.datasourceType,
                  hostName: form.getFieldValue('hostName'),
                  databaseName: form.getFieldValue('databaseName'),
                  port: form.getFieldValue('port'),
                  userName: form.getFieldValue('userName'),
                  password: form.getFieldValue('password'),
                  isPasswordChange: isPasswordChange,
                  attrs: {
                    hmsUris: form.getFieldValue('hmsUris') ? form.getFieldValue('hmsUris') : null,
                    jdbc: form.getFieldValue('jdbc') ? form.getFieldValue('jdbc') : null,
                  },
                };
                DataSourceService.testConnection(d).then((resp) => {
                  if (resp.success) {
                    message.success(
                      intl.formatMessage({
                        id: 'dms.console.workspace.datasource.test.success',
                      }),
                    );
                  }
                });
              }}
            >
              {intl.formatMessage({
                id: 'dms.console.workspace.datasource.test',
              })}
            </Button>
          </Col>
          <Col span={16}>
            <Space>
              <Button onClick={handleCancel}>
                {data?.id
                  ? intl.formatMessage({ id: 'dms.common.operate.cancel' })
                  : intl.formatMessage({
                      id: 'dms.common.operate.step.previous',
                    })}
              </Button>
              <Button
                type="primary"
                onClick={() => {
                  setLoading(true);
                  form.validateFields().then((values: any) => {
                    let d: DMS.DataSource = {
                      workspaceId: data?.workspaceId as string,
                      datasourceName: values.datasourceName,
                      datasourceType: data?.datasourceType,
                      hostName: values.hostName,
                      databaseName: values.databaseName,
                      port: values.port,
                      userName: values.userName,
                      password: values.password,
                      remark: values.remark,
                      attrs: {
                        hmsUris: values.hmsUris ? values.hmsUris : null,
                        jdbc: values.jdbc ? values.jdbc : null,
                      },
                    };
                    data?.id
                      ? DataSourceService.update({
                          ...d,
                          id: data.id,
                          isPasswordChange: isPasswordChange,
                        }).then((resp) => {
                          if (resp.success) {
                            message.success(
                              intl.formatMessage({
                                id: 'dms.common.message.operate.update.success',
                              }),
                            );
                            tableKey.current++;
                            handleOk ? handleOk(false) : null;
                          }
                        })
                      : DataSourceService.add(d).then((resp) => {
                          if (resp.success) {
                            message.success(
                              intl.formatMessage({
                                id: 'dms.common.message.operate.new.success',
                              }),
                            );
                            tableKey.current++;
                            handleOk ? handleOk(false) : null;
                          }
                        });
                  });
                  setLoading(false);
                }}
              >
                {intl.formatMessage({
                  id: 'dms.common.operate.confirm',
                })}
              </Button>
            </Space>
          </Col>
        </Row>
      }
    >
      <Form
        layout="horizontal"
        form={form}
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 16 }}
        initialValues={{
          ...data,
          jdbc: data?.attrs?.jdbc,
          hmsUris: data?.attrs?.hmsUris,
          password: data?.id ? '***' : '',
        }}
      >
        <Tabs defaultActiveKey="general" items={items} />
      </Form>
    </Modal>
  );
};

export default HiveDataSourceForm;
