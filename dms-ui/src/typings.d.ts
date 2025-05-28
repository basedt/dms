declare module 'slash2';
declare module '*.css';
declare module '*.less';
declare module '*.scss';
declare module '*.sass';
declare module '*.svg';
declare module '*.png';
declare module '*.jpg';
declare module '*.jpeg';
declare module '*.gif';
declare module '*.bmp';
declare module '*.tiff';
declare module 'omit.js';
declare module 'numeral';
declare module '@antv/data-set';
declare module 'mockjs';
declare module 'react-fittext';
declare module 'bizcharts-plugin-slider';
declare module 'react-cookies';

declare const REACT_APP_ENV: 'test' | 'dev' | 'pre' | false;
declare module 'monaco-editor/esm/vs/basic-languages/sql/sql';
declare module '@ant-design/icons';
declare module '@ant-design/x';
declare module 'react-syntax-highlighter';
declare module 'react-syntax-highlighter/dist/esm/styles/prism';
declare namespace DMS {
  type QueryParam = {
    pageSize?: number;
    current?: number;
  };

  type Dict = {
    label?: string;
    value?: string | number;
  };

  type ResponseBody<T> = {
    success: boolean;
    data?: T;
    errorCode?: string;
    errorMessage?: string;
    showType?: string;
  };

  type Page<T> = {
    pageSize: number;
    current: number;
    total: number;
    data: T[];
  };

  type TreeNode<T> = {
    key: T;
    parentId: T;
    weight: string | number;
    title: string;
    children: TreeNode<T>[];
  };

  type ModalProps<T> = {
    open?: boolean;
    data?: T;
    handleOk?: (isOpen: boolean, value?: any) => void;
    handleCancel?: () => void;
  };

  type AuthCode = {
    uuid: string;
    img: string;
  };

  type LoginInfo = {
    userName: string;
    password: string;
    authCode: string;
    uuid: string;
    autoLogin: boolean;
  };

  type RegisterInfo = {
    userName: string;
    email: string;
    password: string;
    confirmPassword: string;
    authCode: string;
    uuid: string;
  };
}
