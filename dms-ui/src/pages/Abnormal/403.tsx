import { Button, Result } from "antd";
import React from "react";
import { history, useIntl } from "@umijs/max";

const AccessDenied: React.FC = () => {
  const intl = useIntl();
  return (
    <Result
      status="403"
      title="403"
      subTitle={intl.formatMessage({ id: "dms.common.message.403" })}
      extra={
        <Button type="primary" onClick={() => history.push("/")}>
          {intl.formatMessage({ id: "dms.common.operate.back.home" })}
        </Button>
      }
    ></Result>
  );
};

export default AccessDenied;
