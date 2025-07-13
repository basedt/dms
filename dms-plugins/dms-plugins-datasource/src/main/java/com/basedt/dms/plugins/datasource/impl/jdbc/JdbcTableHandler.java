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
import com.basedt.dms.plugins.datasource.DataTypeMapper;
import com.basedt.dms.plugins.datasource.TableHandler;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.TABLE;

public class JdbcTableHandler implements TableHandler {

    protected DataSource dataSource;

    protected Map<String, String> config;

    protected DataTypeMapper typeMapper;

    @Override
    public void initialize(DataSource dataSource, Map<String, String> config, DataTypeMapper typeMapper) {
        this.dataSource = dataSource;
        this.config = config;
        this.typeMapper = typeMapper;
    }

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return listTables(catalog, schemaPattern, tablePattern, TABLE);
    }

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        List<ObjectDTO> list = this.listTables(catalog, schemaPattern, tablePattern, new String[]{type.name()});
        List<TableDTO> result = new ArrayList<>();
        for (ObjectDTO obj : list) {
            TableDTO table = new TableDTO();
            table.setCatalogName(obj.getCatalogName());
            table.setSchemaName(obj.getSchemaName());
            table.setObjectName(obj.getObjectName());
            table.setObjectType(type.name());
            result.add(table);
        }
        return result;
    }

    /**
     * TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     */
    private List<ObjectDTO> listTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) throws SQLException {
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(catalog, schemaPattern, tableNamePattern, types);
        List<ObjectDTO> result = new ArrayList<>();
        while (rs.next()) {
            ObjectDTO table = new ObjectDTO();
            table.setCatalogName(rs.getString("TABLE_CAT"));
            table.setSchemaName(rs.getString("TABLE_SCHEM"));
            table.setObjectName(rs.getString("TABLE_NAME"));
            table.setObjectType(rs.getString("TABLE_TYPE"));
            result.add(table);
        }
        JdbcUtil.close(conn, rs);
        return result;
    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        return listTables(catalog, schemaPattern, tablePattern, type);
    }

    @Override
    public TableDTO getTableDetail(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        List<TableDTO> tables = listTableDetails(catalog, schemaPattern, tablePattern, type);
        if (CollectionUtil.isNotEmpty(tables)) {
            return tables.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void dropTable(String schema, String tableName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateDropSQL(schema, tableName));
        }
    }

    @Override
    public void renameTable(String schema, String tableName, String newName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateRenameSQL(schema, tableName, newName));
        }
    }

    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getColumns(catalog, schemaPattern, tableName, null);
        List<ColumnDTO> result = new ArrayList<>();
        while (rs.next()) {
            ColumnDTO column = new ColumnDTO();
            column.setCatalogName(rs.getString("TABLE_CAT"));
            column.setSchemaName(rs.getString("TABLE_SCHEM"));
            column.setTableName(rs.getString("TABLE_NAME"));
            column.setColumnName(rs.getString("COLUMN_NAME"));
            column.setSqlType(rs.getInt("DATA_TYPE"));
            column.setDataType(rs.getString("TYPE_NAME"));
            column.setDataLength(rs.getInt("COLUMN_SIZE"));
            column.setRemark(rs.getString("REMARKS"));
            column.setDefaultValue(rs.getString("COLUMN_DEF"));
            column.setColumnOrdinal(rs.getInt("ORDINAL_POSITION"));
            column.setIsNullable(formatString2Bool(rs.getString("IS_NULLABLE")));
            result.add(column);
        }
        JdbcUtil.close(conn, rs);
        return result;
    }

    @Override
    public String getDropDDL(String schema, String tableName) throws SQLException {
        return generateDropSQL(schema, tableName);
    }

    @Override
    public String getRenameDDL(String schema, String tableName, String newName) throws SQLException {
        return generateRenameSQL(schema, tableName, newName);
    }

    @Override
    public String getTableDDL(String catalog, String schema, String tableName) throws SQLException {
        TableDTO table = getTableDetail(catalog, schema, tableName, TABLE);
        if (Objects.isNull(table)) {
            throw new SQLException(StrUtil.format("no such table {}", tableName));
        }
        List<ColumnDTO> columns = listColumnsByTable(catalog, schema, tableName);
        table.setColumns(columns.stream().sorted(Comparator.comparing(ColumnDTO::getColumnOrdinal)).toList());
        return getTableDDL(table);
    }

    /**
     * Get the DDL of a table. Default implementation returns "not supported yet."
     *
     * @param table TableDTO object containing table information
     * @return DDL of the table as a String
     * @throws SQLException if an error occurs while retrieving the DDL
     */
    @Override
    public String getTableDDL(TableDTO table) throws SQLException {
        return "not supported yet.";
    }

    protected String generateDropSQL(String schema, String tableName) {
        return StrUtil.format("DROP TABLE {}.{}", schema, tableName);
    }

    protected String generateRenameSQL(String schema, String tableName, String newName) {
        return StrUtil.format("ALTER TABLE {}.{} RENAME TO {}", schema, tableName, newName);
    }

    protected String generateDistributedSQL(String schema, String tableName) {
        return "";
    }

    protected List<TableDTO> listTableFromDB(String sql) throws SQLException {
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
            tableDTO.setDataBytes(rs.getLong("data_bytes"));
            tableDTO.setTableRows(rs.getLong("table_rows"));
            tableDTO.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            tableDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            tableDTO.setLastAccessTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_access_time")));
            result.add(tableDTO);
        }
        JdbcUtil.close(conn, ps, rs);
        return result;
    }

    protected List<ColumnDTO> listColumnFromTable(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<ColumnDTO> result = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ColumnDTO column = new ColumnDTO();
            column.setCatalogName(rs.getString("catalog_name"));
            column.setSchemaName(rs.getString("schema_name"));
            column.setTableName(rs.getString("table_name"));
            column.setColumnName(rs.getString("column_name"));
            column.setDataType(rs.getString("data_type"));
            column.setDataLength(rs.getInt("data_length"));
            column.setDataPrecision(rs.getInt("data_precision"));
            column.setDataScale(rs.getInt("data_scale"));
            column.setDefaultValue(rs.getString("default_value"));
            column.setColumnOrdinal(rs.getInt("column_ordinal"));
            column.setRemark(rs.getString("remark"));
            column.setIsNullable(rs.getBoolean("is_nullable"));
            result.add(column);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }

    private Boolean formatString2Bool(String str) {
        if ("YES".equalsIgnoreCase(str)) {
            return true;
        } else if ("NO".equalsIgnoreCase(str)) {
            return false;
        } else {
            return null;
        }
    }
}
