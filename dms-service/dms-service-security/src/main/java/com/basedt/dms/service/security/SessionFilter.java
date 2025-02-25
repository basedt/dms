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
package com.basedt.dms.service.security;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.service.security.utils.SecurityUtil;
import com.basedt.dms.service.sys.SysUserService;
import com.basedt.dms.service.sys.dto.SysPrivilegeDTO;
import com.basedt.dms.service.sys.dto.SysRoleDTO;
import com.basedt.dms.service.sys.dto.SysUserDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SessionFilter extends GenericFilterBean {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    private final SysUserService sysUserService;

    public SessionFilter(FindByIndexNameSessionRepository<? extends Session> sessionRepository, SysUserService sysUserService) {
        this.sessionRepository = sessionRepository;
        this.sysUserService = sysUserService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String sessionId = SecurityUtil.getSessionIdFromCookie(httpServletRequest);
        if (StrUtil.isNotEmpty(sessionId)) {
            Session session = this.sessionRepository.findById(sessionId);
            Authentication authentication = getAuthentication(session);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    /**
     * get privilege info from session.
     * set role to null when update role or privilege info , query privileges form database
     *
     * @param session
     * @return
     */
    private Authentication getAuthentication(Session session) {
        if (session != null && !session.isExpired()) {
            UserDetailsInfo userDetailsInfo = session.getAttribute(Constants.SESSION_USER);
            SysUserDTO userDTO = userDetailsInfo.getUser();
            if (userDTO == null) {
                return null;
            }
            if (userDTO.getRoles() == null) {
                Set<SimpleGrantedAuthority> authorities = new HashSet<>();
                List<SysRoleDTO> roles = this.sysUserService.listAllPrivilege(userDTO.getUserName());
                if (CollectionUtil.isNotEmpty(roles)) {
                    for (SysRoleDTO roleDTO : roles) {
                        authorities.add(new SimpleGrantedAuthority(roleDTO.getRoleCode()));
                        if (CollectionUtil.isNotEmpty(roleDTO.getPrivileges())) {
                            for (SysPrivilegeDTO privilegeDTO : roleDTO.getPrivileges()) {
                                authorities.add(new SimpleGrantedAuthority(privilegeDTO.getPrivilegeCode()));
                            }
                        }
                    }
                }
                return new UsernamePasswordAuthenticationToken(userDetailsInfo, "", authorities);
            } else {
                Collection<? extends GrantedAuthority> authorities = userDetailsInfo.getAuthorities();
                return new UsernamePasswordAuthenticationToken(userDetailsInfo, "", authorities);
            }
        }
        return null;
    }
}
