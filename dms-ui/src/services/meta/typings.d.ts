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
}
