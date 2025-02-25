import { request } from "@umijs/max";

export const RoleService = {
  url: "/api/sys/role",

  list: async (queryParam: DMS.SysRoleParam) => {
    return request<DMS.Page<DMS.SysRole>>(`${RoleService.url}`, {
      method: "GET",
      params: queryParam,
    });
  },

  add: async (data: DMS.SysRole) => {
    return request<DMS.ResponseBody<any>>(`${RoleService.url}`, {
      method: "POST",
      data: data,
    });
  },

  update: async (data: DMS.SysRole) => {
    return request<DMS.ResponseBody<any>>(`${RoleService.url}`, {
      method: "PUT",
      data: data,
    });
  },

  delete: async (data: DMS.SysRole) => {
    return request<DMS.ResponseBody<any>>(`${RoleService.url}/` + data.id, {
      method: "DELETE",
    });
  },

  deleteBatch: async (idList: (number | string)[]) => {
    return request<DMS.ResponseBody<any>>(`${RoleService.url}/batch`, {
      method: "POST",
      data: idList,
    });
  },
  listByUser: async (userName: string) => {
    return request<DMS.ResponseBody<DMS.SysRole[]>>(
      `${RoleService.url}/` + userName,
      {
        method: "GET",
      }
    );
  },
  listAll: async () => {
    return request<DMS.ResponseBody<DMS.SysRole[]>>(`${RoleService.url}/all`, {
      method: "GET",
    });
  },
  grantUserToRole: async (
    roleId: number | string,
    userIds: number[] | string[]
  ) => {
    return request<DMS.ResponseBody<any>>(`${RoleService.url}/` + roleId, {
      method: "POST",
      data: userIds,
    });
  },
};
