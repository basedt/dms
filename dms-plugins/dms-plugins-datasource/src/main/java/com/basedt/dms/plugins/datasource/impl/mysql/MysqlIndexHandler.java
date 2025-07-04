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

package com.basedt.dms.plugins.datasource.impl.mysql;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcIndexHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.FK;
import static com.basedt.dms.plugins.datasource.enums.DbObjectType.PK;

public class MysqlIndexHandler extends JdbcIndexHandler {

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName, String indexName) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " t.index_schema as schema_name," +
                " t.index_name as object_name," +
                " 'INDEX' as object_type," +
                " t.index_type as index_type," +
                " t.table_name as table_name, " +
                " case when max(t.non_unique) >=1 then 0 else 1 end as is_uniqueness," +
                " group_concat(t.column_name order by t.seq_in_index) as columns," +
                " null as index_bytes," +
                " null as create_time," +
                " null as last_ddl_time" +
                " from information_schema.statistics t" +
                " where 1 = 1";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.index_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and t.table_name = '" + tableName + "'";
        }
        if (StrUtil.isNotEmpty(indexName)) {
            sql += " and t.index_name = '" + indexName + "'";
        }
        sql += " group by t.index_schema,t.index_name,t.index_type,t.table_name";
        return super.listIndexFromDB(sql);
    }

    @Override
    public List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return getConstraint(catalog, schemaPattern, tableName, PK);
    }

    @Override
    public List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return getConstraint(catalog, schemaPattern, tableName, FK);
    }

    private List<ObjectDTO> getConstraint(String catalog, String schemaPattern, String tableName, DbObjectType type) throws SQLException {
        List<ObjectDTO> constraints = new ArrayList<>();
        String constraintType = "";
        if (PK.equals(type)) {
            constraintType = "PRIMARY KEY";
        } else if (FK.equals(type)) {
            constraintType = "FOREIGN KEY";
        }
        String sql = "select " +
                " null as catalog_name," +
                " t.constraint_schema as schema_name," +
                " t.constraint_name as object_name," +
                " t.table_name as table_name," +
                " t.constraint_type " +
                " from information_schema.table_constraints t" +
                " where t.constraint_type = '" + constraintType + "'";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.constraint_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and t.table_name = '" + tableName + "'";
        }
        Connection conn = dataSource.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ObjectDTO obj = new ObjectDTO();
            obj.setCatalogName(rs.getString("catalog_name"));
            obj.setSchemaName(rs.getString("schema_name"));
            obj.setObjectName(rs.getString("object_name"));
            obj.setObjectType(type.name());
            constraints.add(obj);
        }
        JdbcUtil.close(conn, pstm, rs);
        return constraints;
    }

    @SneakyThrows
    @Override
    protected String generateDropSQL(String schema, String tableName, String indexName) {
        return StrUtil.format("DROP INDEX {} ON {}.{}", indexName, schema, tableName);
    }

    @Override
    public void renameIndex(String schema, String tableName, String indexName, String newName) throws SQLException {
        throw new UnsupportedOperationException("rename index not supported");
    }
}
