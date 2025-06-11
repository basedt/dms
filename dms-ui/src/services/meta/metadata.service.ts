import { request } from '@umijs/max';

export const MetaDataService = {
  url: '/api/meta',

  listCatalogTree(dataSourceId: number | string) {
    return request<DMS.ResponseBody<DMS.CatalogTreeNode<string>[]>>(
      `${MetaDataService.url}/schema/${dataSourceId}`,
      {
        method: 'GET',
      },
    );
  },
  listChild(dataSourceId: number | string, identifier: string, key: string, type: string) {
    return request<DMS.ResponseBody<DMS.CatalogTreeNode<string>[]>>(
      `${MetaDataService.url}/schema/child`,
      {
        method: 'GET',
        params: {
          dataSourceId: dataSourceId,
          identifier: identifier,
          key: key,
          type: type,
        },
      },
    );
  },
  listTypeInfo(dataSourceId: number | string) {
    return request<DMS.ResponseBody<DMS.TypeInfo[]>>(
      `${MetaDataService.url}/types/${dataSourceId}`,
      {
        method: 'GET',
      },
    );
  },
  newTable(table: DMS.Table) {
    return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/table`, {
      method: 'PUT',
      data: table,
    });
  },
  getTable(tableParam: DMS.TableInfoParam) {
    return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/table`, {
      method: 'GET',
      params: tableParam,
    });
  },
  renameObject(dataSourceId: number | string, identifier: string, type: string, newName: string) {
    const objInfo: string[] = identifier.split('.') as string[];
    return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/obj/rename`, {
      method: 'GET',
      params: {
        dataSourceId: dataSourceId,
        catalog: objInfo[0],
        schemaName: objInfo[1],
        objectName: type === 'INDEX' ? objInfo[3] : objInfo[2],
        objectType: type,
        newName: newName,
      },
    });
  },
  dropObject(dataSourceId: number | string, identifier: string, type: string) {
    const objInfo: string[] = identifier.split('.') as string[];
    return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/obj/drop`, {
      method: 'GET',
      params: {
        dataSourceId: dataSourceId,
        catalog: objInfo[0],
        schemaName: objInfo[1],
        objectName: type === 'INDEX' ? objInfo[3] : objInfo[2],
        objectType: type,
      },
    });
  },
  generateDML(
    dataSourceId: number | string,
    identifier: string,
    type: 'INSERT' | 'UPDATE' | 'DELETE' | 'SELECT',
  ) {
    const objInfo: string[] = identifier.split('.') as string[];
    return request<DMS.ResponseBody<string>>(`${MetaDataService.url}/table/dml`, {
      method: 'GET',
      params: {
        dataSourceId: dataSourceId,
        catalog: objInfo[0],
        schemaName: objInfo[1],
        objectName: objInfo[2],
        dmlType: type,
      },
    });
  },
};
