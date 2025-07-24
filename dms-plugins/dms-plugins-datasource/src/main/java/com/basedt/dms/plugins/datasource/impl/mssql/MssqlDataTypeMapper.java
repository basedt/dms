package com.basedt.dms.plugins.datasource.impl.mssql;

import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.types.Type;

public class MssqlDataTypeMapper extends JdbcDataTypeMapper {
    @Override
    public String fromType(Type type) {
        return super.fromType(type);
    }

    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        return super.toType(typeName, length, precision, scale);
    }
}
