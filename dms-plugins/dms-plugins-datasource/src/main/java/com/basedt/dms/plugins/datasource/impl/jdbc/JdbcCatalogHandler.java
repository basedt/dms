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
import com.basedt.dms.plugins.datasource.CatalogHandler;
import com.basedt.dms.plugins.datasource.dto.CatalogDTO;
import com.basedt.dms.plugins.datasource.dto.SchemaDTO;
import com.basedt.dms.plugins.datasource.dto.TypeInfoDTO;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.basedt.dms.plugins.datasource.DataSourcePlugin.STD_SQL_TYPES_AND_ALIAS;

public class JdbcCatalogHandler implements CatalogHandler {

    protected DataSource dataSource;

    protected Map<String, String> config;

    protected String databaseName;

    @Override
    public void initialize(DataSource dataSource, Map<String, String> config, String databaseName) {
        this.dataSource = dataSource;
        this.config = config;
        this.databaseName = databaseName;
    }

    /**
     * get catalog list
     *
     * @return catalog list
     * @throws SQLException
     */
    @Override
    public List<CatalogDTO> listCatalogs() throws SQLException {
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getCatalogs();
        List<CatalogDTO> result = new ArrayList<>();
        while (rs.next()) {
            CatalogDTO catalog = new CatalogDTO(rs.getString("TABLE_CAT"));
            result.add(catalog);
        }
        JdbcUtil.close(conn, rs);
        if (CollectionUtil.isEmpty(result)) {
            result.add(new CatalogDTO(this.databaseName));
        }
        return result;
    }

    /**
     * list schemas with catalog and schema pattern
     * @param catalog
     * @param schemaPattern
     * @return
     * @throws SQLException
     */
    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getSchemas(catalog, schemaPattern);
        List<SchemaDTO> result = new ArrayList<>();
        while (rs.next()) {
            SchemaDTO schema = new SchemaDTO(rs.getString("TABLE_SCHEM"));
            result.add(schema);
        }
        JdbcUtil.close(conn, rs);
        return result;
    }

    /**
     * get object types
     * @return
     * @throws SQLException
     */
    @Override
    public List<String> listObjectTypes() throws SQLException {
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTableTypes();
        List<String> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getString("TABLE_TYPE"));
        }
        JdbcUtil.close(conn, rs);
        return result;
    }

    @Override
    public Map<String, TypeInfoDTO> listDataType() throws SQLException {
        Map<String, TypeInfoDTO> map = new HashMap<>();
        // sql standard data type
        STD_SQL_TYPES_AND_ALIAS.forEach(item -> {
            map.put(item.toLowerCase(), new TypeInfoDTO(item.toLowerCase()));
        });
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTypeInfo();
        while (rs.next()) {
            TypeInfoDTO typeInfo = new TypeInfoDTO();
            typeInfo.setTypeName(rs.getString("TYPE_NAME").toLowerCase());
            typeInfo.setDataType(rs.getInt("DATA_TYPE"));
            typeInfo.setPrecision(rs.getInt("PRECISION"));
            typeInfo.setLocalTypeName(rs.getString("LOCAL_TYPE_NAME"));
            typeInfo.setAutoIncrement(rs.getBoolean("AUTO_INCREMENT"));
            map.put(typeInfo.getTypeName(), typeInfo);
        }
        JdbcUtil.close(conn, rs);
        return map;
    }
}
