/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.basedt.dms.plugins.datasource.impl.postgre;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.DataTypeMapper;
import com.basedt.dms.plugins.datasource.types.*;

/**
 * pg_catalog.pg_type
 */
public class PostgreDataTypeMapper implements DataTypeMapper {

    static final String BP_CHAR = "bpchar";
    static final String NAME = "name";
    static final String _CHAR = "\"char\"";
    static final String INT2 = "int2";
    static final String INT4 = "int4";
    static final String INT8 = "int8";
    static final String FLOAT4 = "float4";
    static final String FLOAT8 = "float8";
    static final String TIME_TZ = "timetz";
    static final String TIMESTAMP_TZ = "timestamptz";
    static final String TIMESTAMP_WITH_TIME_ZONE = "timestamp with time zone";
    static final String TIMESTAMP_WITHOUT_TIME_ZONE = "timestamp without time zone";
    static final String BYTEA = "bytea";
    static final String CHARACTER_VARYING = "character varying";

    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        if (StrUtil.isEmpty(typeName)) {
            return NullType.get();
        }
        return switch (typeName) {
            case BOOL, BOOLEAN -> BooleanType.get();
            case BIT -> BitType.get(length);
            case CHAR, BP_CHAR, _CHAR -> CharType.get(length);
            case NAME, VARCHAR, CHARACTER_VARYING -> VarcharType.get(length);
            case TEXT -> TextType.get();
            case INT2, SMALLINT -> SmallIntType.get();
            case INT4, INTEGER -> IntegerType.get();
            case INT8, BIGINT -> BigintType.get();
            case FLOAT4 -> RealType.get();
            case FLOAT8, DOUBLE_PRECISION -> DoubleType.get();
            case NUMERIC -> NumericType.get(precision, scale);
            case DATE -> DateType.get();
            case TIME -> TimeType.get();
            case TIME_TZ -> TimeWithTimeZoneType.get();
            case TIMESTAMP, TIMESTAMP_WITHOUT_TIME_ZONE -> TimestampType.get();
            case TIMESTAMP_TZ, TIMESTAMP_WITH_TIME_ZONE -> TimestampWithTimeZoneType.get();
            case JSON -> JsonType.get();
            case JSONB -> JsonbType.get();
            case BYTEA -> BinaryType.get();
            default -> new ExtensionType(typeName.toLowerCase());
        };
    }

    @Override
    public String fromType(Type type) {
        if (type instanceof BooleanType) {
            return BOOL;
        } else if (type instanceof BitType) {
            return type.formatString();
        } else if (type instanceof SmallIntType) {
            return INT2;
        } else if (type instanceof IntegerType) {
            return INT4;
        } else if (type instanceof BigintType) {
            return INT8;
        } else if (type instanceof RealType) {
            return FLOAT4;
        } else if (type instanceof DoubleType || type instanceof NumericType || type instanceof DecimalType) {
            return type.formatString();
        } else if (type instanceof CharType || type instanceof VarcharType || type instanceof TextType) {
            return type.formatString();
        } else if (type instanceof DateType || type instanceof TimeType || type instanceof TimestampType) {
            return type.formatString();
        } else if (type instanceof TimeWithTimeZoneType) {
            return TIME_TZ;
        } else if (type instanceof TimestampWithTimeZoneType) {
            return TIMESTAMP_TZ;
        } else if (type instanceof JsonType || type instanceof JsonbType) {
            return type.formatString();
        } else if (type instanceof BinaryType) {
            return BYTEA;
        } else {
            throw new IllegalArgumentException(StrUtil.format("not supported type {}", type.name()));
        }
    }

}
