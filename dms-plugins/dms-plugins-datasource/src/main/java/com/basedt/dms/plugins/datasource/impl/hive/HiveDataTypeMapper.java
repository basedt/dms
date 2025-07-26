package com.basedt.dms.plugins.datasource.impl.hive;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.types.*;

public class HiveDataTypeMapper extends JdbcDataTypeMapper {

    static final String TIMESTAMP_WITH_LOCAL_TIME_ZONE = "timestamp with local time zone";

    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        if (StrUtil.isEmpty(typeName)) {
            return NullType.get();
        }
        return switch (typeName) {
            case BOOLEAN -> BooleanType.get();
            case TINYINT -> TinyIntType.get();
            case SMALLINT -> SmallIntType.get();
            case INT -> IntegerType.get();
            case BIGINT -> BigintType.get();
            case FLOAT -> FloatType.get(length);
            case DOUBLE -> DoubleType.get();
            case DECIMAL -> DecimalType.get(precision, scale);
            case CHAR -> CharType.get(length);
            case STRING -> StringType.get();
            case VARCHAR -> VarcharType.get(length);
            case DATE -> DateType.get();
            case TIMESTAMP -> TimestampType.get();
            case TIMESTAMP_WITH_LOCAL_TIME_ZONE -> TimestampWithTimeZoneType.get();
            case BINARY -> BinaryType.get(length);
            default -> new ExtensionType(typeName.toLowerCase());
        };
    }

    @Override
    public String fromType(Type type) {
        if (type instanceof BooleanType) {
            return BOOLEAN;
        } else if (type instanceof TinyIntType ||
                type instanceof SmallIntType ||
                type instanceof IntegerType ||
                type instanceof BigintType
        ) {
            return type.formatString();
        } else if (type instanceof FloatType || type instanceof DecimalType) {
            return type.formatString();
        } else if (type instanceof DoubleType) {
            return type.name();
        } else if (type instanceof CharType ||
                type instanceof StringType ||
                type instanceof VarcharType) {
            return type.formatString();
        } else if (type instanceof DateType || type instanceof TimestampType) {
            return type.formatString();
        } else if (type instanceof TimestampWithTimeZoneType) {
            return TIMESTAMP_WITH_LOCAL_TIME_ZONE;
        } else if (type instanceof BinaryType) {
            return type.formatString();
        } else if (type instanceof ExtensionType) {
            return type.formatString();
        } else {
            throw new IllegalArgumentException(StrUtil.format("not supported type {}", type.name()));
        }
    }
}
