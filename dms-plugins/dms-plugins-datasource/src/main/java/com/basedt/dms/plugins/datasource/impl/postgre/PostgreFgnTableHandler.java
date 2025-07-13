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

package com.basedt.dms.plugins.datasource.impl.postgre;

import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcForeignTableHandler;

import java.sql.SQLException;
import java.util.List;

public class PostgreFgnTableHandler extends JdbcForeignTableHandler {

    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        PostgreTableHandler tableHandler = new PostgreTableHandler();
        tableHandler.initialize(this.dataSource, this.config, new PostgreDataTypeMapper());
        return tableHandler.listTableDetails(catalog, schemaPattern, tablePattern, DbObjectType.FOREIGN_TABLE);
    }

}
