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
import com.basedt.dms.plugins.datasource.DataTypeMapper;
import com.basedt.dms.plugins.datasource.IndexHandler;
import com.basedt.dms.plugins.datasource.TableHandler;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.types.Type;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.TABLE;

public class JdbcTableHandler implements TableHandler {

    protected DataSource dataSource;

    protected Map<String, String> config;

    protected DataTypeMapper typeMapper;

    protected IndexHandler indexHandler;

    @Override
    public void initialize(DataSource dataSource, Map<String, String> config, DataTypeMapper typeMapper, IndexHandler indexHandler) {
        this.dataSource = dataSource;
        this.config = config;
        this.typeMapper = typeMapper;
        this.indexHandler = indexHandler;
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
        //hive compatible
        ResultSetMetaData rsMeta = rs.getMetaData();
        int columnCnt = rsMeta.getColumnCount();
        List<String> columnNames = new ArrayList<>();
        for (int i = 1; i <= columnCnt; i++) {
            columnNames.add(rsMeta.getColumnName(i));
        }
        String autoIncrementColumnName = "IS_AUTOINCREMENT";
        for (String columnName : columnNames) {
            if ("IS_AUTOINCREMENT".equals(columnName) || "IS_AUTO_INCREMENT".equals(columnName)) {
                autoIncrementColumnName = columnName;
                break;
            }
        }

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
            column.setAutoIncrement(formatString2Bool(rs.getString(autoIncrementColumnName)));
            column.setType(typeMapper.toType(column.getDataType(), column.getDataLength(), column.getDataPrecision(), column.getDataScale()));
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
        List<IndexDTO> indexes = indexHandler.listIndexDetails(table.getCatalogName(), table.getSchemaName(), table.getTableName(), null);
        table.setIndexes(indexes);
        List<ObjectDTO> pks = indexHandler.listPkByTable(catalog, schema, tableName);
        List<ObjectDTO> fks = indexHandler.listFkByTable(catalog, schema, tableName);
        table.setPks(pks);
        table.setFks(fks);
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

    @Override
    public String getTableDDL(TableDTO originTable, TableDTO table) throws SQLException {
        if (Objects.isNull(originTable) && Objects.isNull(table)) {
            throw new SQLException(StrUtil.format("no such table"));
        } else if (Objects.isNull(originTable)) {
            return getTableDDL(table);
        } else if (Objects.isNull(table)) {
            return getTableDDL(originTable.getCatalogName(), originTable.getSchemaName(), originTable.getTableName());
        } else if (!originTable.getTableName().equalsIgnoreCase(table.getTableName())) {
            return getTableDDL(table);
        } else if (Objects.isNull(originTable.getColumns())) {
            return getTableDDL(table);
        } else {
            //generate alter table script
            StringBuilder builder = new StringBuilder();
            boolean tableChange = false;
            //alter table comment
            if (!originTable.getRemark().equals(table.getRemark())) {
                tableChange = true;
                builder.append(generateTableCommentSQL(table));
            }
            //alter table columns
            String alterColumnDDL = generateAlterColumnDDL(originTable.getColumns(), table.getColumns());
            if (StrUtil.isNotBlank(alterColumnDDL)) {
                tableChange = true;
                builder.append("\n")
                        .append(alterColumnDDL);
            }
            //alter table indexes
            String alterIndexDDL = generateAlterIndexDDL(originTable, table);
            if (StrUtil.isNotBlank(alterIndexDDL)) {
                tableChange = true;
                builder.append("\n")
                        .append(alterIndexDDL);
            }
            if (!tableChange) {
                return getTableDDL(originTable.getCatalogName(), originTable.getSchemaName(), originTable.getTableName());
            }
            return builder.toString();
        }
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
            if (rs.getLong("data_length") >= Integer.MAX_VALUE) {
                column.setDataLength(null);
                column.setDataPrecision(null);
            } else {
                column.setDataLength(rs.getInt("data_length"));
                column.setDataPrecision(rs.getInt("data_precision"));
            }
            column.setDataScale(rs.getInt("data_scale"));
            column.setDefaultValue(rs.getString("default_value"));
            column.setColumnOrdinal(rs.getInt("column_ordinal"));
            column.setRemark(rs.getString("remark"));
            column.setIsNullable(rs.getBoolean("is_nullable"));
            column.setAutoIncrement(rs.getBoolean("auto_increment"));
            column.setType(typeMapper.toType(column.getDataType(), column.getDataLength(), column.getDataPrecision(), column.getDataScale()));
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

    protected String formatColumnDefaultValue(Type type, String defaultValue) {
        if (Objects.isNull(defaultValue)) {
            return "";
        }
        if (StrUtil.isNotEmpty(defaultValue) && type instanceof Type.STRING && !defaultValue.startsWith("'")) {
            if (defaultValue.endsWith(")")) {
                return defaultValue;
            } else if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultValue)) {
                return defaultValue;
            } else {
                return "'" + defaultValue + "'";
            }
        }
        return defaultValue;
    }

    protected String generateAlterIndexDDL(TableDTO originTable, TableDTO table) {
        StringBuilder builder = new StringBuilder();
        if (CollectionUtils.isEmpty(table.getIndexes()) && CollectionUtils.isEmpty(originTable.getIndexes())) {
            return builder.toString();
        } else if (CollectionUtils.isEmpty(table.getIndexes())) {
            //drop index
            for (IndexDTO index : originTable.getIndexes()) {
                builder.append("\n")
                        .append(indexHandler.getDropDDL(index, table.getPks(), table.getFks()));
            }
        } else if (CollectionUtils.isEmpty(originTable.getIndexes())) {
            // new index
            for (IndexDTO index : table.getIndexes()) {
                builder.append("\n")
                        .append(indexHandler.getIndexDDL(index, originTable.getPks(), originTable.getFks()));
            }
        } else {
            //new index
            List<IndexDTO> newList = table.getIndexes().stream().filter(idx -> {
                for (IndexDTO originIdx : originTable.getIndexes()) {
                    if (idx.getId().equals(originIdx.getId())) {
                        return false;
                    }
                }
                return true;
            }).toList();
            for (IndexDTO index : newList) {
                builder.append("\n")
                        .append(indexHandler.getIndexDDL(index, originTable.getPks(), originTable.getFks()));
            }
            //drop index
            List<IndexDTO> dropList = originTable.getIndexes().stream().filter(idx -> {
                for (IndexDTO newIdx : table.getIndexes()) {
                    if (idx.getId().equals(newIdx.getId())) {
                        return false;
                    }
                }
                return true;
            }).toList();
            for (IndexDTO index : dropList) {
                builder.append("\n")
                        .append(indexHandler.getDropDDL(index, table.getPks(), table.getFks()));
            }
            //modify index
            for (IndexDTO originIdx : originTable.getIndexes()) {
                for (IndexDTO newIdx : table.getIndexes()) {
                    if (originIdx.getId().equals(newIdx.getId()) &&
                            (!originIdx.getIndexName().equals(newIdx.getIndexName()) ||
                                    !originIdx.getIsUniqueness().equals(newIdx.getIsUniqueness()) ||
                                    !originIdx.getColumns().equals(newIdx.getColumns()) ||
                                    !originIdx.getIndexType().equals(newIdx.getIndexType()))) {
                        builder.append("\n")
                                .append(indexHandler.getDropDDL(newIdx, originTable.getPks(), originTable.getFks()));
                        builder.append("\n")
                                .append(indexHandler.getIndexDDL(newIdx, table.getPks(), table.getFks()));
                    }
                }
            }
        }
        return builder.toString();
    }

    protected String generateAlterColumnDDL(List<ColumnDTO> originColumns, List<ColumnDTO> newColumns) {
        StringBuilder builder = new StringBuilder();
        if (CollectionUtils.isEmpty(newColumns)) {
            newColumns = List.of();
        }
        if (CollectionUtils.isEmpty(originColumns)) {
            originColumns = List.of();
        }
        //add new columns
        List<ColumnDTO> finalOriginColumns = originColumns;
        List<ColumnDTO> newList = newColumns.stream().filter(col -> {
            for (ColumnDTO originCol : finalOriginColumns) {
                if (col.getId().equals(originCol.getId())) {
                    return false;
                }
            }
            return true;
        }).toList();
        for (ColumnDTO newCol : newList) {
            builder.append(generateAddColumnDDL(newCol));
        }
        //drop columns
        List<ColumnDTO> finalNewColumns = newColumns;
        List<ColumnDTO> dropList = originColumns.stream().filter(col -> {
            for (ColumnDTO newCol : finalNewColumns) {
                if (col.getId().equals(newCol.getId())) {
                    return false;
                }
            }
            return true;
        }).toList();
        for (ColumnDTO column : dropList) {
            builder.append("\n")
                    .append(generateDropColumnDDL(column));
        }
        //modify columns
        for (ColumnDTO column : newColumns) {
            for (ColumnDTO originCol : originColumns) {
                if (column.getId().equals(originCol.getId())) {
                    boolean isColumnRename = false;
                    Type originType = typeMapper.toType(originCol.getDataType());
                    Type newType = typeMapper.toType(column.getDataType());
                    if (!column.getColumnName().equalsIgnoreCase(originCol.getColumnName())) {
                        builder.append("\n")
                                .append(generateRenameColumnDDL(originCol.getSchemaName(), originCol.getTableName(),
                                        originCol.getColumnName(), column.getColumnName()));
                        isColumnRename = true;
                    }
                    if (!originCol.getDefaultValue().equals(column.getDefaultValue())) {
                        builder.append("\n")
                                .append(generateAlterColumnDefaultValueDDL(
                                        originCol.getSchemaName(), originCol.getTableName(),
                                        isColumnRename ? column.getColumnName() : originCol.getColumnName(),
                                        originType.formatString(),
                                        formatColumnDefaultValue(originType, column.getDefaultValue())));
                    }
                    if (!originCol.getIsNullable().equals(column.getIsNullable())) {
                        builder.append("\n")
                                .append(generateAlterColumnNullableDDL(originCol.getSchemaName(),
                                        originCol.getTableName(),
                                        isColumnRename ? column.getColumnName() : originCol.getColumnName(),
                                        originType.formatString(),
                                        column.getIsNullable()));
                    }
                    if (!originType.formatString().equals(newType.formatString())) {
                        builder.append("\n")
                                .append(generateAlertColumnTypeDDL(originCol.getSchemaName(), originCol.getTableName(),
                                        isColumnRename ? column.getColumnName() : originCol.getColumnName(),
                                        newType.formatString()));
                    }


                    if (!originCol.getRemark().equals(column.getRemark())) {
                        builder.append("\n")
                                .append(generateColumnCommentDDL(originCol.getSchemaName(), originCol.getTableName(),
                                        isColumnRename ? column.getColumnName() : originCol.getColumnName(),
                                        column.getRemark())
                                );
                    }
                }
            }
        }
        return builder.toString();
    }

    protected String generateRenameColumnDDL(String schema, String tableName, String columnName, String newColumnName) {
        return StrUtil.format("ALTER TABLE {}.{} RENAME COLUMN {} TO {};",
                schema, tableName, columnName, newColumnName);
    }

    protected String generateAlertColumnTypeDDL(String schema, String tableName, String columnName, String newType) {
        return StrUtil.format("ALTER TABLE {}.{} ALTER COLUMN {} TYPE {};", schema, tableName, columnName, newType);
    }

    protected String generateAlterColumnDefaultValueDDL(String schema, String tableName, String columnName, String columnType, String defaultValue) {
        return StrUtil.format("ALTER TABLE {}.{} ALTER COLUMN {} SET DEFAULT {};", schema, tableName, columnName, defaultValue);
    }

    protected String generateAlterColumnNullableDDL(String schema, String tableName, String columnName, String columnType, boolean nullable) {
        if (nullable) {
            return StrUtil.format("ALTER TABLE {}.{} ALTER COLUMN {} SET NOT NULL;", schema, tableName, columnName);
        } else {
            return StrUtil.format("ALTER TABLE {}.{} ALTER COLUMN {} DROP NOT NULL;", schema, tableName, columnName);
        }
    }

    protected String generateTableCommentSQL(TableDTO table) {
        if (Objects.isNull(table)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (StrUtil.isNotEmpty(table.getRemark())) {
            builder.append("\n")
                    .append(StrUtil.format("COMMENT ON TABLE {}.{} IS '{}';",
                            table.getSchemaName(), table.getTableName(), table.getRemark()));
        }
        return builder.toString();
    }

    protected String generateAddColumnDDL(ColumnDTO column) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n")
                .append("ALTER TABLE ")
                .append(column.getSchemaName())
                .append(Constants.SEPARATOR_DOT)
                .append(column.getTableName())
                .append(" ADD ")
                .append(column.getColumnName())
                .append(" ")
                .append(typeMapper.toType(column.getDataType()).formatString())
                .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                .append(StrUtil.isNotEmpty(column.getDefaultValue()) ? " DEFAULT " + formatColumnDefaultValue(typeMapper.toType(column.getDataType()), column.getDefaultValue()) : "")
                .append(";")
        ;
        if (StrUtil.isNotEmpty(column.getRemark())) {
            builder.append("\n")
                    .append(generateColumnCommentDDL(column.getSchemaName(), column.getTableName(), column.getColumnName(), column.getRemark()));
        }
        return builder.toString();
    }

    protected String generateColumnCommentDDL(String schema, String tableName, String columnName, String comment) {
        return StrUtil.format("COMMENT ON COLUMN {}.{}.{} IS '{}';",
                schema, tableName, columnName, comment);
    }

    protected String generateDropColumnDDL(ColumnDTO column) {
        return StrUtil.format("ALTER TABLE {}.{} DROP COLUMN {};",
                column.getSchemaName(), column.getTableName(), column.getColumnName());
    }

}
