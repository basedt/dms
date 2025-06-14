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

package com.basedt.dms.plugins.datasource.impl.mssql;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.basedt.dms.plugins.datasource.dto.ViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcViewHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class MssqlViewHandler extends JdbcViewHandler {

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schema, String viewName) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o.name as object_name," +
                "    'VIEW' as object_type," +
                "    ep.value as remark," +
                "    o.create_date as create_time," +
                "    o.modify_date as last_ddl_time," +
                "    m.definition as query_sql" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " left join sys.extended_properties ep" +
                " on o.object_id = ep.major_id" +
                " and ep.minor_id = 0" +
                " left join sys.all_sql_modules m" +
                " on o.object_id = m.object_id" +
                " where o.type in ('V')";
        if (StrUtil.isNotEmpty(schema)) {
            sql += " and s.name = '" + schema + "'";
        }
        if (StrUtil.isNotEmpty(viewName)) {
            sql += " and o.name = '" + viewName + "'";
        }
        return super.listViewFromDB(sql);
    }

    @Override
    protected String generateRenameSQL(String schema, String viewName, String newName) {
        return StrUtil.format("exec sp_rename '{}.{}',{},'OBJECT'", schema, viewName, newName);
    }

    @Override
    public String getViewDdl(String catalog, String schema, String viewName) throws SQLException {
        ViewDTO viewInfo = getViewDetail(catalog, schema, viewName);
        if (Objects.nonNull(viewInfo)) {
            return SQLUtils.format(viewInfo.getQuerySql(), DbType.sqlserver);
        } else {
            throw new SQLException(StrUtil.format("view {} does not exist in {}", viewName, schema));
        }
    }
}
