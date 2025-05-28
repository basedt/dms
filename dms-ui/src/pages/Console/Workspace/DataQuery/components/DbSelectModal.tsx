import { DataSourceService } from '@/services/workspace/datasource.service';
import { PlusOutlined } from '@ant-design/icons';
import { Link, useIntl } from '@umijs/max';
import { Button, Divider, Modal, Select, Space } from 'antd';
import { useEffect, useState } from 'react';

interface DbSelectModalProps {
  open: boolean;
  data: any;
  handleOk: (isOpen: boolean, value: any) => void;
  handleCancel: () => void;
}

const DbSelectModal = (props: DbSelectModalProps) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [dbList, setDbList] = useState<DMS.Dict[]>([]);
  const [selectDb, setSelectDb] = useState<string | number>();
  useEffect(() => {
    if (data?.defaultValue) {
      setSelectDb(data?.defaultValue);
    }
    DataSourceService.listByWorkspace(data?.workspaceId as string).then((resp) => {
      if (resp.success) {
        setDbList(resp.data as DMS.Dict[]);
      }
    });
  }, []);

  return (
    <Modal
      title={intl.formatMessage({
        id: 'dms.console.workspace.dataquery.select',
      })}
      open={open}
      onOk={() => {
        handleOk ? handleOk(false, selectDb) : null;
      }}
      destroyOnHidden={true}
      maskClosable={false}
      onCancel={handleCancel}
      styles={{ body: { paddingTop: 8 } }}
      width={540}
    >
      <Select
        style={{ width: '100%' }}
        allowClear={false}
        showSearch={true}
        placeholder={intl.formatMessage({
          id: 'dms.console.workspace.dataquery.select',
        })}
        defaultValue={data?.defaultValue}
        onChange={(value: any) => {
          setSelectDb(value);
        }}
        dropdownRender={(menu: any) => {
          return (
            <>
              {menu}
              <Divider style={{ margin: '8px 0' }} />
              <Link
                to={`/workspace/${data?.workspaceId as string}?m=datasource&n=true`}
                reloadDocument={true}
                onClick={() => sessionStorage.setItem('selectOpen', 'true')}
              >
                <Button type="text" block icon={<PlusOutlined />} style={{ marginBottom: 2 }}>
                  {intl.formatMessage({ id: 'dms.console.workspace.datasource.new' }, { type: '' })}
                </Button>
              </Link>
            </>
          );
        }}
      >
        {dbList &&
          dbList.map((item) => {
            const label: string = item.label || 'img-db';
            const dbinfo: string[] = label.split('-');
            return (
              <Select.Option key={item.value} value={item.value}>
                <Space style={{ display: 'flex', alignItems: 'center' }}>
                  <img
                    src={'/images/databases/' + dbinfo[0].toLowerCase() + '.svg'}
                    style={{ width: 16, height: 16 }}
                    alt=""
                  />
                  {dbinfo[1]}
                </Space>
              </Select.Option>
            );
          })}
      </Select>
    </Modal>
  );
};

export default DbSelectModal;
