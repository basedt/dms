package com.basedt.dms.plugins.datasource.impl.mssql;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.FunctionDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcFunctionHandler;

import java.sql.SQLException;
import java.util.List;

public class MssqlFunctionHandler extends JdbcFunctionHandler {

    @Override
    public List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        return listFunctionDetails(catalog, schemaPattern, functionPattern);
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o.name as object_name," +
                "    'FUNCTION' as object_type," +
                "    o.create_date as create_time," +
                "    o.modify_date as last_ddl_time," +
                "    m.definition as source_code" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " left join sys.all_sql_modules m" +
                " on o.object_id = m.object_id" +
                " where o.type in ('FN','IF','FS','AF','TF')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and o.name = '" + functionPattern + "'";
        }
        return super.listFunctionFromDB(sql);
    }
}
