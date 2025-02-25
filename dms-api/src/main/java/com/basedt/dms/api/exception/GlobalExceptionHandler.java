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
package com.basedt.dms.api.exception;

import cn.hutool.extra.mail.MailException;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.common.utils.I18nUtil;
import com.basedt.dms.common.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.basedt.dms.common.enums.ResponseCode.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @AuditLogging
    @ResponseBody
    @ExceptionHandler(DmsException.class)
    public ResponseEntity<ResponseVO<Object>> custom(DmsException e,
                                                     HandlerMethod method,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        ResponseVO<Object> errorInfo;
        if (StringUtils.hasText(e.getExceptionCode())) {
            errorInfo = ResponseVO.error(e.getExceptionCode(), e.getMessage());
        } else {
            errorInfo = ResponseVO.error(e.getMessage());
        }
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    @AuditLogging
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseVO<Object>> accessDenied(AccessDeniedException e,
                                                           HandlerMethod method,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {
        ResponseVO<Object> errorInfo = ResponseVO.error(ERROR_NO_PRIVILEGE.getValue(),
                I18nUtil.get(ERROR_NO_PRIVILEGE.getLabel()));
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    /**
     * primary key duplicate
     */
    @AuditLogging
    @ResponseBody
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ResponseVO<Object>> duplicateKey(DuplicateKeyException e,
                                                           HandlerMethod method,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {
        ResponseVO<Object> errorInfo = ResponseVO.error(ERROR_DUPLICATE_DATA.getValue(),
                I18nUtil.get(ERROR_DUPLICATE_DATA.getLabel()));
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    @AuditLogging
    @ResponseBody
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ResponseVO<Object>> mail(MailException e,
                                                   HandlerMethod method,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        ResponseVO<Object> errorInfo = ResponseVO.error(ERROR_EMAIL.getValue(),
                I18nUtil.get(ERROR_EMAIL.getLabel()));
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    @AuditLogging
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseVO<Object>> exception(Exception e,
                                                        HandlerMethod method,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
        ResponseVO<Object> errorInfo = ResponseVO.error(I18nUtil.get(ERROR.getLabel()));
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }
}
