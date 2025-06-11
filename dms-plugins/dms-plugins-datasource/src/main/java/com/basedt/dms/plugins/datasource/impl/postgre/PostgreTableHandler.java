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
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcTableHandler;

import java.sql.SQLException;
import java.util.List;

public class PostgreTableHandler extends JdbcTableHandler {

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " case c.relkind when 'r' then 'TABLE' when 'i' then 'INDEX' when 'v' then 'VIEW' when 'S' then 'SEQUENCE'" +
                " when 'f' then 'FOREIGN_TABLE' when 'm' then 'MATERIALIZED_VIEW' else null end as object_type," +
                " d.description as remark," +
                " pg_table_size(concat_ws('.',n.nspname, c.relname)) as data_bytes," +
                " c.reltuples as table_rows," +
                " null as create_time," +
                " null as last_ddl_time," +
                " null as last_access_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c " +
                " on n.oid = c.relnamespace  " +
                " left join pg_catalog.pg_description d " +
                " on c.oid  = d.objoid " +
                " and d.objsubid = 0" +
                " and d.classoid  = 'pg_class'::regclass" +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(type) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and c.relname = '" + tablePattern + "'";
        }
        return super.listTableFromDB(sql);
    }

    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select" +
                " t.table_catalog as catalog_name," +
                " coalesce(t.table_schema,n.nspname) as schema_name," +
                " coalesce(t.table_name,c.relname) as table_name," +
                " coalesce(t.column_name,attr.attname) as column_name," +
                " coalesce(t.data_type,format_type(attr.atttypid,null)) as data_type," +
                " t.character_maximum_length as data_length," +
                " t.numeric_precision as data_precision," +
                " t.numeric_scale as data_scale," +
                " t.column_default as default_value," +
                " coalesce(t.ordinal_position,attr.attnum) as column_ordinal," +
                " col_description(attr.attrelid, attr.attnum) as remark," +
                " t.is_nullable as is_nullable" +
                " from pg_catalog.pg_class c" +
                " join pg_catalog.pg_namespace n" +
                " on n.oid = c.relnamespace" +
                " left join pg_catalog.pg_attribute attr" +
                " on attr.attrelid = c.oid" +
                " left join information_schema.columns t" +
                " on n.nspname = t.table_schema" +
                " and c.relname = t.table_name" +
                " and attr.attname = t.column_name" +
                " where c.relkind in ('r','v','m','f')" +
                " and attr.attnum > 0";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and coalesce(t.table_schema,n.nspname) = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and coalesce(t.table_name,c.relname) = '" + tableName + "'";
        }
        return super.listColumnFromTable(sql);
    }
}
