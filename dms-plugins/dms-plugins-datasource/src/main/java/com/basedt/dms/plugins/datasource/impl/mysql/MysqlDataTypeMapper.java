package com.basedt.dms.plugins.datasource.impl.mysql;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.types.*;

public class MysqlDataTypeMapper extends JdbcDataTypeMapper {

    static final String BIGINT_UNSIGNED = "bigint unsigned";
    static final String INT_UNSIGNED = "int unsigned";
    static final String INTEGER_UNSIGNED = "integer unsigned";
    static final String TINYINT_UNSIGNED = "tinyint unsigned";
    static final String SMALLINT_UNSIGNED = "smallint unsigned";
    static final String MEDIUMINT_UNSIGNED = "mediumint unsigned";
    static final String LONG_VARBINARY = "long varbinary";
    static final String LONG_VARCHAR = "long varchar";


    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        if (StrUtil.isEmpty(typeName)) {
            return NullType.get();
        }
        return switch (typeName) {
            case BIT -> BitType.get(length);
            case BOOLEAN, BOOL -> BooleanType.get();
            case TINYINT -> TinyIntType.get();
            case TINYINT_UNSIGNED -> TinyIntUnsignedType.get();
            case SMALLINT -> SmallIntType.get();
            case SMALLINT_UNSIGNED -> SmallIntUnsignedType.get();
            case MEDIUMINT -> MediumIntType.get();
            case MEDIUMINT_UNSIGNED -> MediumIntUnsignedType.get();
            case INT, INTEGER -> IntegerType.get();
            case INT_UNSIGNED, INTEGER_UNSIGNED -> IntegerUnsignedType.get();
            case BIGINT -> BigintType.get();
            case BIGINT_UNSIGNED -> BigintUnsignedType.get();
            case DECIMAL -> DecimalType.get(precision, scale);
            case NUMERIC -> NumericType.get(precision, scale);
            case FLOAT -> FloatType.get(length);
            case REAL -> RealType.get();
            case DOUBLE, DOUBLE_PRECISION -> DoubleType.get();
            case CHAR -> CharType.get(length);
            case VARCHAR -> VarcharType.get(length);
            case VARBINARY -> VarbinaryType.get(length);
            case TEXT -> TextType.get();
            case TINYTEXT -> TinyTextType.get();
            case MEDIUMTEXT, LONG_VARCHAR -> MediumTextType.get();
            case LONGTEXT -> LongTextType.get();
            case DATE -> DateType.get();
            case DATETIME -> DatetimeType.get();
            case TIME -> TimeType.get();
            case TIMESTAMP -> TimestampType.get();
            case YEAR -> YearType.get();
            case BLOB -> BlobType.get();
            case TINYBLOB -> TinyBlobType.get();
            case MEDIUMBLOB -> MediumBlobType.get();
            case LONGBLOB, LONG_VARBINARY -> LongBlobType.get();
            case BINARY -> BinaryType.get(length);
            default -> new ExtensionType(typeName.toLowerCase());
        };
    }

    @Override
    public String fromType(Type type) {
        if (type instanceof BooleanType) {
            return BOOL;
        } else if (type instanceof BitType) {
            return type.formatString();
        } else if (type instanceof TinyIntType ||
                type instanceof TinyIntUnsignedType ||
                type instanceof SmallIntType ||
                type instanceof SmallIntUnsignedType ||
                type instanceof MediumIntType ||
                type instanceof MediumIntUnsignedType ||
                type instanceof IntegerType ||
                type instanceof IntegerUnsignedType ||
                type instanceof BigintType ||
                type instanceof BigintUnsignedType) {
            return type.formatString();
        } else if (type instanceof DecimalType ||
                type instanceof NumericType ||
                type instanceof FloatType ||
                type instanceof RealType ||
                type instanceof DoubleType
        ) {
            return type.formatString();
        } else if (type instanceof CharType ||
                type instanceof VarcharType ||
                type instanceof VarbinaryType ||
                type instanceof TextType ||
                type instanceof TinyTextType ||
                type instanceof MediumTextType ||
                type instanceof LongTextType
        ) {
            return type.formatString();
        } else if (type instanceof DateType ||
                type instanceof DatetimeType ||
                type instanceof TimeType ||
                type instanceof TimestampType ||
                type instanceof YearType) {
            return type.formatString();
        } else if (type instanceof BlobType ||
                type instanceof TinyBlobType ||
                type instanceof MediumBlobType ||
                type instanceof LongBlobType ||
                type instanceof BinaryType) {
            return type.formatString();
        } else if (type instanceof ExtensionType) {
            return type.formatString();
        } else {
            throw new IllegalArgumentException(StrUtil.format("not supported type {}", type.name()));
        }
    }
}
