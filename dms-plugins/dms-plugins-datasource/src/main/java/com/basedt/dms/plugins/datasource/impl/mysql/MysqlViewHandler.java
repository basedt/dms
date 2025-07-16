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

package com.basedt.dms.plugins.datasource.impl.mysql;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.basedt.dms.plugins.datasource.dto.ViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcViewHandler;

import java.sql.SQLException;
import java.util.List;

public class MysqlViewHandler extends JdbcViewHandler {

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schema, String viewName) throws SQLException {
        String sql = "select" +
                " null as catalog_name," +
                " v.table_schema as schema_name," +
                " v.table_name as object_name," +
                " 'VIEW' as object_type," +
                " t.table_comment as remark," +
                " t.create_time as create_time," +
                " t.update_time as last_ddl_time," +
                " v.view_definition as query_sql" +
                " from information_schema.views v" +
                " join information_schema.tables t" +
                " on v.table_schema = t.table_schema " +
                " and v.table_name = t.table_name " +
                " where t.table_type in ('VIEW','SYSTEM VIEW')";
        if (StrUtil.isNotEmpty(schema)) {
            sql += " and v.table_schema = '" + schema + "'";
        }
        if (StrUtil.isNotEmpty(viewName)) {
            sql += " and v.table_name = '" + viewName + "'";
        }
        return super.listViewFromDB(sql);
    }

    @Override
    protected String generateRenameSQL(String schema, String viewName, String newName) {
        return StrUtil.format("RENAME TABLE {}.{} TO {}", schema, viewName, newName);
    }

    @Override
    public String getViewDDL(String catalog, String schema, String viewName) throws SQLException {
        String ddl = super.getViewDDL(catalog, schema, viewName);
        return SQLUtils.format(ddl, DbType.mysql, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION);
    }
}
