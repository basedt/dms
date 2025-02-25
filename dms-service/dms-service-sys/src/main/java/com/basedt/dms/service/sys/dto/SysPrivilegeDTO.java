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
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SysPrivilegeDTO", title = "Sys Privilege")
public class SysPrivilegeDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Length(max = 32)
    @Schema(name = "privilegeCode", title = "privilege code")
    private String privilegeCode;

    @NotBlank
    @Length(max = 32)
    @Schema(name = "privilegeName", title = "privilege name")
    private String privilegeName;

    @Length(max = 32)
    @Schema(name = "parentCode", title = "parent code")
    private String parentCode;

    @Schema(name = "privilege level", title = "privilege level")
    private Integer level;
}
