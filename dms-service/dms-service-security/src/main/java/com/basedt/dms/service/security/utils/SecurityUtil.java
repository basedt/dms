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
package com.basedt.dms.service.security.utils;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.service.security.UserDetailsInfo;
import com.basedt.dms.service.security.enums.ActionCode;
import com.basedt.dms.service.security.enums.BlockCode;
import com.basedt.dms.service.security.enums.ModuleCode;
import com.basedt.dms.service.security.enums.PageCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class SecurityUtil {

    public static UserDetailsInfo getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication) &&
                authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetailsInfo) authentication.getPrincipal();
        }
        return null;
    }

    public static String getCurrentUserName() {
        UserDetails user = getCurrentUser();
        if (Objects.nonNull(user)) {
            return user.getUsername();
        } else {
            return null;
        }
    }

    public static String getPrivilegeCode(ModuleCode module, PageCode page, BlockCode block, ActionCode action) {
        return StrUtil.join(Constants.SEPARATOR_COLON, module.getValue(), page.getValue(), block.getValue(), action.getValue()).toLowerCase();
    }

    public static String getPrivilegeName(ModuleCode module, PageCode page, BlockCode block, ActionCode action) {
        return StrUtil.join(Constants.SEPARATOR_DOT, "dms.p", module.getValue(), page.getValue(), block.getValue(), action).toLowerCase();
    }

    public static String getSessionIdFromCookie(HttpServletRequest request) {
        Cookie cookie = CookieUtil.getCookie(request, Constants.SESSION_ID);
        if (cookie != null && StrUtil.isNotEmpty(cookie.getValue())) {
            return cookie.getValue();
        }
        return null;
    }
}
