/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.basedt.dms.plugins.datasource.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.ResponseCode;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.common.utils.I18nUtil;
import com.basedt.dms.common.vo.TreeNodeVO;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.DataSourcePluginManager;
import com.basedt.dms.plugins.datasource.MetaDataService;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.basedt.dms.common.enums.ResponseCode.ERROR_INVALID_ARGUMENT;
import static com.basedt.dms.plugins.datasource.enums.DbGroupType.*;
import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

@Service
public class MetaDataServiceImpl implements MetaDataService {

    /**
     * list catalog and schemas
     *
     * @return
     */
    @Override
    public List<CatalogDTO> listSchemas(DataSourcePlugin dataSourcePlugin) throws DmsException {
        if (Objects.isNull(dataSourcePlugin)) {
            return null;
        }
        List<CatalogDTO> catalogs = null;
        try {
            catalogs = dataSourcePlugin.listCatalogs();
            for (CatalogDTO catalog : catalogs) {
                List<SchemaDTO> schemas = null;
                if (StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.POSTGRESQL.getValue()).toUpperCase().equals(dataSourcePlugin.getPluginName())
                        || StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.MSSQL.getValue()).toUpperCase().equals(dataSourcePlugin.getPluginName())
                        || StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.HOLOGRES.getValue()).toUpperCase().equals(dataSourcePlugin.getPluginName())
                        || StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.GAUSSDB.getValue()).toUpperCase().equals(dataSourcePlugin.getPluginName())
                        || StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.GREENPLUM.getValue()).toUpperCase().equals(dataSourcePlugin.getPluginName())
                        || StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.POLARDB_POSTGRE.getValue()).toUpperCase().equals(dataSourcePlugin.getPluginName())
                ) {
                    schemas = dataSourcePlugin.listSchemas(catalog.getCatalogName(), null);
                } else {
                    schemas = dataSourcePlugin.listSchemas(catalog.getCatalogName(), dataSourcePlugin.getDatabaseName());
                }
                catalog.setSchemas(schemas);
            }
        } catch (Exception e) {
            throw new DmsException(ResponseCode.ERROR_CUSTOM.getValue(), I18nUtil.get("response.error.datasource.catalog"));
        }
        return catalogs;
    }

    @Override
    public List<CatalogDTO> listSchemas(DataSourceDTO dataSourceDTO) throws DmsException {
        DataSourcePlugin dataSourcePlugin = getDataSourcePluginInstance(dataSourceDTO);
        return listSchemas(dataSourcePlugin);
    }

    @Override
    public List<Tree<String>> listSchemasTree(DataSourceDTO dataSourceDTO) throws DmsException {
        if (Objects.isNull(dataSourceDTO)) {
            return buildTreeList(new ArrayList<>(), DigestUtil.md5Hex(Constants.ROOT_CATALOG_CODE));
        }
        List<CatalogDTO> catalogs = listSchemas(dataSourceDTO);
        List<TreeNodeVO> treeNodeList = new ArrayList<>();
        for (CatalogDTO catalog : catalogs) {
            TreeNodeVO catalogNode = catalog.toTreeNodeVO();
            treeNodeList.add(catalogNode);
            //add schema group
            TreeNodeVO schemaGroupNode = this.getGroupNode(catalogNode, SCHEMA);
            treeNodeList.add(schemaGroupNode);
            //add schema nodes
            if (CollectionUtil.isNotEmpty(catalog.getSchemas())) {
                List<SchemaDTO> schemaList = catalog.getSchemas();
                for (SchemaDTO schema : schemaList) {
                    TreeNodeVO schemaNode = schema.toTreeNodeVO(schemaGroupNode);
                    treeNodeList.add(schemaNode);
                    List<TreeNodeVO> objectGroupNodeList = this.getAllObjectGroupNode(dataSourceDTO, schemaNode);
                    treeNodeList.addAll(objectGroupNodeList);
                }
            }
        }
        return buildTreeList(treeNodeList, DigestUtil.md5Hex(Constants.ROOT_CATALOG_CODE));
    }

    @Override
    public List<Tree<String>> listChildNode(DataSourceDTO dataSourceDTO, String identifier, String key, String type) throws DmsException, SQLException {
        DataSourcePlugin dataSourcePlugin = getDataSourcePluginInstance(dataSourceDTO);
        if (Objects.isNull(dataSourcePlugin) || Objects.isNull(type)) {
            return null;
        }
        if (G_TABLE.name().equalsIgnoreCase(type)) {
            return listTables(identifier, key, dataSourcePlugin, TABLE);
        } else if (G_VIEW.name().equalsIgnoreCase(type)) {
            return listViews(identifier, key, dataSourcePlugin);
        } else if (G_MATERIALIZED_VIEW.name().equalsIgnoreCase(type)) {
            return listMViews(identifier, key, dataSourcePlugin);
        } else if (G_INDEX.name().equalsIgnoreCase(type)) {
            return listIndex(identifier, key, dataSourcePlugin);
        } else if (G_FUNCTION.name().equalsIgnoreCase(type)) {
            return listFunction(identifier, key, dataSourcePlugin);
        } else if (G_SEQUENCE.name().equalsIgnoreCase(type)) {
            return listSequence(identifier, key, dataSourcePlugin);
        } else if (G_FOREIGN_TABLE.name().equalsIgnoreCase(type)) {
            return listTables(identifier, key, dataSourcePlugin, FOREIGN_TABLE);
        } else if (TABLE.name().equalsIgnoreCase(type)) {
            return listTableChild(identifier, key, dataSourcePlugin);
        } else if (VIEW.name().equalsIgnoreCase(type) || FOREIGN_TABLE.name().equalsIgnoreCase(type) || MATERIALIZED_VIEW.name().equalsIgnoreCase(type)) {
            return listViewColumn(identifier, key, dataSourcePlugin);
        } else {
            return null;
        }
    }

    private List<Tree<String>> listTables(String identifier, String key, DataSourcePlugin dataSourcePlugin, DbObjectType type) throws DmsException, SQLException {
        String catalog = parseIdentifier(identifier, 1);
        String schema = parseIdentifier(identifier, 2);
        List<TableDTO> tableList = new ArrayList<>();
        if (DbObjectType.TABLE.equals(type)) {
            tableList = dataSourcePlugin.listTables(catalog, schema, null);
        } else if (DbObjectType.FOREIGN_TABLE.equals(type)) {
            tableList = dataSourcePlugin.listForeignTables(catalog, schema, null);
        }
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(tableList)) {
            TreeNodeVO parent = new TreeNodeVO();
            parent.setKey(key);
            parent.setIdentifier(identifier);
            for (TableDTO tableDTO : tableList) {
                TreeNodeVO node = tableDTO.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return buildTreeList(treeList, key);
    }

    private List<Tree<String>> listViews(String identifier, String key, DataSourcePlugin dataSourcePlugin) throws DmsException, SQLException {
        String catalog = parseIdentifier(identifier, 1);
        String schema = parseIdentifier(identifier, 2);
        List<ViewDTO> viewList = dataSourcePlugin.listViews(catalog, schema, null);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(viewList)) {
            TreeNodeVO parent = new TreeNodeVO();
            parent.setKey(key);
            parent.setIdentifier(identifier);
            for (ViewDTO view : viewList) {
                TreeNodeVO node = view.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return buildTreeList(treeList, key);
    }

    private List<Tree<String>> listMViews(String identifier, String key, DataSourcePlugin dataSourcePlugin) throws DmsException, SQLException {
        String catalog = parseIdentifier(identifier, 1);
        String schema = parseIdentifier(identifier, 2);
        List<MaterializedViewDTO> list = dataSourcePlugin.listMViews(catalog, schema, null);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            TreeNodeVO parent = new TreeNodeVO();
            parent.setKey(key);
            parent.setIdentifier(identifier);
            for (MaterializedViewDTO mView : list) {
                TreeNodeVO node = mView.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return buildTreeList(treeList, key);
    }

    private List<Tree<String>> listIndex(String identifier, String key, DataSourcePlugin dataSourcePlugin) throws DmsException, SQLException {
        String catalog = parseIdentifier(identifier, 1);
        String schema = parseIdentifier(identifier, 2);
        List<IndexDTO> list = dataSourcePlugin.listIndexes(catalog, schema, null);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            TreeNodeVO parent = new TreeNodeVO();
            parent.setKey(key);
            parent.setIdentifier(identifier);
            for (IndexDTO index : list) {
                TreeNodeVO node = index.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return buildTreeList(treeList, key);
    }

    private List<Tree<String>> listFunction(String identifier, String key, DataSourcePlugin dataSourcePlugin) throws DmsException, SQLException {
        String catalog = parseIdentifier(identifier, 1);
        String schema = parseIdentifier(identifier, 2);
        List<FunctionDTO> list = dataSourcePlugin.listFunctions(catalog, schema, null);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            TreeNodeVO parent = new TreeNodeVO();
            parent.setKey(key);
            parent.setIdentifier(identifier);
            for (FunctionDTO function : list) {
                TreeNodeVO node = function.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return buildTreeList(treeList, key);
    }

    private List<Tree<String>> listSequence(String identifier, String key, DataSourcePlugin dataSourcePlugin) throws DmsException, SQLException {
        String catalog = parseIdentifier(identifier, 1);
        String schema = parseIdentifier(identifier, 2);
        List<SequenceDTO> list = dataSourcePlugin.listSequences(catalog, schema, null);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            TreeNodeVO parent = new TreeNodeVO();
            parent.setKey(key);
            parent.setIdentifier(identifier);
            for (SequenceDTO sequence : list) {
                TreeNodeVO node = sequence.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return buildTreeList(treeList, key);
    }

    private List<Tree<String>> listViewColumn(String identifier, String key, DataSourcePlugin dataSourcePlugin) throws DmsException, SQLException {
        List<TreeNodeVO> treeList = new ArrayList<>();
        String catalog = parseIdentifier(identifier, 1);
        String schema = parseIdentifier(identifier, 2);
        String tableName = parseIdentifier(identifier, 3);
        TreeNodeVO parent = new TreeNodeVO();
        parent.setIdentifier(identifier);
        parent.setKey(key);
        TreeNodeVO columnGroup = getGroupNode(parent, COLUMN);
        List<TreeNodeVO> columns = listColumnNode(catalog, schema, tableName, dataSourcePlugin, columnGroup);
        if (CollectionUtil.isNotEmpty(columns)) {
            treeList.add(columnGroup);
            treeList.addAll(columns);
        }
        return buildTreeList(treeList, key);
    }

    private List<Tree<String>> listTableChild(String identifier, String key, DataSourcePlugin dataSourcePlugin) throws DmsException, SQLException {
        String catalog = parseIdentifier(identifier, 1);
        String schema = parseIdentifier(identifier, 2);
        String tableName = parseIdentifier(identifier, 3);
        List<TreeNodeVO> treeList = new ArrayList<>();
        TreeNodeVO parent = new TreeNodeVO();
        parent.setIdentifier(identifier);
        parent.setKey(key);
        // init group node
        TreeNodeVO columnGroup = getGroupNode(parent, COLUMN);
        columnGroup.setOrder("01");
        TreeNodeVO pkGroup = getGroupNode(parent, PK);
        pkGroup.setOrder("02");
        TreeNodeVO fkGroup = getGroupNode(parent, FK);
        fkGroup.setOrder("03");
        TreeNodeVO indexGroup = getGroupNode(parent, INDEX);
        indexGroup.setOrder("04");
        // add columns node
        List<TreeNodeVO> columns = listColumnNode(catalog, schema, tableName, dataSourcePlugin, columnGroup);
        if (CollectionUtil.isNotEmpty(columns)) {
            treeList.add(columnGroup);
            treeList.addAll(columns);
        }
        // add indexes node
        List<TreeNodeVO> indexes = listIndexesNode(catalog, schema, tableName, dataSourcePlugin, indexGroup);
        if (CollectionUtil.isNotEmpty(indexes)) {
            treeList.add(indexGroup);
            treeList.addAll(indexes);
        }
        // add pks node
        List<TreeNodeVO> pks = listPkNode(catalog, schema, tableName, dataSourcePlugin, pkGroup);
        if (CollectionUtil.isNotEmpty(pks)) {
            treeList.add(pkGroup);
            treeList.addAll(pks);
        }
        // add fks node
        List<TreeNodeVO> fks = listFkNode(catalog, schema, tableName, dataSourcePlugin, fkGroup);
        if (CollectionUtil.isNotEmpty(fks)) {
            treeList.add(fkGroup);
            treeList.addAll(fks);
        }
        return buildTreeList(treeList, key);
    }

    private List<TreeNodeVO> listColumnNode(String catalog, String schema, String tableName, DataSourcePlugin dataSourcePlugin, TreeNodeVO parent) throws SQLException {
        List<ColumnDTO> columnList = dataSourcePlugin.listColumnsByTable(catalog, schema, tableName);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(columnList)) {
            for (ColumnDTO column : columnList) {
                TreeNodeVO node = column.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return treeList;
    }

    private List<TreeNodeVO> listIndexesNode(String catalog, String schema, String tableName, DataSourcePlugin dataSourcePlugin, TreeNodeVO parent) throws SQLException {
        List<IndexDTO> indexList = dataSourcePlugin.listIndexes(catalog, schema, tableName);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(indexList)) {
            for (IndexDTO index : indexList) {
                TreeNodeVO node = index.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return treeList;
    }

    private List<TreeNodeVO> listPkNode(String catalog, String schema, String tableName, DataSourcePlugin dataSourcePlugin, TreeNodeVO parent) throws SQLException {
        List<ObjectDTO> pkList = dataSourcePlugin.listPkByTable(catalog, schema, tableName);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(pkList)) {
            for (ObjectDTO obj : pkList) {
                TreeNodeVO node = obj.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return treeList;
    }

    private List<TreeNodeVO> listFkNode(String catalog, String schema, String tableName, DataSourcePlugin dataSourcePlugin, TreeNodeVO parent) throws SQLException {
        List<ObjectDTO> fkList = dataSourcePlugin.listFkByTable(catalog, schema, tableName);
        List<TreeNodeVO> treeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(fkList)) {
            for (ObjectDTO obj : fkList) {
                TreeNodeVO node = obj.toTreeNodeVO(parent);
                treeList.add(node);
            }
        }
        return treeList;
    }

    private String parseIdentifier(String identifier, int position) throws DmsException {
        if (StrUtil.isEmpty(identifier)) {
            return null;
        }
        String[] params = identifier.split("\\" + Constants.SEPARATOR_DOT);
        if (params.length < position) {
            throw new DmsException(ERROR_INVALID_ARGUMENT.getValue(), I18nUtil.get(ERROR_INVALID_ARGUMENT.getLabel()));
        } else {
            return params[position - 1];
        }
    }

    public DataSourcePlugin getDataSourcePluginInstance(DataSourceDTO dataSourceDTO) {
        if (Objects.nonNull(dataSourceDTO)) {
            DataSourceDTO ds = new DataSourceDTO();
            BeanUtil.copyProperties(dataSourceDTO, ds);
            String decodePwd = Base64.decodeStr(ds.getPassword());
            ds.setPassword(decodePwd);
            return DataSourcePluginManager.newInstance(
                    StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, ds.getDatasourceType().getValue()).toUpperCase(),
                    ds.toProperties()
            );
        }
        return null;
    }

    @Override
    public List<String> listObjectType(DataSourceDTO dataSourceDTO) throws DmsException {
        DataSourcePlugin dataSourcePlugin = getDataSourcePluginInstance(dataSourceDTO);
        if (Objects.isNull(dataSourcePlugin)) {
            return null;
        }
        try {
            return dataSourcePlugin.listObjectTypes();
        } catch (Exception e) {
            throw new DmsException(ResponseCode.ERROR_CUSTOM.getValue(), I18nUtil.get("response.error.datasource.catalog"));
        }

    }

    private TreeNodeVO getGroupNode(TreeNodeVO parent, DbObjectType type) {
        String parentKey = parent.getKey();
        String keyStr = type.name().toLowerCase();
        TreeNodeVO objectGroupNode = new TreeNodeVO();
        GroupDTO groupInfo = getGroupInfo(type);
        objectGroupNode.setKey(DigestUtil.md5Hex(parentKey + keyStr));
        objectGroupNode.setTitle(groupInfo.getGroupLabel());
        objectGroupNode.setType(groupInfo.getGroupType());
        objectGroupNode.setIdentifier(parent.getIdentifier());
        objectGroupNode.setParentKey(parentKey);
        objectGroupNode.setOrder(groupInfo.getGroupOrder());
        objectGroupNode.setIsLeaf(false);
        return objectGroupNode;
    }

    private List<Tree<String>> buildTreeList(List<TreeNodeVO> treeNodeList, String rootKey) {
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("key");
        config.setNameKey("title");
        return TreeUtil.build(treeNodeList, rootKey, config, (node, tree) -> {
            tree.setId(node.getKey());
            tree.setName(node.getTitle());
            tree.setParentId(node.getParentKey());
            tree.setWeight(node.getOrder());
            tree.putExtra("type", node.getType());
            tree.putExtra("isLeaf", node.getIsLeaf());
            tree.putExtra("identifier", node.getIdentifier());
        });
    }

    private List<TreeNodeVO> getAllObjectGroupNode(DataSourceDTO dataSourceDTO, TreeNodeVO parent) throws DmsException {
        List<String> typeList = this.listObjectType(dataSourceDTO);
        List<TreeNodeVO> treeNodeList = new ArrayList<>();
        for (String type : typeList) {
            TreeNodeVO nodeVO = new TreeNodeVO();
            String keyStr = type.toLowerCase();
            GroupDTO groupInfo = getGroupInfo(DbObjectType.valueOf(type.toUpperCase()));
            nodeVO.setKey(DigestUtil.md5Hex(parent.getKey() + keyStr));
            nodeVO.setTitle(groupInfo.getGroupLabel());
            nodeVO.setType(groupInfo.getGroupType());
            nodeVO.setIdentifier(parent.getIdentifier());
            nodeVO.setParentKey(parent.getKey());
            nodeVO.setOrder(groupInfo.getGroupOrder());
            nodeVO.setIsLeaf(false);
            treeNodeList.add(nodeVO);
        }
        return treeNodeList;
    }

    private GroupDTO getGroupInfo(DbObjectType type) {
        switch (type) {
            case DATABASE:
                return new GroupDTO(G_DATABASE.name(), I18nUtil.get("dms.meta.catalog.database"), G_DATABASE.name(), "0");
            case SCHEMA:
                return new GroupDTO(G_SCHEMA.name(), I18nUtil.get("dms.meta.catalog.schema"), G_SCHEMA.name(), "0");
            case TABLE:
                return new GroupDTO(G_TABLE.name(), I18nUtil.get("dms.meta.catalog.table"), G_TABLE.name(), "01");
            case VIEW:
                return new GroupDTO(G_VIEW.name(), I18nUtil.get("dms.meta.catalog.view"), G_VIEW.name(), "02");
            case MATERIALIZED_VIEW:
                return new GroupDTO(G_MATERIALIZED_VIEW.name(), I18nUtil.get("dms.meta.catalog.materializedView"), G_MATERIALIZED_VIEW.name(), "03");
            case INDEX:
                return new GroupDTO(G_INDEX.name(), I18nUtil.get("dms.meta.catalog.index"), G_INDEX.name(), "04");
            case FUNCTION:
                return new GroupDTO(G_FUNCTION.name(), I18nUtil.get("dms.meta.catalog.function"), G_FUNCTION.name(), "05");
            case SEQUENCE:
                return new GroupDTO(G_SEQUENCE.name(), I18nUtil.get("dms.meta.catalog.sequence"), G_SEQUENCE.name(), "06");
            case FOREIGN_TABLE:
                return new GroupDTO(G_FOREIGN_TABLE.name(), I18nUtil.get("dms.meta.catalog.foreignTable"), G_FOREIGN_TABLE.name(), "07");
            case COLUMN:
                return new GroupDTO(G_TABLE_COLUMN.name(), I18nUtil.get("dms.meta.catalog.table.column"), G_TABLE_COLUMN.name(), "08");
            case PK:
                return new GroupDTO(G_TABLE_PK.name(), I18nUtil.get("dms.meta.catalog.table.pk"), G_TABLE_PK.name(), "09");
            case FK:
                return new GroupDTO(G_TABLE_FK.name(), I18nUtil.get("dms.meta.catalog.table.fk"), G_TABLE_FK.name(), "10");
            default:
                return new GroupDTO(G_DEFAULT.name(), I18nUtil.get("dms.meta.catalog.object"), G_DEFAULT.name(), "99");
        }
    }

}
