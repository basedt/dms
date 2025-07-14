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
  getTable(tableParam: DMS.TableInfoParam) {
    return request<DMS.ResponseBody<DMS.Table>>(`${MetaDataService.url}/table`, {
      method: 'GET',
      params: tableParam,
    });
  },
  getTableDDL(param: DMS.TableEditParam) {
    return request<DMS.ResponseBody<string>>(`${MetaDataService.url}/table/ddl`, {
      method: 'PUT',
      data: param,
    });
  },
  renameObject(dataSourceId: number | string, identifier: string, type: string, newName: string) {
    const objInfo: string[] = identifier.split('.') as string[];
    if (
      (type === 'INDEX' || type === 'PK' || type === 'FK' || type === 'COLUMN') &&
      objInfo.length >= 4
    ) {
      return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/obj/table/rename`, {
        method: 'GET',
        params: {
          dataSourceId: dataSourceId,
          catalog: objInfo[0],
          schemaName: objInfo[1],
          tableName: objInfo[2],
          objectName: objInfo[3],
          objectType: type,
          newName: newName,
        },
      });
    } else if (objInfo.length >= 3) {
      return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/obj/rename`, {
        method: 'GET',
        params: {
          dataSourceId: dataSourceId,
          catalog: objInfo[0],
          schemaName: objInfo[1],
          objectName: objInfo[2],
          objectType: type,
          newName: newName,
        },
      });
    } else {
      return Promise.reject(new Error('Invalid identifier format'));
    }
  },
  dropObject(dataSourceId: number | string, identifier: string, type: string) {
    const objInfo: string[] = identifier.split('.') as string[];
    if (
      (type === 'INDEX' || type === 'PK' || type === 'FK' || type === 'COLUMN') &&
      objInfo.length >= 4
    ) {
      return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/obj/table/drop`, {
        method: 'GET',
        params: {
          dataSourceId: dataSourceId,
          catalog: objInfo[0],
          schemaName: objInfo[1],
          tableName: objInfo[2],
          objectName: objInfo[3],
          objectType: type,
        },
      });
    } else if (objInfo.length >= 3) {
      return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/obj/drop`, {
        method: 'GET',
        params: {
          dataSourceId: dataSourceId,
          catalog: objInfo[0],
          schemaName: objInfo[1],
          objectName: objInfo[2],
          objectType: type,
        },
      });
    } else {
      return Promise.reject(new Error('Invalid identifier format'));
    }
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
  generateDDL(dataSourceId: number | string, identifier: string, objectType: string) {
    const objInfo: string[] = identifier.split('.') as string[];
    if (objectType === 'INDEX' && objInfo.length >= 4) {
      return request<DMS.ResponseBody<string>>(`${MetaDataService.url}/obj/table/ddl`, {
        method: 'GET',
        params: {
          dataSourceId: dataSourceId,
          catalog: objInfo[0],
          schemaName: objInfo[1],
          tableName: objInfo[2],
          objectName: objInfo[3],
          objectType: objectType,
        },
      });
    } else if (objInfo.length >= 3) {
      return request<DMS.ResponseBody<string>>(`${MetaDataService.url}/obj/ddl`, {
        method: 'GET',
        params: {
          dataSourceId: dataSourceId,
          catalog: objInfo[0],
          schemaName: objInfo[1],
          objectName: objInfo[2],
          objectType: objectType,
        },
      });
    } else {
      return Promise.reject(new Error('Invalid identifier format'));
    }
  },
  executeDDL(param: DMS.sqlScriptParam) {
    return request<DMS.ResponseBody<any>>(`${MetaDataService.url}/ddl/exec`, {
      method: 'POST',
      data: param,
    });
  },
};
