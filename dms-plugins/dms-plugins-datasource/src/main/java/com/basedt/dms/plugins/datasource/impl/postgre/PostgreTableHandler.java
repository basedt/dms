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
                " and attr.attnum > 0";
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
                    generateColumnDDL(table.getColumns().get(i), builder);
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
            for (ColumnDTO column : table.getColumns()) {
                if (StrUtil.isNotEmpty(column.getRemark())) {
                    builder.append("\n")
                            .append(StrUtil.format("COMMENT ON COLUMN {}.{}.{} IS '{}';",
                                    column.getSchemaName(), column.getTableName(), column.getColumnName(), column.getRemark()));
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

        //TODO  待实现
        //            如果对比后发现没有修改任何内容，则返回原始表的DDL语句
//             需要注意顺序，例如先处理表名修改，再处理列的增删，然后是索引和分区
//             1. 改表名  不支持同步改表名
//             2. 改表注释
//             3. 新增列
//             4. 删除列
//             5. 调整列顺序 -- 不支持
//             6. 修改字段名称
//             7. 调整字段类型
//             8. 调整字段空值属性
//             9. 调整字段默认值
//             10. 调整字段注释
//             11. 新建索引
//             12. 删除索引
//             13. 修改索引名称
//             14. 修改索引列或者类型  =》 删除重建
//             15. 分区操作 暂不支持后续再说
        return super.getTableDDL(originTable, table);
    }

    private void generateColumnDDL(ColumnDTO column, StringBuilder builder) {
        if (Objects.nonNull(column)) {
            Type type = typeMapper.toType(column.getDataType(), column.getDataLength(), column.getDataPrecision(), column.getDataScale());
            builder.append("\t")
                    .append(column.getColumnName())
                    .append(" ");
            if (StrUtil.isNotEmpty(column.getDefaultValue()) && column.getDefaultValue().toLowerCase().startsWith("nextval")) {
                //auto increment
                builder.append("serial NOT NULL");
            } else {
                builder.append(typeMapper.fromType(type))
                        .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                        .append(StrUtil.isEmpty(column.getDefaultValue()) ? "" : " DEFAULT " + column.getDefaultValue())
                ;
            }
        }
    }
}
