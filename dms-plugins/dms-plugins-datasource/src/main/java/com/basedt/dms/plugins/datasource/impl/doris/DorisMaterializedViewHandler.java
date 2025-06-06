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
import com.basedt.dms.plugins.datasource.dto.MaterializedViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcMaterializedViewHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.MATERIALIZED_VIEW;

/**
 * only support async materialized view
 * async => select * from mv_infos("database"="schema")
 * sync => show alter table materialized view from schema
 */
public class DorisMaterializedViewHandler extends JdbcMaterializedViewHandler {

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return listMViewDetails(catalog, schemaPattern, mViewPattern);
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schema, String mViewName) throws SQLException {
        List<MaterializedViewDTO> mViewList = new ArrayList<>();
        DorisCatalogHandler handler = new DorisCatalogHandler();
        handler.initialize(this.dataSource, new HashMap<>(), catalog);
        if (handler.isInternalCatalog(catalog)) {
            String asyncSQL = "select * from mv_infos('database'= '" + schema + "' )";
            if (StrUtil.isNotEmpty(mViewName)) {
                asyncSQL += " where name = '" + mViewName + "'";
            }
            try (Connection conn = dataSource.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(asyncSQL);) {
                while (rs.next()) {
                    MaterializedViewDTO mView = new MaterializedViewDTO();
                    mView.setCatalogName(catalog);
                    mView.setSchemaName(schema);
                    mView.setObjectName(rs.getString("name"));
                    mView.setObjectType(MATERIALIZED_VIEW.name());
                    mViewList.add(mView);
                }
            }
        }
        return mViewList;
    }

    @Override
    protected String generateRenameSQL(String schema, String mViewName, String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
