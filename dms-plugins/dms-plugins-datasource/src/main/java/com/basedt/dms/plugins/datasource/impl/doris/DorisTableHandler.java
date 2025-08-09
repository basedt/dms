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
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.mysql.MysqlTableHandler;
import com.basedt.dms.plugins.datasource.types.Type;
import org.springframework.util.CollectionUtils;

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
    public String getTableDDL(TableDTO table) throws SQLException {
        if (Objects.isNull(table)) {
            throw new SQLException(StrUtil.format("no such table"));
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE IF NOT EXISTS ")
                    .append(table.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(table.getTableName())
                    .append(" (\n");
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
                    builder.append("\t")
                            .append("INDEX ")
                            .append(index.getIndexName())
                            .append(" (")
                            .append(index.getColumns())
                            .append(")")
                            .append(StrUtil.isNotEmpty(index.getIndexType()) ? " USING " + index.getIndexType() : "");
                    if (i < table.getIndexes().size() - 1) {
                        builder.append(",\n");
                    }
                }
            }
            builder.append("\n)");
            if (!CollectionUtils.isEmpty(table.getPks())) {
                ObjectDTO pk = table.getPks().get(0);
                IndexDTO pkIdx = table.getIndexes().stream().filter(idx ->
                        idx.getIndexName().equals(pk.getObjectName())).toList().get(0);
                builder.append("\n")
                        .append("UNIQUE KEY (")
                        .append(pkIdx.getColumns())
                        .append(")")
                        .append("\n")
                        .append("DISTRIBUTED BY HASH(")
                        .append(pkIdx.getColumns())
                        .append(");");
            } else if (!CollectionUtils.isEmpty(table.getColumns())) {
                String cols = table.getColumns().get(0).getColumnName();
                builder.append("\n")
                        .append("DUPLICATE KEY (")
                        .append(cols)
                        .append(")")
                        .append("\n")
                        .append("DISTRIBUTED BY HASH(")
                        .append(cols)
                        .append(");")
                ;
            }
            if (StrUtil.isNotEmpty(table.getRemark())) {
                builder.append("\n")
                        .append(generateTableCommentSQL(table));
            }

            return builder.toString();
        }
    }

    private void generateTableColumnDDL(ColumnDTO column, StringBuilder builder) {
        if (Objects.nonNull(column)) {
            Type type = typeMapper.toType(column.getDataType());
            builder.append("\t")
                    .append(column.getColumnName())
                    .append(" ");
            if (column.getAutoIncrement()) {
                builder.append(typeMapper.fromType(type))
                        .append(" NOT NULL AUTO_INCREMENT");
            } else {
                builder.append(typeMapper.fromType(type))
                        .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                        .append(StrUtil.isEmpty(column.getDefaultValue()) ? "" : " DEFAULT " +
                                formatColumnDefaultValue(type, column.getDefaultValue()));
            }
            if (StrUtil.isNotEmpty(column.getRemark())) {
                builder.append(" COMMENT '")
                        .append(column.getRemark())
                        .append("'");
            }
        }
    }

    @Override
    protected String formatColumnDefaultValue(Type type, String defaultValue) {
        if (Objects.isNull(defaultValue)) {
            return "";
        }
        if (StrUtil.isNotEmpty(defaultValue) && !defaultValue.startsWith("'") && !defaultValue.startsWith("\"")) {
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

    @Override
    public String getTableDDL(String catalog, String schema, String tableName) throws SQLException {
        String ddl = super.getTableDDL(catalog, schema, tableName);
        if (StrUtil.isNotEmpty(ddl)) {
            return ddl.substring(0, ddl.length() - 1);
        } else {
            return ddl;
        }
    }

    @Override
    protected String generateRenameSQL(String schema, String tableName, String newName) {
        return StrUtil.format("ALTER TABLE {}.{} RENAME {}", schema, tableName, newName);
    }

    @Override
    protected List<ColumnDTO> listColumnFromTable(String sql) throws SQLException {
        return super.listColumnFromTable(sql);
    }

    @Override
    protected String generateTableCommentSQL(TableDTO table) {
        if (Objects.isNull(table)) {
            return "";
        }
        if (StrUtil.isNotEmpty(table.getRemark())) {
            return StrUtil.format("\nALTER TABLE {}.{} MODIFY COMMENT '{}';", table.getSchemaName(), table.getTableName(), table.getRemark());
        } else {
            return "";
        }
    }

    @Override
    protected String generateAddColumnDDL(ColumnDTO column) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n")
                .append("ALTER TABLE ")
                .append(column.getSchemaName())
                .append(Constants.SEPARATOR_DOT)
                .append(column.getTableName())
                .append(" ADD COLUMN ")
                .append(column.getColumnName())
                .append(" ")
                .append(typeMapper.toType(column.getDataType()).formatString())
                .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                .append(StrUtil.isNotEmpty(column.getDefaultValue()) ? " DEFAULT " + formatColumnDefaultValue(typeMapper.toType(column.getDataType()), column.getDefaultValue()) : "")
                .append(StrUtil.isNotEmpty(column.getRemark()) ? " COMMENT '" + column.getRemark() + "'" : "")
                .append(";");
        return builder.toString();
    }

    @Override
    protected String generateRenameColumnDDL(ColumnDTO originCol, ColumnDTO column) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n")
                .append("ALTER TABLE ")
                .append(originCol.getSchemaName())
                .append(Constants.SEPARATOR_DOT)
                .append(originCol.getTableName())
                .append(" RENAME COLUMN ")
                .append(originCol.getColumnName())
                .append(" ")
                .append(column.getColumnName())
                .append(";");
        return builder.toString();
    }
}
