import { MsgService } from '@/services/user/msg.service';
import { BellOutlined, GithubOutlined } from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { Badge } from 'antd';
import { createStyles } from 'antd-style';
import React, { useEffect, useState } from 'react';
import SelectLang from '../../SelectLang';
import Avatar from './AvatarDropdown';

export type SiderTheme = 'light' | 'dark';

const useStyles = createStyles(({ token, css }) => ({
  headerContainer: {
    display: 'flex',
    height: '48px',
    marginLeft: 'auto',
    overflow: 'hidden',
    cursor: 'pointer',
    gap: 4,
  },
  actionIcon: {
    display: 'flex',
    float: 'right',
    height: '48px',
    marginLeft: 'auto',
    overflow: 'hidden',
    cursor: 'pointer',
    padding: '0 12px',
    borderRadius: token.borderRadius,
    '&:hover': {
      backgroundColor: token.colorBgTextHover,
    },
  },
  bellIcon: {
    padding: '0 12px',
    borderRadius: token.borderRadius,
    '&:hover': {
      backgroundColor: token.colorBgTextHover,
    },
  },
}));

const GlobalHeaderRight: React.FC = () => {
  const [msgCount, setMsgCount] = useState<number>();
  const { styles, cx, theme } = useStyles();

  const { initialState } = useModel('@@initialState');

  if (!initialState || !initialState.settings) {
    return null;
  }

  useEffect(() => {
    MsgService.countUnReadMsg().then((resp) => {
      if (resp.success) {
        setMsgCount(resp.data);
      }
    });
  }, []);

  return (
    <div className={styles.headerContainer}>
      <span
        className={styles.actionIcon}
        onClick={() => {
          window.open('https://github.com/basedt/dms', '_blank');
        }}
      >
        <GithubOutlined />
      </span>
      <span
        className={styles.bellIcon}
        onClick={() => {
          history.push('/user/center', { key: 'message' });
        }}
      >
        <div style={{ marginTop: -3 }}>
          <Badge count={msgCount} showZero={false} size="small" dot={true}>
            <BellOutlined />
          </Badge>
        </div>
      </span>
      <SelectLang className={styles.actionIcon} />
      <Avatar menu={true} />
    </div>
  );
};
export default GlobalHeaderRight;
