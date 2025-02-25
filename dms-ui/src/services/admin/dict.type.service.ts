import { request } from "@umijs/max";

export const DictTypeService = {
  url: "/api/sys/dict/type",

  list: async (queryParam: DMS.SysDictTypeParam) => {
    return request<DMS.Page<DMS.SysDictType>>(`${DictTypeService.url}`, {
      method: "GET",
      params: queryParam,
    });
  },

  add: async (dictType: DMS.SysDictType) => {
    return request<DMS.ResponseBody<any>>(`${DictTypeService.url}`, {
      method: "POST",
      data: dictType,
    });
  },

  update: async (dictType: DMS.SysDictType) => {
    return request<DMS.ResponseBody<any>>(`${DictTypeService.url}`, {
      method: "PUT",
      data: dictType,
    });
  },

  delete: async (dictType: DMS.SysDictType) => {
    return request<DMS.ResponseBody<any>>(
      `${DictTypeService.url}/` + dictType.id,
      {
        method: "DELETE",
      }
    );
  },

  deleteBatch: async (idList: (number | string)[]) => {
    return request<DMS.ResponseBody<any>>(`${DictTypeService.url}/batch`, {
      method: "POST",
      data: idList,
    });
  },
};
