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
package com.basedt.dms.service.sys.dto;

import com.basedt.dms.service.base.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SysRolePrivilegeDTO", title = "sys role privilege dto")
public class SysRolePrivilegeDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(name = "roleId", title = "role id")
    private Long roleId;

    @NotNull
    @Schema(name = "privilegeId", title = "privilege id")
    private Long privilegeId;

}
