import { Key } from "react";
import { request } from "@umijs/max";

export const PrivilegeService = {
  url: "/api/sys/privilege",

  listAllPrivileges: async () => {
    return request<DMS.ResponseBody<any>>(`${PrivilegeService.url}`, {
      method: "GET",
    });
  },
  listPrivilegeByRole: async (roleId: string | number) => {
    return request<DMS.ResponseBody<any>>(`${PrivilegeService.url}/` + roleId, {
      method: "GET",
    });
  },
  grantPrivilegeToRole: async (roleId: string | number, privileges: Key[]) => {
    return request<DMS.ResponseBody<any>>(`${PrivilegeService.url}/` + roleId, {
      method: "POST",
      data: privileges,
    });
  },
};
