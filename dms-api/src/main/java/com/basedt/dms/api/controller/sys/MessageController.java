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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.alert.SysMessageService;
import com.basedt.dms.alert.dto.SysMessageDTO;
import com.basedt.dms.alert.param.SysMessageParam;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.enums.Bool;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.security.annotation.AnonymousAccess;
import com.basedt.dms.service.security.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/api/sys/message")
@Tag(name = "MESSAGE")
public class MessageController {

    private final SysMessageService sysMessageService;

    public MessageController(SysMessageService sysMessageService) {
        this.sysMessageService = sysMessageService;
    }

    @GetMapping
    @AuditLogging
    @AnonymousAccess
    @Operation(summary = "list user messages", description = "list user messages")
    public ResponseEntity<PageDTO<SysMessageDTO>> listUserMessages(SysMessageParam params) {
        String userName = SecurityUtil.getCurrentUserName();
        if (StrUtil.isNotEmpty(userName)) {
            params.setReceiver(userName);
            PageDTO<SysMessageDTO> page = this.sysMessageService.listMessageByPage(params);
            return new ResponseEntity<>(page, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping
    @AuditLogging
    @AnonymousAccess
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "read messages", description = "read messages")
    public ResponseEntity<ResponseVO<Object>> readMessages(@RequestBody Set<Long> ids) {
        String userName = SecurityUtil.getCurrentUserName();
        if (StrUtil.isNotEmpty(userName) && CollectionUtil.isNotEmpty(ids)) {
            List<SysMessageDTO> list = this.sysMessageService.listByIds(ids);
            list.stream()
                    .filter(msg -> Bool.NO.equalsAsDict(msg.getIsRead()))
                    .map(SysMessageDTO::getId)
                    .forEach(id -> {
                        SysMessageDTO message = new SysMessageDTO();
                        message.setId(id);
                        message.setIsRead(Bool.YES.toDict());
                        this.sysMessageService.update(message);
                    });
            return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping(path = "/count")
    @AuditLogging
    @AnonymousAccess
    @Operation(summary = "count unRead Messages", description = "count unRead Messages")
    public ResponseEntity<ResponseVO<Long>> countUnReadMessages() {
        String userName = SecurityUtil.getCurrentUserName();
        if (StrUtil.isNotEmpty(userName)) {
            Long num = this.sysMessageService.countUnReadMsg(userName);
            return new ResponseEntity<>(ResponseVO.success(num), HttpStatus.OK);

        }
        return new ResponseEntity<>(ResponseVO.success(0L), HttpStatus.OK);
    }
}
