package com.basedt.dms.plugins.datasource.impl.mssql;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.dto.ObjectDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcIndexHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.FK;
import static com.basedt.dms.plugins.datasource.enums.DbObjectType.PK;

public class MssqlIndexHandler extends JdbcIndexHandler {

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName, String indexName) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    i.name as object_name," +
                "    'INDEX' as object_type," +
                "    i.type_desc as index_type," +
                "    i.is_unique as is_uniqueness," +
                "    o.name as table_name," +
                "    ic.columns as columns," +
                "    null as index_bytes," +
                "    null as create_time," +
                "    null as last_ddl_time" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " join sys.indexes i" +
                " on o.object_id = i.object_id" +
                " join (select string_agg(c.name,',') within group (order by ic.key_ordinal) as columns,ic.object_id,ic.index_id from sys.index_columns ic join sys.columns c on ic.object_id = c.object_id and ic.column_id = c.column_id group by ic.object_id,ic.index_id) ic" +
                " on ic.index_id = i.index_id and ic.object_id = i.object_id" +
                " where i.name is not null";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and o.name = '" + tableName + "'";
        }
        if (StrUtil.isNotEmpty(indexName)) {
            sql += " and i.name = '" + indexName + "'";
        }
        return super.listIndexFromDB(sql);
    }

    /**
     * https://learn.microsoft.com/en-us/sql/t-sql/statements/create-index-transact-sql?view=sql-server-ver17#syntax
     */
    @Override
    public String getIndexDDL(String catalog, String schema, String tableName, String indexName) throws SQLException {
        if (StrUtil.isEmpty(indexName)) {
            return "";
        } else {
            IndexDTO index = getIndexDetail(catalog, schema, tableName, indexName);
            if (Objects.nonNull(index)) {
                StringBuilder builder = new StringBuilder();
                builder.append("CREATE ");
                if (index.getIsUniqueness()) {
                    builder.append(" UNIQUE ");
                } else if ("CLUSTERED".equalsIgnoreCase(index.getIndexType()) || "NONCLUSTERED".equalsIgnoreCase(index.getIndexType())) {
                    builder.append(index.getIndexType());
                }
                builder.append(" INDEX ")
                        .append(index.getIndexName())
                        .append(" ON ")
                        .append(index.getSchemaName())
                        .append(Constants.SEPARATOR_DOT)
                        .append(index.getTableName())
                        .append(" (")
                        .append(index.getColumns())
                        .append(");");
                return builder.toString();
            } else {
                throw new SQLException(StrUtil.format("index {} does not exist in {}", indexName, schema));

            }
        }
    }

    @Override
    public String getIndexDDL(IndexDTO index, List<ObjectDTO> pks, List<ObjectDTO> fks) {
      return super.getIndexDDL(index,pks,fks);
    }

    @Override
    public List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return getConstraint(catalog, schemaPattern, tableName, PK);
    }

    @Override
    public List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return getConstraint(catalog, schemaPattern, tableName, FK);
    }

    private List<ObjectDTO> getConstraint(String catalog, String schemaPattern, String tableName, DbObjectType type) throws SQLException {
        List<ObjectDTO> list = new ArrayList<>();
        String constraintType = "";
        if (PK.equals(type)) {
            constraintType = "PK";
        } else if (FK.equals(type)) {
            constraintType = "F";
        }
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o1.name as constraint_name," +
                "    o1.type as constraint_type," +
                "    o2.name as table_name," +
                "    o1.create_date as create_time," +
                "    o1.modify_date as last_ddl_time" +
                " from sys.sysconstraints c" +
                " left join sys.all_objects o1" +
                " on c.constid = o1.object_id" +
                " left join sys.schemas s" +
                " on o1.schema_id = s.schema_id" +
                " left join sys.all_objects o2" +
                " on c.id = o2.object_id" +
                " where o1.type = '" + constraintType + "'";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and o2.name = '" + tableName + "'";
        }
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ObjectDTO obj = new ObjectDTO();
            obj.setCatalogName(rs.getString("catalog_name"));
            obj.setSchemaName(rs.getString("schema_name"));
            obj.setObjectName(rs.getString("constraint_name"));
            obj.setObjectType(type.name());
            obj.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            obj.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            list.add(obj);
        }
        JdbcUtil.close(conn, ps, rs);
        return list;
    }

    @SneakyThrows
    @Override
    protected String generateRenameSQL(String schema, String tableName, String indexName, String newName) {
        return StrUtil.format("exec sp_rename '{}.{}.{}',{},'INDEX'", schema, tableName, indexName, newName);
    }

    @SneakyThrows
    @Override
    protected String generateDropSQL(String schema, String tableName, String indexName) {
        return StrUtil.format("DROP INDEX {} ON {}.{}", indexName, schema, tableName);
    }

}
