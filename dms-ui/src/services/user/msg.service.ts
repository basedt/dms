import { request } from "@umijs/max";

export const MsgService = {
  url: "/api/sys/message",

  list: async (param: DMS.SysMessageParam) => {
    return request<DMS.Page<DMS.SysMessage>>(`${MsgService.url}`, {
      method: "GET",
      params: param,
    });
  },
  read: async (ids: string | string[] | number[]) => {
    return request<DMS.ResponseBody<any>>(`${MsgService.url}`, {
      method: "POST",
      data: ids,
    });
  },
  countUnReadMsg: async () => {
    return request<DMS.ResponseBody<number>>(`${MsgService.url}/count`, {
      method: "GET",
    });
  },
};
