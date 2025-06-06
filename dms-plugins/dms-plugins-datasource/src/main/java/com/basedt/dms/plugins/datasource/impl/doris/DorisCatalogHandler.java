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

import com.basedt.dms.plugins.datasource.dto.CatalogDTO;
import com.basedt.dms.plugins.datasource.dto.SchemaDTO;
import com.basedt.dms.plugins.datasource.impl.mysql.MysqlCatalogHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.INDEX;
import static com.basedt.dms.plugins.datasource.enums.DbObjectType.MATERIALIZED_VIEW;

public class DorisCatalogHandler extends MysqlCatalogHandler {

    @Override
    public List<CatalogDTO> listCatalogs() throws SQLException {
        List<CatalogDTO> list = new ArrayList<>();
        String sql = "select distinct catalogid,catalogname from catalogs()";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String catalogName = rs.getString("catalogname");
                CatalogDTO catalogDTO = new CatalogDTO(catalogName);
                list.add(catalogDTO);
            }
        }
        return list;
    }

    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        List<SchemaDTO> list = new ArrayList<>();
        String sql = "show databases from " + catalog;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String schemaName = rs.getString("database");
                if (!("__internal_schema".equals(schemaName) ||
                        "performance_schema".equals(schemaName) ||
                        "information_schema".equals(schemaName) ||
                        "mysql".equals(schemaName)
                )) {
                    list.add(new SchemaDTO(schemaName));
                }
            }
        }
        return list;
    }

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> objectTypes = super.listObjectTypes();
//        objectTypes.add(FOREIGN_TABLE.name());
        objectTypes.add(MATERIALIZED_VIEW.name());
        return objectTypes.stream().filter(s -> {
            if (INDEX.name().equalsIgnoreCase(s)) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());
    }

    protected boolean isInternalCatalog(String catalog) throws SQLException {
        if (Objects.isNull(catalog)) {
            return false;
        }
        boolean flag = false;
        String sql = "select distinct catalogid,catalogname from catalogs() where catalogtype = 'internal' and catalogname = ? ";
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, catalog);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            flag = true;
        }
        JdbcUtil.close(conn, ps, rs);
        return flag;
    }
}
