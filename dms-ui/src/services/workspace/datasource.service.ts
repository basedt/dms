import { request } from "@umijs/max";

export const DataSourceService = {
  url: "/api/workspace/ds",

  list: async (queryParam: DMS.DataSourceParam) => {
    return request<DMS.Page<DMS.DataSource>>(`${DataSourceService.url}`, {
      method: "GET",
      params: queryParam,
    });
  },

  add: async (data: DMS.DataSource) => {
    return request<DMS.ResponseBody<any>>(`${DataSourceService.url}`, {
      method: "POST",
      data: data,
    });
  },

  update: async (data: DMS.DataSource) => {
    return request<DMS.ResponseBody<any>>(`${DataSourceService.url}`, {
      method: "PUT",
      data: data,
    });
  },
  selectOne: async (dataSourceId: number | string) => {
    return request<DMS.ResponseBody<DMS.DataSource>>(
      `${DataSourceService.url}/` + dataSourceId,
      { method: "GET" }
    );
  },
  delete: async (data: DMS.DataSource) => {
    return request<DMS.ResponseBody<any>>(
      `${DataSourceService.url}/` + data.id,
      {
        method: "DELETE",
      }
    );
  },

  deleteBatch: async (idList: (number | string)[]) => {
    return request<DMS.ResponseBody<any>>(`${DataSourceService.url}/batch`, {
      method: "POST",
      data: idList,
    });
  },

  testConnection: async (data: DMS.DataSource) => {
    return request<DMS.ResponseBody<any>>(`${DataSourceService.url}/test`, {
      method: "POST",
      data: data,
    });
  },

  listByWorkspace: async (workspaceId: string | number) => {
    return request<DMS.ResponseBody<DMS.Dict[]>>(
      `${DataSourceService.url}/list/${workspaceId}`,
      {
        method: "GET",
      }
    );
  },
};
