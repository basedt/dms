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

import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.service.base.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DmsFileDTO", title = "Dms File DTO")
public class DmsFileDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(name = "workspaceId", title = "workspace id")
    private Long workspaceId;

    @Schema(name = "datasourceId", title = "datasource id")
    private Long datasourceId;

    @NotBlank
    @Length(max = 64)
    @Pattern(regexp = Constants.REGEX_EXCLUDE_SPECIAL_CHAR)
    @Schema(name = "fileName", title = "file name")
    private String fileName;

    @NotNull
    @Schema(name = "fileType", title = "file type")
    private DictVO fileType;

    @NotNull
    @Schema(name = "fileCatalog", title = "file catalog")
    private Long fileCatalog;

    @Schema(name = "fileStatus", title = "file status")
    private DictVO fileStatus;

    @Schema(name = "content", title = "content")
    private String content;

    @Schema(name = "version", title = "version")
    private Integer version;

    @Schema(name = "owner", title = "owner")
    private String owner;

    @Length(max = 512)
    @Schema(name = "remark", title = "remark")
    private String remark;

}
