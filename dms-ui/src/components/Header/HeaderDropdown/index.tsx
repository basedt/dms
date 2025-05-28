import { Dropdown } from 'antd';
import { createStyles } from 'antd-style';
import type { DropDownProps } from 'antd/es/dropdown';
import React from 'react';

export type HeaderDropdownProps = {
  overlayClassName?: string;
  placement?: 'bottomLeft' | 'bottomRight' | 'topLeft' | 'topCenter' | 'topRight' | 'bottomCenter';
} & Omit<DropDownProps, 'overlay'>;

const useStyles = createStyles(({ token, css }) => ({
  headerDropdown: {
    [`@media screen and (max-width: ${token.screenXS})`]: {
      width: '100%',
    },
  },
}));

const HeaderDropdown: React.FC<HeaderDropdownProps> = ({ overlayClassName: cls, ...restProps }) => {
  const { styles, cx, theme } = useStyles();

  return (
    <Dropdown arrow overlayClassName={[styles.headerDropdown, cls].join(' ')} {...restProps} />
  );
};

export default HeaderDropdown;
