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

package com.basedt.dms.plugins.datasource.impl.oracle;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcIndexHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.FK;
import static com.basedt.dms.plugins.datasource.enums.DbObjectType.PK;

public class OracleIndexHandler extends JdbcIndexHandler {

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName, String indexName) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    i.owner as schema_name," +
                "    i.index_name as object_name," +
                "    'INDEX' as object_type," +
                "    i.table_name as table_name," +
                "    i.index_type as index_type," +
                "    decode(i.uniqueness,'UNIQUE',1,'NONUNIQUE',0,0) as is_uniqueness," +
                "    ic.columns as columns," +
                "    d.bytes as index_bytes," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_indexes i" +
                " on o.owner = i.owner" +
                " and o.object_name = i.index_name" +
                " and o.object_type = 'INDEX'" +
                " left join dba_segments d" +
                " on o.owner = d.owner" +
                " and o.object_name = d.segment_name " +
                " left join (select ic.index_owner,ic.index_name,listagg(ic.column_name,',') within group (order by ic.column_position) as columns from all_ind_columns ic group by ic.index_owner,ic.index_name ) ic" +
                " on o.owner = ic.index_owner and o.object_name = ic.index_name" +
                " where o.owner = '" + schemaPattern.toUpperCase() + "'";
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and i.table_name = '" + tableName.toUpperCase() + "'";
        }
        if (StrUtil.isNotEmpty(indexName)) {
            sql += " and i.index_name = '" + indexName.toUpperCase() + "'";
        }
        return super.listIndexFromDB(sql);
    }

    @Override
    public String getIndexDdl(String catalog, String schema, String tableName, String indexName) throws SQLException {
        String ddl = "";
        String sql = "SELECT DBMS_METADATA.GET_DDL('INDEX', ?, ?) AS ddl FROM DUAL";
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, StrUtil.isEmpty(indexName) ? "" : indexName.toUpperCase());
        ps.setString(2, StrUtil.isEmpty(schema) ? "" : schema.toUpperCase());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ddl = rs.getString("ddl");
        }
        JdbcUtil.close(conn, ps, rs);
        return ddl;
    }

    @Override
    public List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return this.getConstraint(catalog, schemaPattern, tableName, "P");
    }

    @Override
    public List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return this.getConstraint(catalog, schemaPattern, tableName, "F");
    }

    /**
     * https://docs.oracle.com/en/database/oracle/oracle-database/21/refrn/ALL_CONSTRAINTS.html
     *
     * @param catalog
     * @param schemaPattern
     * @param tableName
     * @param type
     * @return
     * @throws SQLException
     */
    private List<ObjectDTO> getConstraint(String catalog, String schemaPattern, String tableName, String type) throws SQLException {
        List<ObjectDTO> list = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    o.object_type as object_type," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join  all_constraints t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.constraint_name" +
                " where o.owner = ?" +
                " and t.table_name = ? " +
                " and t.constraint_type = ? ";
        ;
        Connection conn = dataSource.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        pstm.setString(2, tableName.toUpperCase());
        pstm.setString(3, type.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ObjectDTO object = new ObjectDTO();
            object.setCatalogName(rs.getString("catalog_name"));
            object.setSchemaName(rs.getString("schema_name"));
            object.setObjectName(rs.getString("object_name"));
            if ("P".equals(type)) {
                object.setObjectType(PK.name());
            } else if ("F".equals(type)) {
                object.setObjectType(FK.name());
            } else {
                object.setObjectType(rs.getString("object_type"));
            }
            object.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            object.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            list.add(object);
        }
        JdbcUtil.close(conn, pstm, rs);
        return list;
    }
}
