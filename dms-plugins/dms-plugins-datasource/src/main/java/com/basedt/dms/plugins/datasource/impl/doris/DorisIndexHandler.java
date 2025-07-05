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
package com.basedt.dms.plugins.datasource.impl.doris;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcIndexHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DorisIndexHandler extends JdbcIndexHandler {

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName, String indexName) throws SQLException {
        List<IndexDTO> indexList = new ArrayList<>();
        DorisCatalogHandler handler = new DorisCatalogHandler();
        handler.initialize(this.dataSource, new HashMap<>(), catalog);
        if (handler.isInternalCatalog(catalog)) {
            String sql = "show index from " + StrUtil.concat(true, schemaPattern, Constants.SEPARATOR_DOT, tableName);
            Connection conn = dataSource.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                IndexDTO index = new IndexDTO();
                index.setCatalogName(catalog);
                index.setSchemaName(schemaPattern);
                index.setTableName(tableName);
                index.setObjectName(rs.getString("key_name"));
                index.setIndexType(rs.getString("index_type"));
                index.setColumns(rs.getString("column_name"));
                index.setIsUniqueness(false);
                if (StrUtil.isNotEmpty(indexName) && indexName.equals(index.getIndexName())) {
                    indexList.add(index);
                } else if (StrUtil.isEmpty(indexName)) {
                    indexList.add(index);
                }
            }
            JdbcUtil.close(conn, st, rs);
        }
        return indexList;
    }

    @Override
    protected String generateDropSQL(String schema, String tableName, String indexName) {
        return StrUtil.format("DROP INDEX {} ON {}.{}", indexName, schema, tableName);
    }

    @Override
    protected String generateRenameSQL(String schema, String tableName, String indexName, String newName) {
        throw new UnsupportedOperationException("rename index not supported");
    }

    @Override
    public String getIndexDdl(String catalog, String schema, String tableName, String indexName) throws SQLException {
        if (StrUtil.isEmpty(indexName)) {
            return "";
        } else {
            IndexDTO index = getIndexDetail(catalog, schema, tableName, indexName);
            if (Objects.nonNull(index)) {
                StringBuilder builder = new StringBuilder();
                builder.append("CREATE INDEX IF NOT EXISTS ")
                        .append(index.getIndexName())
                        .append(" ON ")
                        .append(index.getSchemaName())
                        .append(Constants.SEPARATOR_DOT)
                        .append(index.getTableName())
                        .append(" (")
                        .append(index.getColumns())
                        .append(")")
                        .append(StrUtil.isNotEmpty(index.getIndexType()) ? " USING " + index.getIndexType() : "")
                        .append(";");
                return builder.toString();
            } else {
                throw new SQLException(StrUtil.format("index {} does not exist in {}", indexName, schema));
            }
        }
    }
}
