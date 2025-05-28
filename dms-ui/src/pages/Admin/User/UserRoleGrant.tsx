import { RoleService } from '@/services/admin/role.service';
import { UserService } from '@/services/admin/user.service';
import { useIntl } from '@umijs/max';
import { message, Modal, Transfer } from 'antd';
import { useEffect, useState } from 'react';

const UserRoleGrant: React.FC<DMS.ModalProps<DMS.SysUser>> = (props) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [targetKeys, setTargetKeys] = useState<string[]>();
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [roleList, setRoleList] = useState<DMS.SysRole[]>();

  useEffect(() => {
    refreshRoleList();
    refreshUserRoles();
  }, []);

  const refreshUserRoles = () => {
    RoleService.listByUser(data?.userName as string).then((resp) => {
      if (resp.success) {
        const keys = resp.data?.map((role) => role.id);
        setTargetKeys(keys as string[]);
      }
    });
  };

  const refreshRoleList = () => {
    RoleService.listAll().then((resp) => {
      if (resp.success) {
        setRoleList(resp.data);
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
      title={intl.formatMessage({ id: 'dms.admin.user.role.grant' })}
      open={open}
      onOk={() => {
        setLoading(true);
        UserService.grantRoleToUser(data?.id as number, targetKeys as string[]).then((resp) => {
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
        dataSource={roleList}
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
        render={(item) => item.roleName as string}
        rowKey={(item) => item.id as string}
        pagination={true}
      ></Transfer>
    </Modal>
  );
};

export default UserRoleGrant;
