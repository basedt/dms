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

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.ViewHandler;
import com.basedt.dms.plugins.datasource.dto.ViewDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JdbcViewHandler implements ViewHandler {

    protected DataSource dataSource;

    protected Map<String, String> config;

    @Override
    public void initialize(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.config = config;
    }

    @Override
    public ViewDTO getViewDetail(String catalog, String schema, String viewName) throws SQLException {
        List<ViewDTO> list = listViewDetails(catalog, schema, viewName);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public List<ViewDTO> listViews(String catalog, String schema, String viewName) throws SQLException {
        List<ViewDTO> list = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(catalog, schema, viewName, new String[]{"VIEW"});
        while (rs.next()) {
            ViewDTO view = new ViewDTO();
            view.setCatalogName(rs.getString("TABLE_CAT"));
            view.setSchemaName(rs.getString("TABLE_SCHEM"));
            view.setViewName(rs.getString("TABLE_NAME"));
            view.setObjectType(DbObjectType.VIEW.name());
            view.setRemark(rs.getString("REMARKS"));
            list.add(view);
        }
        JdbcUtil.close(conn, rs);
        return list;

    }

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schema, String viewName) throws SQLException {
        return listViews(catalog, schema, viewName);
    }

    @Override
    public void dropView(String schema, String viewName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateDropSQL(schema, viewName));
        }
    }

    @Override
    public void renameView(String schema, String viewName, String newName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateRenameSQL(schema, viewName, newName));
        }
    }

    @Override
    public String getViewDDL(String catalog, String schema, String viewName) throws SQLException {
        if (StrUtil.isEmpty(viewName)) {
            return "";
        }
        ViewDTO viewInfo = getViewDetail(catalog, schema, viewName);
        if (Objects.nonNull(viewInfo)) {
            StringBuilder ddlBuilder = new StringBuilder();
            ddlBuilder.append("CREATE OR REPLACE VIEW ")
                    .append(viewInfo.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(viewInfo.getViewName())
                    .append("\n AS \n");
            if (StrUtil.isNotEmpty(viewInfo.getQuerySql())) {
                ddlBuilder.append(viewInfo.getQuerySql());
            }
            return ddlBuilder.toString();
        } else {
            throw new SQLException(StrUtil.format("view {} does not exist in {}", viewName, schema));
        }
    }

    @Override
    public String getDropDDL(String schema, String viewName) throws SQLException {
        return generateDropSQL(schema, viewName);
    }

    @Override
    public String getRenameDDL(String schema, String viewName, String newName) throws SQLException {
        return generateRenameSQL(schema, viewName, newName);
    }

    protected String generateDropSQL(String schema, String viewName) {
        return StrUtil.format("DROP VIEW {}.{}", schema, viewName);
    }

    protected String generateRenameSQL(String schema, String viewName, String newName) {
        return StrUtil.format("ALTER VIEW {}.{} RENAME TO {}", schema, viewName, newName);
    }

    protected List<ViewDTO> listViewFromDB(String sql) throws SQLException {
        List<ViewDTO> result = new ArrayList<>();
        if (StrUtil.isBlank(sql)) {
            return result;
        }
        Connection conn = dataSource.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ViewDTO view = new ViewDTO();
            view.setCatalogName(rs.getString("catalog_name"));
            view.setSchemaName(rs.getString("schema_name"));
            view.setObjectName(rs.getString("object_name"));
            view.setObjectType(rs.getString("object_type"));
            view.setRemark(rs.getString("remark"));
            view.setQuerySql(rs.getString("query_sql"));
            view.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            view.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            result.add(view);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }
}
