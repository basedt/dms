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
package com.basedt.dms.api.controller.workspace;

import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.utils.RedisUtil;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.log.LogSqlHistoryService;
import com.basedt.dms.service.log.dto.LogSqlHistoryDTO;
import com.basedt.dms.service.log.param.LogSqlHistoryParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping(path = "/api/workspace/sql")
@Tag(name = "SQL")
public class SqlController {

    private final LogSqlHistoryService logSqlHistoryService;
    private final RedisUtil redisUtil;

    public SqlController(LogSqlHistoryService logSqlHistoryService, RedisUtil redisUtil) {
        this.logSqlHistoryService = logSqlHistoryService;
        this.redisUtil = redisUtil;
    }

    @GetMapping
    @AuditLogging
    @Operation(summary = "query sql history", description = "query sql history")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_HISTORY_SHOW)")
    public ResponseEntity<PageDTO<LogSqlHistoryDTO>> listSqlHistory(final LogSqlHistoryParam param) {
        PageDTO<LogSqlHistoryDTO> page = this.logSqlHistoryService.listByPage(param);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PostMapping(path = "/stop/{id}")
    @AuditLogging
    @Operation(summary = "stop sql script", description = "stop sql script")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> stopSqlScript(@PathVariable("id") @NotBlank String id) {
        redisUtil.delKeys(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

}
