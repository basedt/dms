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

package com.basedt.dms.plugins.datasource.impl.oracle;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcTableHandler;

import java.sql.SQLException;
import java.util.List;

public class OracleTableHandler extends JdbcTableHandler {

    @Override
    public List<TableDTO> listTables(String catalog, String schema, String tableName) throws SQLException {
        return super.listTables(StrUtil.isEmpty(catalog) ? catalog : catalog.toUpperCase(),
                StrUtil.isEmpty(schema) ? schema : schema.toUpperCase(),
                StrUtil.isEmpty(tableName) ? tableName : tableName.toUpperCase());
    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    o.object_type as  object_type," +
                "    c.comments as remark," +
                "    d.total_data_bytes + i.total_index_bytes as data_bytes," +
                "    nvl(p.num_rows,t.num_rows) as table_rows," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time," +
                "    null as last_access_time" +
                " from all_objects o" +
                " join all_tables t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.table_name" +
                " left join all_tab_comments c" +
                " on t.table_name = c.table_name" +
                " and t.owner = c.owner" +
                " left join" +
                "    (" +
                "        select" +
                "            s.owner," +
                "            s.segment_name," +
                "            sum(nvl(s.bytes,0) + nvl(b.lob_data_bytes,0)) as total_data_bytes" +
                "        from dba_segments s" +
                "        left join (select" +
                "                       b.owner," +
                "                       b.table_name," +
                "                       sum(t.bytes) as lob_data_bytes" +
                "                    from dba_lobs b" +
                "                    join dba_segments t" +
                "                    on b.owner = t.owner" +
                "                    and  t.segment_name = b.segment_name" +
                "                    group by b.owner, b.table_name" +
                "                    ) b" +
                "        on s.owner = b.owner" +
                "        and s.segment_name = b.table_name" +
                "        where s.segment_type in ('TABLE', 'TABLE PARTITION', 'TABLE SUBPARTITION')" +
                "        group by s.owner, s.segment_name" +
                "    ) d" +
                " on t.table_name = d.segment_name" +
                " and t.owner = d.owner" +
                " left join" +
                "    (" +
                "        select" +
                "            t1.table_owner as owner," +
                "            t1.table_name," +
                "            sum(t2.bytes) as total_index_bytes" +
                "        from" +
                "            (" +
                "                select" +
                "                    t.table_owner," +
                "                    t.table_name," +
                "                    t.index_name" +
                "                from all_indexes t" +
                "                union" +
                "                select" +
                "                    t.owner," +
                "                    t.table_name," +
                "                    t.index_name" +
                "                from dba_lobs t" +
                "            ) t1" +
                "        join dba_segments t2" +
                "        on t1.table_owner = t2.owner" +
                "        and t1.index_name = t2.segment_name" +
                "      group by t1.table_owner, t1.table_name) i" +
                " on t.table_name = i.table_name" +
                " and t.owner = i.owner" +
                " left join" +
                "     (" +
                "         select" +
                "             table_owner as owner," +
                "             table_name," +
                "             nvl(sum(num_rows), 0) as num_rows" +
                "         from all_tab_partitions o" +
                "         group by table_owner, table_name) p" +
                " on t.table_name = p.table_name" +
                " and t.owner = p.owner" +
                " where o.owner ='" + schemaPattern.toUpperCase() + "' ";
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and o.object_name = '" + tablePattern.toUpperCase() + "'";
        }
        return super.listTableFromDB(sql);
    }

    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    t.owner as schema_name," +
                "    t.table_name as table_name," +
                "    t.column_name as column_name," +
                "    t.data_type as data_type," +
                "    t.data_length as data_length," +
                "    t.data_precision as data_precision," +
                "    t.data_scale as data_scale," +
                "    decode(t.nullable,'Y',1,'N',0,0) as is_nullable," +
                "    t.data_default as default_value," +
                "    t.column_id as column_ordinal," +
                "    c.comments as remark" +
                " from all_tab_columns t" +
                " left join all_tab_comments c" +
                " on t.owner = c.owner" +
                " and t.table_name = c.table_name" +
                " and t.column_name = c.table_name" +
                " where t.owner = '" + schemaPattern.toUpperCase() + "'" +
                " and t.table_name = '" + tableName.toUpperCase() + "'";
        return super.listColumnFromTable(sql);
    }
}
