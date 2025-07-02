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

package com.basedt.dms.plugins.datasource.impl.greenplum;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.impl.postgre.PostgreMaterializedViewHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GreenplumMaterializedViewHandler extends PostgreMaterializedViewHandler {

    @Override
    public String getMViewDdl(String catalog, String schema, String mViewName) throws SQLException {
        String ddl = super.getMViewDdl(catalog, schema, mViewName);
        if (StrUtil.isNotEmpty(ddl)) {
            String distributedSQL = generateDistributedSQL(schema, mViewName);
            return StrUtil.concat(true, ddl.replace(";", "\n"), distributedSQL, ";");
        } else {
            return "";
        }
    }

    @Override
    protected String generateDistributedSQL(String schema, String mViewName) throws SQLException {
        String distributedDDL = "";
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(GreenplumPluginImpl.DISTRIBUTED_SQL);
        ps.setString(1, schema);
        ps.setString(2, mViewName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            distributedDDL = rs.getString("distributed_ddl");
        }
        JdbcUtil.close(conn, ps, rs);
        return distributedDDL;
    }

}
