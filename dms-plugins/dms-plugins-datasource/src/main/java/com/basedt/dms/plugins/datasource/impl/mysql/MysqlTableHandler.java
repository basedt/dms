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
        if (Objects.isNull(table)){
            throw new SQLException(StrUtil.format("no such table"));
        }else {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE ")
                    .append(MYSQL_QUOTE)
                    .append(table.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(table.getTableName())
                    .append("(\n");
            if (!CollectionUtils.isEmpty(table.getColumns())){
                for (int i=0;i<table.getColumns().size();i++){
                    generateTableColumnDDL(table.getColumns().get(i), builder);
                    if (i < table.getColumns().size() - 1) {
                        builder.append(",\n");
                    }
                }
            }

            return builder.toString();
        }
    }

    @Override
    protected String generateRenameColumnDDL(String schema, String tableName, String columnName, String newColumnName) {
        //TODO
        return super.generateRenameColumnDDL(schema, tableName, columnName, newColumnName);
    }

    @Override
    protected String generateAlertColumnTypeDDL(String schema, String tableName, String columnName, String newType) {
        //TODO
        return super.generateAlertColumnTypeDDL(schema, tableName, columnName, newType);
    }

    @Override
    protected String generateAlterColumnDefaultValueDDL(String schema, String tableName, String columnName, String columnType, String defaultValue) {
        //TODO
        return super.generateAlterColumnDefaultValueDDL(schema, tableName, columnName, columnType, defaultValue);
    }

    @Override
    protected String generateAlterColumnNullableDDL(String schema, String tableName, String columnName, boolean nullable) {
        //TODO
        return super.generateAlterColumnNullableDDL(schema, tableName, columnName, nullable);
    }

    @Override
    protected String generateTableCommentSQL(TableDTO table) {
        //TODO
        return super.generateTableCommentSQL(table);
    }

    @Override
    protected String generateAddColumnDDL(ColumnDTO column) {
        //TODO
        return super.generateAddColumnDDL(column);
    }

    @Override
    protected String generateColumnCommentDDL(String schema, String tableName, String columnName, String comment) {
        //TODO
        return super.generateColumnCommentDDL(schema, tableName, columnName, comment);
    }

    @Override
    protected String generateDropColumnDDL(ColumnDTO column) {
        //TODO
        return super.generateDropColumnDDL(column);
    }

    private void generateTableColumnDDL(ColumnDTO column, StringBuilder builder) {
        if (Objects.nonNull(column)) {
            Type type = typeMapper.toType(column.getDataType());
            builder.append("\t")
                    .append(MYSQL_QUOTE)
                    .append(column.getColumnName())
                    .append(MYSQL_QUOTE)
                    .append(" ");
            if (StrUtil.isNotEmpty(column.getDefaultValue()) && column.getDefaultValue().toLowerCase().contains(".nextval")) {
                //auto increment
                builder.append("GENERATED BY DEFAULT AS IDENTITY");
            } else {
                builder.append(typeMapper.fromType(type))
                        .append(StrUtil.isEmpty(column.getDefaultValue()) ? "" : " DEFAULT " +
                                formatColumnDefaultValue(typeMapper.toType(column.getDataType()), column.getDefaultValue()))
                        .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                ;
            }
        }
    }
}
