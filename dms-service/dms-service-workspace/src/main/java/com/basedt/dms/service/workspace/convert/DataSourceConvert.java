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
package com.basedt.dms.service.workspace.convert;

import com.basedt.dms.plugins.datasource.dto.DataSourceDTO;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;

import java.util.Objects;

public class DataSourceConvert {

    public static DataSourceDTO toDataSource(DmsDataSourceDTO dto) {
        if (Objects.isNull(dto)) {
            return null;
        }
        DataSourceDTO datasource = new DataSourceDTO();
        datasource.setId(dto.getId());
        datasource.setDatasourceName(dto.getDatasourceName());
        datasource.setDatasourceType(dto.getDatasourceType());
        datasource.setHostName(dto.getHostName());
        datasource.setDatabaseName(dto.getDatabaseName());
        datasource.setPort(dto.getPort());
        datasource.setUserName(dto.getUserName());
        datasource.setPassword(dto.getPassword());
        datasource.setAttrs(dto.getAttrs());
        return datasource;
    }
}
