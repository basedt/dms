import { LogService } from "@/services/user/log.service";
import { List, Pagination, Tooltip, Typography } from "antd";
import moment from "moment";
import { useEffect, useState } from "react";
import { useIntl } from "@umijs/max";

const UserLog: React.FC = () => {
  const intl = useIntl();
  const [logs, setLogs] = useState<DMS.LogLogin[]>([]);
  const [pageInfo, setPageInfo] = useState<{
    pageSize: number;
    current: number;
  }>({
    pageSize: 10,
    current: 1,
  });
  const [total, setTotal] = useState<number>(0);

  useEffect(() => {
    refreshLogs(pageInfo.pageSize, pageInfo.current);
  }, []);

  const refreshLogs = (pageSize: number, current: number) => {
    LogService.list({ pageSize, current }).then((resp) => {
      setLogs(resp.data);
      setTotal(resp.total);
    });
  };

  return (
    <>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        {intl.formatMessage({ id: "dms.user.center.log" })}
      </Typography.Title>
      <div>
        <List
          itemLayout="horizontal"
          dataSource={logs}
          renderItem={(item) => (
            <List.Item style={{ padding: "0px 0px 0px 0px" }}>
              <List.Item.Meta
                title={
                  <Typography.Text strong>
                    {item.loginType?.label}&nbsp;&nbsp;
                    <Tooltip title={item.loginTime}>
                      <Typography.Text type="secondary">
                        {moment(item.loginTime).fromNow()}
                      </Typography.Text>
                    </Tooltip>
                  </Typography.Text>
                }
                description={
                  <Typography.Text type="secondary">
                    {intl.formatMessage({
                      id: "dms.user.center.log.ipAddress",
                    }) + item.ipAddress}
                    &nbsp;&nbsp;
                    {intl.formatMessage({
                      id: "dms.user.center.log.clientInfo",
                    }) + item.clientInfo}
                    &nbsp;&nbsp;
                    {intl.formatMessage({
                      id: "dms.user.center.log.os",
                    }) + item.osInfo}
                    &nbsp;&nbsp;
                    {intl.formatMessage({
                      id: "dms.user.center.log.browser",
                    }) + item.browserInfo}
                    &nbsp;&nbsp;
                  </Typography.Text>
                }
              ></List.Item.Meta>
            </List.Item>
          )}
        ></List>
        <Pagination
          size="small"
          showSizeChanger
          showQuickJumper
          hideOnSinglePage
          onChange={(page, pageSize) => {
            setPageInfo({ pageSize: pageSize, current: page });
            refreshLogs(pageSize, page);
          }}
          total={total}
          pageSize={pageInfo.pageSize}
          current={pageInfo.current}
          style={{ paddingTop: 12 }}
        ></Pagination>
      </div>
    </>
  );
};

export default UserLog;
