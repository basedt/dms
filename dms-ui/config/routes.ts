import { PRIVILEGES } from "../src/constants";

/**
 * @name umi 的路由配置
 * @description 只支持 path,component,routes,redirect,wrappers,name,icon 的配置
 * @param path  path 只支持两种占位符配置，第一种是动态参数 :id 的形式，第二种是 * 通配符，通配符只能出现路由字符串的最后。
 * @param component 配置 location 和 path 匹配后用于渲染的 React 组件路径。可以是绝对路径，也可以是相对路径，如果是相对路径，会从 src/pages 开始找起。
 * @param routes 配置子路由，通常在需要为多个路径增加 layout 组件时使用。
 * @param redirect 配置路由跳转
 * @param wrappers 配置路由组件的包装组件，通过包装组件可以为当前的路由组件组合进更多的功能。 比如，可以用于路由级别的权限校验
 * @param name 配置路由的标题，默认读取国际化文件 menu.ts 中 menu.xxxx 的值，如配置 name 为 login，则读取 menu.ts 中 menu.login 的取值作为标题
 * @param icon 配置路由的图标，取值参考 https://ant.design/components/icon-cn， 注意去除风格后缀和大小写，如想要配置图标为 <StepBackwardOutlined /> 则取值应为 stepBackward 或 StepBackward，如想要配置图标为 <UserOutlined /> 则取值应为 user 或者 User
 * @doc https://umijs.org/docs/guides/routes
 */
export default [
  {
    path: "/user",
    layout: false,
    routes: [
      {
        name: "login",
        path: "/user/login",
        component: "./User/Login",
        exact: true,
      },
      {
        name: "register",
        path: "/user/register",
        component: "./User/Register",
        exact: true,
      },
    ],
  },
  {
    name: "console",
    path: "/console",
    icon: "bank",
    pCode: PRIVILEGES.wsDftDftShow,
    access: "normalRouteFilter",
    routes: [
      {
        path: "/console",
        redirect: "/console/workspace",
        pCode: PRIVILEGES.wsWssDftShow,
        access: "normalRouteFilter",
        exact: true,
      },
      // {
      //   name: "dashboard",
      //   path: "/console/dashboard",
      //   icon: "dashboard",
      //   component: "./Console/Dashboard",
      //   pCode: PRIVILEGES.wsWsoDftShow,
      //   access: "normalRouteFilter",
      //   exact: true,
      // },
      {
        name: "workspace",
        path: "/console/workspace",
        icon: "project",
        component: "./Console",
        pCode: PRIVILEGES.wsWssDftShow,
        access: "normalRouteFilter",
        exact: true,
      },
    ],
  },
  {
    path: "/workspace/:id",
    pCode: PRIVILEGES.wsWssDftShow,
    access: "normalRouteFilter",
    exact: true,
    menuRender: false,
    routes: [
      {
        path: "/workspace/:id",
        component: "./Console/Workspace",
        pCode: PRIVILEGES.wsWssDftShow,
        access: "normalRouteFilter",
        exact: true,
      },
    ],
  },
  {
    name: "admin",
    path: "/admin",
    icon: "setting",
    pCode: PRIVILEGES.sysDftDftShow,
    access: "normalRouteFilter",
    routes: [
      {
        path: "/admin",
        redirect: "/admin/user",
        pCode: PRIVILEGES.sysUsrDftShow,
        access: "normalRouteFilter",
        exact: true,
      },
      {
        name: "user",
        path: "/admin/user",
        icon: "user",
        component: "./Admin/User",
        pCode: PRIVILEGES.sysUsrDftShow,
        access: "normalRouteFilter",
        exact: true,
      },
      {
        name: "role",
        path: "/admin/role",
        icon: "team",
        component: "./Admin/Role",
        pCode: PRIVILEGES.sysRolDftShow,
        access: "normalRouteFilter",
        exact: true,
      },
      {
        name: "dict",
        path: "/admin/dict",
        icon: "profile",
        component: "./Admin/Dict",
        pCode: PRIVILEGES.sysDicDftShow,
        access: "normalRouteFilter",
        exact: true,
      },
      {
        path: "/admin/dict/data",
        component: "./Admin/Dict/Data",
        pCode: PRIVILEGES.sysDicDcdShow,
        access: "normalRouteFilter",
        exact: true,
      },
      {
        name: "setting",
        path: "/admin/setting",
        icon: "setting",
        component: "./Admin/Setting",
        pCode: PRIVILEGES.sysSetDftShow,
        access: "normalRouteFilter",
        exact: true,
      },
    ],
  },
  {
    path: "/user/center",
    component: "./User",
    exact: true,
  },
  {
    path: "/403",
    component: "./Abnormal/403",
    layout: false,
    exact: true,
  },
  {
    path: "/404",
    component: "./Abnormal/404",
    layout: false,
    exact: true,
  },
  {
    path: "/500",
    component: "./Abnormal/500",
    layout: false,
    exact: true,
  },
  {
    path: "/",
    redirect: "/console",
  },
  {
    path: "*",
    layout: false,
    component: "./Abnormal/404",
  },
];
