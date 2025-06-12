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

import cn.hutool.core.collection.CollectionUtil;
import com.basedt.dms.plugins.datasource.dto.CatalogDTO;
import com.basedt.dms.plugins.datasource.dto.SchemaDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcCatalogHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

public class MysqlCatalogHandler extends JdbcCatalogHandler {

    @Override
    public List<CatalogDTO> listCatalogs() throws SQLException {
        CatalogDTO catalog = new CatalogDTO();
        catalog.setCatalogName(this.databaseName);
        return new ArrayList<CatalogDTO>() {{
            add(catalog);
        }};
    }

    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        List<CatalogDTO> schemaList = listCatalogs();
        List<SchemaDTO> resultList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(schemaList)) {
            for (CatalogDTO catalogDTO : schemaList) {
                if (!catalogDTO.getCatalogName().equalsIgnoreCase("performance_schema") &&
                        !catalogDTO.getCatalogName().equalsIgnoreCase("information_schema") &&
                        !catalogDTO.getCatalogName().equalsIgnoreCase("mysql")
                ) {
                    resultList.add(new SchemaDTO(catalogDTO.getCatalogName()));
                }
            }
        }
        return resultList;
    }

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(FUNCTION.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

}
