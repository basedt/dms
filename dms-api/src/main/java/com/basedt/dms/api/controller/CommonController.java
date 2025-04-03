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
package com.basedt.dms.api.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.RandomUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.RedisUtil;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.security.annotation.AnonymousAccess;
import com.basedt.dms.service.security.utils.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api")
@Tag(name = "COMMON")
public class CommonController {

    @Value("${spring.application.name}")
    private String appName;

    private final RedisUtil redisUtil;

    public CommonController(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @AnonymousAccess
    @GetMapping("/health/status")
    @Operation(summary = "app service health check", description = "app service health check")
    public ResponseEntity<ResponseVO<String>> appStatusCheck() {
        return new ResponseEntity<>(ResponseVO.success(appName), HttpStatus.OK);
    }

    @AnonymousAccess
    @Operation(summary = "generate auth code", description = "generate auth code")
    @GetMapping(path = {"/authCode"})
    public ResponseEntity<ResponseVO<Map<String, Object>>> authCode(HttpServletRequest req, HttpServletResponse resp) {
        LineCaptcha lineCaptcha =
                CaptchaUtil.createLineCaptcha(150, 32, 5, RandomUtil.randomInt(6, 10));
        Font font = new Font("Stencil", Font.BOLD + Font.ITALIC, 20);
        lineCaptcha.setFont(font);
        lineCaptcha.setBackground(new Color(246, 250, 254));
        lineCaptcha.createCode();
        String uuid = Constants.AUTH_CODE_KEY + UUID.randomUUID();
        redisUtil.set(uuid, lineCaptcha.getCode(), 10 * 60);
        Map<String, Object> map = new HashMap<>(2);
        map.put("uuid", uuid);
        map.put("img", lineCaptcha.getImageBase64Data());
        resp.addCookie(CookieUtil.invalidCookie(Constants.SESSION_ID));
        return new ResponseEntity<>(ResponseVO.success(map), HttpStatus.OK);
    }

}
