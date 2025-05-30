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
package com.basedt.dms.plugins.datasource;

import cn.hutool.core.lang.tree.Tree;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.plugins.datasource.dto.CatalogDTO;
import com.basedt.dms.plugins.datasource.dto.DataSourceDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.dto.TypeInfoDTO;
import com.basedt.dms.plugins.datasource.enums.DmlType;

import java.sql.SQLException;
import java.util.List;

public interface MetaDataService {

    /**
     * catalog	  schema	database_objects
     */
    List<CatalogDTO> listSchemas(DataSourcePlugin dataSourcePlugin) throws DmsException;

    List<CatalogDTO> listSchemas(DataSourceDTO dataSourceDTO) throws DmsException;

    List<Tree<String>> listSchemasTree(DataSourceDTO dataSourceDTO) throws DmsException;

    List<String> listObjectType(DataSourceDTO dataSourceDTO) throws DmsException;

    List<Tree<String>> listChildNode(DataSourceDTO dataSourceDTO, String identifier, String key, String type) throws DmsException, SQLException;

    DataSourcePlugin getDataSourcePluginInstance(DataSourceDTO dataSourceDTO);

    List<TypeInfoDTO> listTypeInfo(DataSourceDTO dataSourceDTO) throws DmsException;

    TableDTO getTableInfo(DataSourceDTO dataSource, String catalog, String schemaName, String tableName) throws DmsException;

    String getTableDdl(DataSourceDTO dataSource, String catalog, String schemaName, String tableName) throws DmsException;

    void renameObject(DataSourceDTO dataSource, String catalog, String schemaName,String objectType, String objectName, String newName) throws DmsException;

    void dropObject(DataSourceDTO dataSource, String catalog, String schemaName, String objectName,String objectType) throws DmsException;

    String generateDml(DataSourceDTO dataSource, String catalog, String schemaName, String tableName, DmlType type) throws DmsException;

//    String getDdlScript(DataSourceDTO dataSourceDTO) throws DmsException;

//    String getDmlScript(DataSourceDTO dataSourceDTO) throws DmsException;

}
