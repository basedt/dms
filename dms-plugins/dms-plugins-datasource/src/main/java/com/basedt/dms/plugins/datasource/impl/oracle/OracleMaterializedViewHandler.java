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
import cn.hutool.db.sql.SqlUtil;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.dto.MaterializedViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcMaterializedViewHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OracleMaterializedViewHandler extends JdbcMaterializedViewHandler {

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    'MATERIALIZED_VIEW' as object_type," +
                "    t.mview_name as table_name," +
                "    null as query_sql," +
                "    null as remark," +
                "    null as data_bytes," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_mviews t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.mview_name" +
                " where o.object_type = 'MATERIALIZED VIEW'" +
                " and o.owner = '" + schemaPattern.toUpperCase() + "'";
        if (StrUtil.isNotEmpty(mViewPattern)) {
            sql += " and o.object_name = '" + mViewPattern.toUpperCase() + "'";
        }
        return listMViewFromDB(sql);
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    'MATERIALIZED_VIEW' as object_type," +
                "    t.mview_name as table_name," +
                "    t.query as query_sql," +
                "    d.bytes as data_bytes," +
                "    c.comments as remark," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_mviews t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.mview_name" +
                " left join dba_segments d" +
                " on o.owner = d.owner" +
                " and o.object_name = d.segment_name" +
                " left join all_tab_comments c" +
                " on t.owner = c.owner" +
                " and t.mview_name = c.table_name " +
                " where o.object_type = 'MATERIALIZED VIEW'" +
                " and o.owner = '" + schemaPattern.toUpperCase() + "'";
        if (StrUtil.isNotEmpty(mViewPattern)) {
            sql += " and o.object_name = '" + mViewPattern.toUpperCase() + "'";
        }
        return listMViewFromDB(sql);
    }

    @Override
    protected List<MaterializedViewDTO> listMViewFromDB(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<MaterializedViewDTO> result = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            MaterializedViewDTO matView = new MaterializedViewDTO();
            matView.setCatalogName(rs.getString("catalog_name"));
            matView.setSchemaName(rs.getString("schema_name"));
            matView.setObjectName(rs.getString("object_name"));
            matView.setObjectType(rs.getString("object_type"));
            matView.setRemark(rs.getString("remark"));
            matView.setDataBytes(rs.getLong("data_bytes"));
            Clob clob = rs.getClob("query_sql");
            if (Objects.nonNull(clob)) {
                matView.setQuerySql(SqlUtil.clobToStr(clob));
            } else {
                matView.setQuerySql("");
            }
            matView.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            matView.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            result.add(matView);
        }
        JdbcUtil.close(conn, ps, rs);
        return result;
    }

    @Override
    public void renameMView(String schema, String mViewName, String newName) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getMViewDdl(String catalog, String schema, String mViewName) throws SQLException {
        String ddl = "";
        String sql = "SELECT DBMS_METADATA.GET_DDL('MATERIALIZED_VIEW', ?, ?) AS ddl FROM DUAL";
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, StrUtil.isEmpty(mViewName) ? "" : mViewName.toUpperCase());
        ps.setString(2, StrUtil.isEmpty(schema) ? "" : schema.toUpperCase());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ddl = rs.getString("ddl");
        }
        JdbcUtil.close(conn, ps, rs);
        return ddl;
    }
}
