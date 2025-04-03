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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.llm.DmsChatClient;
import com.basedt.dms.service.sys.SysConfigService;
import com.basedt.dms.service.sys.dto.EmailConfigDTO;
import com.basedt.dms.service.sys.dto.LLMConfigDTO;
import com.basedt.dms.service.sys.dto.SysConfigDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.basedt.dms.common.constant.Constants.CODEC_STR_PREFIX;

@RestController
@RequestMapping("/api/sys/config")
@Tag(name = "SYS-CONFIG")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @GetMapping(path = "email")
    @AuditLogging
    @Operation(summary = "get email config info", description = "get email config info")
    public ResponseEntity<ResponseVO<EmailConfigDTO>> getEmail() {
        String emailConfig =
                this.sysConfigService.selectValueByKey(Constants.CFG_EMAIL_CODE);
        EmailConfigDTO config;
        if (StrUtil.isNotBlank(emailConfig)) {
            config = JSONUtil.toBean(emailConfig, EmailConfigDTO.class);
            config.setPassword(null);
        } else {
            config = new EmailConfigDTO();
        }
        return new ResponseEntity<>(ResponseVO.success(config), HttpStatus.OK);
    }

    @PutMapping(path = "email")
    @AuditLogging
    @Operation(summary = "set email config info", description = "set email config info")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResponseVO<Object>> configEmail(
            @Validated @RequestBody EmailConfigDTO emailConfig) {
        String password = emailConfig.getPassword();
        if (!password.startsWith(CODEC_STR_PREFIX)) {
            emailConfig.setPassword(CODEC_STR_PREFIX + Base64.encode(password));
        } else {
            emailConfig.setPassword(password);
        }
        this.sysConfigService.deleteByKey(Constants.CFG_EMAIL_CODE);
        SysConfigDTO sysConfig = new SysConfigDTO();
        sysConfig.setCfgCode(Constants.CFG_EMAIL_CODE);
        sysConfig.setCfgValue(JSONUtil.toJsonStr(emailConfig));
        this.sysConfigService.insert(sysConfig);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @GetMapping(path = "llm")
    @AuditLogging
    @Operation(summary = "get llm config info", description = "get email config info")
    public ResponseEntity<ResponseVO<LLMConfigDTO>> getLLM() {
        LLMConfigDTO config = this.sysConfigService.getLLMConfig();
        return new ResponseEntity<>(ResponseVO.success(config), HttpStatus.OK);
    }


    @PutMapping(path = "llm")
    @AuditLogging
    @Operation(summary = "set llm config info", description = "set llm config info")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResponseVO<Object>> configLLM(
            @Validated @RequestBody LLMConfigDTO llmConfig) {
        this.sysConfigService.deleteByKey(Constants.CFG_LLM_CODE);
        SysConfigDTO sysConfig = new SysConfigDTO();
        sysConfig.setCfgCode(Constants.CFG_LLM_CODE);
        sysConfig.setCfgValue(JSONUtil.toJsonStr(llmConfig));
        this.sysConfigService.insert(sysConfig);
        DmsChatClient.refreshInstance(llmConfig);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }
}
