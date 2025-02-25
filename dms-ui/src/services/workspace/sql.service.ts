import { request } from "@umijs/max";

export const SqlHistoryService = {
  url: "/api/workspace/sql",

  list: async (queryParam: DMS.LogSqlHistoryParam) => {
    return request<DMS.Page<DMS.LogSqlHistory>>(`${SqlHistoryService.url}`, {
      method: "GET",
      params: queryParam,
    });
  },
  stop: async (socketId: string) => {
    return request<DMS.ResponseBody<any>>(
      `${SqlHistoryService.url}/stop/` + socketId,
      {
        method: "POST",
      }
    );
  },
};
