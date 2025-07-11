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

package com.basedt.dms.plugins.datasource.impl.jdbc;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.DataTypeMapper;
import com.basedt.dms.plugins.datasource.types.*;

import java.sql.JDBCType;
import java.util.Objects;

public class JdbcDataTypeMapper implements DataTypeMapper<JDBCType, Type> {


    @Override
    public Type toType(JDBCType fromType) {
        if (Objects.isNull(fromType)) {
            return Types.NULL;
        }
        return switch (fromType) {
            case BIT -> Types.BIT;
            case TINYINT -> Types.TINYINT;
            case SMALLINT -> Types.SMALLINT;
            case INTEGER -> Types.INTEGER;
            case BIGINT -> Types.BIGINT;
            case FLOAT -> Types.FLOAT;
            case REAL -> Types.REAL;
            case DOUBLE -> Types.DOUBLE;
            case NUMERIC -> Types.NUMERIC;
            case DECIMAL -> Types.DECIMAL;
            case CHAR -> Types.CHAR;
            case VARCHAR -> Types.VARCHAR;
            case DATE -> Types.DATE;
            case TIME -> Types.TIME;
            case TIMESTAMP -> Types.TIMESTAMP;
            case BINARY -> Types.BINARY;
            case BLOB, VARBINARY, LONGVARBINARY -> Types.BLOB;
            case NULL -> Types.NULL;
            case CLOB -> Types.CLOB;
            case BOOLEAN -> Types.BOOLEAN;
            case NCHAR -> Types.NCHAR;
            case NVARCHAR -> Types.NVARCHAR;
            case LONGVARCHAR, LONGNVARCHAR -> Types.TEXT;
            case NCLOB -> Types.NCLOB;
            case TIMESTAMP_WITH_TIMEZONE -> Types.TIMESTAMP_TZ;
            default -> new ExtensionType(fromType.name().toLowerCase());
        };
    }

    @Override
    public JDBCType fromType(Type type) {
        if (type instanceof NullType) {
            return JDBCType.NULL;
        } else if (type instanceof BooleanType) {
            return JDBCType.BOOLEAN;
        } else if (type instanceof BitType) {
            return JDBCType.BIT;
        } else if (type instanceof TinyintType) {
            return JDBCType.TINYINT;
        } else if (type instanceof SmallIntType) {
            return JDBCType.SMALLINT;
        } else if (type instanceof IntegerType) {
            return JDBCType.INTEGER;
        } else if (type instanceof BigintType) {
            return JDBCType.BIGINT;
        } else if (type instanceof RealType) {
            return JDBCType.REAL;
        } else if (type instanceof FloatType) {
            return JDBCType.FLOAT;
        } else if (type instanceof DoubleType) {
            return JDBCType.DOUBLE;
        } else if (type instanceof DecimalType) {
            return JDBCType.DECIMAL;
        } else if (type instanceof NumericType || type instanceof NumberType) {
            return JDBCType.NUMERIC;
        } else if (type instanceof CharType) {
            return JDBCType.CHAR;
        } else if (type instanceof VarcharType || type instanceof StringType || type instanceof TextType) {
            return JDBCType.VARCHAR;
        } else if (type instanceof NCharType) {
            return JDBCType.NCHAR;
        } else if (type instanceof DateType) {
            return JDBCType.DATE;
        } else if (type instanceof TimeType) {
            return JDBCType.TIME;
        } else if (type instanceof TimestampType || type instanceof DatetimeType) {
            return JDBCType.TIMESTAMP;
        } else if (type instanceof TimestampWithTimeZoneType) {
            return JDBCType.TIMESTAMP_WITH_TIMEZONE;
        } else if (type instanceof BlobType) {
            return JDBCType.BLOB;
        } else if (type instanceof ClobType) {
            return JDBCType.CLOB;
        } else if (type instanceof NClobType) {
            return JDBCType.NCLOB;
        } else if (type instanceof BinaryType) {
            return JDBCType.BINARY;
        } else {
            throw new IllegalArgumentException(StrUtil.format("not supported type {}", type.name()));
        }
    }
}
