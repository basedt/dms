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

package com.basedt.dms.plugins.datasource.impl.jdbc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.IndexHandler;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdbcIndexHandler implements IndexHandler {

    protected DataSource dataSource;

    protected Map<String, String> config;

    @Override
    public void initialize(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.config = config;
    }

    @Override
    public List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException {
        return listIndexDetails(catalog, schemaPattern, tableName);
    }

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public IndexDTO getIndexDetail(String catalog, String schemaPattern, String tableName) throws SQLException {
        List<IndexDTO> indexes = listIndexDetails(catalog, schemaPattern, tableName);
        if (CollectionUtil.isNotEmpty(indexes)) {
            return indexes.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public void dropIndex(String schema, String indexName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateDropSQL(schema, indexName));
        }
    }

    @Override
    public void renameIndex(String schema, String indexName, String newName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateRenameSQL(schema, indexName, newName));
        }
    }

    protected List<IndexDTO> listIndexFromDB(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<IndexDTO> result = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            IndexDTO index = new IndexDTO();
            index.setCatalogName(rs.getString("catalog_name"));
            index.setSchemaName(rs.getString("schema_name"));
            index.setObjectName(rs.getString("object_name"));
            index.setObjectType(rs.getString("object_type"));
            index.setTableName(rs.getString("table_name"));
            index.setIndexBytes(rs.getLong("index_bytes"));
            index.setIndexType(rs.getString("index_type"));
            index.setIsUniqueness(rs.getBoolean("is_uniqueness"));
            index.setColumns(rs.getString("columns"));
            index.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            index.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            result.add(index);
        }
        JdbcUtil.close(conn, ps, rs);
        return result;
    }

    protected String generateDropSQL(String schema, String indexName) {
        return StrUtil.format("DROP INDEX {}.{}", schema, indexName);
    }

    protected String generateRenameSQL(String schema, String indexName, String newName) {
        return StrUtil.format("ALTER INDEX {}.{} RENAME TO {}", schema, indexName, newName);
    }

}
