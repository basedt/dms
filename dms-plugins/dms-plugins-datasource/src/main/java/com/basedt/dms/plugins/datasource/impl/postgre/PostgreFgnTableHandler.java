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
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcForeignTableHandler;
import com.basedt.dms.plugins.datasource.types.Type;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostgreFgnTableHandler extends JdbcForeignTableHandler {

    private static final String SERVER = "SERVER";
    private static final String OPTIONS = "OPTIONS";


    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        PostgreTableHandler tableHandler = new PostgreTableHandler();
        PostgreIndexHandler indexHandler = new PostgreIndexHandler();
        indexHandler.initialize(this.dataSource, new HashMap<>());
        tableHandler.initialize(this.dataSource, this.config, new PostgreDataTypeMapper(), indexHandler);
        return tableHandler.listTableDetails(catalog, schemaPattern, tablePattern, DbObjectType.FOREIGN_TABLE);
    }

    @Override
    public TableDTO getTableDetail(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        List<TableDTO> tables = listForeignTables(catalog, schemaPattern, tablePattern);
        if (!CollectionUtils.isEmpty(tables)) {
            return tables.get(0);
        } else {
            return null;
        }
    }

    @Override
    public String getTableDDL(String catalog, String schema, String tableName) throws SQLException {
        TableDTO table = getTableDetail(catalog, schema, tableName, DbObjectType.FOREIGN_TABLE);
        if (Objects.isNull(table)) {
            throw new SQLException(StrUtil.format("no such foreign table {}", tableName));
        }
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE FOREIGN TABLE IF NOT EXISTS ")
                .append(table.getSchemaName())
                .append(Constants.SEPARATOR_DOT)
                .append(table.getTableName())
                .append(" (\n");
        List<ColumnDTO> columns = listColumnsByTable(catalog, schema, tableName);
        table.setColumns(columns);
        if (!CollectionUtils.isEmpty(table.getColumns())) {
            for (int i = 0; i < table.getColumns().size(); i++) {
                generateTableColumnDDL(table.getColumns().get(i), builder);
                if (i < table.getColumns().size() - 1) {
                    builder.append(",\n");
                }
            }
        }
        builder.append("\n)");
        Map<String, String> option = getForeignTableOption(table.getSchemaName(), table.getTableName());
        builder.append("\n")
                .append(SERVER)
                .append(" ")
                .append(StrUtil.nullToEmpty(option.get(SERVER)))
                .append("\n")
                .append(OPTIONS)
                .append(" (")
                .append(StrUtil.nullToEmpty(option.get(OPTIONS)))
                .append(");");

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

    private Map<String, String> getForeignTableOption(String schema, String tableName) throws SQLException {
        Map<String, String> options = new HashMap<>();
        String sql = "select " +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " s.srvname as server_name," +
                " concat(replace(array_to_string(f.ftoptions,''','),'=',' '''),'''') as ftoptions" +
                " from pg_catalog.pg_foreign_table f" +
                " join pg_catalog.pg_class c" +
                " on f.ftrelid = c.oid" +
                " join pg_catalog.pg_namespace n " +
                " on n.oid = c.relnamespace" +
                " join pg_catalog.pg_foreign_server s " +
                " on f.ftserver = s.oid" +
                " where 1 = 1";
        if (StrUtil.isNotEmpty(schema)) {
            sql += " and n.nspname = '" + schema + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and c.relname = '" + tableName + "'";
        }
        Connection conn = dataSource.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            options.put(SERVER, rs.getString("server_name"));
            options.put(OPTIONS, rs.getString("ftoptions"));
            break;
        }

        JdbcUtil.close(conn, st, rs);
        return options;
    }

    protected void generateTableColumnDDL(ColumnDTO column, StringBuilder builder) {
        if (Objects.nonNull(column)) {
            Type type = typeMapper.toType(column.getDataType());
            builder.append("\t")
                    .append(column.getColumnName())
                    .append(" ");

            builder.append(typeMapper.fromType(type))
                    .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                    .append(StrUtil.isEmpty(column.getDefaultValue()) ? "" : " DEFAULT " + formatColumnDefaultValue(typeMapper.toType(column.getDataType()), column.getDefaultValue()))
            ;
        }

    }
}
