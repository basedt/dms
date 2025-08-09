package com.basedt.dms.plugins.datasource.impl.clickhouse;

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

public class ClickHouseTableHandler extends JdbcTableHandler {

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
        String sql = " select" +
                "    t.table_catalog as catalog_name," +
                "    t.table_schema as schema_name," +
                "    t.table_name as object_name," +
                "    case when t.table_type = 'SYSTEM VIEW' then 'VIEW' when t.table_type='BASE TABLE' then 'TABLE' else t.table_type end as object_type," +
                "    t.table_rows as table_rows," +
                "    t.data_length as data_bytes," +
                "    t.table_comment as remark," +
                "    st.metadata_modification_time as create_time," +
                "    st.metadata_modification_time as last_ddl_time," +
                "    toDateTime('1970-01-01 00:00:00') as last_access_time" +
                " from information_schema.tables t" +
                " left join system.tables st" +
                " on t.table_schema = st.database" +
                " and t.table_name = st.name" +
                " where t.table_type in ('BASE TABLE')" +
                " and t.table_name not like ('.inner_id%')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and t.table_name = '" + tablePattern + "'";
        }
        return super.listTableFromDB(sql);
    }

    @Override
    public String getTableDDL(String catalog, String schema, String tableName) throws SQLException {
        String sql = StrUtil.format("show create table {}.{}", schema, tableName);
        String ddl = "";
        Connection conn = dataSource.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            ddl = rs.getString(1);
        }
        JdbcUtil.close(conn, st, rs);
        return ddl;
    }

    @Override
    public String getTableDDL(TableDTO table) throws SQLException {
        if (Objects.isNull(table)) {
            throw new SQLException("no such table");
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE IF NOT EXISTS ")
                    .append(table.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(table.getTableName())
                    .append(" (\n");
            String orderCol = "";
            if (!CollectionUtils.isEmpty(table.getColumns())) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    generateTableColumnDDL(table.getColumns().get(i), builder);
                    if (i < table.getColumns().size() - 1) {
                        builder.append(",\n");
                    }
                }
                orderCol = table.getColumns().get(0).getColumnName();
            }
            builder.append("\n)");
            builder.append("\n")
                    .append("ENGINE = MergeTree")
                    .append("\n")
                    .append("ORDER BY (")
                    .append(orderCol)
                    .append(")");
            if (StrUtil.isNotEmpty(table.getRemark())){
                builder.append("\n")
                        .append("COMMENT '")
                        .append(table.getRemark())
                        .append("'");
            }
            builder.append(";");
            return builder.toString();
        }
    }

    private void generateTableColumnDDL(ColumnDTO column, StringBuilder builder) {
        if (Objects.nonNull(column)) {
            Type type = typeMapper.toType(column.getDataType());
            builder.append("\t")
                    .append(column.getColumnName())
                    .append(" ")
                    .append(typeMapper.fromType(type))
                    .append(column.getIsNullable() ? " NULL" : " NOT NULL")
                    .append(StrUtil.isEmpty(column.getDefaultValue()) ? "" : " DEFAULT " +
                            formatColumnDefaultValue(type, column.getDefaultValue()));
            if (StrUtil.isNotEmpty(column.getRemark())) {
                builder.append(" COMMENT '")
                        .append(column.getRemark())
                        .append("'");
            }
        }
    }

}
