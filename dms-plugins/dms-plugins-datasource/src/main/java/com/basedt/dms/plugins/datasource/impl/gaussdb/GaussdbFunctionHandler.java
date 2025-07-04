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

package com.basedt.dms.plugins.datasource.impl.gaussdb;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.impl.postgre.PostgreFunctionHandler;

import java.sql.SQLException;

public class GaussdbFunctionHandler extends PostgreFunctionHandler {

    @Override
    public String getFunctionDDL(String catalog, String schema, String functionName) throws SQLException {
        String ddl = super.getFunctionDDL(catalog, schema, functionName);
        if (StrUtil.isEmpty(ddl)) {
            return ddl;
        } else {
            int startIndex = ddl.indexOf("\"") + 1;
            int endIndex = ddl.lastIndexOf("\"");
            return ddl.substring(startIndex, endIndex);
        }
    }
}
