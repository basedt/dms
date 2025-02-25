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
package com.basedt.dms.api.aspect;

import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONUtil;
import com.basedt.dms.common.constant.DictConstants;
import com.basedt.dms.common.enums.LoginType;
import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.service.log.LogActionService;
import com.basedt.dms.service.log.LogLoginService;
import com.basedt.dms.service.log.dto.LogActionDTO;
import com.basedt.dms.service.log.dto.LogLoginDTO;
import com.basedt.dms.service.security.utils.SecurityUtil;
import com.basedt.dms.service.sys.cache.DictCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LogAspect {

    private final LogActionService logActionService;

    private final LogLoginService logLoginService;

    public LogAspect(LogActionService logActionService, LogLoginService logLoginService) {
        this.logActionService = logActionService;
        this.logLoginService = logLoginService;
    }


    @Pointcut("@annotation(com.basedt.dms.api.annotation.AuditLogging)")
    public void actionPointCut() {
    }

    @Pointcut("execution(* com.basedt.dms.api.controller.sys.UserController.login(..))")
    public void loginPointCut() {
    }

    @Pointcut("execution(* com.basedt.dms.api.controller.sys.UserController.logout(..))")
    public void logoutPointCut() {
    }

    @Around("actionPointCut()")
    public Object actionLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        insertActionLog(joinPoint, startTime);
        return result;
    }

    @Around("loginPointCut()")
    public Object loginLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        insertLoginLog(result, startTime,
                DictCache.getValueByKey(DictConstants.LOGIN_TYPE, LoginType.LOGIN.getValue()));
        return result;
    }

    @Around("logoutPointCut()")
    public Object logoutLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        insertLoginLog(result, startTime,
                DictCache.getValueByKey(DictConstants.LOGIN_TYPE, LoginType.LOGOUT.getValue()));
        return result;
    }


    private HttpServletRequest getRequest() {
        ServletRequestAttributes sra =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert sra != null;
        return sra.getRequest();
    }

    private String getParameter(Object[] args) throws JsonProcessingException {
        List<Object> argList = new ArrayList<>();
        for (Object o : args) {
            if (o instanceof ServletRequest || o instanceof ServletResponse ||
                    o instanceof MultipartFile) {
                continue;
            }
            argList.add(o);
        }
        return argList.size() < 1 ? "" : JSONUtil.toJsonStr(argList);
    }

    private void insertActionLog(JoinPoint joinPoint, long startTime) {
        try {
            long endTime = System.currentTimeMillis();
            LogActionDTO log = new LogActionDTO();
            HttpServletRequest request = getRequest();
            log.setUserName(SecurityUtil.getCurrentUserName());
            log.setActionTime(LocalDateTime.now());
            String agentStr = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgentUtil.parse(agentStr);
            OS os = userAgent.getOs();
            Browser browser = userAgent.getBrowser();
            log.setIpAddress(ServletUtil.getClientIP(request));
            log.setActionUrl(request.getRequestURL().toString());
            log.setToken(SecurityUtil.getSessionIdFromCookie(request));
            log.setClientInfo(userAgent.getPlatform().getName());
            log.setOsInfo(os.getName());
            log.setBrowserInfo(browser.toString());
            //action info
            Map<String, Object> actionInfo = new HashMap<>(3);
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length >= 1 && !(args[0] instanceof Throwable)) {
                actionInfo.put("params", getParameter(joinPoint.getArgs()));
                actionInfo.put("result", "success");
            } else if (args != null && args.length >= 1) {
                actionInfo.put("result", "error");
                actionInfo.put("exception", JSONUtil.toJsonStr(((Throwable) args[0]).getMessage()));
            } else {
                actionInfo.put("result", "success");
            }
            actionInfo.put("method", request.getMethod());
            actionInfo.put("elapsed_time", endTime - startTime);
            log.setActionInfo(JSONUtil.toJsonStr(actionInfo));
            this.logActionService.insert(log);
        } catch (Exception e) {
            log.error("failed to record operate action log ！", e);
        }
    }

    private void insertLoginLog(Object result, long startTime, DictVO loginType) {
        try {
            long endTime = System.currentTimeMillis();
            LogLoginDTO log = new LogLoginDTO();
            HttpServletRequest request = getRequest();
            log.setUserName(SecurityUtil.getCurrentUserName());
            log.setLoginTime(LocalDateTime.now());
            String agentStr = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgentUtil.parse(agentStr);
            OS os = userAgent.getOs();
            Browser browser = userAgent.getBrowser();
            log.setIpAddress(ServletUtil.getClientIP(request));
            log.setLoginType(loginType);
            log.setClientInfo(userAgent.getPlatform().getName());
            log.setOsInfo(os.getName());
            log.setBrowserInfo(browser.toString());
            //action info
            Map<String, Object> actionInfo = new HashMap<>(3);
            actionInfo.put("data", JSONUtil.toJsonStr(result));
            actionInfo.put("method", request.getMethod());
            actionInfo.put("elapsed_time", endTime - startTime);
            log.setActionInfo(JSONUtil.toJsonStr(actionInfo));
            this.logLoginService.insert(log);
        } catch (Exception e) {
            log.error("failed to record login log ！", e);
        }
    }
}
