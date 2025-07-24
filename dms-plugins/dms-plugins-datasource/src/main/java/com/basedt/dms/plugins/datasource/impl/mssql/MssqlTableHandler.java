package com.basedt.dms.plugins.datasource.impl.mssql;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcTableHandler;

import java.sql.SQLException;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.TABLE;

public class MssqlTableHandler extends JdbcTableHandler {

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
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o.name as object_name," +
                "    'TABLE' as object_type," +
                "    p.rows as table_rows," +
                "    p.size as data_bytes," +
                "    ep.value as remark," +
                "    o.create_date as create_time," +
                "    o.modify_date as last_ddl_time," +
                "    ius.last_user_scan as last_access_time" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " left join sys.dm_db_index_usage_stats ius" +
                " on o.object_id = ius.object_id" +
                " and ius.index_id in (0,1)" +
                " left join sys.extended_properties ep" +
                " on o.object_id = ep.major_id" +
                " and ep.minor_id = 0" +
                " left join (select s.object_id,sum(s.rows) as rows,sum(a.total_pages) * 8 as size from sys.partitions s inner join sys.allocation_units a on s.partition_id = a.container_id where index_id < 2 group by object_id) p " +
                " on o.object_id = p.object_id" +
                " where o.type in ('U','S','IT')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and o.name = '" + tablePattern + "'";
        }
        return super.listTableFromDB(sql);
    }

    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o.name as table_name," +
                "    c.name as column_name," +
                "    t1.name as data_type," +
                "    c.max_length as data_length," +
                "    c.precision as data_precision," +
                "    c.scale as data_scale," +
                "    stuff(stuff(dc.definition, 1, 1, ''), len(dc.definition) - 1, 1, '') as default_value," +
                "    c.column_id as column_ordinal," +
                "    ep.value as remark," +
                "    c.is_nullable as is_nullable," +
                "    case when ic.object_id is not null then 1 else 0 end as auto_increment" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " join sys.all_columns c" +
                " on o.object_id = c.object_id" +
                " left join sys.extended_properties ep" +
                " on c.object_id = ep.major_id" +
                " and c.column_id = ep.minor_id" +
                " and ep.major_id >0" +
                " left join sys.types t1" +
                " on c.user_type_id = t1.user_type_id" +
                " left join sys.default_constraints dc" +
                " on c.default_object_id = dc.object_id" +
                " left join sys.identity_columns ic" +
                " on c.object_id = ic.object_id " +
                " and c.column_id = ic.column_id" +
                " where o.type in ('U','S','IT','V')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and o.name = '" + tableName + "'";
        }
        return super.listColumnFromTable(sql);
    }

    @Override
    protected String generateRenameSQL(String schema, String tableName, String newName) {
        return StrUtil.format("exec sp_rename '{}.{}',{},'OBJECT'", schema, tableName, newName);
    }

    @Override
    public String getTableDDL(String catalog, String schema, String tableName) throws SQLException {
        return super.getTableDDL(catalog, schema, tableName);
    }

    @Override
    public String getTableDDL(TableDTO table) throws SQLException {
        return super.getTableDDL(table);
    }
}
