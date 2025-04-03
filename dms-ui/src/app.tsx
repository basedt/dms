import { AliveScope } from "react-activation";
import { Settings as LayoutSettings } from "@ant-design/pro-components";
import type { RunTimeLayoutConfig } from "@umijs/max";
import { history } from "@umijs/max";
import defaultSettings from "../config/defaultSettings";
import CollapsedIcon from "./components/CollapsedIcon";
import Header from "./components/Header";
import RightContent from "./components/Header/RightContent";
import { errorConfig } from "./requestErrorConfig";
import { AuthService } from "./services/admin/auth.service";
const whiteList: string[] = ["/user/login", "/user/register"];

/**
 * @see  https://umijs.org/zh-CN/plugins/plugin-initial-state
 * */
export async function getInitialState(): Promise<{
  settings?: Partial<LayoutSettings>;
  currentUser?: DMS.SysUser;
  loading?: boolean;
  collapsed?: boolean;
  fetchUserInfo?: () => Promise<DMS.SysUser | undefined>;
}> {
  const fetchUserInfo = async () => {
    let user: DMS.SysUser = {};
    try {
      await AuthService.getCurrentUser(false).then((resp) => {
        if (resp.success && resp.data) {
          user = resp.data;
        } else {
          history.push("/user/login");
        }
      });
    } catch (error) {
      history.push("/user/login");
    }
    return user;
  };
  // 如果不是登录页面，执行
  const { location } = history;
  if (!whiteList.includes(location.pathname)) {
    const currentUser = await fetchUserInfo();
    return {
      fetchUserInfo,
      currentUser,
      collapsed: false,
      settings: defaultSettings,
    };
  }
  return {
    fetchUserInfo,
    collapsed: false,
    settings: defaultSettings,
  };
}

// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout: RunTimeLayoutConfig = ({
  initialState,
  setInitialState,
}) => {
  return {
    siderWidth: 256,
    headerRender: (props) => {
      /**custom Header */
      return <Header headerProps={props} />;
    },
    token: {
      bgLayout: "#fff",
      header: {
        colorBgHeader: "#fff",
      },
      sider: {
        colorMenuBackground: "#fff",
      },
      pageContainer: {
        colorBgPageContainer: "#fff",
      },
    },
    breakpoint: false,
    collapsed: initialState?.collapsed,
    collapsedButtonRender: (collapsed) => {
      return (
        <CollapsedIcon
          collapsed={collapsed}
          type="outer"
          onToggle={() => {
            setInitialState({ ...initialState, collapsed: !collapsed });
          }}
        ></CollapsedIcon>
      );
    },
    rightContentRender: () => <RightContent />,
    menuFooterRender: (props) => {
      false;
    },
    onPageChange: () => {
      const { location } = history;
      if (
        !whiteList.includes(location.pathname) &&
        !initialState?.currentUser?.userName
      ) {
        history.push("/user/login");
      }
    },
    layoutBgImgList: [
      {
        src: "https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/D2LWSqNny4sAAAAAAAAAAAAAFl94AQBr",
        left: 85,
        bottom: 100,
        height: "303px",
      },
      {
        src: "https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/C2TWRpJpiC0AAAAAAAAAAAAAFl94AQBr",
        bottom: -68,
        right: -45,
        height: "303px",
      },
      {
        src: "https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/F6vSTbj8KpYAAAAAAAAAAAAAFl94AQBr",
        bottom: 0,
        left: 0,
        width: "331px",
      },
    ],
    // links: isDev
    //   ? [
    //     <Link key="openapi" to="/umi/plugin/openapi" target="_blank">
    //       <LinkOutlined />
    //       <span>OpenAPI 文档</span>
    //     </Link>,
    //   ]
    //   : [],
    menuHeaderRender: undefined,
    ErrorBoundary: false,
    childrenRender: (children) => {
      return <AliveScope>{children}</AliveScope>;
    },
    ...initialState?.settings,
  };
};

/**
 * @name request 配置，可以配置错误处理
 * 它基于 axios 和 ahooks 的 useRequest 提供了一套统一的网络请求和错误处理方案。
 * @doc https://umijs.org/docs/max/request#配置
 */
export const request = {
  ...errorConfig,
};
