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
package com.basedt.dms.service.log.dto;

import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.service.base.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "LogSqlHistoryDTO", title = "Log Sql History DTO")
public class LogSqlHistoryDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(name = "workspaceId", title = "workspace id")
    private Long workspaceId;

    @NotNull
    @Schema(name = "datasourceId", title = "datasource id")
    private Long datasourceId;

    @NotNull
    @Schema(name = "sqlScript", title = "sql script")
    private String sqlScript;

    @NotNull
    @Schema(name = "startTime", title = "start time")
    private LocalDateTime startTime;

    @NotNull
    @Schema(name = "endTime", title = "end time")
    private LocalDateTime endTime;

    @Schema(name = "sqlStatus", title = "sql status")
    private DictVO sqlStatus;

    @Schema(name = "remark", title = "error message when sql status is failure")
    private String remark;
}
