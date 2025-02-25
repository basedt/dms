import { request } from "@umijs/max";

export const WorkspaceService = {
  url: "/api/workspace",

  list: async (queryParam: DMS.WorkspaceParam) => {
    return request<DMS.Page<DMS.Workspace>>(`${WorkspaceService.url}`, {
      method: "GET",
      params: queryParam,
    });
  },

  add: async (data: DMS.Workspace) => {
    return request<DMS.ResponseBody<any>>(`${WorkspaceService.url}`, {
      method: "POST",
      data: data,
    });
  },

  update: async (data: DMS.Workspace) => {
    return request<DMS.ResponseBody<any>>(`${WorkspaceService.url}`, {
      method: "PUT",
      data: data,
    });
  },

  delete: async (data: DMS.Workspace) => {
    return request<DMS.ResponseBody<any>>(
      `${WorkspaceService.url}/` + data.id,
      {
        method: "DELETE",
      }
    );
  },

  deleteBatch: async (idList: (number | string)[]) => {
    return request<DMS.ResponseBody<any>>(`${WorkspaceService.url}/batch`, {
      method: "POST",
      data: idList,
    });
  },
};
