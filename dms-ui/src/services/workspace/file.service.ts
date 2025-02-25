import { request } from "@umijs/max";

export const FileService = {
  url: "/api/workspace/file",

  listFileTree: async (
    workspaceId: string | number,
    datasourceId: string | number
  ) => {
    return request<DMS.ResponseBody<DMS.FileTreeNode<string>[]>>(
      `${FileService.url}/list`,
      {
        method: "GET",
        params: { workspaceId: workspaceId, datasourceId: datasourceId },
      }
    );
  },
  selectOne: async (fileId: string | number) => {
    return request<DMS.ResponseBody<DMS.File>>(`${FileService.url}/` + fileId, {
      method: "GET",
    });
  },
  getLatestFile: async (
    workspaceId: string | number | undefined,
    catalogId: string | number | undefined,
    fileName: string | undefined
  ) => {
    return request<DMS.ResponseBody<DMS.File>>(`${FileService.url}`, {
      method: "GET",
      params: {
        workspaceId: workspaceId,
        catalogId: catalogId,
        fileName: fileName,
      },
    });
  },
  save: async (data: DMS.File) => {
    return request<DMS.ResponseBody<any>>(`${FileService.url}`, {
      method: "POST",
      data: data,
    });
  },
  renameFile: async (data: DMS.File) => {
    return request<DMS.ResponseBody<any>>(`${FileService.url}/rename`, {
      method: "PUT",
      data: data,
    });
  },
  moveFileCatalog: async (data: DMS.File) => {
    return request<DMS.ResponseBody<any>>(`${FileService.url}/move`, {
      method: "PUT",
      data: data,
    });
  },
  publish: async (id: string | number) => {
    return request<DMS.ResponseBody<any>>(`${FileService.url}/` + id, {
      method: "PUT",
    });
  },
  delete: async (id: string | number) => {
    return request<DMS.ResponseBody<any>>(`${FileService.url}/` + id, {
      method: "DELETE",
    });
  },
  format: async (data: DMS.File) => {
    return request<DMS.ResponseBody<any>>(`${FileService.url}/format`, {
      method: "POST",
      data: data,
    });
  },
};
