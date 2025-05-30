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

import com.basedt.dms.plugins.core.Plugin;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import org.apache.arrow.vector.ipc.ArrowStreamReader;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface DataSourcePlugin extends Plugin {

    String JDBC = "jdbc";

    List<String> STD_SQL_TYPES_AND_ALIAS = Arrays.asList("char", "varchar","text","nchar", "bigint","int", "integer", "smallint", "decimal", "numeric", "float", "real", "double precision", "date", "time", "timestamp", "bit", "boolean");

    String getHostName();

    void setHostName(String hostName);

    Integer getPort();

    void setPort(Integer port);

    String getDriverClassName();

    void setDriverClassName(String driverClassName);

    String getDatabaseName();

    void setDatabaseName(String databaseName);

    String getUserName();

    void setUserName(String userName);

    String getPassword();

    void setPassword(String password);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    Connection getConnection();

    DataSource getDataSource();

    Boolean testConnection() throws SQLException, ClassNotFoundException;

    List<CatalogDTO> listCatalogs() throws SQLException;

    List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException;

    List<String> listObjectTypes() throws SQLException;

    List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern) throws SQLException;

    List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException;

    List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException;

    TableDTO getTableDetail(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException;

    List<ViewDTO> listViews(String catalog, String schemaPattern, String viewPattern) throws SQLException;

    List<ViewDTO> listViewDetails(String catalog, String schemaPattern, String viewPattern) throws SQLException;

    ViewDTO getViewDetail(String catalog, String schemaPattern, String viewPattern) throws SQLException;

    List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException;

    List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException;

    List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException;

    IndexDTO getIndexDetail(String catalog, String schemaPattern, String tableName) throws SQLException;

    List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException;

    List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException;

    MaterializedViewDTO getMViewDetail(String catalog, String schemaPattern, String mViewPattern) throws SQLException;

    List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException;

    List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException;

    SequenceDTO getSequenceDetail(String catalog, String schemaPattern, String sequencePattern) throws SQLException;

    List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException;

    List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException;

    FunctionDTO getFunctionDetail(String catalog, String schemaPattern, String functionPattern) throws SQLException;

    List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException;

    List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException;

    List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException;

    Boolean isSupportRowEdit();

    void execute(String sql) throws SQLException;

    void insertBatch(ArrowStreamReader reader, String catalog, String schemaName, String tableName) throws SQLException, IOException;

    Map<String, TypeInfoDTO> listDataType() throws SQLException;

    String renameTable(String catalog, String schemaPattern, String tableName,String newTableName);
}
