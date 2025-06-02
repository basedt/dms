package com.basedt.dms.plugins.datasource.impl.clickhouse;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.FunctionDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcFunctionHandler;

import java.sql.SQLException;
import java.util.List;

public class ClickHouseFunctionHandler extends JdbcFunctionHandler {

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select" + catalog +
                "     as catalog_name," + schemaPattern +
                "     as schema_name," +
                "    t.name as object_name," +
                "    'FUNCTION' as object_type," +
                "    '' as  source_code," +
                "    toDateTime('1970-01-01 00:00:00') as create_time," +
                "    toDateTime('1970-01-01 00:00:00') as last_ddl_time," +
                "    t.description as remark" +
                " from system.functions t " +
                " where 1=1 ";
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and t.name = '" + functionPattern + "'";
        }
        return super.listFunctionFromDB(sql);
    }
}
