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
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.mysql.MysqlTableHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.TABLE;

/**
 * https://doris.apache.org/zh-CN/docs/lakehouse/database/jdbc
 */
public class DorisTableHandler extends MysqlTableHandler {

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        List<TableDTO> list = new ArrayList<>();
        DorisCatalogHandler handler = new DorisCatalogHandler();
        handler.initialize(this.dataSource, new HashMap<>(), catalog);
        if (!handler.isInternalCatalog(catalog)) {
            String sql = StrUtil.format("show tables from {}.{};", catalog, schemaPattern);
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TableDTO tableDTO = new TableDTO();
                    tableDTO.setCatalogName(catalog);
                    tableDTO.setSchemaName(schemaPattern);
                    tableDTO.setObjectName(rs.getString(1));
                    tableDTO.setObjectType(DbObjectType.TABLE.name());
                    if (Objects.nonNull(tablePattern) && tableDTO.getObjectName().startsWith(tablePattern)) {
                        list.add(tableDTO);
                    } else if (Objects.isNull(tablePattern)) {
                        list.add(tableDTO);
                    }
                }
            }
            return list;
        } else {
            return listTableDetails(catalog, schemaPattern, tablePattern, TABLE);
        }

    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        String sql = "select " +
                " t.table_catalog as catalog_name," +
                " t.table_schema as schema_name," +
                " t.table_name as object_name," +
                " case when t.table_type = 'SYSTEM VIEW' then 'VIEW' when t.table_type = 'BASE TABLE' then 'TABLE' else t.table_type end as object_type," +
                " t.table_rows as table_rows," +
                " t.data_length as data_bytes," +
                " t.table_comment as remark," +
                " t.create_time as create_time, " +
                " t.create_time as last_ddl_time," +
                " t.update_time as last_access_time" +
                " from information_schema.tables t " +
                " where t.table_type = 'BASE TABLE'";
        if (StrUtil.isNotEmpty(catalog)) {
            sql += "and t.table_catalog = '" + catalog + "'";
        }
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and t.table_name = '" + tablePattern + "'";
        }
        return super.listTableFromDB(sql);
    }

    @Override
    protected String generateRenameSQL(String schema, String tableName, String newName) {
        return StrUtil.format("ALTER TABLE {}.{} RENAME {}", schema, tableName, newName);
    }

    @Override
    protected List<ColumnDTO> listColumnFromTable(String sql) throws SQLException {
        return super.listColumnFromTable(sql);
    }
}
