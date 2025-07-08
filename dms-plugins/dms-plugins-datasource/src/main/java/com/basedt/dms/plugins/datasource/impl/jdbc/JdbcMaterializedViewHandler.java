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
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.MaterializedViewHandler;
import com.basedt.dms.plugins.datasource.dto.MaterializedViewDTO;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JdbcMaterializedViewHandler implements MaterializedViewHandler {

    protected DataSource dataSource;

    protected Map<String, String> config;

    @Override
    public void initialize(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.config = config;
    }

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return listMViewDetails(catalog, schemaPattern, mViewPattern);
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return List.of();
    }

    @Override
    public MaterializedViewDTO getMViewDetail(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> mvs = listMViewDetails(catalog, schemaPattern, mViewPattern);
        if (CollectionUtil.isNotEmpty(mvs)) {
            return mvs.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void dropMView(String schema, String mViewName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateDropSQL(schema, mViewName));
        }
    }

    @Override
    public void renameMView(String schema, String mViewName, String newName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateRenameSQL(schema, mViewName, newName));
        }
    }

    @Override
    public String getMViewDdl(String catalog, String schema, String mViewName) throws SQLException {
        if (StrUtil.isEmpty(mViewName)) {
            return "";
        }
        MaterializedViewDTO mv = getMViewDetail(catalog, schema, mViewName);
        if (Objects.nonNull(mv)) {
            StringBuilder ddlBuilder = new StringBuilder();
            ddlBuilder.append("CREATE MATERIALIZED VIEW ")
                    .append(mv.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(mv.getMViewName())
                    .append("\n AS \n");
            if (StrUtil.isNotEmpty(mv.getQuerySql())) {
                ddlBuilder.append(mv.getQuerySql());
            }
            return ddlBuilder.toString();
        } else {
            throw new SQLException(StrUtil.format("materialized view {} does not exist in {}", mViewName, schema));
        }
    }

    @Override
    public String getDropDDL(String schema, String mViewName) throws SQLException {
        return generateDropSQL(schema, mViewName);
    }

    @Override
    public String getRenameDDL(String schema, String mViewName, String newName) throws SQLException {
        return generateRenameSQL(schema, mViewName, newName);
    }

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
            matView.setQuerySql(rs.getString("query_sql"));
            matView.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            matView.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            result.add(matView);
        }
        JdbcUtil.close(conn, ps, rs);
        return result;
    }

    protected String generateDropSQL(String schema, String mViewName) {
        return StrUtil.format("DROP MATERIALIZED VIEW {}.{}", schema, mViewName);
    }

    protected String generateRenameSQL(String schema, String mViewName, String newName) {
        return StrUtil.format("ALTER MATERIALIZED VIEW {}.{} RENAME TO {}", schema, mViewName, newName);
    }

    protected String generateDistributedSQL(String schema, String tableName) throws SQLException {
        return "";
    }

}
