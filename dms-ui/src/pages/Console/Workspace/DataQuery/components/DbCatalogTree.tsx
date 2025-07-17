import DataExportModal from '@/components/DmsAgGrid/DataExportModal';
import { idbAPI } from '@/idb';
import { MetaDataService } from '@/services/meta/metadata.service';
import { DataSourceService } from '@/services/workspace/datasource.service';
import { debounce, findNodeByTitle, searchKeysByTitle } from '@/utils/ExcelUtil';
import { DownloadOutlined, PlusOutlined, SearchOutlined, UploadOutlined } from '@ant-design/icons';
import { Icon, useIntl, useModel } from '@umijs/max';
import { Button, Col, Divider, Dropdown, Input, MenuProps, message, Modal, Row, Tree } from 'antd';
import { ItemType } from 'antd/lib/menu/interface';
import copy from 'copy-to-clipboard';
import { useEffect, useState } from 'react';
import { v4 as uuidv4 } from 'uuid';
import styles from '../index.less';
import DataImportModal, { DataImportModalProps } from './DataImportModal';
import DbCreateModal from './DbCreateModal';
import DbRenameModal, { DbRenameModalProps } from './DbRenameModal';
import FileModal from './FileModal';
import ScriptPreviewModal from './ScriptPreviewModal';

export type DbCatalogTreeViewProps = {
  workspaceId: string | number;
  datasourceId: string | number;
  height: number;
  onCallback: (
    tabName: string,
    node: DMS.CatalogTreeNode<string>,
    type: string,
    action: string,
  ) => void;
};

const DbCatalogTreeView: React.FC<DbCatalogTreeViewProps> = (props) => {
  const intl = useIntl();
  const { datasourceId, height, onCallback, workspaceId } = props;
  const [treeData, setTreeData] = useState<DMS.CatalogTreeNode<string>[]>([]);
  const [defaultData, setDefaultData] = useState<DMS.CatalogTreeNode<string>[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>();
  const [expandedParentKeys, setExpandedParentKeys] = useState<DMS.TreeNodeType>();
  const [datasource, setDataSource] = useState<DMS.DataSource>();
  const [dbCreateData, setDbCreateData] = useState<DMS.ModalProps<DMS.DataSource>>({ open: false });
  const [fileData, setFileData] = useState<DMS.ModalProps<DMS.File>>({
    open: false,
  });
  const [dataExportData, setDataExportData] = useState<DMS.ModalProps<DMS.DataTask>>({
    open: false,
  });
  const [dataImportData, setDataImportData] = useState<DMS.ModalProps<DataImportModalProps>>({
    open: false,
  });
  const [renameData, setRenameData] = useState<DMS.ModalProps<DbRenameModalProps>>({
    open: false,
  });
  const [sqlPreview, setSqlPreview] = useState<DMS.ModalProps<{ script: string }>>({
    open: false,
  });
  const { setUpDateFile } = useModel('global');

  useEffect(() => {
    idbAPI.cleanupOldMetadata();
    if (!datasourceId) {
      setTreeData([]);
      return;
    }
    initTreeData(datasourceId as string);
    DataSourceService.selectOne(datasourceId).then((resp) => {
      if (resp.success) {
        setDataSource(resp.data);
      }
    });
  }, [datasourceId]);

  const initTreeData = (datasourceId: string) => {
    MetaDataService.listCatalogTree(datasourceId).then((resp) => {
      if (resp.success && resp.data) {
        setTreeData([...resp.data] as DMS.CatalogTreeNode<string>[]);
        setDefaultData([...resp.data] as DMS.CatalogTreeNode<string>[]);
      }
    });
  };

  const updateTreeData = (
    originTree: DMS.CatalogTreeNode<string>[],
    key: string,
    children: DMS.CatalogTreeNode<string>[],
  ): DMS.CatalogTreeNode<string>[] =>
    originTree.map((node) => {
      if (node.key === key) {
        return { ...node, children };
      }
      if (node.children) {
        return {
          ...node,
          children: updateTreeData(node.children, key, children),
        };
      }
      return node;
    });

  const onLoadData = (node: DMS.CatalogTreeNode<string>) =>
    new Promise<void>((resolve) => {
      const { key, children, type, identifier, parentId } = node;
      if (type) {
        MetaDataService.listChild(datasourceId as string, identifier, key, type).then((resp) => {
          if (
            type == 'G_TABLE' ||
            type == 'G_VIEW' ||
            type == 'G_MATERIALIZED_VIEW' ||
            type == 'G_FOREIGN_TABLE'
          ) {
            const tables: DMS.CatalogTreeNode<string>[] =
              resp.data as DMS.CatalogTreeNode<string>[];
            tables?.map((table) => {
              let t: DMS.TableMetaInfo = {
                databaseId: datasourceId as string,
                identifier: table.identifier,
                tableName: table.title,
                type: type.replace('G_', ''),
                columns: [],
                createAt: new Date(),
              };
              idbAPI.upsertTable(t);
            });
          } else if (
            type == 'TABLE' ||
            type == 'VIEW' ||
            type == 'MATERIALIZED_VIEW' ||
            type == 'FOREIGN_TABLE'
          ) {
            const childs: DMS.CatalogTreeNode<string>[] = resp.data?.filter(
              (item) => item.type == 'G_COLUMN',
            ) as DMS.CatalogTreeNode<string>[];

            if (childs?.length == 1) {
              const columns = childs[0];
              idbAPI.getTableByIdentifier(datasourceId as string, columns.identifier).then((t) => {
                let columnList: string[] = [];
                childs[0].children?.map((column) => {
                  columnList.push(column.title);
                });
                t.columns = columnList;
                idbAPI.upsertTable(t);
              });
            }
          }
          setTreeData((origin) =>
            updateTreeData(origin, key, resp.data as DMS.CatalogTreeNode<string>[]),
          );
          setDefaultData((origin) =>
            updateTreeData(origin, key, resp.data as DMS.CatalogTreeNode<string>[]),
          );
          resolve();
        });
      } else {
        resolve();
        return;
      }
    });

  function getAllIds(tree: DMS.TreeNodeType): string[] {
    const ids: string[] = [];

    function traverse(node: DMS.TreeNodeType) {
      ids.push(node.id);
      for (let child of node.child) {
        traverse(child);
      }
    }

    traverse(tree);
    return ids;
  }

  function findObjectById(obj: { id: any; child: any }, targetId: string) {
    if (obj.id === targetId) {
      return obj;
    }

    for (let child of obj.child) {
      const foundObject: any = findObjectById(child, targetId);
      if (foundObject) {
        return foundObject;
      }
    }

    return null;
  }

  function getTreeObj(
    obj: { type: any; parentId: string },
    objList: { id: any; child: any },
    targetType: string[],
  ) {
    if (targetType.includes(obj.type)) {
      return obj;
    } else {
      let parentObject = findObjectById(objList, obj.parentId);
      if (parentObject?.type) {
        return getTreeObj(parentObject, objList, targetType);
      } else {
        return null;
      }
    }
  }

  const refreshTreeData = (node: DMS.CatalogTreeNode<string>) => {
    if (['SCHEMA', 'CATALOG', 'G_SCHEMA'].includes(node.type)) {
      initTreeData(datasourceId as string);
      setExpandedParentKeys(updateTree({ ...node, expanded: true }, expandedParentKeys));
      setExpandedKeys(getAllIds(updateTree({ ...node, expanded: true }, expandedParentKeys)));
    } else {
      const typeArray: string[] = [
        'G_TABLE',
        'TABLE',
        'G_VIEW',
        'VIEW',
        'G_MATERIALIZED_VIEW',
        'MATERIALIZED_VIEW',
        'G_FUNCTION',
        'FUNCTION',
        'G_SEQUENCE',
        'SEQUENCE',
        'G_FOREIGN_TABLE',
        'FOREIGN_TABLE',
        'G_INDEX',
        'INDEX',
        'G_COLUMN',
        'COLUMN',
        'G_PK',
        'PK',
        'G_FK',
        'FK',
      ];
      let targetList = getTreeObj(
        findObjectById(updateTree(node, expandedParentKeys), node.key),
        updateTree(node, expandedParentKeys),
        typeArray,
      );
      // console.log('targetList', targetList, node);
      onLoadData(targetList as DMS.CatalogTreeNode<string>);
      setExpandedParentKeys(updateTree({ ...node, expanded: true }, expandedParentKeys));
      setExpandedKeys(getAllIds(updateTree({ ...node, expanded: true }, expandedParentKeys)));
    }
  };

  const nodeTitleContextmenuItems = (node: DMS.CatalogTreeNode<string>) => {
    const nodeInfo: string[] = node.identifier.split('.');
    const menuItems: MenuProps['items'] = [];
    if (
      datasource?.datasourceType?.value === 'doris' &&
      nodeInfo[0] != 'internal' &&
      !node.type.startsWith('G_')
    ) {
      copyMenuItem(node, menuItems);
    } else if (node.type === 'TABLE') {
      tableMenuItems(node, menuItems);
    } else if (node.type === 'VIEW') {
      viewMenuItems(node, menuItems);
    } else if (node.type === 'MATERIALIZED_VIEW') {
      mviewMenuItems(node, menuItems);
    } else if (node.type === 'INDEX') {
      indexMenuItems(node, menuItems);
    } else if (node.type === 'FOREIGN_TABLE') {
      fgnTableMenuItems(node, menuItems);
    } else if (node.type === 'SEQUENCE') {
      seqMenuItems(node, menuItems);
    } else if (node.type === 'FUNCTION') {
      funMenuItems(node, menuItems);
    } else if (node.type === 'G_TABLE') {
      gTableMenuItems(node, menuItems);
    } else if (node.type === 'G_VIEW') {
      gViewMenuItems(node, menuItems);
    } else if (node.type === 'G_MATERIALIZED_VIEW') {
      gMviewMenuItems(node, menuItems);
    } else if (node.type === 'G_SEQUENCE') {
      gSeqMenuItems(node, menuItems);
    } else if (node.type === 'G_INDEX') {
      gIdxMenuItems(node, menuItems);
    } else if (node.type === 'G_FOREIGN_TABLE') {
      gFgnTableMenuItems(node, menuItems);
    }
    if (
      node.type.startsWith('G_') &&
      node.type !== 'G_TABLE' &&
      node.type !== 'G_VIEW' &&
      node.type !== 'G_MATERIALIZED_VIEW' &&
      node.type !== 'G_SEQUENCE' &&
      node.type !== 'G_INDEX' &&
      node.type !== 'G_FOREIGN_TABLE'
    ) {
      refreshMenuItem(node, menuItems);
    }
    return menuItems;
  };

  const dividerMenuItem = (menuItems: MenuProps['items']) => {
    menuItems?.push({
      type: 'divider',
    });
  };

  const newMenuItem = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    menuItems?.push({
      key: node.key + 'new',
      label: intl.formatMessage(
        {
          id: 'dms.console.workspace.dataquery.new',
        },
        { type: '...' },
      ),
      onClick: () => {
        if (node.type === 'TABLE' || node.type === 'G_TABLE') {
          onCallback(
            intl.formatMessage(
              {
                id: 'dms.console.workspace.dataquery.new',
              },
              { type: 'TABLE' },
            ),
            { ...node, key: uuidv4() },
            'tableInfo',
            'create',
          );
        } else if (
          node.type === 'VIEW' ||
          node.type === 'G_VIEW' ||
          node.type === 'MATERIALIZED_VIEW' ||
          node.type === 'G_MATERIALIZED_VIEW' ||
          node.type === 'SEQUENCE' ||
          node.type === 'G_SEQUENCE' ||
          node.type === 'INDEX' ||
          node.type === 'G_INDEX' ||
          node.type === 'G_FOREIGN_TABLE' ||
          node.type === 'FOREIGN_TABLE'
        ) {
          onCallback(
            intl.formatMessage(
              {
                id: 'dms.console.workspace.dataquery.new',
              },
              { type: node.type.replace('G_', '').replace('_', ' ') },
            ),
            { ...node, key: uuidv4() },
            'objInfo',
            'create',
          );
        }
      },
    });
  };

  const copyMenuItem = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    menuItems?.push({
      key: node.key + 'copy',
      label: intl.formatMessage({
        id: 'dms.console.workspace.dataquery.copy',
      }),
      onClick: () => {
        copy(node.title);
      },
    });
  };

  const refreshMenuItem = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    menuItems?.push({
      key: node.key + 'refresh',
      label: intl.formatMessage({
        id: 'dms.console.workspace.dataquery.refresh',
      }),
      onClick: () => {
        refreshTreeData(node);
      },
    });
  };

  const editMenuItem = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    menuItems?.push({
      key: node.key + 'edit',
      label: intl.formatMessage({
        id: 'dms.console.workspace.dataquery.edit',
      }),
      onClick: () => {
        if (node.type === 'TABLE') {
          onCallback(node.title, node, 'tableInfo', 'edit');
        } else if (
          node.type === 'VIEW' ||
          node.type === 'MATERIALIZED_VIEW' ||
          node.type === 'SEQUENCE' ||
          node.type === 'INDEX' ||
          node.type === 'FOREIGN_TABLE'
        ) {
          onCallback(node.title, node, 'objInfo', 'edit');
        }
      },
    });
  };

  const renameMenuItem = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    menuItems?.push({
      key: node.key + 'rename',
      label: intl.formatMessage({
        id: 'dms.console.workspace.dataquery.rename',
      }),
      disabled: !supportRename(node),
      onClick: () => {
        setRenameData({
          open: true,
          data: {
            dataSourceId: datasourceId as string,
            node: node,
          },
        });
      },
    });
  };

  const supportRename = (node: DMS.CatalogTreeNode<string>): boolean => {
    if (
      (datasource?.datasourceType?.value === 'oracle' ||
        datasource?.datasourceType?.value === 'apachehive' ||
        datasource?.datasourceType?.value === 'doris' ||
        datasource?.datasourceType?.value === 'clickhouse') &&
      node.type === 'MATERIALIZED_VIEW'
    ) {
      return false;
    } else if (datasource?.datasourceType?.value === 'doris' && node.type === 'VIEW') {
      return false;
    } else if (
      (datasource?.datasourceType?.value === 'mysql' ||
        datasource?.datasourceType?.value === 'mariadb' ||
        datasource?.datasourceType?.value === 'polardb_mysql' ||
        datasource?.datasourceType?.value === 'doris') &&
      node.type === 'INDEX'
    ) {
      return false;
    } else {
      return true;
    }
  };

  const dropMenuItem = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    menuItems?.push({
      key: node.key + 'drop',
      label: intl.formatMessage({
        id: 'dms.console.workspace.dataquery.drop',
      }),
      danger: true,
      onClick: () => {
        Modal.confirm({
          title: intl.formatMessage({ id: 'dms.common.operate.delete.confirm.title' }),
          content: node.type.replace('_', ' ') + ' : ' + node.title,
          onOk: () => {
            MetaDataService.dropObject(datasourceId, node.identifier, node.type).then((resp) => {
              if (resp.success) {
                message.success(
                  intl.formatMessage({
                    id: 'dms.common.message.operate.success',
                  }),
                );
                refreshTreeData({
                  ...node,
                  key: node.parentId,
                  type: 'G_' + node.type,
                });
              }
            });
          },
        });
      },
    });
  };

  const scriptMenuItem = (
    node: DMS.CatalogTreeNode<string>,
    menuItems: MenuProps['items'],
    ddl: boolean,
    select: boolean,
    insert: boolean,
    update: boolean,
    del: boolean,
  ) => {
    const menu: ItemType = {
      key: node.key + 'script',
      label: intl.formatMessage({ id: 'dms.console.workspace.dataquery.script' }),
      children: [],
    };
    if (ddl) {
      menu.children?.push({
        key: node.key + 'ddl',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.script.ddl',
        }),
        disabled: !supportDDL(node),
        onClick: () => {
          if (node.type === 'TABLE') {
            const objInfo: string[] = node.identifier.split('.') as string[];
            let tableInfo: DMS.Table = {
              catalog: objInfo[0],
              schemaName: objInfo[1],
              tableName: objInfo[2],
            };
            MetaDataService.getTableDDL({
              dataSourceId: datasource?.id as string,
              originTable: tableInfo,
              newTable: null,
            }).then((resp) => {
              if (resp.success) {
                setSqlPreview({ open: true, data: { script: resp.data as string } });
              }
            });
          } else {
            MetaDataService.generateDDL(datasourceId, node.identifier, node.type).then((resp) => {
              if (resp.success) {
                setSqlPreview({ open: true, data: { script: resp.data as string } });
              }
            });
          }
        },
      });
    }
    if (select) {
      menu.children?.push({
        key: node.key + 'select',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.script.select',
        }),
        onClick: () => {
          MetaDataService.generateDML(datasourceId, node.identifier, 'SELECT').then((resp) => {
            if (resp.success) {
              setSqlPreview({ open: true, data: { script: resp.data as string } });
            }
          });
        },
      });
    }
    if (insert) {
      menu.children?.push({
        key: node.key + 'insert',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.script.insert',
        }),
        onClick: () => {
          MetaDataService.generateDML(datasourceId, node.identifier, 'INSERT').then((resp) => {
            if (resp.success) {
              setSqlPreview({ open: true, data: { script: resp.data as string } });
            }
          });
        },
      });
    }
    if (update) {
      menu.children?.push({
        key: node.key + 'update',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.script.update',
        }),
        onClick: () => {
          MetaDataService.generateDML(datasourceId, node.identifier, 'UPDATE').then((resp) => {
            if (resp.success) {
              setSqlPreview({ open: true, data: { script: resp.data as string } });
            }
          });
        },
      });
    }
    if (del) {
      menu.children?.push({
        key: node.key + 'delete',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.script.delete',
        }),
        onClick: () => {
          MetaDataService.generateDML(datasourceId, node.identifier, 'DELETE').then((resp) => {
            if (resp.success) {
              setSqlPreview({ open: true, data: { script: resp.data as string } });
            }
          });
        },
      });
    }
    menuItems?.push(menu);
  };

  const supportDDL = (node: DMS.CatalogTreeNode<string>): boolean => {
    if (
      (datasource?.datasourceType?.value === 'clickhouse' ||
        datasource?.datasourceType?.value === 'doris') &&
      node.type === 'FUNCTION'
    ) {
      return false;
    } else if (
      node.type === 'INDEX' &&
      (datasource?.datasourceType?.value === 'clickhouse' ||
        datasource?.datasourceType?.value === 'hologres')
    ) {
      return false;
    } else {
      return true;
    }
  };

  const ioMenuItem = (
    node: DMS.CatalogTreeNode<string>,
    menuItems: MenuProps['items'],
    input: boolean,
    output: boolean,
  ) => {
    const menu: ItemType = {
      key: node.key + 'io',
      label: intl.formatMessage({ id: 'dms.console.workspace.dataquery.io' }),
      children: [],
    };
    if (input) {
      menu.children?.push({
        key: node.key + 'export',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.io.export',
        }),
        icon: <DownloadOutlined />,
        onClick: () => {
          dataExport(node);
        },
      });
    }
    if (output) {
      menu.children?.push({
        key: node.key + 'import',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.io.import',
        }),
        icon: <UploadOutlined />,
        onClick: () => {
          dataImport(node);
        },
      });
    }
    menuItems?.push(menu);
  };

  const gTableMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
  };

  const tableMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    copyMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    editMenuItem(node, menuItems);
    renameMenuItem(node, menuItems);
    dropMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    ioMenuItem(node, menuItems, true, true);
    scriptMenuItem(node, menuItems, true, true, true, true, true);
  };

  const gViewMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
  };

  const viewMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    copyMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    editMenuItem(node, menuItems);
    renameMenuItem(node, menuItems);
    dropMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    ioMenuItem(node, menuItems, true, false);
    scriptMenuItem(node, menuItems, true, true, false, false, false);
  };

  const gMviewMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
  };

  const mviewMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    copyMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    editMenuItem(node, menuItems);
    renameMenuItem(node, menuItems);
    dropMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    ioMenuItem(node, menuItems, true, false);
    scriptMenuItem(node, menuItems, true, true, false, false, false);
  };

  const gIdxMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
  };

  const indexMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    copyMenuItem(node, menuItems);
    // refreshMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    editMenuItem(node, menuItems);
    renameMenuItem(node, menuItems);
    dropMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    scriptMenuItem(node, menuItems, true, false, false, false, false);
  };

  const gFgnTableMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
  };

  const fgnTableMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    copyMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    editMenuItem(node, menuItems);
    renameMenuItem(node, menuItems);
    dropMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    ioMenuItem(node, menuItems, true, false);
    scriptMenuItem(node, menuItems, true, true, false, false, false);
  };

  const gSeqMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
  };

  const seqMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    newMenuItem(node, menuItems);
    copyMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    editMenuItem(node, menuItems);
    renameMenuItem(node, menuItems);
    dropMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    scriptMenuItem(node, menuItems, true, false, false, false, false);
  };

  const funMenuItems = (node: DMS.CatalogTreeNode<string>, menuItems: MenuProps['items']) => {
    copyMenuItem(node, menuItems);
    refreshMenuItem(node, menuItems);
    dividerMenuItem(menuItems);
    scriptMenuItem(node, menuItems, true, false, false, false, false);
  };

  const dataExport = (node: DMS.CatalogTreeNode<string>) => {
    setDataExportData({
      open: true,
      data: {
        workspaceId: workspaceId,
        datasourceId: datasourceId,
        sqlScript: 'select * from ' + node.title,
        fileName: node.title,
      },
    });
  };

  const dataImport = (node: DMS.CatalogTreeNode<string>) => {
    let tableInfo: string[] = node.identifier.split('.');
    setDataImportData({
      open: true,
      data: {
        workspaceId: workspaceId,
        datasourceId: datasourceId,
        schema: tableInfo[1],
        table: node.title,
        tableIdentifier: node.identifier,
      },
    });
  };

  const nodeTitleRender = (node: DMS.CatalogTreeNode<string>) => {
    return (
      <Dropdown menu={{ items: nodeTitleContextmenuItems(node) }} trigger={['contextMenu']}>
        <div
          style={{ width: '100%' }}
          onDoubleClick={() => {
            console.log('node info', node);
          }}
        >
          {node.title}
        </div>
      </Dropdown>
    );
  };

  const nodeTitleIconRender = (nodeType: string) => {
    if (nodeType.startsWith('G_')) {
      return <Icon icon="local:folder" height="16" width="16"></Icon>;
    }
    switch (nodeType) {
      case 'CATALOG':
        return (
          <img
            src={
              '/images/databases/' +
              (datasource?.datasourceType?.value as string).toLowerCase() +
              '.svg'
            }
            style={{ width: 16, height: 16 }}
            alt=""
          />
        );
      case 'SCHEMA':
        return <Icon icon="local:schema" height="16" width="16"></Icon>;
      case 'TABLE':
      case 'FOREIGN_TABLE':
        return <Icon icon="local:table" height="16" width="16"></Icon>;
      case 'VIEW':
      case 'MATERIALIZED_VIEW':
        return <Icon icon="local:view" height="16" width="16"></Icon>;
      case 'COLUMN':
        return <Icon icon="local:column" height="16" width="16"></Icon>;
      case 'INDEX':
      case 'PK':
      case 'FK':
        return <Icon icon="local:index" height="16" width="16"></Icon>;
      case 'SEQUENCE':
        return <Icon icon="local:sequence" height="16" width="16"></Icon>;
      case 'FUNCTION':
        return <Icon icon="local:function" height="16" width="16"></Icon>;
      default:
        return <Icon icon="local:file" height="16" width="16"></Icon>;
    }
  };

  function updateTree(
    obj: DMS.CatalogTreeNode<string>,
    tree: DMS.TreeNodeType = {
      id: '',
      child: [],
      type: '',
      identifier: '',
      key: '',
      parentId: '',
    },
  ): DMS.TreeNodeType {
    const { key, expanded, parentId, type, identifier } = obj;
    function findAndUpdateNode(node: DMS.TreeNodeType): boolean {
      if (node.id === parentId) {
        if (!expanded) {
          // 检查是否已经存在相同的节点
          const existingChild = node.child.find((child) => child.id === key);
          if (!existingChild) {
            // 如果不存在相同的节点，将新节点添加到child数组中
            node.child.push({
              id: key,
              child: [],
              type,
              identifier,
              key,
              parentId,
            });
          }
        } else {
          // 移除节点
          node.child = node.child.filter((child) => child.id !== key);
        }
        return true;
      }
      // 递归检查子节点
      for (let child of node.child) {
        if (findAndUpdateNode(child)) {
          return true;
        }
      }
      return false;
    }
    if (!tree.id) {
      // 如果树为空，初始化根节点
      tree = { id: key, child: [], type, identifier, key, parentId };
    } else {
      // 调用递归函数来更新节点
      findAndUpdateNode(tree);
    }
    return tree;
  }

  const onExpand = (
    newExpandedKeys: React.Key[],
    expanded: { node: DMS.CatalogTreeNode<string> },
  ) => {
    setExpandedParentKeys(updateTree(expanded.node, expandedParentKeys));
    setExpandedKeys(newExpandedKeys);
  };
  const onChange = debounce((e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    setTreeData(findNodeByTitle(defaultData, value));
    setExpandedKeys([...new Set(searchKeysByTitle(defaultData, value))]);
  }, 500);

  const plusMenuItems = () => {
    const menuItems: MenuProps['items'] = [];
    menuItems.push({
      key: 'newQueryConsle',
      label: (
        <a>
          {intl.formatMessage({
            id: 'dms.console.workspace.dataquery.database.newQueryConsle',
          })}
        </a>
      ),
      onClick: () => {
        setFileData({
          open: true,
          data: {
            workspaceId: workspaceId,
            datasourceId: datasourceId,
            fileType: { value: 'sql' },
          },
        });
      },
    });
    return menuItems;
  };

  return (
    <div className={styles.catalogTreeContainer}>
      <Row gutter={[6, 6]} style={{ marginTop: 8, marginBottom: 8 }}>
        <Col flex="1 1 1px">
          <Input
            allowClear
            onChange={onChange}
            size="small"
            style={{ height: 20, fontSize: 12 }}
            placeholder={intl.formatMessage({
              id: 'dms.common.operate.search.placeholder',
            })}
            suffix={<SearchOutlined />}
          />
        </Col>
        <Col flex="0 1 auto">
          <Dropdown
            placement="bottomRight"
            arrow
            menu={{ items: plusMenuItems() }}
            disabled={!datasourceId}
          >
            <Button
              icon={<PlusOutlined />}
              size="small"
              type="primary"
              disabled={!datasourceId}
              style={{ height: 20, width: 20, fontSize: 12, marginRight: 4 }}
            />
          </Dropdown>
        </Col>
      </Row>
      <Divider style={{ marginTop: 5, marginBottom: 6 }} />
      <Tree
        treeData={treeData}
        checkable={false}
        blockNode={true}
        showLine={true}
        onExpand={onExpand}
        expandedKeys={expandedKeys}
        loadedKeys={expandedKeys}
        loadData={onLoadData}
        height={height}
        icon={(props: any) => {
          return <div style={{ paddingTop: 4 }}>{nodeTitleIconRender(props.data.type)}</div>;
        }}
        showIcon={true}
        titleRender={nodeTitleRender}
      />
      {dbCreateData.open && (
        <DbCreateModal
          open={dbCreateData.open}
          data={dbCreateData.data}
          handleOk={(isOpen: boolean) => {
            setDbCreateData({ open: isOpen });
            //reload info ?
          }}
          handleCancel={() => {
            setDbCreateData({ open: false });
          }}
        ></DbCreateModal>
      )}
      {fileData.open && (
        <FileModal
          open={fileData.open}
          data={fileData.data}
          handleCancel={() => {
            setFileData({ open: false });
          }}
          handleOk={(isOpen: boolean, value: DMS.File) => {
            setUpDateFile((res) => ++res);
            setFileData({ open: isOpen });
            const node = {
              title: value.fileName + '',
              parentId: value.fileCatalog as string,
              type: value.fileType?.value as string,
              key: '',
              identifier: '',
            };
            onCallback(value.fileName + '', node, 'editor', '');
          }}
        />
      )}
      {dataExportData.open && (
        <DataExportModal
          open={dataExportData.open}
          data={dataExportData.data}
          handleOk={(isOpen: boolean) => {
            setDataExportData({ open: isOpen });
          }}
          handleCancel={() => {
            setDataExportData({ open: false });
          }}
        ></DataExportModal>
      )}
      {dataImportData.open && (
        <DataImportModal
          open={dataImportData.open}
          data={dataImportData.data}
          handleOk={(isOpen: boolean) => {
            setDataImportData({ open: isOpen });
          }}
          handleCancel={() => {
            setDataImportData({ open: false });
          }}
        ></DataImportModal>
      )}
      {renameData.open && (
        <DbRenameModal
          open={renameData.open}
          data={renameData.data}
          handleOk={(isOpen: boolean, node: DMS.CatalogTreeNode<string>) => {
            setRenameData({ open: isOpen });
            refreshTreeData({
              ...node,
              key: node.parentId,
              type: 'G_' + node.type,
            });
          }}
          handleCancel={() => {
            setRenameData({ open: false });
          }}
        ></DbRenameModal>
      )}
      {sqlPreview.open && (
        <ScriptPreviewModal
          open={sqlPreview.open}
          data={sqlPreview.data}
          handleOk={(isOpen: boolean) => {
            setSqlPreview({ open: isOpen });
          }}
          handleCancel={() => {
            setSqlPreview({ open: false });
          }}
        ></ScriptPreviewModal>
      )}
    </div>
  );
};

export default DbCatalogTreeView;
