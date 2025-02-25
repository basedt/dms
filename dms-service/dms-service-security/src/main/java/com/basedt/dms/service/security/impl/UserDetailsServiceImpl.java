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
import com.basedt.dms.common.utils.I18nUtil;
import com.basedt.dms.service.security.UserDetailsInfo;
import com.basedt.dms.service.sys.SysUserService;
import com.basedt.dms.service.sys.dto.SysPrivilegeDTO;
import com.basedt.dms.service.sys.dto.SysRoleDTO;
import com.basedt.dms.service.sys.dto.SysUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService sysUserService;

    public UserDetailsServiceImpl(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserDTO userInfo = this.sysUserService.selectByUserName(username);
        if (Objects.isNull(userInfo)) {
            throw new UsernameNotFoundException(I18nUtil.get("response.error.login.password"));
        }
        UserDetailsInfo userDetail = new UserDetailsInfo();
        userDetail.setUser(userInfo);
        List<SysRoleDTO> roleList = this.sysUserService.listAllPrivilege(userDetail.getUsername());
        userDetail.getUser().setRoles(roleList);
        userDetail.setAuthorities(toGrantedAuthority(roleList));
        return userDetail;
    }

    private List<GrantedAuthority> toGrantedAuthority(List<SysRoleDTO> roles) {
        if (CollectionUtil.isEmpty(roles)) {
            return null;
        }
        List<GrantedAuthority> list = new ArrayList<>();
        for (SysRoleDTO roleDTO : roles) {
            list.add(new SimpleGrantedAuthority(roleDTO.getRoleCode()));
            if (CollectionUtil.isNotEmpty(roleDTO.getPrivileges())) {
                for (SysPrivilegeDTO privilegeDTO : roleDTO.getPrivileges()) {
                    list.add(new SimpleGrantedAuthority(privilegeDTO.getPrivilegeCode()));
                }
            }
        }
        return list;
    }
}
