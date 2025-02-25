import { request } from "@umijs/max";

export const UserService = {
  url: "/api/sys/user",

  list: async (queryParam: DMS.SysUserParam) => {
    return request<DMS.Page<DMS.SysUser>>(`${UserService.url}`, {
      method: "GET",
      params: queryParam,
    });
  },

  add: async (data: DMS.SysUser) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}`, {
      method: "POST",
      data: data,
    });
  },

  update: async (data: DMS.SysUser) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}`, {
      method: "PUT",
      data: data,
    });
  },

  delete: async (data: DMS.SysUser) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}/` + data.id, {
      method: "DELETE",
    });
  },

  deleteBatch: async (idList: (number | string)[]) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}/batch`, {
      method: "POST",
      data: idList,
    });
  },
  enableUsers: async (idList: (number | string)[]) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}/enable`, {
      method: "POST",
      data: idList,
    });
  },
  isUserExists: async (userName: string) => {
    return request<boolean>("/api/user/validate/userName", {
      method: "GET",
      params: { userName: userName },
    });
  },
  isEmailExists: async (email: string) => {
    return request<boolean>("/api/user/validate/email", {
      method: "GET",
      params: { email: email },
    });
  },
  grantRoleToUser: async (userId: number, roleIds: number[] | string[]) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}/role/` + userId, {
      method: "POST",
      data: roleIds,
    });
  },
  listUserWithRole: async (roleId: string | number) => {
    return request<DMS.ResponseBody<DMS.SysUser[]>>(
      `${UserService.url}/` + roleId,
      {
        method: "GET",
      }
    );
  },
  changePassword: async (pwdInfo: {
    oldPassword: string;
    password: string;
    confirmPassword: string;
  }) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}/pwd/edit`, {
      method: "POST",
      data: pwdInfo,
    });
  },
  getEmailAuthCode: async (email: string) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}/mail/auth`, {
      method: "GET",
      params: { email: email },
    });
  },
  bindEmail: async (value: { email: string; authCode: string }) => {
    return request<DMS.ResponseBody<any>>(`${UserService.url}/mail/bind`, {
      method: "POST",
      data: value,
    });
  },
};
