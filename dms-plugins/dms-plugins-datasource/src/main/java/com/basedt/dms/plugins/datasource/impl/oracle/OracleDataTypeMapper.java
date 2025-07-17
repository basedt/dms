package com.basedt.dms.plugins.datasource.impl.oracle;

import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.types.Type;

public class OracleDataTypeMapper extends JdbcDataTypeMapper {

    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        return super.toType(typeName, length, precision, scale);
    }

    @Override
    public String fromType(Type type) {
        return super.fromType(type);
    }
}
