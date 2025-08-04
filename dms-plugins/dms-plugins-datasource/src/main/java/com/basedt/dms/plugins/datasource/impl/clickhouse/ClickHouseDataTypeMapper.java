package com.basedt.dms.plugins.datasource.impl.clickhouse;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.types.*;
import com.basedt.dms.plugins.datasource.types.ck.*;

public class ClickHouseDataTypeMapper extends JdbcDataTypeMapper {

    public static final String INT8 = "Int8";
    public static final String INT16 = "Int16";
    public static final String INT32 = "Int32";
    public static final String INT64 = "Int64";
    public static final String INT128 = "Int128";
    public static final String INT256 = "Int256";
    public static final String UINT8 = "UInt8";
    public static final String UINT16 = "UInt16";
    public static final String UINT32 = "UInt32";
    public static final String UINT64 = "UInt64";
    public static final String UINT128 = "UInt128";
    public static final String UINT256 = "UInt256";
    public static final String FLOAT32 = "Float32";
    public static final String FLOAT64 = "Float64";
    public static final String CK_DECIMAL = "Decimal";
    public static final String DECIMAL32 = "Decimal32";
    public static final String DECIMAL64 = "Decimal64";
    public static final String DECIMAL128 = "Decimal128";
    public static final String DECIMAL256 = "Decimal256";
    public static final String CK_DATE = "Date";
    public static final String DATE32 = "Date32";
    public static final String DATETIME32 = "DateTime32";
    public static final String DATETIME64 = "DateTime64";
    public static final String CK_STRING = "String";
    public static final String FIXEDSTRING = "FixedString";

    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        if (StrUtil.isEmpty(typeName)) {
            return NullType.get();
        }
        return switch (typeName) {
            case BOOL, BOOLEAN -> BooleanType.get();
            case INT8 -> Int8Type.get();
            case INT16 -> Int16Type.get();
            case INT32 -> Int32Type.get();
            case INT64 -> Int64Type.get();
            case INT128 -> Int128Type.get();
            case INT256 -> Int256Type.get();
            case UINT8 -> UInt8Type.get();
            case UINT16 -> UInt16Type.get();
            case UINT32 -> UInt32Type.get();
            case UINT64 -> UInt64Type.get();
            case UINT128 -> UInt128Type.get();
            case UINT256 -> UInt256Type.get();
            case FLOAT32 -> Float32Type.get();
            case FLOAT64 -> Float64Type.get();
            case DECIMAL, CK_DECIMAL -> DecimalType.get(precision, scale);
            case DECIMAL32 -> Decimal32Type.get(scale);
            case DECIMAL64 -> Decimal64Type.get(scale);
            case DECIMAL128 -> Decimal128Type.get(scale);
            case DECIMAL256 -> Decimal256Type.get(scale);
            case DATE, CK_DATE -> DateType.get();
            case DATE32 -> Date32Type.get();
            case DATETIME -> DatetimeType.get();
            case DATETIME32 -> DateTime32Type.get();
            case DATETIME64 -> DateTime64Type.get();
            case CK_STRING -> CkStringType.get();
            case FIXEDSTRING -> FixedStringType.get(length);
            default -> new ExtensionType(typeName.toLowerCase());
        };
    }

    @Override
    public String fromType(Type type) {
        if (type instanceof BooleanType) {
            return BOOL;
        } else if (type instanceof Int8Type ||
                type instanceof Int16Type ||
                type instanceof Int32Type ||
                type instanceof Int64Type ||
                type instanceof Int128Type ||
                type instanceof Int256Type ||
                type instanceof UInt8Type ||
                type instanceof UInt16Type ||
                type instanceof UInt32Type ||
                type instanceof UInt64Type ||
                type instanceof UInt128Type ||
                type instanceof UInt256Type) {
            return type.formatString();
        } else if (type instanceof Float32Type ||
                type instanceof Float64Type) {
            return type.formatString();
        } else if (type instanceof DecimalType ||
                type instanceof Decimal32Type ||
                type instanceof Decimal64Type ||
                type instanceof Decimal128Type ||
                type instanceof Decimal256Type) {
            return type.formatString();
        } else if (type instanceof DateType ||
                type instanceof Date32Type ||
                type instanceof DatetimeType ||
                type instanceof DateTime32Type ||
                type instanceof DateTime64Type) {
            return type.formatString();
        } else if (type instanceof CkStringType ||
                type instanceof FixedStringType) {
            return type.formatString();
        } else if (type instanceof ExtensionType) {
            return type.formatString();
        } else {
            throw new IllegalArgumentException(StrUtil.format("not supported type {}", type.name()));
        }
    }

    @Override
    protected ColumnDTO toColumnDTO(String formatedTypeName) {
        if (StrUtil.isEmpty(formatedTypeName)) {
            return null;
        } else if (formatedTypeName.contains("Decimal") &&
                formatedTypeName.contains("(")
                && !formatedTypeName.contains(",")) {
            ColumnDTO column = new ColumnDTO();
            column.setDataType(StrUtil.subBefore(formatedTypeName, "(", false));
            column.setDataScale(Integer.parseInt(StrUtil.sub(formatedTypeName, formatedTypeName.indexOf("(") + 1, formatedTypeName.indexOf(")"))));
            return column;
        } else {
            return super.toColumnDTO(formatedTypeName);
        }
    }
}
