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

import com.basedt.dms.service.sys.dto.SysUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.basedt.dms.common.enums.UserStatus.CANCELLATION;
import static com.basedt.dms.common.enums.UserStatus.FORBIDDEN;

public class UserDetailsInfo implements UserDetails {

    private SysUserDTO user;

    private List<GrantedAuthority> authorities;

    private String loginIp;

    private LocalDateTime loginTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        if (Objects.nonNull(this.user)) {
            return this.getUser().getPassword();
        } else {
            return null;
        }
    }

    @Override
    public String getUsername() {
        if (Objects.nonNull(this.user)) {
            return this.getUser().getUserName();
        } else {
            return null;
        }
    }

    @Override
    public boolean isAccountNonExpired() {
        if (Objects.nonNull(this.user)) {
            return !CANCELLATION.equalsAsDict(getUser().getUserStatus());
        } else {
            return false;
        }
    }

    @Override
    public boolean isAccountNonLocked() {
        if (Objects.nonNull(this.user)) {
            return !FORBIDDEN.equalsAsDict(getUser().getUserStatus());
        } else {
            return false;
        }
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isAccountNonExpired() && isAccountNonLocked();
    }

    public void setAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public void setUser(SysUserDTO user) {
        this.user = user;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public SysUserDTO getUser() {
        return user;
    }


}
