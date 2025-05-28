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

package com.basedt.dms.plugins.datasource.param;

import com.basedt.dms.plugins.datasource.dto.TableDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "TableInfoParam", title = "Table Info Param")
public class TableInfoParam {

    @NotNull
    @Schema(name = "dataSourceId", title = "data source id")
    private Long dataSourceId;

    @Schema(name = "catalog", title = "catalog name")
    private String catalog;

    @NotNull
    @Schema(name = "schemaName", title = "schema name")
    private String schemaName;

    @NotNull
    @Schema(name = "tableName", title = "table name")
    private String tableName;

    private TableDTO tableInfo;
}
