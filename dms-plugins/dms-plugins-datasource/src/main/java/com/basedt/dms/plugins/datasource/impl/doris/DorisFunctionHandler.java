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
import com.basedt.dms.plugins.datasource.dto.FunctionDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcFunctionHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.FUNCTION;

public class DorisFunctionHandler extends JdbcFunctionHandler {

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> functionList = new ArrayList<>();
        DorisCatalogHandler handler = new DorisCatalogHandler();
        handler.initialize(this.dataSource, new HashMap<>(), catalog);
        if (handler.isInternalCatalog(catalog)) {
            String sql = "show global full functions";
            Connection connection = dataSource.getConnection();
            if (StrUtil.isNotEmpty(functionPattern)) {
                sql += StrUtil.concat(true, " like ", functionPattern, "%");
            }
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                FunctionDTO function = new FunctionDTO();
                function.setCatalogName(catalog);
                function.setSchemaName(schemaPattern);
                function.setObjectName(rs.getString("Signature"));
                function.setObjectType(FUNCTION.name());
                functionList.add(function);
            }
            JdbcUtil.close(connection, st, rs);
        }
        return functionList;
    }
}
