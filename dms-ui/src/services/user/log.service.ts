import { request } from "@umijs/max";

export const LogService = {
  url: "/api/sys/log",

  list: async (param: DMS.QueryParam) => {
    return request<DMS.Page<DMS.LogLogin>>(`${LogService.url}`, {
      method: "GET",
      params: param,
    });
  },
};
