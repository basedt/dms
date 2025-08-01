package com.basedt.dms.plugins.datasource.impl.hive;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcTableHandler;
import com.basedt.dms.plugins.datasource.types.Type;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.TABLE;
import static com.basedt.dms.plugins.datasource.impl.hive.HivePluginImpl.METASTORE_URIS;

public class HiveTableHandler extends JdbcTableHandler {

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        try {
            List<TableDTO> result = new ArrayList<>();
            String uris = this.config.get(METASTORE_URIS);
            Configuration conf = new Configuration();
            conf.set("hive.metastore.uris", uris);
            HiveMetaStoreClient client = new HiveMetaStoreClient(conf);
            List<String> tables = client.getAllTables(null, schemaPattern);
            List<Table> tableList = client.getTableObjectsByName(null, schemaPattern, tables);
            for (Table table : tableList) {
                if ("MANAGED_TABLE".equals(table.getTableType()) || "EXTERNAL_TABLE".equals(table.getTableType())) {
                    TableDTO tableDTO = new TableDTO();
                    tableDTO.setCatalogName(catalog);
                    tableDTO.setSchemaName(table.getDbName());
                    tableDTO.setObjectName(table.getTableName());
                    tableDTO.setObjectType(TABLE.name());
                    tableDTO.setCreateTime(DateTimeUtil.toLocalDateTime(table.getCreateTime()));
                    tableDTO.setLastAccessTime(DateTimeUtil.toLocalDateTime(table.getLastAccessTime()));
                    Map<String, String> params = table.getParameters();
                    tableDTO.setRemark(params.get("comment"));
                    tableDTO.setTableRows(Long.parseLong(Objects.isNull(params.get("numRows")) ? "0" : params.get("numRows")));
                    tableDTO.setDataBytes(Long.parseLong(Objects.isNull(params.get("totalSize")) ? "0" : params.get("totalSize")));
                    String lastDdlTime = params.get("transient_lastDdlTime");
                    tableDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(Integer.parseInt(Objects.isNull(lastDdlTime) ? "0" : lastDdlTime)));
                    if (Objects.isNull(tablePattern) || StrUtil.contains(tableDTO.getTableName(), tablePattern)) {
                        result.add(tableDTO);
                    }
                }
            }
            client.close();
            return result;
        } catch (TException e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public String getTableDDL(String catalog, String schema, String tableName) throws SQLException {
        String sql = StrUtil.format("show create table {}.{}", schema, tableName);
        StringBuilder ddl = new StringBuilder();
        Connection conn = dataSource.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            ddl.append(rs.getString(1))
                    .append("\n");
        }
        JdbcUtil.close(conn, st, rs);
        return ddl.append(";").toString();
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
                    if (i < table.getColumns().size() - 1) {
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
            if (!tableChange) {
                return getTableDDL(table);
            }
            return builder.toString();
        }
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
        //modify columns
        for (ColumnDTO column : newColumns) {
            for (ColumnDTO originCol : originColumns) {
                if (column.getId().equals(originCol.getId())) {
                    Type originType = typeMapper.toType(originCol.getDataType());
                    Type newType = typeMapper.toType(column.getDataType());
                    if (!originCol.getColumnName().equalsIgnoreCase(column.getColumnName()) ||
                            !originType.formatString().equalsIgnoreCase(newType.formatString()) ||
                            !originCol.getRemark().equalsIgnoreCase(column.getRemark())
                    ) {
                        builder.append("\n")
                                .append("ALTER TABLE ")
                                .append(originCol.getSchemaName())
                                .append(Constants.SEPARATOR_DOT)
                                .append(originCol.getTableName())
                                .append(" CHANGE COLUMN ")
                                .append(originCol.getColumnName())
                                .append(" ")
                                .append(column.getColumnName())
                                .append(" ")
                                .append(newType.formatString())
                                .append(StrUtil.isNotEmpty(column.getRemark()) ? " COMMENT '" + column.getRemark() + "'" : "")
                                .append(";")
                        ;
                    }
                }
            }
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
                .append(" ADD COLUMNS(")
                .append(column.getColumnName())
                .append(" ")
                .append(typeMapper.toType(column.getDataType()).formatString())
                .append(StrUtil.isNotEmpty(column.getRemark()) ? " COMMENT '" + column.getRemark() + "'" : "")
                .append(");")
        ;
        return builder.toString();
    }

    @Override
    protected String generateTableCommentSQL(TableDTO table) {
        if (Objects.isNull(table)) {
            return "";
        }
        if (StrUtil.isNotEmpty(table.getRemark())) {
            return StrUtil.format("\nALTER TABLE {}.{} SET TBLPROPERTIES ('comment' = '{}');",
                    table.getSchemaName(), table.getTableName(), table.getRemark());
        } else {
            return "";
        }
    }

    private void generateTableColumnDDL(ColumnDTO column, StringBuilder builder) {
        if (Objects.nonNull(column)) {
            Type type = typeMapper.toType(column.getDataType());
            builder.append("\t")
                    .append(column.getColumnName())
                    .append(" ")
                    .append(typeMapper.fromType(type));
        }
        if (StrUtil.isNotEmpty(column.getRemark())) {
            builder.append(" COMMENT '")
                    .append(column.getRemark())
                    .append("'");
        }
    }

}
