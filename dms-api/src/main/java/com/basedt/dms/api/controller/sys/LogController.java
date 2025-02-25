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
package com.basedt.dms.api.controller.sys;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.log.LogLoginService;
import com.basedt.dms.service.log.dto.LogLoginDTO;
import com.basedt.dms.service.log.param.LogLoginParam;
import com.basedt.dms.service.security.annotation.AnonymousAccess;
import com.basedt.dms.service.security.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping(path = "/api/sys/log")
@Tag(name = "LOG")
public class LogController {

    private final LogLoginService logLoginService;

    public LogController(LogLoginService logLoginService) {
        this.logLoginService = logLoginService;
    }

    @GetMapping
    @AuditLogging
    @AnonymousAccess
    @Operation(summary = "list user login log in last 30 day", description = "list user login log in last 30 day")
    public ResponseEntity<PageDTO<LogLoginDTO>> listLast30DayLoginLog(LogLoginParam param) {
        String userName = SecurityUtil.getCurrentUserName();
        if (StrUtil.isNotEmpty(userName)) {
            param.setUserName(userName);
            param.setLoginTime(LocalDate.now().minusDays(30).atStartOfDay());
            PageDTO<LogLoginDTO> page = this.logLoginService.listByPage(param);
            return new ResponseEntity<>(page, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
