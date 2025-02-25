import { PrivilegeService } from "@/services/admin/privilege.service";
import {
  Alert,
  Button,
  Col,
  Drawer,
  Row,
  Space,
  Tag,
  Tree,
  Typography,
} from "antd";
import { Key, useEffect, useState } from "react";
import { useIntl } from "@umijs/max";

const PrivilegeGrant: React.FC<DMS.ModalProps<DMS.SysRole>> = (props) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [privileges, setPrivileges] = useState<DMS.TreeNode<string>[]>([]);
  const [checkedKeys, setCheckedKeys] = useState<Key[]>([]);
  useEffect(() => {
    PrivilegeService.listAllPrivileges().then((resp) => {
      if (resp.success) {
        setPrivileges(resp.data);
      }
    });
    PrivilegeService.listPrivilegeByRole(data?.id as string).then((resp) => {
      if (resp.success) {
        setCheckedKeys(resp.data);
      }
    });
  }, []);

  const handleSubmit = () => {
    PrivilegeService.grantPrivilegeToRole(data?.id as string, checkedKeys).then(
      (resp) => {
        if (resp.success) {
          handleOk ? handleOk(false) : null;
        }
      }
    );
  };

  const onCheck = (
    checked:
      | {
          checked: Key[];
          halfChecked: Key[];
        }
      | Key[]
  ) => {
    if ("checked" in checked) {
      const keys = checked.checked.concat(checked.halfChecked);
      setCheckedKeys(keys);
    } else {
      setCheckedKeys(checked);
    }
  };

  return (
    <Drawer
      open={open}
      width={"40%"}
      placement="right"
      title={
        intl.formatMessage({
          id: "dms.admin.role.privilege.grant",
        }) +
        " - " +
        data?.roleName
      }
      onClose={handleCancel}
      footer={
        <Row>
          <Col span={12}>
            <Button onClick={handleCancel}>
              {intl.formatMessage({ id: "dms.common.operate.cancel" })}
            </Button>
          </Col>
          <Col span={12} style={{ textAlign: "right" }}>
            <Button onClick={handleSubmit} type="primary">
              {intl.formatMessage({ id: "dms.common.operate.confirm" })}
            </Button>
          </Col>
        </Row>
      }
    >
      <Alert
        message={intl.formatMessage({
          id: "dms.admin.role.privilege.grant.help",
        })}
        type="info"
        showIcon
        style={{
          marginBottom: 8,
          marginLeft: 4,
          marginRight: 4,
          marginTop: 8,
        }}
      />
      <Tree
        checkable={true}
        checkStrictly={true}
        selectable={false}
        showLine={true}
        onCheck={onCheck}
        treeData={privileges}
        checkedKeys={checkedKeys}
        autoExpandParent={true}
        titleRender={(node: any) => {
          return (
            <>
              {node?.level && node?.level == 1 && (
                <Tag color="#108ee9">
                  {intl.formatMessage({
                    id: "dms.admin.role.privilege.module",
                  })}
                </Tag>
              )}
              {node?.level && node?.level == 2 && (
                <Tag color="#87d068">
                  {intl.formatMessage({
                    id: "dms.admin.role.privilege.page",
                  })}
                </Tag>
              )}
              {node?.level && node?.level == 3 && (
                <Tag color="gold">
                  {intl.formatMessage({
                    id: "dms.admin.role.privilege.action",
                  })}
                </Tag>
              )}
              <Typography.Text>{node.title}</Typography.Text>
            </>
          );
        }}
      />
    </Drawer>
  );
};
export default PrivilegeGrant;
