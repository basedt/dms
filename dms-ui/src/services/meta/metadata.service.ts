import { request } from "@umijs/max";

export const MetaDataService = {
  url: "/api/meta",

  listCatalogTree(dataSourceId: number | string) {
    return request<DMS.ResponseBody<DMS.CatalogTreeNode<string>[]>>(
      `${MetaDataService.url}/schema/${dataSourceId}`,
      {
        method: "GET",
      }
    );
  },
  listChild(
    dataSourceId: number | string,
    identifier: string,
    key: string,
    type: string
  ) {
    return request<DMS.ResponseBody<DMS.CatalogTreeNode<string>[]>>(
      `${MetaDataService.url}/schema/child`,
      {
        method: "GET",
        params: {
          dataSourceId: dataSourceId,
          identifier: identifier,
          key: key,
          type: type,
        },
      }
    );
  },
};
