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

package com.basedt.dms.plugins.datasource.impl.doris;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.basedt.dms.plugins.datasource.dto.ViewDTO;
import com.basedt.dms.plugins.datasource.impl.mysql.MysqlViewHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DorisViewHandler extends MysqlViewHandler {

    @Override
    public List<ViewDTO> listViews(String catalog, String schema, String viewName) throws SQLException {
        DorisCatalogHandler handler = new DorisCatalogHandler();
        handler.initialize(this.dataSource, new HashMap<>(), catalog);
        if (!handler.isInternalCatalog(catalog)) {
            return new ArrayList<>();
        } else {
            return super.listViewDetails(catalog, schema, viewName);
        }
    }

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schema, String viewName) throws SQLException {
        return listViews(catalog, schema, viewName);
    }

    @Override
    public void renameView(String schema, String viewName, String newName) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getViewDDL(String catalog, String schema, String viewName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(StrUtil.format("show create view {}.{}", schema, viewName));
        ) {
            while (rs.next()) {
                String ddl = rs.getString(2);
                return SQLUtils.format(ddl, DbType.doris);
            }
            return "";
        }
    }
}
