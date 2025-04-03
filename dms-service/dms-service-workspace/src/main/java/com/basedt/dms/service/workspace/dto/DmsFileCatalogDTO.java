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
@Schema(name = "DmsFileCatalogDTO", title = "Dms File Catalog DTO")
public class DmsFileCatalogDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /**
     * exclude special characters
     */
    @NotBlank
    @Length(max = 64)
    @Pattern(regexp = Constants.REGEX_EXCLUDE_SPECIAL_CHAR)
    @Schema(name = "name", title = "name")
    private String name;

    @Schema(name = "pid", title = "parent catalog id")
    private Long pid;

    @NotNull
    @Schema(name = "workspaceId", title = "workspace id")
    private Long workspaceId;
}
