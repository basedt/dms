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

package com.basedt.dms.plugins.datasource.types;

public class Types {

    public static Type NULL = NullType.get();

    public static Type BOOLEAN = BooleanType.get();

    public static Type BIT = BitType.get();

    public static Type TINYINT = TinyintType.get();

    public static Type SMALLINT = SmallIntType.get();

    public static Type INTEGER = IntegerType.get();

    public static Type BIGINT = BigintType.get();

    public static Type REAL = RealType.get();

    public static Type FLOAT = FloatType.get();

    public static Type DOUBLE = DoubleType.get();

    public static Type DECIMAL = DecimalType.get();

    public static Type NUMERIC = NumericType.get();

    public static Type NUMBER = NumberType.get();

    public static Type CHAR = CharType.get();

    public static Type VARCHAR = VarcharType.get();

    public static Type NCHAR = NCharType.get();

    public static Type NVARCHAR = NVarcharType.get();

    public static Type STRING = StringType.get();

    public static Type TEXT = TextType.get();

    public static Type DATE = DateType.get();

    public static Type TIME = TimeType.get();

    public static Type DATETIME = DatetimeType.get();

    public static Type TIMESTAMP = TimestampType.get();

    public static Type TIMESTAMP_TZ = TimestampWithTimeZoneType.get();

    public static Type BLOB = BlobType.get();

    public static Type CLOB = ClobType.get();

    public static Type NCLOB = NClobType.get();

    public static Type BINARY = BinaryType.get();

}
