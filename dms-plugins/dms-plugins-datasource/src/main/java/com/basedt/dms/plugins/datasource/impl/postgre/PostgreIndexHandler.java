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

package com.basedt.dms.plugins.datasource.impl.postgre;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcIndexHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

public class PostgreIndexHandler extends JdbcIndexHandler {

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName, String indexName) throws SQLException {
        String sql = " select" +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " 'INDEX'as object_type," +
                " i.tablename as table_name," +
                " pg_relation_size(pgi.indexrelid::regclass) as index_bytes," +
                " pgi.indisunique as is_uniqueness," +
                " a.columns as columns," +
                " null as index_type," +
                " null as create_time," +
                " null as last_ddl_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c" +
                " on n.oid = c.relnamespace" +
                " join pg_catalog.pg_indexes i" +
                " on n.nspname = i.schemaname" +
                " and c.relname = i.indexname" +
                " join pg_catalog.pg_index pgi" +
                " on c.oid = pgi.indexrelid" +
                " left join (" +
                "   SELECT" +
                "        n.nspname as schemaname," +
                "        t.relname AS tablename," +
                "        c.relname AS indexname," +
                "        string_agg(a.attname,',')  AS columns" +
                "    FROM" +
                "        pg_catalog.pg_index i" +
                "    JOIN" +
                "        pg_catalog.pg_class c ON c.oid = i.indexrelid" +
                "    join pg_catalog.pg_namespace n on c.relnamespace  = n.oid" +
                "    JOIN" +
                "        pg_catalog.pg_class t ON t.oid = i.indrelid" +
                "    JOIN" +
                "        pg_catalog.pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(i.indkey)" +
                "    group by n.nspname,t.relname,c.relname" +
                " ) a" +
                "                 on a.schemaname = n.nspname" +
                "                and a.tablename = i.tablename" +
                "                and a.indexname = c.relname" +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(INDEX) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and i.tablename = '" + tableName + "'";
        }
        if (StrUtil.isNotEmpty(indexName)) {
            sql += " and c.relname = '" + indexName + "'";
        }
        return super.listIndexFromDB(sql);
    }

    @Override
    public String getIndexDDL(String catalog, String schema, String tableName, String indexName) throws SQLException {
        List<ObjectDTO> pks = listPkByTable(catalog, schema, tableName);
        IndexDTO indexInfo = getIndexDetail(catalog, schema, tableName, indexName);
        if (!CollectionUtils.isEmpty(pks) && Objects.nonNull(indexInfo)) {
            for (ObjectDTO pk : pks) {
                if (pk.getObjectName().equalsIgnoreCase(indexName)) {
                    return StrUtil.format("ALTER TABLE {}.{} ADD CONSTRAINT {} PRIMARY KEY ({});", schema, tableName, indexInfo.getIndexName(), indexInfo.getColumns());
                }
            }
        }
        String ddl = "";
        String sql = "select pg_get_indexdef(format('%I.%I', ?, ?)::regclass) as ddl";
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, schema);
        ps.setString(2, indexName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ddl = rs.getString("ddl");

        }
        JdbcUtil.close(conn, ps, rs);
        return ddl + ";";
    }

    @Override
    public String getIndexDDL(IndexDTO index, List<ObjectDTO> pks, List<ObjectDTO> fks) {
        if (Objects.isNull(index)) {
            return "";
        }
        if (!CollectionUtils.isEmpty(pks)) {
            for (ObjectDTO pk : pks) {
                if (pk.getObjectName().equalsIgnoreCase(index.getIndexName())) {
                    return StrUtil.format("ALTER TABLE {}.{} ADD CONSTRAINT {} PRIMARY KEY ({});", index.getSchemaName(), index.getTableName(), index.getIndexName(), index.getColumns());
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE ")
                .append(index.getIsUniqueness() ? "UNIQUE INDEX " : "INDEX ")
                .append(index.getIndexName())
                .append(" ON ")
                .append(index.getSchemaName())
                .append(Constants.SEPARATOR_DOT)
                .append(index.getTableName())
                .append(StrUtil.isNotEmpty(index.getIndexType()) ? " USING " + index.getIndexType() : "")
                .append(" (")
                .append(index.getColumns())
                .append(");");
        return builder.toString();
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
        List<ObjectDTO> pkList = new ArrayList<>();
        String constraintType = "";
        if (PK.equals(type)) {
            constraintType = "PRIMARY KEY";
        } else if (FK.equals(type)) {
            constraintType = "FOREIGN KEY";
        }
        String sql = "select" +
                "    t.table_catalog," +
                "    t.table_schema," +
                "    t.table_name," +
                "    t.constraint_name," +
                "    t.constraint_type" +
                " from information_schema.table_constraints t" +
                " where t.constraint_type = '" + constraintType + "'";
        if (StrUtil.isNotEmpty(catalog)) {
            sql += " and t.table_catalog ='" + catalog + "'";
        }
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and t.table_name = '" + tableName + "'";
        }
        Connection conn = dataSource.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ObjectDTO obj = new ObjectDTO();
            obj.setCatalogName(rs.getString("table_catalog"));
            obj.setSchemaName(rs.getString("table_schema"));
            obj.setObjectName(rs.getString("constraint_name"));
            obj.setObjectType(type.name());
            pkList.add(obj);
        }
        JdbcUtil.close(conn, pstm, rs);
        return pkList;
    }
}
