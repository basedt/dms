import { HeaderViewProps } from '@ant-design/pro-layout/es/components/Header';
import { Divider } from 'antd';
import React from 'react';
import AppsLogo from '../AppLogoComponent';
import HeaderMegaMenu from './HeaderMegaMenu';
import GlobalHeaderRight from './RightContent';
import './index.less';

const Header: React.FC<{
  headerProps: HeaderViewProps;
}> = (props) => {
  const { headerProps } = props;

  return (
    <>
      <div className="dms-header">
        <div className="dms-header-main">
          <div
            className="dms-header-main-left"
            style={{ width: (headerProps.siderWidth as number) - 8 }}
          >
            <HeaderMegaMenu headerProps={headerProps} />
            <Divider type="vertical" style={{ height: '80%' }}></Divider>
            <div className="dms-header-main-left-title">
              <a className="dms-header-main-left-title-logo" href="/">
                <AppsLogo width={24} height={24} color={headerProps.colorPrimary as string} />
                <h1 className="dms-header-main-left-title-text">{headerProps.title}</h1>
              </a>
            </div>
          </div>
          <div className="dms-header-main-center"></div>
          <div className="dms-header-main-right">
            <GlobalHeaderRight />
          </div>
        </div>
      </div>
    </>
  );
};
export default Header;
