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

    List<String> STD_SQL_TYPES_AND_ALIAS = Arrays.asList("char", "varchar", "text", "nchar", "bigint", "int", "integer", "smallint", "decimal", "numeric", "float", "real", "double precision", "date", "time", "timestamp", "bit", "boolean");

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

    DataSource getDataSource();

    Boolean testConnection() throws Exception;

    CatalogHandler getCatalogHandler();

    TableHandler getTableHandler();

    ViewHandler getViewHandler();

    ForeignTableHandler getForeignTableHandler();

    FunctionHandler getFunctionHandler();

    IndexHandler getIndexHandler();

    MaterializedViewHandler getMaterializedViewHandler();

    SequenceHandler getSequenceHandler();

    Boolean isSupportRowEdit();

    void execute(String sql) throws SQLException;

    void insertBatch(ArrowStreamReader reader, String catalog, String schemaName, String tableName) throws SQLException, IOException;

}
