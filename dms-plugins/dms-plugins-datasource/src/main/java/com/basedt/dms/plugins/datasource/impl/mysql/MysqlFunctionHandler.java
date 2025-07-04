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
import com.basedt.dms.plugins.datasource.dto.FunctionDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcFunctionHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MysqlFunctionHandler extends JdbcFunctionHandler {

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " r.routine_schema as schema_name," +
                " r.routine_name as object_name," +
                " 'FUNCTION' as object_type," +
                " r.routine_definition as source_code," +
                " r.created as create_time," +
                " r.last_altered as last_ddl_time," +
                " r.routine_comment as remark" +
                " from information_schema.routines r" +
                " where 1=1 ";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and r.routine_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and r.routine_name = '" + functionPattern + "'";
        }
        return super.listFunctionFromDB(sql);
    }

    @Override
    public String getFunctionDDL(String catalog, String schema, String functionName) throws SQLException {
        String ddl = "";
        String sql = StrUtil.format("show create function {}.{}", schema, functionName);
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        while (rs.next()) {
            ddl = rs.getString(3);
        }
        JdbcUtil.close(conn, ps, rs);
        return ddl;
    }
}
