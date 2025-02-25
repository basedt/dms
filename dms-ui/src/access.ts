import { AuthService } from "./services/admin/auth.service";

/**
 * @see https://umijs.org/zh-CN/plugins/plugin-access
 * */
export default function access(
  initialState: { currentUser?: DMS.SysUser } | undefined
) {
  const { currentUser } = initialState ?? {};

  return {
    canAccess: (code: string) => {
      return AuthService.hasPrivilege(code, currentUser as DMS.SysUser);
    },
    normalRouteFilter: (route: any) => {
      return AuthService.hasPrivilege(route?.pCode, currentUser as DMS.SysUser);
    },
  };
}
