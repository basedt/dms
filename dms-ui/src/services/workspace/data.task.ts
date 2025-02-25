import { request } from "@umijs/max";

export const DataTaskService = {
  url: "/api/workspace/task",

  newExportTask: async (data: DMS.DataTask) => {
    return request<DMS.ResponseBody<any>>(`${DataTaskService.url}/export`, {
      method: "POST",
      data: data,
    });
  },

  newImportTask: async (data: DMS.ImportDataTaskParam) => {
    return request<DMS.ResponseBody<any>>(`${DataTaskService.url}/import`, {
      method: "POST",
      data: data,
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  list: async (queryParam: DMS.DataTaskParam) => {
    return request<DMS.Page<DMS.DataTask>>(`${DataTaskService.url}`, {
      method: "GET",
      params: queryParam,
    });
  },

  download: async (data: DMS.DataTask) => {
    const a = document.createElement("a");
    const url = `${DataTaskService.url}/download/` + data.id;
    a.href = url;
    a.download = data.fileName + "";
    a.click();
    window.URL.revokeObjectURL(url);
  },

  viewLog: async (taskId: number | string) => {
    return request<DMS.ResponseBody<DMS.DataTaskLog[]>>(
      `${DataTaskService.url}/log/` + taskId,
      { method: "GET" }
    );
  },
};
