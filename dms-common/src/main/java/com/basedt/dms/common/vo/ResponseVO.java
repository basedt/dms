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
package com.basedt.dms.common.vo;

import cn.hutool.json.JSONUtil;
import com.basedt.dms.common.enums.ErrorShowType;
import com.basedt.dms.common.enums.ResponseCode;
import com.basedt.dms.common.utils.I18nUtil;
import lombok.Data;

import static com.basedt.dms.common.enums.ErrorShowType.NOTIFICATION;
import static com.basedt.dms.common.enums.ResponseCode.ERROR;

@Data
public class ResponseVO<T> {
    /**
     * operate result
     */
    private Boolean success;

    /**
     * result data
     */
    private T data;
    /**
     * error code
     */
    private String errorCode;
    /**
     * error message
     */
    private String errorMessage;

    private String showType;

    private ResponseVO() {
    }

    public static <T> ResponseVO<T> success() {
        ResponseVO<T> info = new ResponseVO<>();
        info.setSuccess(true);
        return info;
    }

    public static <T> ResponseVO<T> success(T data) {
        ResponseVO<T> info = new ResponseVO<>();
        info.setSuccess(true);
        info.setData(data);
        return info;
    }

    /**
     * default error info
     *
     * @param message message
     * @return ResponseVO
     */
    public static <T> ResponseVO<T> error(String message) {
        ResponseVO<T> info = new ResponseVO<>();
        info.setSuccess(false);
        info.setErrorCode(ERROR.getValue());
        info.setErrorMessage(message);
        info.setShowType(NOTIFICATION.getValue());
        return info;
    }

    public static <T> ResponseVO<T> error(ResponseCode info) {
        return error(info.getValue(), I18nUtil.get(info.getLabel()));
    }

    public static <T> ResponseVO<T> error(ResponseCode info, String message) {
        return error(info.getValue(), I18nUtil.get(message));
    }

    public static <T> ResponseVO<T> error(String code, String message) {
        ResponseVO<T> info = new ResponseVO<>();
        info.setSuccess(false);
        info.setErrorCode(code);
        info.setErrorMessage(message);
        info.setShowType(NOTIFICATION.getValue());
        return info;
    }

    public static <T> ResponseVO<T> error(String code, String message, ErrorShowType showType) {
        ResponseVO<T> info = new ResponseVO<>();
        info.setSuccess(false);
        info.setErrorCode(code);
        info.setErrorMessage(message);
        info.setShowType(showType.getValue());
        return info;
    }

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
