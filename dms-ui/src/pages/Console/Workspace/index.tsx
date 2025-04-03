import CollapsedIcon from '@/components/CollapsedIcon';
import {
  DatabaseOutlined,
  DownloadOutlined,
  HistoryOutlined,
  SearchOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useAccess, useIntl, useModel, useParams, useSearchParams } from '@umijs/max';
import { Col, Menu, Row } from 'antd';
import { useEffect, useState } from 'react';
import KeepAlive from 'react-activation';
import DataQueryView from './DataQuery';
import DataSourceView from './DataSource';
import QueryHistoryView from './QueryHistory';

import { PRIVILEGES } from '@/constants';
import type { MenuProps } from 'antd';
import DataExportView from './DataExport';
import DataImportView from './DataImport';
type MenuItem = Required<MenuProps>['items'][number];

const WorkspaceView: React.FC = () => {
  const intl = useIntl();
  const access = useAccess();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const { initialState } = useModel('@@initialState');
  const [collapsed, setCollapsed] = useState(true);
  const [siderWidth, setSiderWidth] = useState<number>(65);
  const { setAgGridkey, menuKey, setMenuKey } = useModel('global');

  useEffect(() => {
    const m = searchParams.get('m');
    if (m) {
      setAgGridkey(m as string);
      setMenuKey(m as string);
    }
  }, []);

  const toggleCollapsed = () => {
    setCollapsed(!collapsed);
    setSiderWidth(!collapsed ? 65 : 256);
  };

  const getMenuItems = () => {
    const menuItems: MenuItem[] = [
      {
        key: 'query',
        label: intl.formatMessage({ id: 'menu.query' }),
        icon: <SearchOutlined />,
      },
    ];
    if (access.canAccess(PRIVILEGES.wsWsdDftShow)) {
      menuItems.push({
        key: 'datasource',
        label: intl.formatMessage({ id: 'menu.datasource' }),
        icon: <DatabaseOutlined />,
      });
    }
    if (access.canAccess(PRIVILEGES.wsWseDftShow)) {
      menuItems.push({
        key: 'export',
        label: intl.formatMessage({ id: 'menu.export' }),
        icon: <DownloadOutlined />,
      });
    }
    if (access.canAccess(PRIVILEGES.wsWsiDftShow)) {
      menuItems.push({
        key: 'import',
        label: intl.formatMessage({ id: 'menu.import' }),
        icon: <UploadOutlined />,
      });
    }
    if (access.canAccess(PRIVILEGES.wsWshDftShow)) {
      menuItems.push({
        key: 'log',
        label: intl.formatMessage({ id: 'menu.log' }),
        icon: <HistoryOutlined />,
      });
    }
    return menuItems;
  };

  const renderChildren = () => {
    switch (menuKey) {
      case 'datasource':
        return <DataSourceView workspaceId={id as string}></DataSourceView>;
      case 'query':
        return (
          <KeepAlive cacheId={`data-query-view`}>
            <DataQueryView workspaceId={id as string}></DataQueryView>
          </KeepAlive>
        );
      case 'log':
        return <QueryHistoryView workspaceId={id as string}></QueryHistoryView>;
      case 'export':
        return <DataExportView workspaceId={id as string}></DataExportView>;
      case 'import':
        return <DataImportView workspaceId={id as string}></DataImportView>;
      default:
        return <></>;
    }
  };

  return (
    <PageContainer header={{ title: null, breadcrumb: {} }} childrenContentStyle={{ padding: 0 }}>
      <Row>
        <Col flex={siderWidth} style={{ position: 'fixed' }}>
          <Menu
            theme="light"
            mode="inline"
            items={getMenuItems()}
            inlineCollapsed={collapsed}
            style={{
              width: siderWidth,
              height: 'calc( 100vh - 112px )',
              padding: '0px 8px',
            }}
            onClick={({ key }) => {
              setAgGridkey(key);
              setMenuKey(key);
            }}
            selectedKeys={[menuKey]}
          />
          <CollapsedIcon onToggle={toggleCollapsed} type="inner" collapsed={collapsed} />
        </Col>
        <Col flex="auto" style={{ marginLeft: siderWidth, overflow: 'hidden' }}>
          {renderChildren()}
        </Col>
      </Row>
    </PageContainer>
  );
};

export default WorkspaceView;
