import { AuthService } from "@/services/admin/auth.service";
import { Button, List, Typography } from "antd";
import { useEffect, useState } from "react";
import { useIntl } from "@umijs/max";
import MailChangeForm from "./MailChangeForm";
import PasswordChangeForm from "./PasswordChangeForm";

const UserSecurity: React.FC = () => {
  const intl = useIntl();
  const [userInfo, setUserInfo] = useState<DMS.SysUser>();
  const [pwdChangeData, setPwdChangeData] = useState<
    DMS.ModalProps<DMS.SysUser>
  >({
    open: false,
  });
  const [mailChangeData, setMailChangeData] = useState<
    DMS.ModalProps<DMS.SysUser>
  >({
    open: false,
  });

  const refreshUserInfo = () => {
    AuthService.getCurrentUser(true).then((resp) => {
      setUserInfo(resp.data);
    });
  };

  useEffect(() => {
    refreshUserInfo();
  }, []);

  return (
    <>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        {intl.formatMessage({ id: "dms.user.center.security" })}
      </Typography.Title>
      <List itemLayout="horizontal">
        <List.Item
          style={{ padding: "0px 0px 12px 0px" }}
          extra={
            <Button
              type="link"
              onClick={() => {
                setPwdChangeData({ open: true });
              }}
            >
              {intl.formatMessage({
                id: "dms.user.center.security.password.edit",
              })}
            </Button>
          }
        >
          <List.Item.Meta
            title={intl.formatMessage({
              id: "dms.user.center.security.password",
            })}
            description={intl.formatMessage({
              id: "dms.user.center.security.password.desc",
            })}
          ></List.Item.Meta>
        </List.Item>
        <List.Item
          style={{ padding: "0px 0px 12px 0px" }}
          extra={
            <Button
              type="link"
              onClick={() => {
                setMailChangeData({ open: true });
              }}
            >
              {intl.formatMessage({
                id: "dms.user.center.security.email.edit",
              })}
            </Button>
          }
        >
          <List.Item.Meta
            title={intl.formatMessage({
              id: "dms.user.center.security.email",
            })}
            description={
              intl.formatMessage({
                id: "dms.user.center.security.email.desc",
              }) + (userInfo?.email ? userInfo?.email : "")
            }
          ></List.Item.Meta>
        </List.Item>
      </List>
      {pwdChangeData.open && (
        <PasswordChangeForm
          open={pwdChangeData.open}
          data={pwdChangeData.data}
          handleOk={(isOpen: boolean) => {
            setPwdChangeData({ open: isOpen });
          }}
          handleCancel={() => {
            setPwdChangeData({ open: false });
          }}
        ></PasswordChangeForm>
      )}
      {mailChangeData.open && (
        <MailChangeForm
          open={mailChangeData.open}
          data={mailChangeData.data}
          handleOk={(isOpen: boolean) => {
            setMailChangeData({ open: isOpen });
            refreshUserInfo();
          }}
          handleCancel={() => {
            setMailChangeData({ open: false });
          }}
        ></MailChangeForm>
      )}
    </>
  );
};

export default UserSecurity;
