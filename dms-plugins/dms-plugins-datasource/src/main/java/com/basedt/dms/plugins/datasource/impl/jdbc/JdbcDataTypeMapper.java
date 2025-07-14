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
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.types.*;

import java.sql.JDBCType;

public class JdbcDataTypeMapper implements DataTypeMapper {

    @Override
    public Type toType(String typeName, Integer length, Integer precision, Integer scale) {
        if (StrUtil.isEmpty(typeName)) {
            return NullType.get();
        }
        if (JDBCType.BIT.getName().equalsIgnoreCase(typeName)) {
            return BitType.get(length);
        } else if (JDBCType.TINYINT.getName().equalsIgnoreCase(typeName)) {
            return TinyintType.get();
        } else if (JDBCType.SMALLINT.getName().equalsIgnoreCase(typeName)) {
            return SmallIntType.get();
        } else if (JDBCType.INTEGER.getName().equalsIgnoreCase(typeName)) {
            return IntegerType.get();
        } else if (JDBCType.BIGINT.getName().equalsIgnoreCase(typeName)) {
            return BigintType.get();
        } else if (JDBCType.FLOAT.getName().equalsIgnoreCase(typeName)) {
            return FloatType.get(length);
        } else if (JDBCType.REAL.getName().equalsIgnoreCase(typeName)) {
            return RealType.get();
        } else if (JDBCType.DOUBLE.getName().equalsIgnoreCase(typeName)) {
            return DoubleType.get();
        } else if (JDBCType.NUMERIC.getName().equalsIgnoreCase(typeName)) {
            return NumericType.get(precision, scale);
        } else if (JDBCType.DECIMAL.getName().equalsIgnoreCase(typeName)) {
            return DecimalType.get(precision, scale);
        } else if (JDBCType.CHAR.getName().equalsIgnoreCase(typeName)) {
            return CharType.get(length);
        } else if (JDBCType.VARCHAR.getName().equalsIgnoreCase(typeName)) {
            return VarcharType.get(length);
        } else if (JDBCType.DATE.getName().equalsIgnoreCase(typeName)) {
            return DateType.get();
        } else if (JDBCType.TIME.getName().equalsIgnoreCase(typeName)) {
            return TimeType.get();
        } else if (JDBCType.TIMESTAMP.getName().equalsIgnoreCase(typeName)) {
            return TimestampType.get();
        } else if (JDBCType.BINARY.getName().equalsIgnoreCase(typeName)) {
            return BinaryType.get();
        } else if (JDBCType.BLOB.getName().equalsIgnoreCase(typeName) ||
                JDBCType.VARBINARY.getName().equalsIgnoreCase(typeName) ||
                JDBCType.LONGVARBINARY.getName().equalsIgnoreCase(typeName)) {
            return BlobType.get();
        } else if (JDBCType.NULL.getName().equalsIgnoreCase(typeName)) {
            return NullType.get();
        } else if (JDBCType.CLOB.getName().equalsIgnoreCase(typeName)) {
            return ClobType.get();
        } else if (JDBCType.BOOLEAN.getName().equalsIgnoreCase(typeName)) {
            return BooleanType.get();
        } else if (JDBCType.NCHAR.getName().equalsIgnoreCase(typeName)) {
            return NCharType.get(length);
        } else if (JDBCType.NVARCHAR.getName().equalsIgnoreCase(typeName)) {
            return NVarcharType.get(length);
        } else if (JDBCType.LONGVARCHAR.getName().equalsIgnoreCase(typeName) ||
                JDBCType.LONGNVARCHAR.getName().equalsIgnoreCase(typeName)) {
            return TextType.get();
        } else if (JDBCType.NCLOB.getName().equalsIgnoreCase(typeName)) {
            return NClobType.get();
        } else if (JDBCType.TIME_WITH_TIMEZONE.getName().equalsIgnoreCase(typeName)) {
            return TimestampWithTimeZoneType.get();
        } else {
            return new ExtensionType(typeName.toLowerCase());
        }
    }


    @Override
    public String fromType(Type type) {
        if (type instanceof NullType) {
            return JDBCType.NULL.getName();
        } else if (type instanceof BooleanType) {
            return JDBCType.BOOLEAN.getName();
        } else if (type instanceof BitType) {
            return JDBCType.BIT.getName();
        } else if (type instanceof TinyintType) {
            return JDBCType.TINYINT.getName();
        } else if (type instanceof SmallIntType) {
            return JDBCType.SMALLINT.getName();
        } else if (type instanceof IntegerType) {
            return JDBCType.INTEGER.getName();
        } else if (type instanceof BigintType) {
            return JDBCType.BIGINT.getName();
        } else if (type instanceof RealType) {
            return JDBCType.REAL.getName();
        } else if (type instanceof FloatType) {
            return JDBCType.FLOAT.getName();
        } else if (type instanceof DoubleType) {
            return JDBCType.DOUBLE.getName();
        } else if (type instanceof DecimalType) {
            return JDBCType.DECIMAL.getName();
        } else if (type instanceof NumericType || type instanceof NumberType) {
            return JDBCType.NUMERIC.getName();
        } else if (type instanceof CharType) {
            return JDBCType.CHAR.getName();
        } else if (type instanceof VarcharType || type instanceof StringType || type instanceof TextType) {
            return JDBCType.VARCHAR.getName();
        } else if (type instanceof NCharType) {
            return JDBCType.NCHAR.getName();
        } else if (type instanceof DateType) {
            return JDBCType.DATE.getName();
        } else if (type instanceof TimeType) {
            return JDBCType.TIME.getName();
        } else if (type instanceof TimestampType || type instanceof DatetimeType) {
            return JDBCType.TIMESTAMP.getName();
        } else if (type instanceof TimestampWithTimeZoneType) {
            return JDBCType.TIMESTAMP_WITH_TIMEZONE.getName();
        } else if (type instanceof BlobType) {
            return JDBCType.BLOB.getName();
        } else if (type instanceof ClobType) {
            return JDBCType.CLOB.getName();
        } else if (type instanceof NClobType) {
            return JDBCType.NCLOB.getName();
        } else if (type instanceof BinaryType) {
            return JDBCType.BINARY.getName();
        } else {
            throw new IllegalArgumentException(StrUtil.format("not supported type {}", type.name()));
        }
    }

    protected ColumnDTO toColumnDTO(String formatedTypeName) {
        if (StrUtil.isEmpty(formatedTypeName)) {
            return null;
        } else if (formatedTypeName.contains(",")) {
            ColumnDTO column = new ColumnDTO();
            column.setColumnName(StrUtil.subBefore(formatedTypeName, "(", false));
            column.setDataPrecision(Integer.parseInt(StrUtil.sub(formatedTypeName, formatedTypeName.indexOf("(") + 1, formatedTypeName.indexOf(","))));
            column.setDataScale(Integer.parseInt(StrUtil.sub(formatedTypeName, formatedTypeName.indexOf(",") + 1, formatedTypeName.indexOf(")"))));
            return column;
        } else if (formatedTypeName.contains("(")) {
            ColumnDTO column = new ColumnDTO();
            column.setColumnName(StrUtil.subBefore(formatedTypeName, "(", false));
            column.setDataLength(Integer.parseInt(StrUtil.sub(formatedTypeName, formatedTypeName.indexOf("(") + 1, formatedTypeName.indexOf(")"))));
            return column;
        } else {
            ColumnDTO column = new ColumnDTO();
            column.setColumnName(formatedTypeName);
            return column;
        }
    }

    protected Type toType(String formatTypeName) {
        ColumnDTO col = toColumnDTO(formatTypeName);
        return toType(col.getDataType(), col.getDataLength(), col.getDataPrecision(), col.getDataScale());
    }
}
