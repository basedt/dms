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
package com.basedt.dms.plugins.datasource.enums;

public enum DataSourceType {
    MYSQL("mysql", "Mysql"),
    ORACLE("oracle", "Oracle"),
    POSTGRESQL("postgreSQL", "PostgreSQL"),
    MSSQL("mssql", "SQL Server"),
    DORIS("doris", "Doris"),
    HOLOGRES("hologres", "Hologres"),
    GAUSSDB("gaussdb", "GaussDB"),
    CLICKHOUSE("clickhouse", "ClickHouse"),
    MARIADB("mariadb", "MariaDB"),
    POLARDB_MYSQL("polardb_mysql", "PolarDB"),
    POLARDB_POSTGRE("polardb_postgre", "PolarDB"),
    GREENPLUM("greenplum", "Greenplum"),
    APACHEHIVE("apachehive", "Apache Hive");

    private final String value;
    private final String label;

    DataSourceType(String value, String label) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}
