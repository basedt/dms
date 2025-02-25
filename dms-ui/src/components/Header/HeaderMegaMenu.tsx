import { ProProvider } from '@ant-design/pro-components';
import { HeaderViewProps } from '@ant-design/pro-layout/es/components/Header';
import { history, Icon } from '@umijs/max';
import { Drawer } from 'antd';
import classNames from 'classnames';
import React, { useContext, useState } from 'react';
import './index.less';

export type HeaderMegaMenuProps = {
  headerProps: HeaderViewProps;
};

type HeaderMegaMenuItem = {
  name?: string;
  icon?: React.ReactNode;
  children?: HeaderMegaMenuItem[];
  path?: string;
};

const MegaMenuItem: React.FC<{
  item: HeaderMegaMenuItem;
  hoverStyle: Record<string, string>;
  handleMouseEnter: (item: HeaderMegaMenuItem, type: string) => void;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  setHoverStyle: React.Dispatch<React.SetStateAction<Record<string, string>>>;
  setHoverList: React.Dispatch<React.SetStateAction<HeaderMegaMenuItem[]>>;
  setWidth: React.Dispatch<React.SetStateAction<number>>;
  type?: string;
}> = ({
  item,
  hoverStyle,
  handleMouseEnter,
  setOpen,
  type = '1',
  setHoverStyle,
  setHoverList,
  setWidth,
}) => (
    <li
      className={classNames(
        'dms-menus-drawer__body_list_li',
        hoverStyle[type] === item.name && 'list_liActive'
      )}
      onMouseEnter={() => {
        handleMouseEnter(item, type);
      }}
      onClick={
        type === '2'
          ? () => {
            history.push(item.path as string);
            setOpen(false);
            setHoverStyle({});
            setHoverList([]);
            setWidth(256);
          }
          : undefined
      }
    >
      <div className="dms-menus-drawer__body_list_li_left">
        {item.icon}
        <span className="dms-menus-drawer__body_list_li_left_title">{item.name}</span>
      </div>
      {type !== '2' && (
        <div className="dms-menus-drawer__body_list_li_right">
          {hoverStyle[type] === item.name ? (
            <Icon icon="local:unfoldBolb" height="16" width="16" />
          ) : (
            <Icon icon="local:unfold" height="13" width="13" />
          )}
        </div>
      )}
    </li>
  );

const HeaderMegaMenu: React.FC<HeaderMegaMenuProps> = ({ headerProps }) => {
  const { token } = useContext(ProProvider);
  const [open, setOpen] = useState(false);
  const [width, setWidth] = useState(256);
  const [hoverList, setHoverList] = useState<HeaderMegaMenuItem[]>([]);
  const [hoverStyle, setHoverStyle] = useState<Record<string, string>>({});

  const onClose = () => {
    setOpen(false);
    setHoverStyle({});
    setHoverList([]);
    setWidth(256);
  };

  const handleMouseEnter = (item: HeaderMegaMenuItem, type: string) => {
    setHoverStyle({ ...hoverStyle, [type]: item.name || '' });
    setWidth(512);
    setTimeout(() => {
      item.children && setHoverList(item.children);
    }, 100);
  };

  return (
    <div className="dms-left-menu">
      <span
        className={classNames('dms-header-main-left-mega')}
        onClick={() => {
          if (open) {
            setHoverStyle({});
            setHoverList([]);
            setWidth(256);
          }
          setOpen(!open);
        }}
        style={{
          color: open ? token?.layout?.colorTextAppListIconHover : '',
          backgroundColor: open ? token?.layout?.colorBgAppListIconHover : '',
        }}
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 448 512"
          className={classNames('dms-header-main-left-mega-icon')}
        >
          <path d="M0 96C0 78.3 14.3 64 32 64H416c17.7 0 32 14.3 32 32s-14.3 32-32 32H32C14.3 128 0 113.7 0 96zM0 256c0-17.7 14.3-32 32-32H416c17.7 0 32 14.3 32 32s-14.3 32-32 32H32c-17.7 0-32-14.3-32-32zM448 416c0 17.7-14.3 32-32 32H32c-17.7 0-32-14.3-32-32s14.3-32 32-32H416c17.7 0 32 14.3 32 32z" />
        </svg>
      </span>
      <Drawer
        onClose={onClose}
        open={open}
        placement="left"
        width={width}
      >
        <div className="dms-menus-drawer__body">
          <ul className="dms-menus-drawer__body_list">
            {headerProps?.menuData?.map((item, index) => (
              item.name && (
                <MegaMenuItem
                  key={index}
                  item={item}
                  hoverStyle={hoverStyle}
                  handleMouseEnter={handleMouseEnter}
                  setOpen={setOpen}
                  setHoverStyle={setHoverStyle}
                  setHoverList={setHoverList}
                  setWidth={setWidth}
                />
              )
            ))}
          </ul>
          {hoverList.length > 0 && (
            <ul className="dms-menus-drawer__body_list">
              {hoverList.map((item, index) => (
                item.name && (
                  <MegaMenuItem
                    key={index}
                    item={item}
                    hoverStyle={hoverStyle}
                    handleMouseEnter={handleMouseEnter}
                    setOpen={setOpen}
                    setHoverStyle={setHoverStyle}
                    setHoverList={setHoverList}
                    setWidth={setWidth}
                    type="2"
                  />
                )
              ))}
            </ul>
          )}
        </div>
      </Drawer>
    </div>
  );
};

export default HeaderMegaMenu;