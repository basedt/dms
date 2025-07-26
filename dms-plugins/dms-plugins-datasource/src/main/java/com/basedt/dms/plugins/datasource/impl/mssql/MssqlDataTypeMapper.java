package com.basedt.dms.plugins.datasource.impl.mssql;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.types.*;

public class MssqlDataTypeMapper extends JdbcDataTypeMapper {

    static final String DATETIME2 = "datetime2";
    static final String DATETIME_OFFSET = "datetimeoffset";

    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        if (StrUtil.isEmpty(typeName)) {
            return NullType.get();
        }
        return switch (typeName) {
            case BIT -> BitType.get(length);
            case TINYINT -> TinyIntType.get();
            case SMALLINT -> SmallIntType.get();
            case INT -> IntegerType.get();
            case BIGINT -> BigintType.get();
            case DECIMAL -> DecimalType.get(precision, scale);
            case NUMERIC -> NumericType.get(precision, scale);
            case FLOAT -> FloatType.get(length);
            case REAL -> RealType.get();
            case CHAR -> CharType.get(length);
            case VARCHAR -> VarcharType.get(length);
            case NCHAR -> NCharType.get(length);
            case NVARCHAR -> NVarcharType.get(length);
            case TEXT -> TextType.get();
            case DATE -> DateType.get();
            case TIME -> TimeType.get();
            case DATETIME -> DatetimeType.get();
            case DATETIME2 -> TimestampType.get();
            case DATETIME_OFFSET -> TimestampWithTimeZoneType.get();
            //  case TIMESTAMP -> TimestampType.get();
            case BINARY -> BinaryType.get(length);
            case VARBINARY -> VarbinaryType.get(length);
            default -> new ExtensionType(typeName.toLowerCase());
        };
    }

    @Override
    public String fromType(Type type) {
        if (type instanceof BitType) {
            return type.name();
        } else if (type instanceof TinyIntType ||
                type instanceof SmallIntType ||
                type instanceof IntegerType ||
                type instanceof BigintType
        ) {
            return type.formatString();
        } else if (type instanceof DecimalType ||
                type instanceof NumericType ||
                type instanceof FloatType ||
                type instanceof RealType
        ) {
            return type.formatString();
        } else if (type instanceof CharType ||
                type instanceof VarcharType ||
                type instanceof NCharType ||
                type instanceof NVarcharType ||
                type instanceof TextType) {
            return type.formatString();
        } else if (type instanceof DateType ||
                type instanceof TimeType ||
                type instanceof DatetimeType) {
            return type.formatString();
        } else if (type instanceof TimestampWithTimeZoneType) {
            return DATETIME_OFFSET;
        } else if (type instanceof TimestampType) {
            return DATETIME2;
        } else if (type instanceof BinaryType ||
                type instanceof VarbinaryType
        ) {
            return type.formatString();
        } else if (type instanceof ExtensionType) {
            return type.formatString();
        } else {
            throw new IllegalArgumentException(StrUtil.format("not supported type {}", type.name()));
        }
    }
}
