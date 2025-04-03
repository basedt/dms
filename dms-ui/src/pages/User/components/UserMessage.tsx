import { MsgService } from '@/services/user/msg.service';
import { useIntl } from '@umijs/max';
import { Collapse, Space, Tag, Typography } from 'antd';
import moment from 'moment';
import { useEffect, useState } from 'react';

const UserMessage: React.FC = () => {
  const intl = useIntl();
  const [messages, setMessages] = useState<DMS.SysMessage[]>([]);
  // const [messageTypeList, setMessageTypeList] = useState<DMS.Dict[]>();

  const refreshMessages = () => {
    MsgService.list({ isDelete: '0' }).then((resp) => {
      if (resp.data) {
        setMessages(resp.data);
      }
    });
  };

  useEffect(() => {
    // DictDataService.listByType(DICT_TYPE.messageType).then((resp) => {
    //   if (resp.success) {
    //     setMessageTypeList(resp.data);
    //   }
    // });
    refreshMessages();
  }, []);

  return (
    <>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        {intl.formatMessage({ id: 'dms.user.center.message' })}
      </Typography.Title>
      <Collapse
        ghost
        accordion
        onChange={(key: string | string[]) => {
          MsgService.read(key).then((resp) => {
            if (resp.success) {
              refreshMessages();
            }
          });
        }}
      >
        {messages.map((item) => {
          return (
            <Collapse.Panel
              header={
                <Space>
                  <Typography.Text strong>{item.title}</Typography.Text>
                  <Tag color={item.isRead.value === '0' ? 'red' : 'blue'}>{item.isRead.label}</Tag>
                </Space>
              }
              key={item.id as number}
              extra={<Typography.Text>{moment(item.createTime).fromNow()}</Typography.Text>}
            >
              <Typography.Text>{item.content}</Typography.Text>
            </Collapse.Panel>
          );
        })}
      </Collapse>
    </>
  );
};

export default UserMessage;
