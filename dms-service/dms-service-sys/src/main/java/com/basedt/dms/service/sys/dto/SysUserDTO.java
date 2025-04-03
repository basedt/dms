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

import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.service.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SysUserDTO", title = "Sys User DTO")
public class SysUserDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Length(max = 32)
    @Pattern(regexp = Constants.REGEX_WORD_CHAR)
    @Schema(name = "userName", title = "user name")
    private String userName;

    @Length(max = 50)
    @Schema(name = "nickName", title = "nick name")
    private String nickName;

    @Length(max = 64)
    @Schema(name = "realName", title = "real name")
    private String realName;

    @NotBlank
    @Length(max = 128)
    @Schema(name = "email", title = "email")
    private String email;

    @Length(max = 16)
    @Schema(name = "mobilePhone", title = "mobile phone")
    private String mobilePhone;

    @JsonIgnore
    @Length(max = 64)
    @Schema(name = "password", title = "password")
    private String password;

    @Schema(name = "userStatus", title = "user status")
    private DictVO userStatus;

    @Length(max = 200)
    @Schema(name = "summary", title = "summary")
    private String summary;

    @Schema(name = "registerChannel", title = "register channel")
    private DictVO registerChannel;

    @Schema(name = "registerTime", title = "register time")
    private LocalDateTime registerTime;

    @Length(max = 16)
    @Schema(name = "registerIp", title = "register ip")
    private String registerIp;

    @Schema(name = "roles", title = "roles")
    private List<SysRoleDTO> roles;
}