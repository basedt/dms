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
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        List<ColumnDTO> columns = super.listColumnsByTable(catalog, schemaPattern, tableName);
        if (!CollectionUtils.isEmpty(columns)) {
            for (ColumnDTO col : columns) {
                if (col.getDataType().startsWith("Nullable")) {
                    String dataType = StrUtil.sub(col.getDataType(),
                            StrUtil.indexOf(col.getDataType(), '(', 0) + 1,
                            StrUtil.lastIndexOf(col.getDataType(), ")", 0, false));
                    col.setDataType(dataType);
                    col.setIsNullable(true);
                    col.setType(typeMapper.toType(dataType));
                }
            }
        }
        return columns;
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
            if (StrUtil.isNotEmpty(table.getRemark())) {
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

    @Override
    protected String generateTableCommentSQL(TableDTO table) {
        if (Objects.isNull(table)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (StrUtil.isNotEmpty(table.getRemark())) {
            builder.append("\n")
                    .append(StrUtil.format("ALTER TABLE {}.{} MODIFY COMMENT '{}';",
                            table.getSchemaName(), table.getTableName(), table.getRemark()));
        }
        return builder.toString();
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
                .append(" ");
        if (column.getIsNullable()) {
            builder.append("Nullable(")
                    .append(typeMapper.toType(column.getDataType()).formatString())
                    .append(")");
        } else {
            builder.append(typeMapper.toType(column.getDataType()).formatString());
        }
        builder.append(StrUtil.isNotEmpty(column.getDefaultValue()) ? " DEFAULT " + formatColumnDefaultValue(typeMapper.toType(column.getDataType()), column.getDefaultValue()) : "")
                .append(";");
        if (StrUtil.isNotEmpty(column.getRemark())) {
            builder.append("\n")
                    .append(generateColumnCommentDDL(column.getSchemaName(), column.getTableName(), column.getColumnName(), column.getRemark()));
        }
        return builder.toString();
    }

    @Override
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
                    if (!originCol.getIsNullable().equals(column.getIsNullable()) || !originType.formatString().equals(newType.formatString())) {
                        String newTypeName = newType.formatString();
                        if (column.getIsNullable()) {
                            newTypeName = "Nullable(" + newType.formatString() + ")";
                        }
                        builder.append("\n")
                                .append(generateAlterColumnTypeDDL(originCol.getSchemaName(), originCol.getTableName(),
                                        isColumnRename ? column.getColumnName() : originCol.getColumnName(),
                                        newTypeName));
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

    @Override
    protected String generateColumnCommentDDL(String schema, String tableName, String columnName, String comment) {
        return StrUtil.format("ALTER TABLE {}.{} COMMENT COLUMN {} '{}';", schema, tableName, columnName, comment);
    }

    @Override
    protected String generateRenameSQL(String schema, String tableName, String newName) {
        return StrUtil.format("RENAME TABLE {}.{} TO {}", schema, tableName, newName);
    }

    @Override
    protected String generateRenameColumnDDL(String schema, String tableName, String columnName, String newColumnName) {
        return super.generateRenameColumnDDL(schema, tableName, columnName, newColumnName);
    }

    @Override
    protected String generateAlterColumnTypeDDL(String schema, String tableName, String columnName, String newType) {
        return StrUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} {};", schema, tableName, columnName, newType);
    }

    @Override
    protected String generateAlterColumnDefaultValueDDL(String schema, String tableName, String columnName, String columnType, String defaultValue) {
        return StrUtil.format("ALTER TABLE {}.{} MODIFY COLUMN {} DEFAULT {};", schema, tableName, columnName, defaultValue);
    }
}
