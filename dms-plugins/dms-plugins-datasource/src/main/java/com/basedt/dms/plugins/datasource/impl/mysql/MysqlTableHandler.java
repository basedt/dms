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
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcTableHandler;
import com.basedt.dms.plugins.datasource.types.Type;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.TABLE;

public class MysqlTableHandler extends JdbcTableHandler {

    static final String MYSQL_QUOTE = "`";

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return listTableDetails(catalog, schemaPattern, tablePattern, TABLE);
    }

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        return listTableDetails(catalog, schemaPattern, tablePattern, type);
    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
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
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and t.table_name = '" + tablePattern + "'";
        }
        return super.listTableFromDB(sql);
    }


    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " t.table_schema as schema_name," +
                " t.table_name as table_name," +
                " t.column_name  as column_name," +
                " t.data_type as data_type," +
                " t.character_maximum_length as data_length," +
                " t.numeric_precision as data_precision," +
                " t.numeric_scale as data_scale," +
                " case when t.EXTRA = 'auto_increment' then 'auto_increment' else t.column_default end as default_value," +
                " t.ordinal_position as column_ordinal," +
                " t.column_comment as remark," +
                " case when t.is_nullable = 'YES' then 1 else 0 end as is_nullable," +
                " case when t.extra = 'auto_increment' then 1 else 0 end as auto_increment" +
                " from information_schema.columns t" +
                " where 1=1 ";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and t.table_name = '" + tableName + "'";
        }
        return super.listColumnFromTable(sql);
    }

    @Override
    protected String generateRenameSQL(String schema, String tableName, String newName) {
        return StrUtil.format("rename table {}.{} to {}", schema, tableName, newName);
    }

    @Override
    public String getTableDDL(String catalog, String schema, String tableName) throws SQLException {
        String sql = StrUtil.format("show create table {}.{}", schema, tableName);
        String ddl = "";
        Connection conn = dataSource.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            ddl = rs.getString(2);
        }
        JdbcUtil.close(conn, st, rs);
        return ddl + ";";
    }

    @Override
    public String getTableDDL(TableDTO table) throws SQLException {
        if (Objects.isNull(table)) {
            throw new SQLException(StrUtil.format("no such table"));
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE ")
                    .append(MYSQL_QUOTE)
                    .append(table.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(table.getTableName())
                    .append("(\n");
            if (!CollectionUtils.isEmpty(table.getColumns())) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    generateTableColumnDDL(table.getColumns().get(i), builder);
                    if (!CollectionUtils.isEmpty(table.getIndexes())) {
                        builder.append(",\n");
                    } else if (i < table.getColumns().size() - 1) {
                        builder.append(",\n");
                    }
                }
            }
            if (!CollectionUtils.isEmpty(table.getIndexes())) {
                for (int i = 0; i < table.getIndexes().size(); i++) {
                    IndexDTO index = table.getIndexes().get(i);
                    if (isPrimaryKey(index, table.getPks())) {
                        builder.append("\tPRIMARY KEY ");
                    } else if (index.getIsUniqueness()) {
                        builder.append("\tUNIQUE KEY ");

                    } else {
                        builder.append("\tKEY ");
                    }
                    builder.append(MYSQL_QUOTE)
                            .append(index.getIndexName())
                            .append(MYSQL_QUOTE)
                            .append(" (")
                            .append(index.getColumns())
                            .append(")");
                    //判断是主键还是普通索引
                    if (i < table.getIndexes().size() - 1) {
                        builder.append(",\n");
                    }
                }
            }
            if (StrUtil.isNotEmpty(table.getRemark())) {
                builder.append("\n)COMMENT = '")
                        .append(table.getRemark())
                        .append("';");
            } else {
                builder.append("\n);");
            }
            return builder.toString();
        }
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
                return getTableDDL(table);
            }
            return builder.toString();
        }
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
                    Type originType = typeMapper.toType(originCol.getDataType());
                    Type newType = typeMapper.toType(column.getDataType());
                    boolean isColumnRename = false;
                    if (!column.getColumnName().equalsIgnoreCase(originCol.getColumnName())) {
                        isColumnRename = true;
                        //ALTER TABLE sample.test_007 CHANGE salary sala decimal(10,2) NULL;
                        builder.append("\n")
                                .append("ALTER TABLE ")
                                .append(originCol.getSchemaName())
                                .append(Constants.SEPARATOR_DOT)
                                .append(originCol.getTableName())
                                .append(" CHANGE ")
                                .append(originCol.getColumnName())
                                .append(" ")
                                .append(column.getColumnName())
                                .append(" ")
                                .append(originType.formatString())
                                .append(Objects.nonNull(originCol.getIsNullable()) && originCol.getIsNullable() ? " NULL" : " NOT NULL")
                                .append(StrUtil.isNotEmpty(originCol.getDefaultValue()) ? " DEFAULT " + formatColumnDefaultValue(originType, originCol.getDefaultValue()) : "")
                                .append(StrUtil.isNotEmpty(originCol.getRemark()) ? " COMMENT '" + originCol.getRemark() + "'" : "")
                                .append(";")
                        ;
                    }
                    if (!originCol.getDefaultValue().equals(column.getDefaultValue()) ||
                            !originType.formatString().equals(newType.formatString()) ||
                            !originCol.getIsNullable().equals(column.getIsNullable()) ||
                            !originCol.getRemark().equals(column.getRemark())
                    ) {
                        //ALTER TABLE sample.test_007 MODIFY COLUMN profile_img longblob DEFAULT 111 NOT NULL;
                        String columnName = isColumnRename ? column.getColumnName() : originCol.getColumnName();
                        builder.append("\n")
                                .append("ALTER TABLE ")
                                .append(originCol.getSchemaName())
                                .append(Constants.SEPARATOR_DOT)
                                .append(originCol.getTableName())
                                .append(" MODIFY COLUMN ")
                                .append(columnName)
                                .append(" ")
                                .append(newType.formatString())
                                .append(Objects.nonNull(column.getIsNullable()) && column.getIsNullable() ? " NULL" : " NOT NULL")
                                .append(StrUtil.isNotEmpty(column.getDefaultValue()) ? " DEFAULT " + formatColumnDefaultValue(newType, column.getDefaultValue()) : "")
                                .append(StrUtil.isNotEmpty(column.getRemark()) ? " COMMENT '" + column.getRemark() + "'" : "")
                        ;
                    }
                }
            }
        }
        return builder.toString();
    }

    @Override
    protected String generateTableCommentSQL(TableDTO table) {
        if (Objects.isNull(table)) {
            return "";
        }
        if (StrUtil.isNotEmpty(table.getRemark())) {
            return StrUtil.format("\nALTER TABLE {}.{} COMMENT = '{}';", table.getSchemaName(), table.getTableName(), table.getRemark());
        } else {
            return "";
        }
    }

    @Override
    protected String generateAddColumnDDL(ColumnDTO column) {
        StringBuilder builer = new StringBuilder();
        builer.append("\n")
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
                .append(StrUtil.isNotEmpty(column.getRemark()) ? " COMMENT '" + column.getRemark() + "'" : "")
                .append(";");
        return builer.toString();
    }

    @Override
    protected String generateDropColumnDDL(ColumnDTO column) {
        return super.generateDropColumnDDL(column);
    }

    private boolean isPrimaryKey(IndexDTO index, List<ObjectDTO> pks) {
        if (CollectionUtils.isEmpty(pks) || Objects.isNull(index)) {
            return false;
        }
        for (ObjectDTO pk : pks) {
            if (pk.getObjectName().equals(index.getIndexName())) {
                return true;
            }
        }
        return false;
    }

    private void generateTableColumnDDL(ColumnDTO column, StringBuilder builder) {
        if (Objects.nonNull(column)) {
            Type type = typeMapper.toType(column.getDataType());
            builder.append("\t")
                    .append(MYSQL_QUOTE)
                    .append(column.getColumnName())
                    .append(MYSQL_QUOTE)
                    .append(" ");
            if (column.getAutoIncrement()) {
                //auto increment
                builder.append(typeMapper.fromType(type))
                        .append(" AUTO_INCREMENT NOT NULL")
                ;
            } else {
                builder.append(typeMapper.fromType(type))
                        .append(StrUtil.isEmpty(column.getDefaultValue()) ? "" : " DEFAULT " +
                                formatColumnDefaultValue(typeMapper.toType(column.getDataType()), column.getDefaultValue()))
                        .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                ;
            }
            if (StrUtil.isNotEmpty(column.getRemark())) {
                builder.append(" COMMENT '")
                        .append(column.getRemark())
                        .append("'");
            }
        }
    }
}
