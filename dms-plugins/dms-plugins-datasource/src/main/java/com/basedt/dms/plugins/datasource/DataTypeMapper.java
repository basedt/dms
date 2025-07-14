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

package com.basedt.dms.plugins.datasource;

import com.basedt.dms.plugins.datasource.types.Type;

public interface DataTypeMapper {

    String BOOL = "bool";
    String BOOLEAN = "boolean";
    String BIT = "bit";
    String TINYINT = "tinyint";
    String SMALLINT = "smallint";
    String INTEGER = "integer";
    String BIGINT = "bigint";
    String REAL = "real";
    String FLOAT = "float";
    String DOUBLE = "double";
    String DOUBLE_PRECISION = "double precision";
    String DECIMAL = "decimal";
    String NUMERIC = "numeric";
    String NUMBER = "number";
    String CHAR = "char";
    String VARCHAR = "varchar";
    String TEXT = "text";
    String NCHAR = "nchar";
    String NVARCHAR = "nvarchar";
    String STRING = "string";
    String DATE = "date";
    String TIME = "time";
    String TIMESTAMP = "timestamp";
    String BLOB = "blob";
    String CLOB = "clob";
    String NCLOB = "nclob";
    String JSON = "json";
    String JSONB = "jsonb";
    String BINARY = "binary";
    String VARBINARY = "varbinary";
    String NULL = "null";


    /**
     * Convert from a source type to a target type.
     *
     * @param typeName
     * @param length
     * @param precision
     * @param scale
     * @return
     */
    Type toType(String typeName, Integer length, Integer precision, Integer scale);

    /**
     * Convert from a format type name string to a target type.
     *
     * @param formatTypeName
     * @return
     */
//    Type toType(String formatTypeName);

    /**
     * Convert from a target type to a source type.
     *
     * @param type type
     * @return fromType
     */
    String fromType(Type type);

}
