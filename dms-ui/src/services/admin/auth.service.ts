import { PRIVILEGES } from "@/constants";
import { request } from "@umijs/max";

export const AuthService = {
  refreshAuthImage: async () => {
    return request<DMS.ResponseBody<DMS.AuthCode>>("/api/authCode", {
      method: "GET",
      params: { d: new Date().getTime() },
    });
  },
  login: async (loginInfo: DMS.LoginInfo) => {
    return request<DMS.ResponseBody<DMS.SysUser>>("/api/user/login", {
      method: "POST",
      data: loginInfo,
    });
  },
  register: async (registerInfo: DMS.RegisterInfo) => {
    return request<DMS.ResponseBody<any>>("/api/user/register", {
      method: "POST",
      data: registerInfo,
    });
  },
  getCurrentUser: async (profile: boolean) => {
    return request<DMS.ResponseBody<DMS.SysUser>>("/api/user/current", {
      method: "GET",
      params: { profile: profile },
    });
  },
  logout: async () => {
    return request<DMS.ResponseBody<any>>("/api/user/logout", {
      method: "GET",
    });
  },
  hasPrivilege: (code: string, currentUser: DMS.SysUser) => {
    if (currentUser) {
      let roles: DMS.SysRole[] = currentUser.roles as DMS.SysRole[];
      for (let i = 0; i < roles.length; i++) {
        if (
          roles[i].roleCode == code ||
          PRIVILEGES.roleSuperAdmin == roles[i].roleCode
        ) {
          return true;
        }
        if (roles[i].privileges) {
          let privileges: DMS.SysPrivilege[] = roles[i]
            .privileges as DMS.SysPrivilege[];
          for (let p = 0; p < privileges.length; p++) {
            if (
              privileges[p].privilegeCode == code ||
              privileges[p].privilegeCode == PRIVILEGES.roleSuperAdmin
            ) {
              return true;
            }
          }
        }
      }
    }
    return false;
  },
};
