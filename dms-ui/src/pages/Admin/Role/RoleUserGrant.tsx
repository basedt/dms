import { RoleService } from '@/services/admin/role.service';
import { UserService } from '@/services/admin/user.service';
import { useIntl } from '@umijs/max';
import { message, Modal, Transfer } from 'antd';
import { useEffect, useState } from 'react';

const RoleUserGrant: React.FC<DMS.ModalProps<DMS.SysRole>> = (props) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [targetKeys, setTargetKeys] = useState<string[]>();
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [userList, setUserList] = useState<DMS.SysUser[]>();

  useEffect(() => {
    refreshUsers();
    refreshRoleUsers();
  }, []);

  const refreshUsers = () => {
    UserService.list({ pageSize: 10000, current: 1 }).then((resp) => {
      setUserList(resp.data);
    });
  };

  const refreshRoleUsers = () => {
    UserService.listUserWithRole(data?.id as string).then((resp) => {
      if (resp.success) {
        const keys = resp.data?.map((user) => user.id);
        setTargetKeys(keys as string[]);
      }
    });
  };

  const onChange = (nextTargetKeys: string[]) => {
    setTargetKeys(nextTargetKeys);
  };

  const onSelectChange = (sourceSelectedKeys: string[], targetSelectedKeys: string[]) => {
    setSelectedKeys([...sourceSelectedKeys, ...targetSelectedKeys]);
  };

  return (
    <Modal
      title={intl.formatMessage({ id: 'dms.admin.role.user.grant' })}
      open={open}
      onOk={() => {
        setLoading(true);
        RoleService.grantUserToRole(data?.id as string, targetKeys as string[]).then((resp) => {
          if (resp.success) {
            message.success(
              intl.formatMessage({
                id: 'dms.common.message.operate.success',
              }),
            );
            handleOk ? handleOk(false) : null;
          }
        });
        setLoading(false);
      }}
      destroyOnHidden={true}
      confirmLoading={loading}
      onCancel={handleCancel}
      styles={{ body: { paddingTop: 8 } }}
      width={640}
    >
      <Transfer
        dataSource={userList}
        titles={[
          intl.formatMessage({ id: 'dms.common.operate.grant.source' }),
          intl.formatMessage({ id: 'dms.common.operate.grant.target' }),
        ]}
        listStyle={{ width: '100%', minHeight: 420 }}
        showSearch={true}
        showSelectAll={true}
        targetKeys={targetKeys}
        selectedKeys={selectedKeys}
        onChange={onChange}
        onSelectChange={onSelectChange}
        render={(item) => item.userName as string}
        rowKey={(item) => item.id as string}
        pagination={true}
      ></Transfer>
    </Modal>
  );
};

export default RoleUserGrant;
