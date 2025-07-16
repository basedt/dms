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
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.basedt.dms.plugins.datasource.dto.ViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcViewHandler;

import java.sql.SQLException;
import java.util.List;

public class OracleViewHandler extends JdbcViewHandler {

    @Override
    protected String generateRenameSQL(String schema, String viewName, String newName) {
        return StrUtil.format("RENAME {} TO {}", viewName, newName);
    }

    @Override
    public List<ViewDTO> listViews(String catalog, String schema, String viewName) throws SQLException {
        return super.listViews(StrUtil.isEmpty(catalog) ? catalog : catalog.toUpperCase(),
                StrUtil.isEmpty(schema) ? schema : schema.toUpperCase(),
                StrUtil.isEmpty(viewName) ? viewName : viewName.toUpperCase());
    }

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schema, String viewName) throws SQLException {
        String sql = "select" +
                "    null as catalog_name," +
                "    t.owner as schema_name," +
                "    t.view_name as object_name," +
                "    'VIEW' as object_type," +
                "    c.comments as remark," +
                "    t.text as query_sql," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_views t" +
                " left join all_tab_comments c on" +
                " t.owner = c.owner" +
                " and t.view_name = c.table_name" +
                " left join all_objects o" +
                " on t.owner = o.owner" +
                " and t.view_name = o.object_name" +
                " where t.owner = '" + schema.toUpperCase() + "'";
        if (StrUtil.isNotEmpty(viewName)) {
            sql += " and t.view_name = '" + viewName.toUpperCase() + "'";
        }
        return super.listViewFromDB(sql);
    }

    @Override
    public String getViewDDL(String catalog, String schema, String viewName) throws SQLException {
        String ddl = super.getViewDDL(catalog, schema, viewName);
        return SQLUtils.format(ddl, DbType.oracle);
    }
}
