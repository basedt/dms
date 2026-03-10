import { ProProvider } from '@ant-design/pro-components';
import { HeaderViewProps } from '@ant-design/pro-layout/es/components/Header';
import { history, Icon } from '@umijs/max';
import { Drawer } from 'antd';
import React, { useContext, useLayoutEffect, useCallback, useState, useMemo } from 'react';
import './index.less';

export type HeaderMegaMenuProps = {
  headerProps: HeaderViewProps;
};

type MenuDataItem = {
  name?: string;
  icon?: React.ReactNode;
  children?: MenuDataItem[];
  path?: string;
  [key: string]: any;
};

type HeaderMegaMenuItem = MenuDataItem & {
  level: number;
};

// check if menu item should be displayed (filter out redirect types)
const shouldDisplayMenuItem = (item: MenuDataItem): boolean => {
  if (!item.name) return false;
  if (item.redirect) return false;
  if (item.menuRender === false) return false;
  return true;
};

// convert MenuDataItem to HeaderMegaMenuItem and filter out items that shouldn't be displayed
const convertMenuDataToHeaderMegaMenuItem = (menuData: MenuDataItem[], level: number = 1): HeaderMegaMenuItem[] => {
  return menuData
    .filter(shouldDisplayMenuItem)
    .map(item => ({
      ...item,
      level,
      children: item.children ? convertMenuDataToHeaderMegaMenuItem(item.children, level + 1) : undefined,
    }));
};

// get menu items for the current level based on hover state
const getCurrentLevelMenuItems = (
  menuData: HeaderMegaMenuItem[],
  hoverStyle: Record<number, string>,
  currentLevel: number
): HeaderMegaMenuItem[] => {
  if (currentLevel === 1) {
    return menuData;
  }

  let currentItems = menuData;
  for (let level = 1; level < currentLevel; level++) {
    const activeItemName = hoverStyle[level];
    if (!activeItemName) {
      return [];
    }

    const activeItem = currentItems.find(item => item.name === activeItemName);
    if (!activeItem || !activeItem.children) {
      return [];
    }

    currentItems = activeItem.children as HeaderMegaMenuItem[];
  }

  return currentItems;
};

const MenuLevelRenderer: React.FC<{
  items: HeaderMegaMenuItem[];
  hoverStyle: Record<number, string>;
  handleMouseEnter: (item: HeaderMegaMenuItem, level: number) => void;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  setHoverStyle: React.Dispatch<React.SetStateAction<Record<number, string>>>;
  setWidth: React.Dispatch<React.SetStateAction<number>>;
  level: number;
}> = ({ items, hoverStyle, handleMouseEnter, setOpen, setHoverStyle, setWidth, level }) => (
  <ul className="dms-menus-drawer__body_list">
    {items.filter(item => item.name).map((item, index) => (
      <li
        key={index}
        className={[
          'dms-menus-drawer__body_list_li',
          hoverStyle[level] === item.name ? 'list_liActive' : '',
        ].join(' ')}
        onMouseEnter={() => {
          handleMouseEnter(item, level);
        }}
        onClick={
          item.children ? undefined : () => {
            history.push(item.path as string);
            setOpen(false);
            setHoverStyle({});
          }
        }
      >
        <div className="dms-menus-drawer__body_list_li_left">
          {item.icon ? item.icon : ""}
          <span className="dms-menus-drawer__body_list_li_left_title">{item.name}</span>
        </div>
        {item.children && (
          <div className="dms-menus-drawer__body_list_li_right">
            {(hoverStyle[level] === item.name) ? (
              <Icon icon="local:unfoldBolb" height="16" width="16" />
            ) : (
              <Icon icon="local:unfold" height="13" width="13" />
            )}
          </div>
        )}
      </li>
    ))}
  </ul>
);

const HeaderMegaMenu: React.FC<HeaderMegaMenuProps> = ({ headerProps }) => {
  const { token } = useContext(ProProvider);
  const [open, setOpen] = useState(false);
  const [width, setWidth] = useState(256);
  const [hoverStyle, setHoverStyle] = useState<Record<number, string>>({});

  const onClose = () => {
    setOpen(false);
    setHoverStyle({});
  };

  const handleMouseEnter = useCallback((item: HeaderMegaMenuItem, level: number) => {
    // clear hover state for current level and below
    const newHoverStyle = { ...hoverStyle };
    for (let i = level; i <= Object.keys(hoverStyle).length; i++) {
      delete newHoverStyle[i];
    }
    newHoverStyle[level] = item.name || '';
    setHoverStyle(newHoverStyle);
  }, [hoverStyle]);

  // convert menuData to HeaderMegaMenuItem format and filter out items that shouldn't be displayed
  const convertedMenuData = useMemo(() => {
    return headerProps?.menuData ? convertMenuDataToHeaderMegaMenuItem(headerProps.menuData) : [];
  }, [headerProps?.menuData]);

  // compute the number of menu levels to show based on the current hover state
  const maxLevelToShow = useMemo(() => {
    if (!open || convertedMenuData.length === 0) return 1;

    // find the maximum depth of the current hover path
    let maxLevel = 1;
    let currentItems: HeaderMegaMenuItem[] = convertedMenuData;

    const hoverLevels = Object.keys(hoverStyle).map(Number).sort((a, b) => a - b);
    if (hoverLevels.length === 0) return 1;

    for (let i = 0; i < hoverLevels.length; i++) {
      const level = hoverLevels[i];
      const activeItemName = hoverStyle[level];
      if (!activeItemName) break;

      const activeItem = currentItems.find(item => item.name === activeItemName);
      if (!activeItem || !activeItem.children) break;

      maxLevel = level + 1;
      currentItems = activeItem.children as HeaderMegaMenuItem[];
    }

    return maxLevel;
  }, [open, hoverStyle, convertedMenuData]);

  const currentWidth = useMemo(() => {
    return open ? 256 * maxLevelToShow : 256;
  }, [open, maxLevelToShow]);

  // sync width state with currentWidth
  useLayoutEffect(() => {
    const rafId = requestAnimationFrame(() => {
      setWidth(currentWidth);
    });
    return () => {
      cancelAnimationFrame(rafId);
    };
  }, [currentWidth]);

  return (
    <div className="dms-left-menu">
      <span
        className="dms-header-main-left-mega"
        onClick={() => {
          if (open) {
            setHoverStyle({});
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
          className="dms-header-main-left-mega-icon"
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
          {Array.from({ length: maxLevelToShow }, (_, index) => {
            const level = index + 1;
            const menuItems = getCurrentLevelMenuItems(
              convertedMenuData,
              hoverStyle,
              level
            ).filter(item => item.name);

            if (menuItems.length === 0) return null;

            return (
              <MenuLevelRenderer
                key={level}
                items={menuItems}
                hoverStyle={hoverStyle}
                handleMouseEnter={handleMouseEnter}
                setOpen={setOpen}
                setHoverStyle={setHoverStyle}
                setWidth={setWidth}
                level={level}
              />
            );
          })}
        </div>
      </Drawer>
    </div>
  );
};

export default HeaderMegaMenu;
