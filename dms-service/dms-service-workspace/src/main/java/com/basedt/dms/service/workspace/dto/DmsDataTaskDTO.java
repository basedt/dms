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
package com.basedt.dms.service.workspace.dto;

import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.service.base.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DmsDataTaskDTO", title = "Dms Data Task DTO")
public class DmsDataTaskDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(name = "workspaceId", title = "workspace id")
    private Long workspaceId;

    @NotNull
    @Schema(name = "datasourceId", title = "datasource id")
    private Long datasourceId;

    @NotNull
    @Schema(name = "fileName", title = "file name")
    private String fileName;

    @NotNull
    @Schema(name = "fileType", title = "file type")
    private DictVO fileType;

    @Schema(name = "fileSize", title = "file size")
    private Long fileSize;

    @Schema(name = "fileUrl", title = "file url")
    private String fileUrl;

    @Schema(name = "splitRow", title = "split row")
    private Long splitRow;

    @Schema(name = "fileEncoding", title = "file encoding")
    private DictVO fileEncoding;

    @Schema(name = "taskStatus", title = "taskStatus")
    private DictVO taskStatus;

    @Schema(name = "taskType", title = "taskType")
    private DictVO taskType;

    @Schema(name = "sqlScript", title = "sql script")
    private String sqlScript;
}
