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

package com.basedt.dms.plugins.datasource.impl.postgre;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcTableHandler;
import com.basedt.dms.plugins.datasource.types.Type;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class PostgreTableHandler extends JdbcTableHandler {

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " case c.relkind when 'r' then 'TABLE' when 'i' then 'INDEX' when 'v' then 'VIEW' when 'S' then 'SEQUENCE'" +
                " when 'f' then 'FOREIGN_TABLE' when 'm' then 'MATERIALIZED_VIEW' else null end as object_type," +
                " d.description as remark," +
                " pg_table_size(concat_ws('.',n.nspname, c.relname)) as data_bytes," +
                " c.reltuples as table_rows," +
                " null as create_time," +
                " null as last_ddl_time," +
                " null as last_access_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c " +
                " on n.oid = c.relnamespace  " +
                " left join pg_catalog.pg_description d " +
                " on c.oid  = d.objoid " +
                " and d.objsubid = 0" +
                " and d.classoid  = 'pg_class'::regclass" +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(type) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and c.relname = '" + tablePattern + "'";
        }
        return super.listTableFromDB(sql);
    }

    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select" +
                " t.table_catalog as catalog_name," +
                " coalesce(t.table_schema,n.nspname) as schema_name," +
                " coalesce(t.table_name,c.relname) as table_name," +
                " coalesce(t.column_name,attr.attname) as column_name," +
                " coalesce(t.data_type,format_type(attr.atttypid,null)) as data_type," +
                " t.character_maximum_length as data_length," +
                " t.numeric_precision as data_precision," +
                " t.numeric_scale as data_scale," +
                " t.column_default as default_value," +
                " coalesce(t.ordinal_position,attr.attnum) as column_ordinal," +
                " col_description(attr.attrelid, attr.attnum) as remark," +
                " t.is_nullable as is_nullable" +
                " from pg_catalog.pg_class c" +
                " join pg_catalog.pg_namespace n" +
                " on n.oid = c.relnamespace" +
                " left join pg_catalog.pg_attribute attr" +
                " on attr.attrelid = c.oid" +
                " left join information_schema.columns t" +
                " on n.nspname = t.table_schema" +
                " and c.relname = t.table_name" +
                " and attr.attname = t.column_name" +
                " where c.relkind in ('r','v','m','f')" +
                " and attr.attnum > 0 " +
                " and attr.attisdropped = false";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and coalesce(t.table_schema,n.nspname) = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and coalesce(t.table_name,c.relname) = '" + tableName + "'";
        }
        return super.listColumnFromTable(sql);
    }

    /**
     * create table : https://www.postgresql.org/docs/17/sql-createtable.html
     */
    @Override
    public String getTableDDL(TableDTO table) throws SQLException {
        if (Objects.isNull(table)) {
            throw new SQLException(StrUtil.format("no such table"));
        } else {
            StringBuilder builder = new StringBuilder();
            //create table
            builder.append("CREATE TABLE IF NOT EXISTS ")
                    .append(table.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(table.getTableName())
                    .append(" (\n");
            if (!CollectionUtils.isEmpty(table.getColumns())) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    generateTableColumnDDL(table.getColumns().get(i), builder);
                    if (i < table.getColumns().size() - 1) {
                        builder.append(",\n");
                    }
                }
            }
            builder.append("\n);");
            //constraints and indexes
            List<IndexDTO> indexes = table.getIndexes();
            if (!CollectionUtils.isEmpty(indexes)) {
                builder.append("\n")
                        .append("-- constraint and index");
                for (IndexDTO index : indexes) {
                    builder.append("\n")
                            .append(indexHandler.getIndexDDL(index, table.getPks(), table.getFks()));
                }
            }
            //comment on table
            if (StrUtil.isNotEmpty(table.getRemark())) {
                builder.append("\n")
                        .append("-- comments")
                        .append("\n")
                        .append(StrUtil.format("COMMENT ON TABLE {}.{} IS '{}';",
                                table.getSchemaName(), table.getTableName(), table.getRemark()))
                        .append("\n");
            }
            //comment on columns
            if (!CollectionUtils.isEmpty(table.getColumns())) {
                for (ColumnDTO column : table.getColumns()) {
                    if (StrUtil.isNotEmpty(column.getRemark())) {
                        builder.append("\n")
                                .append(StrUtil.format("COMMENT ON COLUMN {}.{}.{} IS '{}';",
                                        column.getSchemaName(), column.getTableName(), column.getColumnName(), column.getRemark()));
                    }
                }
            }
            return builder.toString();
        }
    }

    /**
     * alter table : https://www.postgresql.org/docs/17/sql-altertable.html
     *
     * @param originTable
     * @param table
     * @return
     * @throws SQLException
     */
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
                builder.append("\n")
                        .append(StrUtil.format("COMMENT ON TABLE {}.{} IS '{}';",
                                table.getSchemaName(), table.getTableName(), StrUtil.nullToEmpty(table.getRemark())));
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
        if (Objects.nonNull(column.getRemark())) {
            builder.append("\n")
                    .append(StrUtil.format("COMMENT ON COLUMN {}.{}.{} IS '{}';",
                            column.getSchemaName(), column.getTableName(), column.getColumnName(), column.getRemark()));
        }
        return builder.toString();
    }

    protected String generateDropColumnDDL(ColumnDTO column) {
        return StrUtil.format("\nALTER TABLE {}.{} DROP COLUMN {};",
                column.getSchemaName(), column.getTableName(), column.getColumnName());
    }

    private String generateAlterColumnDDL(List<ColumnDTO> originColumns, List<ColumnDTO> newColumns) {
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
            builder.append(generateDropColumnDDL(column));
        }
        //modify columns
        for (ColumnDTO column : newColumns) {
            for (ColumnDTO originCol : originColumns) {
                if (column.getId().equals(originCol.getId())) {
                    boolean isColumnRename = false;
                    if (!column.getColumnName().equalsIgnoreCase(originCol.getColumnName())) {
                        builder.append("\n")
                                .append(StrUtil.format("ALTER TABLE {}.{} RENAME COLUMN {} TO {};",
                                        originCol.getSchemaName(), originCol.getTableName(), originCol.getColumnName(), column.getColumnName()));
                        isColumnRename = true;
                    }
                    Type originType = typeMapper.toType(originCol.getDataType(), originCol.getDataLength(), originCol.getDataPrecision(), originCol.getDataScale());
                    Type newType = typeMapper.toType(column.getDataType(), column.getDataLength(), column.getDataPrecision(), column.getDataScale());
                    if (!originType.formatString().equals(newType.formatString())) {
                        builder.append("\n")
                                .append(StrUtil.format("ALTER TABLE {}.{} ALTER COLUMN {} TYPE {};",
                                        originCol.getSchemaName(), originCol.getTableName(),
                                        isColumnRename ? column.getColumnName() : originCol.getColumnName(), newType.formatString()));
                    }
                    if (!originCol.getDefaultValue().equals(column.getDefaultValue())) {
                        builder.append("\n")
                                .append(StrUtil.format("ALTER TABLE {}.{} ALTER COLUMN {} SET DEFAULT {};",
                                        originCol.getSchemaName(), originCol.getTableName(),
                                        isColumnRename ? column.getColumnName() : originCol.getColumnName(), formatColumnDefaultValue(column.getType(), column.getDefaultValue())));
                    }
                    if (!originCol.getIsNullable().equals(column.getIsNullable())) {
                        builder.append("\n")
                                .append("ALTER TABLE ")
                                .append(originCol.getSchemaName())
                                .append(Constants.SEPARATOR_DOT)
                                .append(originCol.getTableName())
                                .append(" ALTER COLUMN ")
                                .append(isColumnRename ? column.getColumnName() : originCol.getColumnName())
                                .append(column.getIsNullable() ? " SET NOT NULL" : " DROP NOT NULL")
                                .append(";");
                    }
                    if (!originCol.getRemark().equals(column.getRemark())) {
                        builder.append("\n")
                                .append(StrUtil.format("COMMENT ON COLUMN {}.{}.{} IS '{}';",
                                        originCol.getSchemaName(), originCol.getTableName(), isColumnRename ? column.getColumnName() : originCol.getColumnName(), column.getRemark()));
                    }
                }
            }
        }
        return builder.toString();
    }

    private String generateAlterIndexDDL(TableDTO originTable, TableDTO table) {
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

    private void generateTableColumnDDL(ColumnDTO column, StringBuilder builder) {
        if (Objects.nonNull(column)) {
            Type type = typeMapper.toType(column.getDataType());
            builder.append("\t")
                    .append(column.getColumnName())
                    .append(" ");
            if (StrUtil.isNotEmpty(column.getDefaultValue()) && column.getDefaultValue().toLowerCase().startsWith("nextval")) {
                //auto increment
                builder.append("serial NOT NULL");
            } else {
                builder.append(typeMapper.fromType(type))
                        .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                        .append(StrUtil.isEmpty(column.getDefaultValue()) ? "" : " DEFAULT " + formatColumnDefaultValue(typeMapper.toType(column.getDataType()), column.getDefaultValue()))
                ;
            }
        }
    }
}
