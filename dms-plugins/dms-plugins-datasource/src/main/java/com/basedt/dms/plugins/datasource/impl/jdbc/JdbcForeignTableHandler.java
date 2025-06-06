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
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.ForeignTableHandler;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcForeignTableHandler extends JdbcTableHandler implements ForeignTableHandler {

    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return listTableDetails(catalog, schemaPattern, tablePattern, DbObjectType.FOREIGN_TABLE);
    }

    @Override
    protected String generateDropSQL(String schema, String tableName) {
        return StrUtil.format("DROP FOREIGN TABLE {}.{}", schema, tableName);
    }

    @Override
    protected String generateRenameSQL(String schema, String tableName, String newName) {
        return StrUtil.format("ALTER FOREIGN TABLE {}.{} RENAME TO {}", schema, tableName, newName);
    }

    protected List<TableDTO> listFgnTableFromDB(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<TableDTO> result = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            TableDTO tableDTO = new TableDTO();
            tableDTO.setCatalogName(rs.getString("catalog_name"));
            tableDTO.setSchemaName(rs.getString("schema_name"));
            tableDTO.setObjectName(rs.getString("object_name"));
            tableDTO.setObjectType(rs.getString("object_type"));
            tableDTO.setRemark(rs.getString("remark"));
            tableDTO.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            tableDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            result.add(tableDTO);
        }
        JdbcUtil.close(conn, ps, rs);
        return result;
    }

}
