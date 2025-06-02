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
import com.basedt.dms.plugins.datasource.dto.SequenceDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcSequenceHandler;

import java.sql.SQLException;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.SEQUENCE;

public class PostgreSequenceHandler extends JdbcSequenceHandler {

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        String sql = " select " +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " 'SEQUENCE'as object_type," +
                " s.start_value as start_value," +
                " s.minimum_value as min_value," +
                " s.maximum_value as max_value," +
                " s.increment as increment_by," +
                " case when cycle_option = 'YES' then 1 else 0 end as is_cycle," +
                " null as last_value," +
                " null as create_time," +
                " null as last_ddl_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c " +
                " on n.oid = c.relnamespace  " +
                " join information_schema.sequences s" +
                " on n.nspname = s.sequence_schema " +
                " and c.relname = s.sequence_name " +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(SEQUENCE) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(sequencePattern)) {
            sql += " and c.relname like '%" + sequencePattern + "%'";
        }
        return super.listSequenceFromDB(sql);
    }
}
