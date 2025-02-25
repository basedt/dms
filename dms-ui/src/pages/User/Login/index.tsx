import AppsLogo from "@/components/AppLogoComponent";
import SelectLang from "@/components/SelectLang";
import { PATTERNS } from "@/constants";
import { AuthService } from "@/services/admin/auth.service";
import { useEmotionCss } from "@ant-design/use-emotion-css";
import { Button, Checkbox, Col, Form, Input, message, Row } from "antd";
import React, { useEffect, useState } from "react";
import { flushSync } from "react-dom";
import { Helmet, useIntl, useModel, useNavigate } from "@umijs/max";
import styles from "../index.less";

const Lang = () => {
  const langClassName = useEmotionCss(({ token }) => {
    return {
      width: 42,
      height: 42,
      lineHeight: "42px",
      position: "fixed",
      right: 16,
      textAlign: "center",
      borderRadius: token.borderRadius,
      ":hover": {
        backgroundColor: token.colorBgTextHover,
      },
    };
  });

  return <SelectLang className={langClassName} />;
};

const Login: React.FC = () => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const [authCode, setAuthCode] = useState<DMS.AuthCode>();
  const { initialState, setInitialState } = useModel("@@initialState");

  useEffect(() => {
    refreshAuthCode();
  }, []);

  const refreshAuthCode = async () => {
    const data = await AuthService.refreshAuthImage();
    setAuthCode(data.data);
  };

  const handleSubmit = async () => {
    try {
      form.validateFields().then((values: DMS.LoginInfo) => {
        const params: DMS.LoginInfo = {
          ...values,
          uuid: authCode?.uuid as string,
        };
        AuthService.login(params).then(async (resp) => {
          if (resp.success) {
            message.success(
              intl.formatMessage({ id: "dms.user.login.success" })
            );
            await flushSync(() => {
              setInitialState((s) => ({
                ...s,
                currentUser: resp.data,
              }));
            });
            setTimeout(() => {
              navigate("/");
            }, 500);
          } else {
            refreshAuthCode();
          }
        });
      });
    } catch (error) {
      message.error(intl.formatMessage({ id: "dms.user.login.failure" }));
    }
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          {intl.formatMessage({ id: "menu.login" })}-{" DMS"}
        </title>
      </Helmet>
      <Lang />
      <div className={styles.mainContent}>
        <div className={styles.logoInfo}>
          <AppsLogo
            width={60}
            height={60}
            color={initialState?.settings?.colorPrimary as string}
          />
          <span className={styles.title}>{initialState?.settings?.title}</span>
        </div>
        <div className={styles.loginForm}>
          <Form
            form={form}
            layout="vertical"
            initialValues={{ autoLogin: true }}
          >
            <Form.Item
              label={intl.formatMessage({ id: "dms.user.login.userName" })}
              name="userName"
              rules={[
                { required: true },
                { max: 30 },
                {
                  pattern: PATTERNS.characterWord,
                  message: intl.formatMessage({
                    id: "dms.common.validate.characterWord",
                  }),
                },
              ]}
            >
              <Input
                placeholder={intl.formatMessage({
                  id: "dms.user.login.userName.placeholder",
                })}
              />
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: "dms.user.login.password" })}
              name="password"
              rules={[{ required: true, min: 6, max: 32 }]}
            >
              <Input.Password
                placeholder={intl.formatMessage({
                  id: "dms.user.login.password.placeholder",
                })}
              />
            </Form.Item>
            <Form.Item
              label={intl.formatMessage({ id: "dms.user.login.authCode" })}
              rules={[{ required: true, len: 5 }]}
              name="authCode"
            >
              <Row gutter={[16, 0]}>
                <Col span={15}>
                  <Form.Item noStyle>
                    <Input
                      placeholder={intl.formatMessage({
                        id: "dms.user.login.authCode.placeholder",
                      })}
                    />
                  </Form.Item>
                </Col>
                <Col span={9}>
                  <img
                    src={authCode?.img}
                    alt={intl.formatMessage({
                      id: "dms.user.login.authCode",
                    })}
                    onClick={refreshAuthCode}
                  />
                </Col>
              </Row>
            </Form.Item>
            <Form.Item style={{ marginBottom: "12px" }}>
              <Form.Item noStyle name="autoLogin" valuePropName="checked">
                <Checkbox>
                  {intl.formatMessage({ id: "dms.user.login.autoLogin" })}
                </Checkbox>
              </Form.Item>
              <div style={{ float: "right" }}>
                <a>
                  {intl.formatMessage({ id: "dms.user.login.forgetPassword" })}
                </a>
              </div>
            </Form.Item>
            <Form.Item style={{ marginBottom: "6px" }}>
              <Button
                type="primary"
                size="large"
                htmlType="submit"
                style={{ width: "100%" }}
                onClick={handleSubmit}
              >
                {intl.formatMessage({ id: "dms.user.login.login" })}
              </Button>
            </Form.Item>
            <Form.Item>
              <a href="/user/register" style={{ float: "right" }}>
                {intl.formatMessage({ id: "dms.user.login.register" })}
              </a>
            </Form.Item>
          </Form>
        </div>
      </div>
    </div>
  );
};

export default Login;
