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
import com.basedt.dms.plugins.datasource.FunctionHandler;
import com.basedt.dms.plugins.datasource.dto.FunctionDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JdbcFunctionHandler implements FunctionHandler {

    protected DataSource dataSource;

    protected Map<String, String> config;

    @Override
    public void initialize(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.config = config;
    }

    @Override
    public List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> functions = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getFunctions(catalog, schemaPattern, functionPattern);
        while (rs.next()) {
            FunctionDTO function = new FunctionDTO();
            function.setCatalogName(rs.getString("FUNCTION_CAT"));
            function.setSchemaName(rs.getString("FUNCTION_SCHEM"));
            function.setObjectName(rs.getString("FUNCTION_NAME"));
            function.setRemark(rs.getString("REMARKS"));
            function.setObjectType(DbObjectType.FUNCTION.name());
            functions.add(function);
        }
        JdbcUtil.close(conn, rs);
        return functions;
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        return listFunctions(catalog, schemaPattern, functionPattern);
    }

    @Override
    public FunctionDTO getFunctionDetail(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> functions = listFunctionDetails(catalog, schemaPattern, functionPattern);
        if (CollectionUtil.isNotEmpty(functions)) {
            return functions.get(0);
        } else {
            return null;
        }
    }

    @Override
    public String getFunctionDDL(String catalog, String schema, String functionName) throws SQLException {
        FunctionDTO function = getFunctionDetail(catalog, schema, functionName);
        if (Objects.isNull(function)) {
            return "";
        } else {
            return function.getSourceCode();
        }
    }

    protected List<FunctionDTO> listFunctionFromDB(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<FunctionDTO> result = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            FunctionDTO function = new FunctionDTO();
            function.setCatalogName(rs.getString("catalog_name"));
            function.setSchemaName(rs.getString("schema_name"));
            function.setObjectName(rs.getString("object_name"));
            function.setObjectType(rs.getString("object_type"));
            function.setSourceCode(rs.getString("source_code"));
            function.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            function.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            result.add(function);
        }
        JdbcUtil.close(conn, ps, rs);
        return result;
    }
}
