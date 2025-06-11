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

import com.basedt.dms.plugins.datasource.dto.CatalogDTO;
import com.basedt.dms.plugins.datasource.dto.SchemaDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcCatalogHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

public class OracleCatalogHandler extends JdbcCatalogHandler {

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(MATERIALIZED_VIEW.name());
            add(FUNCTION.name());
            add(SEQUENCE.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        List<SchemaDTO> schemas = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        PreparedStatement pstm = conn.prepareStatement("select username" +
                " from all_users" +
                " where username not in ('SYS'," +
                "                       'AUDSYS', 'SYSTEM', 'SYSBACKUP', 'SYSDG', 'SYSKM', 'SYSRAC', 'OUTLN', 'REMOTE_SCHEDULER_AGENT'," +
                "                       'XS$NULL', 'GSMADMIN_INTERNAL', 'GSMUSER', 'DIP', 'DBSFWUSER', 'ORACLE_OCM', 'SYS$UMF', 'DBSNMP'," +
                "                       'APPQOSSYS', 'GSMCATUSER', 'GGSYS', 'XDB', 'ANONYMOUS', 'OLAPSYS', 'WMSYS', 'OJVMSYS', 'CTXSYS'," +
                "                       'ORDSYS', 'ORDDATA', 'ORDPLUGINS', 'SI_INFORMTN_SCHEMA', 'MDSYS', 'DVSYS', 'MDDATA', 'LBACSYS'," +
                "                       'DVF'" +
                "    )");
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            SchemaDTO schema = new SchemaDTO();
            schema.setSchemaName(rs.getString("username"));
            schemas.add(schema);
        }
        JdbcUtil.close(conn, pstm, rs);
        return schemas;
    }

    @Override
    public List<CatalogDTO> listCatalogs() throws SQLException {
        CatalogDTO catalog = new CatalogDTO();
        catalog.setCatalogName(this.databaseName);
        return new ArrayList<CatalogDTO>() {{
            add(catalog);
        }};
    }
}
