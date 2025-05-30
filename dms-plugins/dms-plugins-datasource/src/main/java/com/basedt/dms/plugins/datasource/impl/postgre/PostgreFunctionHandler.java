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
import com.basedt.dms.plugins.datasource.dto.FunctionDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcFunctionHandler;

import java.sql.SQLException;
import java.util.List;

public class PostgreFunctionHandler extends JdbcFunctionHandler {

    @Override
    public List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        return listFunctionDetails(catalog, schemaPattern, functionPattern);
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select" +
                "    catalog_name," +
                "    schema_name," +
                "    function_name as object_name," +
                "    object_type," +
                "    source_code," +
                "    null as create_time," +
                "    null as last_ddl_time" +
                " from" +
                "    (" +
                "        select" +
                "            null as catalog_name," +
                "            n.nspname as schema_name," +
                "            p.proname as function_name," +
                "            'FUNCTION' as object_type," +
                "            p.prosrc as source_code," +
                "            row_number() over (partition by n.nspname,p.proname order by p.oid) as rn" +
                "        from pg_catalog.pg_namespace n" +
                "            join pg_catalog.pg_proc p" +
                "            on p.pronamespace = n.oid" +
                "        where p.prokind = 'f'" +
                "    ) tt" +
                " where tt.rn = 1 ";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and tt.schema_name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and tt.function_name = '" + functionPattern + "'";
        }
        return super.listFunctionFromDB(sql);
    }
}
