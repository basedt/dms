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

package com.basedt.dms.plugins.datasource.impl.oracle;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class OracleTableHandler extends JdbcTableHandler {

    @Override
    public List<TableDTO> listTables(String catalog, String schema, String tableName) throws SQLException {
        return super.listTables(StrUtil.isEmpty(catalog) ? catalog : catalog.toUpperCase(),
                StrUtil.isEmpty(schema) ? schema : schema.toUpperCase(),
                StrUtil.isEmpty(tableName) ? tableName : tableName.toUpperCase());
    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    o.object_type as  object_type," +
                "    c.comments as remark," +
                "    d.total_data_bytes + i.total_index_bytes as data_bytes," +
                "    nvl(p.num_rows,t.num_rows) as table_rows," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time," +
                "    null as last_access_time" +
                " from all_objects o" +
                " join all_tables t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.table_name" +
                " left join all_tab_comments c" +
                " on t.table_name = c.table_name" +
                " and t.owner = c.owner" +
                " left join" +
                "    (" +
                "        select" +
                "            s.owner," +
                "            s.segment_name," +
                "            sum(nvl(s.bytes,0) + nvl(b.lob_data_bytes,0)) as total_data_bytes" +
                "        from dba_segments s" +
                "        left join (select" +
                "                       b.owner," +
                "                       b.table_name," +
                "                       sum(t.bytes) as lob_data_bytes" +
                "                    from dba_lobs b" +
                "                    join dba_segments t" +
                "                    on b.owner = t.owner" +
                "                    and  t.segment_name = b.segment_name" +
                "                    group by b.owner, b.table_name" +
                "                    ) b" +
                "        on s.owner = b.owner" +
                "        and s.segment_name = b.table_name" +
                "        where s.segment_type in ('TABLE', 'TABLE PARTITION', 'TABLE SUBPARTITION')" +
                "        group by s.owner, s.segment_name" +
                "    ) d" +
                " on t.table_name = d.segment_name" +
                " and t.owner = d.owner" +
                " left join" +
                "    (" +
                "        select" +
                "            t1.table_owner as owner," +
                "            t1.table_name," +
                "            sum(t2.bytes) as total_index_bytes" +
                "        from" +
                "            (" +
                "                select" +
                "                    t.table_owner," +
                "                    t.table_name," +
                "                    t.index_name" +
                "                from all_indexes t" +
                "                union" +
                "                select" +
                "                    t.owner," +
                "                    t.table_name," +
                "                    t.index_name" +
                "                from dba_lobs t" +
                "            ) t1" +
                "        join dba_segments t2" +
                "        on t1.table_owner = t2.owner" +
                "        and t1.index_name = t2.segment_name" +
                "      group by t1.table_owner, t1.table_name) i" +
                " on t.table_name = i.table_name" +
                " and t.owner = i.owner" +
                " left join" +
                "     (" +
                "         select" +
                "             table_owner as owner," +
                "             table_name," +
                "             nvl(sum(num_rows), 0) as num_rows" +
                "         from all_tab_partitions o" +
                "         group by table_owner, table_name) p" +
                " on t.table_name = p.table_name" +
                " and t.owner = p.owner" +
                " where o.owner ='" + schemaPattern.toUpperCase() + "' ";
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and o.object_name = '" + tablePattern.toUpperCase() + "'";
        }
        return super.listTableFromDB(sql);
    }

    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    t.owner as schema_name," +
                "    t.table_name as table_name," +
                "    t.column_name as column_name," +
                "    t.data_type as data_type," +
                "    t.data_length as data_length," +
                "    t.data_precision as data_precision," +
                "    t.data_scale as data_scale," +
                "    decode(t.nullable,'Y',1,'N',0,0) as is_nullable," +
                // "    case when lower(t.data_default) like '%.nextval' then 1 else 0 end as auto_increment,"
                "    null as auto_increment," +
                "    t.data_default as default_value," +
                "    t.column_id as column_ordinal," +
                "    c.comments as remark" +
                " from all_tab_columns t" +
                " left join all_col_comments c" +
                " on t.owner = c.owner" +
                " and t.table_name = c.table_name" +
                " and t.column_name = c.column_name" +
                " where t.owner = '" + schemaPattern.toUpperCase() + "'" +
                " and t.table_name = '" + tableName.toUpperCase() + "'";
        List<ColumnDTO> columns = super.listColumnFromTable(sql);
        for (ColumnDTO column : columns) {
            if (StrUtil.isNotEmpty(column.getDefaultValue()) && column.getDefaultValue().endsWith(".nextval")) {
                column.setAutoIncrement(true);
            }else {
                column.setAutoIncrement(false);
            }
        }
        return columns;
    }

    @Override
    public String getTableDDL(String catalog, String schema, String tableName) throws SQLException {
        StringBuilder builder = new StringBuilder();
        String sql = "SELECT DBMS_METADATA.GET_DDL('TABLE', ?, ?) AS ddl FROM DUAL";
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, StrUtil.isEmpty(tableName) ? "" : tableName.toUpperCase());
        ps.setString(2, StrUtil.isEmpty(schema) ? "" : schema.toUpperCase());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            builder.append(rs.getString("ddl"))
                    .append(";");
        }
        JdbcUtil.close(conn, ps, rs);
        TableDTO tableInfo = getTableDetail(catalog, schema, tableName, DbObjectType.TABLE);
        tableInfo.setColumns(listColumnsByTable(catalog, schema, tableName));
        //create index
        List<IndexDTO> indexes = indexHandler.listIndexes(catalog, schema, tableName);
        if (!CollectionUtils.isEmpty(indexes)) {
            List<ObjectDTO> pks = indexHandler.listPkByTable(catalog, schema, tableName);
            List<ObjectDTO> fks = indexHandler.listFkByTable(catalog, schema, tableName);
            //filter pk and fk
            indexes = indexes.stream().filter(idx -> {
                for (ObjectDTO pk : pks) {
                    if (idx.getIndexName().equals(pk.getObjectName())) {
                        return false;
                    }
                }
                return true;
            }).filter(idx -> {
                for (ObjectDTO fk : fks) {
                    if (idx.getIndexName().equals(fk.getObjectName())) {
                        return false;
                    }
                }
                return true;
            }).toList();
            builder.append("\n-- indexes ");
            for (IndexDTO idx : indexes) {
                String idxDDL = indexHandler.getIndexDDL(catalog, schema, tableName, idx.getIndexName());
                if (StrUtil.isNotEmpty(idxDDL)) {
                    builder.append("\n")
                            .append(idxDDL)
                            .append(";");
                }
            }
        }
        builder.append("\n-- comments");
        //comments on table
        builder.append(generateTableCommentSQL(tableInfo));
        //comments on columns
        builder.append(generateColumnCommentSQL(tableInfo));
        return builder.toString();
    }

    @Override
    public String getTableDDL(TableDTO table) throws SQLException {
        if (Objects.isNull(table)) {
            throw new SQLException(StrUtil.format("no such table"));
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE ")
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
                builder.append("\n-- indexes ");
                for (IndexDTO index : indexes) {
                    builder.append("\n")
                            .append(indexHandler.getIndexDDL(index, table.getPks(), table.getFks()));
                }
            }
            builder.append("\n-- comments");
            //comments on table
            builder.append(generateTableCommentSQL(table));
            //comments on columns
            builder.append(generateColumnCommentSQL(table));
            return builder.toString();
        }
    }

    @Override
    protected String generateAddColumnDDL(ColumnDTO column) {
        return super.generateAddColumnDDL(column);
    }

    @Override
    protected String generateDropColumnDDL(ColumnDTO column) {
        return super.generateDropColumnDDL(column);
    }

    @Override
    protected String generateRenameColumnDDL(String schema, String tableName, String columnName, String newColumnName) {
        return super.generateRenameColumnDDL(schema, tableName, columnName, newColumnName);
    }

    @Override
    protected String generateAlertColumnTypeDDL(String schema, String tableName, String columnName, String newType) {
        return StrUtil.format("ALTER TABLE {}.{} MODIFY {} {};", schema, tableName, columnName, newType);
    }

    @Override
    protected String generateAlterColumnDefaultValueDDL(String schema, String tableName, String columnName, String columnType, String defaultValue) {
        return StrUtil.format("ALTER TABLE {}.{} MODIFY {} {} DEFAULT {};",
                schema, tableName, columnName, columnType, defaultValue);
    }

    @Override
    protected String generateAlterColumnNullableDDL(String schema, String tableName, String columnName, boolean nullable) {
        //  ALTER TABLE PDBADMIN.TEST_02 MODIFY NAME  NULL;
        if (nullable) {
            return StrUtil.format("ALTER TABLE {}.{} MODIFY {} NULL;", schema, tableName, columnName);
        } else {
            return StrUtil.format("ALTER TABLE {}.{} MODIFY {} NOT NULL;", schema, tableName, columnName);
        }
    }

    @Override
    protected String generateColumnCommentDDL(String schema, String tableName, String columnName, String comment) {
        return super.generateColumnCommentDDL(schema, tableName, columnName, comment);
    }

    private String generateColumnCommentSQL(TableDTO table) {
        if (Objects.isNull(table) || CollectionUtils.isEmpty(table.getColumns())) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (ColumnDTO column : table.getColumns()) {
            if (StrUtil.isNotEmpty(column.getRemark())) {
                builder.append("\n")
                        .append(StrUtil.format("COMMENT ON COLUMN {}.{}.{} IS '{}';",
                                column.getSchemaName(), column.getTableName(), column.getColumnName(), column.getRemark()));
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
            if (StrUtil.isNotEmpty(column.getDefaultValue()) && column.getDefaultValue().toLowerCase().contains(".nextval")) {
                //auto increment
                builder.append("NUMBER GENERATED BY DEFAULT AS IDENTITY");
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
