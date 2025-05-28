declare namespace DMS {
  type Catalog = {
    catalogName: string;
    schemas: DMS.Schema[];
  };

  type Schema = {
    schemaName: string;
    objects: { [key: string]: DMS.Object[] };
  };

  type Object = {
    catalogName: string;
    schemaName: string;
    objectName: string;
    objectType: string;
    remarks: string;
  };

  type CatalogTreeNode<T> = {
    key: T;
    title: string;
    parentId: T;
    type: string;
    expanded?: boolean;
    identifier: string;
    children?: CatalogTreeNode<T>[];
  };

  type TreeNodeType = {
    id: string;
    type: string;
    key: string;
    parentId: string;
    identifier: string;
    child: TreeNode[];
  };

  type TypeInfo = {
    typeName: string;
    precision: number;
    autoIncrement: boolean;
    localTypeName: string;
  };

  //code suggest
  type TableMetaInfo = {
    databaseId: string;
    identifier: string;
    tableName: string;
    type?: string;
    columns?: string[];
    createAt: Date;
  };

  type Table = {
    catalog?: string;
    schemaName: string;
    tableName: string;
    comment?: string;
    columns?: DMS.Column[];
    indexes?: DMS.Index[];
    partitions?: DMS.Partition[];
  };

  type Column = {
    id: string | number;
    columnName: string;
    dataType: string;
    defaultValue: string;
    comment: string;
    nullable: boolean;
  };

  type Index = {
    id: string | number;
    indexName: string;
    indexType: string;
    uniqueness: boolean;
    columns: string[];
    pk: boolean;
    fk: boolean;
  };

  type Partition = {
    id: string | number;
    paritionName: string;
    partitonExpr: string;
    rows: number;
    size: number;
    // createTime: string;
    // updateTime: string;
  };

  type TableInfoParam = {
    dataSourceId: string;
    catalog: string;
    schemaName: string;
    tableName: string;
    tableInfo?: DMS.Table;
  };

  type TableRenameParam = {
    dataSourceId: string;
    catalog?: string;
    schemaName: string;
    tableName: string;
    newTableName: string;
  };
}
