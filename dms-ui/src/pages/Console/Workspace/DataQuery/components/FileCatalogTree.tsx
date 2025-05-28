import { FileCatalogService } from '@/services/workspace/file.catalog.service';
import { FileService } from '@/services/workspace/file.service';
import { debounce, findNodeByTitle, searchKeysByTitle } from '@/utils/ExcelUtil';
import { SearchOutlined } from '@ant-design/icons';
import { Icon, useIntl, useModel } from '@umijs/max';
import { Col, Divider, Dropdown, Input, MenuProps, Modal, Row, Tree, message } from 'antd';
import { useEffect, useState } from 'react';
import styles from '../index.less';
import FileCatalogModal from './FileCatalogModal';
import FileModal from './FileModal';
import FileRenameModal, { FileRenameModalProps } from './FileRenameModal';

export type FileCatalogTreeViewProps = {
  workspaceId: string | number;
  datasourceId: string | number;
  height: number;
  onCallback: (
    tabName: string,
    node: DMS.FileTreeNode<string>,
    type: string,
    action: string,
  ) => void;
};

const FileCatalogTreeView: React.FC<FileCatalogTreeViewProps> = (props) => {
  const intl = useIntl();
  const { upDateFile } = useModel('global');
  const { workspaceId, height, datasourceId, onCallback } = props;
  const [treeData, setTreeData] = useState<DMS.FileTreeNode<string>[]>([]);
  const [defaultData, setDefaultData] = useState<DMS.FileTreeNode<string>[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>();
  const [fileData, setFileData] = useState<DMS.ModalProps<DMS.File>>({
    open: false,
  });
  const [fileCatalogData, setFileCatalogData] = useState<DMS.ModalProps<DMS.FileCatalog>>({
    open: false,
  });
  const [renameData, setRenameData] = useState<DMS.ModalProps<FileRenameModalProps>>({
    open: false,
  });

  useEffect(() => {
    if (!workspaceId || !datasourceId) return;
    reloadTreeData();
  }, [workspaceId, datasourceId]);

  const reloadTreeData = () => {
    FileService.listFileTree(workspaceId, datasourceId).then((resp) => {
      if (resp.success) {
        setTreeData(resp.data as DMS.FileTreeNode<string>[]);
        setDefaultData(resp.data as DMS.FileTreeNode<string>[]);
      }
    });
  };

  const onExpand = (newExpandedKeys: React.Key[]) => {
    setExpandedKeys(newExpandedKeys);
  };

  const onSearch = debounce((e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    setTreeData(findNodeByTitle(defaultData, value));
    setExpandedKeys(searchKeysByTitle(defaultData, value));
  }, 500);

  useEffect(() => {
    if (upDateFile == 0) return;
    reloadTreeData(); //更新文件列表
  }, [upDateFile]);

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

  const nodeTitleRender = (node: DMS.FileTreeNode<string>) => {
    return (
      <Dropdown menu={{ items: nodeTitleContextmenuItems(node) }} trigger={['contextMenu']}>
        <div
          style={{ width: '100%' }}
          onDoubleClick={() => {
            if (node.type != 'catalog') {
              onCallback(node.title, node, 'editor', '');
            }
          }}
        >
          {node.title}
        </div>
      </Dropdown>
    );
  };

  const nodeTitleIconRender = (nodeType: string) => {
    switch (nodeType) {
      case 'catalog':
        return <Icon icon="local:folder" height="16" width="16"></Icon>;
      default:
        return <Icon icon="local:file" height="16" width="16"></Icon>;
    }
  };

  const nodeTitleContextmenuItems = (node: DMS.FileTreeNode<string>) => {
    const menuItems: MenuProps['items'] = [];

    if (node.type === 'catalog') {
      menuItems.push({
        key: node.key + 'new',
        label: intl.formatMessage(
          {
            id: 'dms.console.workspace.dataquery.new',
          },
          { type: '' },
        ),
        children: [
          {
            key: 'sql',
            label: intl.formatMessage({
              id: 'dms.console.workspace.dataquery.file.sql',
            }),
            onClick: () => {
              setFileData({
                open: true,
                data: {
                  workspaceId: workspaceId,
                  datasourceId: datasourceId,
                  fileCatalog: node.key,
                  fileType: { value: 'sql' },
                },
              });
            },
          },
          {
            type: 'divider',
          },
          {
            key: node.key + 'newCatalog',
            label: intl.formatMessage({
              id: 'dms.console.workspace.dataquery.file.catalog',
            }),
            onClick: () => {
              setFileCatalogData({
                open: true,
                data: { workspaceId: workspaceId, pid: node.key },
              });
            },
          },
        ],
      });
      menuItems.push({
        key: node.key + 'rename',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.rename',
        }),
        onClick: () => {
          setRenameData({
            open: true,
            data: {
              type: 'catalog',
              originNode: node,
              originName: node.title,
              workspaceId: workspaceId,
            },
          });
        },
      });
      menuItems.push({
        key: node.key + 'move',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.move',
        }),
        onClick: () => {
          setFileCatalogData({
            open: true,
            data: {
              workspaceId: workspaceId,
              id: node.key,
              pid: node.parentId,
              name: node.title,
            },
          });
        },
      });
      menuItems.push({
        key: node.key + 'del',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.delete',
        }),
        danger: true,
        onClick: () => {
          Modal.confirm({
            title: intl.formatMessage({
              id: 'dms.common.operate.delete.confirm.title',
            }),
            content: intl.formatMessage({
              id: 'dms.common.operate.delete.confirm.content',
            }),
            onOk: () => {
              FileCatalogService.delete(node.key).then((resp) => {
                if (resp.success) {
                  message.success(
                    intl.formatMessage({
                      id: 'dms.common.message.operate.delete.success',
                    }),
                  );
                  reloadTreeData();
                }
              });
            },
          });
        },
      });
    } else if (node.type === 'sql') {
      menuItems.push({
        key: node.key + 'open',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.open',
        }),
        onClick: () => {
          onCallback(node.title, node, 'editor', '');
        },
      });
      menuItems.push({
        key: node.key + 'rename',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.rename',
        }),
        onClick: () => {
          setRenameData({
            open: true,
            data: {
              type: 'file',
              originNode: node,
              originName: node.title,
              workspaceId: workspaceId,
            },
          });
        },
      });
      menuItems.push({
        key: node.key + 'move',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.move',
        }),
        onClick: () => {
          setFileData({
            open: true,
            data: {
              id: node.key.split('.')[1],
              workspaceId: workspaceId,
              fileCatalog: node.parentId,
              fileName: node.title,
              fileType: { value: node.type },
            },
          });
        },
      });
      menuItems.push({
        key: node.key + 'delete',
        label: intl.formatMessage({
          id: 'dms.console.workspace.dataquery.delete',
        }),
        danger: true,
        onClick: () => {
          Modal.confirm({
            title: intl.formatMessage({
              id: 'dms.common.operate.delete.confirm.title',
            }),
            content: intl.formatMessage({
              id: 'dms.common.operate.delete.confirm.content',
            }),
            onOk: () => {
              FileService.delete(node.key.split('.')[1]).then((resp) => {
                if (resp.success) {
                  message.success(
                    intl.formatMessage({
                      id: 'dms.common.message.operate.delete.success',
                    }),
                  );
                  reloadTreeData();
                }
              });
            },
          });
        },
      });
    }
    return menuItems;
  };

  return (
    <div className={styles.catalogTreeContainer}>
      <Row gutter={[6, 6]} style={{ marginTop: 8, marginBottom: 8 }}>
        <Col span={24}>
          <Input
            allowClear
            onChange={onSearch}
            // onBlur={onSearch}
            size="small"
            style={{ height: 20, fontSize: 12 }}
            placeholder={intl.formatMessage({
              id: 'dms.common.operate.search.placeholder',
            })}
            suffix={<SearchOutlined />}
          />
        </Col>
      </Row>
      <Divider style={{ marginTop: 5, marginBottom: 6 }} />
      <Tree
        treeData={treeData}
        checkable={false}
        blockNode={true}
        showLine={true}
        // loadData={onLoadData}
        height={height}
        showIcon={true}
        onExpand={onExpand}
        expandedKeys={expandedKeys}
        icon={(props: any) => {
          return <div style={{ paddingTop: 4 }}>{nodeTitleIconRender(props.data.type)}</div>;
        }}
        titleRender={nodeTitleRender}
      ></Tree>
      {fileData.open && (
        <FileModal
          open={fileData.open}
          data={fileData.data}
          handleCancel={() => {
            setFileData({ open: false });
          }}
          handleOk={(isOpen: boolean) => {
            setFileData({ open: isOpen });
            reloadTreeData();
          }}
        ></FileModal>
      )}
      {fileCatalogData.open && (
        <FileCatalogModal
          open={fileCatalogData.open}
          data={fileCatalogData.data}
          handleCancel={() => {
            setFileCatalogData({ open: false });
          }}
          handleOk={(isOpen: boolean) => {
            setFileCatalogData({ open: isOpen });
            reloadTreeData();
          }}
        ></FileCatalogModal>
      )}
      {renameData.open && (
        <FileRenameModal
          open={renameData.open}
          data={renameData.data}
          handleCancel={() => {
            setRenameData({ open: false });
          }}
          handleOk={(isOpen: boolean) => {
            setRenameData({ open: isOpen });
            reloadTreeData();
          }}
        />
      )}
    </div>
  );
};

export default FileCatalogTreeView;
