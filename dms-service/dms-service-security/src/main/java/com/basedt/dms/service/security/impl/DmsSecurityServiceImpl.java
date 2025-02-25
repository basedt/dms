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
package com.basedt.dms.service.security.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.service.security.DmsSecurityService;
import com.basedt.dms.service.security.UserDetailsInfo;
import com.basedt.dms.service.security.enums.DmsPrivileges;
import com.basedt.dms.service.security.utils.SecurityUtil;
import com.basedt.dms.service.sys.SysUserService;
import com.basedt.dms.service.sys.dto.SysUserDTO;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service(value = "sec")
public class DmsSecurityServiceImpl implements DmsSecurityService {

    private final FindByIndexNameSessionRepository<? extends Session> sessions;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final SysUserService sysUserService;

    public DmsSecurityServiceImpl(FindByIndexNameSessionRepository<? extends Session> sessions,
                                  AuthenticationManagerBuilder authenticationManagerBuilder,
                                  SysUserService sysUserService) {
        this.sessions = sessions;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.sysUserService = sysUserService;
    }

    @Override
    public boolean validate(String... privileges) {
        UserDetailsInfo userDetails = SecurityUtil.getCurrentUser();
        if (userDetails == null || CollectionUtil.isEmpty(userDetails.getAuthorities())) {
            return false;
        }
        Set<String> privilegeSet = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return privilegeSet.contains(Constants.ROLE_SUPER_ADMIN) ||
                Arrays.stream(privileges).anyMatch(privilegeSet::contains);
    }

    @Override
    public boolean validate(DmsPrivileges privileges) {
        String code = privileges.getCode();
        return validate(code);
    }

    @Override
    public void clearPrivilegeByUser(String... userNames) {
        for (String userName : userNames) {
            Map<String, ? extends Session> map = this.sessions.findByPrincipalName(userName);
            if (CollectionUtil.isNotEmpty(map)) {
                map.forEach((k, v) -> {
                    UserDetailsInfo userDetailsInfo = (UserDetailsInfo) v.getAttribute(Constants.SESSION_USER);
                    if (userDetailsInfo != null) {
                        SysUserDTO userDTO = userDetailsInfo.getUser();
                        userDTO.setRoles(null);
                        v.setAttribute(Constants.SESSION_USER, userDTO);
                    }
                });
            }
        }
    }

    @Override
    public void clearPrivilegeByRole(Long... roleIds) {
        List<SysUserDTO> userList = this.sysUserService.listUserByRole(roleIds);
        if (CollectionUtil.isNotEmpty(userList)) {
            disableSessionByUserName(userList.stream()
                    .map(SysUserDTO::getUserName)
                    .distinct()
                    .toArray(String[]::new));
        }
    }

    @Override
    public void disableSessionByUserName(String... userNames) {
        for (String userName : userNames) {
            Map<String, ? extends Session> map = this.sessions.findByPrincipalName(userName);
            if (CollectionUtil.isNotEmpty(map)) {
                map.forEach((k, v) -> {
                    this.sessions.deleteById(v.getId());
                });
            }
        }
    }

    @Override
    public void disableSessionByUserId(Long... idList) {
        List<SysUserDTO> userList = this.sysUserService.listUserById(idList);
        if (CollectionUtil.isNotEmpty(userList)) {
            List<String> userNames = userList.stream().map(SysUserDTO::getUserName).collect(Collectors.toList());
            disableSessionByUserName(userNames.toArray(new String[0]));
        }
    }

    @Override
    public Session findSessionById(String sessionId) {
        return this.sessions.findById(sessionId);
    }

    @Override
    public Authentication authentication(Authentication token) {
        return authenticationManagerBuilder.getObject().authenticate(token);
    }
}
