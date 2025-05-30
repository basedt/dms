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
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcForeignTableHandler;

import java.sql.SQLException;
import java.util.List;

public class OracleFgnTableHandler extends JdbcForeignTableHandler {

    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    t.owner as schema_name," +
                "    t.object_name as object_name," +
                "    'FOREIGN_TABLE' as object_type," +
                "    c.comments as remark," +
                "    t.created as create_time," +
                "    t.last_ddl_time as last_ddl_time" +
                " from all_objects t" +
                " join all_external_tables et" +
                " on t.owner = et.owner" +
                " left join all_tab_comments c" +
                " on t.owner = c.owner" +
                " and t.object_name = c.table_name" +
                " where t.owner = '" + schemaPattern.toUpperCase() + "'";
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and t.object_name = '" + tablePattern.toUpperCase() + "'";
        }
        return super.listFgnTableFromDB(sql);
    }

}
