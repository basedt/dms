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
import com.basedt.dms.plugins.datasource.dto.SequenceDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcSequenceHandler;

import java.sql.SQLException;
import java.util.List;

public class OracleSequenceHandler extends JdbcSequenceHandler {

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    'SEQUENCE' as object_type," +
                "    null as start_value," +
                "    null as min_value," +
                "    null as max_value," +
                "    null as increment_by," +
                "    null as is_cycle," +
                "    null as last_value," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_sequences s" +
                " on o.owner = s.sequence_owner" +
                " and o.object_name = s.sequence_name" +
                " where o.owner = '" + schemaPattern.toUpperCase() + "'";
        if (StrUtil.isNotEmpty(sequencePattern)) {
            sql += " and o.object_name = '" + sequencePattern.toUpperCase() + "'";
        }
        return super.listSequenceFromDB(sql);
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    'SEQUENCE' as object_type," +
                "    s.min_value as start_value," +
                "    s.min_value as min_value," +
                "    s.max_value as max_value," +
                "    s.increment_by as increment_by," +
                "    s.cycle_flag as is_cycle," +
                "    s.last_number as last_value," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_sequences s" +
                " on o.owner = s.sequence_owner" +
                " and o.object_name = s.sequence_name" +
                " where o.owner = '" + schemaPattern.toUpperCase() + "'";
        if (StrUtil.isNotEmpty(sequencePattern)) {
            sql += " and o.object_name = '" + sequencePattern.toUpperCase() + "'";
        }
        return super.listSequenceFromDB(sql);
    }
}
