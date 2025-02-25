import { request } from "@umijs/max";

export const DictDataService = {
  url: "/api/sys/dict/data",

  list: async (queryParam: DMS.SysDictDataParam) => {
    return request<DMS.Page<DMS.SysDictData>>(`${DictDataService.url}`, {
      method: "GET",
      params: queryParam,
    });
  },

  listByType: async (type: string) => {
    return request<DMS.ResponseBody<DMS.Dict[]>>(
      `${DictDataService.url}/` + type,
      {
        method: "GET",
      }
    );
  },

  add: async (data: DMS.SysDictData) => {
    return request<DMS.ResponseBody<any>>(`${DictDataService.url}`, {
      method: "POST",
      data: data,
    });
  },

  update: async (data: DMS.SysDictData) => {
    return request<DMS.ResponseBody<any>>(`${DictDataService.url}`, {
      method: "PUT",
      data: data,
    });
  },

  delete: async (data: DMS.SysDictData) => {
    return request<DMS.ResponseBody<any>>(`${DictDataService.url}/` + data.id, {
      method: "DELETE",
    });
  },

  deleteBatch: async (idList: (number | string)[]) => {
    return request<DMS.ResponseBody<any>>(`${DictDataService.url}/batch`, {
      method: "POST",
      data: idList,
    });
  },
};
