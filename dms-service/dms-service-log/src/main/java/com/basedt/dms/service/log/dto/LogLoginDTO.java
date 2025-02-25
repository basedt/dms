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

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "LogLoginDTO", title = "Login Log DTO")
public class LogLoginDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(name = "userName", title = "user name")
    private String userName;

    @Schema(name = "loginTime", title = "login time")
    private LocalDateTime loginTime;

    @Schema(name = "ipAddress", title = "ip address")
    private String ipAddress;

    @Schema(name = "loginType", title = "login type")
    private DictVO loginType;

    @Schema(name = "clientInfo", title = "client info")
    private String clientInfo;

    @Schema(name = "osInfo", title = "os info")
    private String osInfo;

    @Schema(name = "browserInfo", title = "browser info")
    private String browserInfo;

    @Schema(name = "actionInfo", title = "action info")
    private String actionInfo;

}
