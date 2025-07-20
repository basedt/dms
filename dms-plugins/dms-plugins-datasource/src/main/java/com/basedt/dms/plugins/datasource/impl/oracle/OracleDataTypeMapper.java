package com.basedt.dms.plugins.datasource.impl.oracle;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.types.*;

public class OracleDataTypeMapper extends JdbcDataTypeMapper {

    static final String VARCHAR2 = "varchar2";
    static final String NVARCHAR2 = "nvarchar2";
    static final String LONG = "long";
    static final String LONG_RAW = "long raw";
    static final String RAW = "raw";

    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        if (StrUtil.isEmpty(typeName)) {
            return NullType.get();
        }
        return switch (typeName.toLowerCase()) {
            case CHAR -> CharType.get(length);
            case NCHAR -> NCharType.get(length);
            case VARCHAR, VARCHAR2 -> Varchar2Type.get(length);
            case NVARCHAR, NVARCHAR2 -> NVarchar2Type.get(length);
            case NUMBER, DECIMAL, NUMERIC -> NumberType.get(precision, scale);
            case REAL -> RealType.get();
            case FLOAT -> FloatType.get(length);
            case DOUBLE_PRECISION -> DoubleType.get();
            case DATE -> DateType.get();
            case TIMESTAMP -> TimestampType.get();
            case TIMESTAMP_WITH_TIME_ZONE, TIMESTAMP_WITH_LOCAL_TIME_ZONE -> TimestampWithTimeZoneType.get();
            case RAW -> RawType.get(length);
            case BLOB, LONG_RAW -> BlobType.get();
            case CLOB, LONG -> ClobType.get();
            case NCLOB -> NClobType.get();
            default -> new ExtensionType(typeName.toLowerCase());
        };
    }

    @Override
    public String fromType(Type type) {
        if (type instanceof CharType ||
                type instanceof NCharType ||
                type instanceof Varchar2Type ||
                type instanceof NVarchar2Type) {
            return type.formatString();
        } else if (type instanceof NumberType ||
                type instanceof RealType ||
                type instanceof FloatType ||
                type instanceof DoubleType) {
            return type.formatString();
        } else if (type instanceof DateType ||
                type instanceof TimestampType ||
                type instanceof TimestampWithTimeZoneType) {
            return type.formatString();
        } else if (type instanceof BlobType ||
                type instanceof ClobType ||
                type instanceof NClobType ||
                type instanceof RawType) {
            return type.formatString();
        } else if (type instanceof ExtensionType) {
            return type.formatString();
        } else {
            throw new IllegalArgumentException(StrUtil.format("not supported type {}", type.name()));
        }
    }
}
