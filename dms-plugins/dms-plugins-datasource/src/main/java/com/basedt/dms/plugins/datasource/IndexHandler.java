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

import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IndexHandler {

    void initialize(DataSource dataSource, Map<String, String> config);

    List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException;

    List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName, String indexName) throws SQLException;

    IndexDTO getIndexDetail(String catalog, String schemaPattern, String tableName, String indexName) throws SQLException;

    List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException;

    List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException;

    void dropIndex(String schema, String tableName, String indexName) throws SQLException;

    void renameIndex(String schema, String tableName, String indexName, String newName) throws SQLException;

    String getIndexDDL(String catalog, String schema, String tableName, String indexName) throws SQLException;

    String getIndexDDL(IndexDTO index,List<ObjectDTO> pks,List<ObjectDTO> fks);

    String getDropDDL(String schema, String tableName, String indexName) throws SQLException;

    String getDropDDL(IndexDTO index,List<ObjectDTO> pks,List<ObjectDTO> fks);

    String getRenameDDL(String schema, String tableName, String indexName, String newName) throws SQLException;

}
