import { request } from "@umijs/max";

export const FileCatalogService = {
  url: "/api/workspace/catalog",

  add: async (data: DMS.FileCatalog) => {
    return request<DMS.ResponseBody<any>>(`${FileCatalogService.url}`, {
      method: "POST",
      data: data,
    });
  },

  update: async (data: DMS.FileCatalog) => {
    return request<DMS.ResponseBody<any>>(`${FileCatalogService.url}`, {
      method: "PUT",
      data: data,
    });
  },

  delete: async (id: string | number) => {
    return request<DMS.ResponseBody<any>>(`${FileCatalogService.url}/` + id, {
      method: "DELETE",
    });
  },
  listCatalogTree: async (
    workspaceId: string | number,
    id: string | number
  ) => {
    return request<DMS.ResponseBody<DMS.FileTreeNode<string>[]>>(
      `${FileCatalogService.url}`,
      {
        method: "GET",
        params: { workspaceId: workspaceId, id: id },
      }
    );
  },
};
