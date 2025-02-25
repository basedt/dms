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
package com.basedt.dms.service.log.param;

import com.basedt.dms.service.base.param.PaginationParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "LogSqlHistoryParam", title = "Sql History Log Param")
public class LogSqlHistoryParam extends PaginationParam {

    @Schema(name = "workspaceId", title = "workspace id")
    private Long workspaceId;

    @Schema(name = "datasourceId", title = "datasource id")
    private Long datasourceId;

    @Schema(name = "sqlStatus", title = "sql status")
    private String sqlStatus;

    @Schema(name = "startTimeFrom", title = "start time from")
    private String startTimeFrom;

    @Schema(name = "startTimeTo", title = "start time to")
    private String startTimeTo;

    @Schema(name = "endTimeFrom", title = "end time from")
    private String endTimeFrom;

    @Schema(name = "endTimeTo", title = "end time to")
    private String endTimeTo;

    @Schema(name = "creator", title = "creator")
    private String creator;
}
