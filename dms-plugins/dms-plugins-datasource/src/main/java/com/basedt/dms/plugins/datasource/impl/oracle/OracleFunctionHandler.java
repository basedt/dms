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
import com.basedt.dms.plugins.datasource.dto.FunctionDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcFunctionHandler;

import java.sql.SQLException;
import java.util.List;

public class OracleFunctionHandler extends JdbcFunctionHandler {

    @Override
    public List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select" +
                "     null as catalog_name," +
                "     o.owner as schema_name," +
                "     o.object_name as object_name," +
                "     'FUNCTION' as object_type," +
                "     null as source_code," +
                "     o.created as create_time," +
                "     o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_procedures s" +
                " on o.owner = s.owner" +
                " and o.object_name = s.object_name" +
                " where o.owner = '" + schemaPattern.toUpperCase() + "'" +
                " and o.object_type = 'FUNCTION'";
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and o.object_name = '" + functionPattern.toUpperCase() + "'";
        }
        return super.listFunctionFromDB(sql);
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select" +
                "     null as catalog_name," +
                "     o.owner as schema_name," +
                "     o.object_name as object_name," +
                "     'FUNCTION' as object_type," +
                "     dbms_metadata.get_ddl(o.object_type,o.object_name,o.owner) as source_code," +
                "     o.created as create_time," +
                "     o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_procedures s" +
                " on o.owner = s.owner" +
                " and o.object_name = s.object_name" +
                " where o.owner = '" + schemaPattern.toUpperCase() + "'" +
                " and o.object_type = 'FUNCTION'";
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and o.object_name = '" + functionPattern.toUpperCase() + "'";
        }
        return super.listFunctionFromDB(sql);
    }
}
