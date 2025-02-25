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
package com.basedt.dms.service.sys.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basedt.dms.dao.entity.master.sys.SysUserRole;
import com.basedt.dms.dao.mapper.master.sys.SysUserRoleMapper;
import com.basedt.dms.service.sys.SysUserRoleService;
import com.basedt.dms.service.sys.convert.SysUserRoleConvert;
import com.basedt.dms.service.sys.dto.SysUserRoleDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserRoleServiceImpl implements SysUserRoleService {

    private final SysUserRoleMapper sysUserRoleMapper;

    public SysUserRoleServiceImpl(SysUserRoleMapper sysUserRoleMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Override
    public void insert(SysUserRoleDTO userRoleDTO) {
        SysUserRole userRole = SysUserRoleConvert.INSTANCE.toDo(userRoleDTO);
        this.sysUserRoleMapper.insert(userRole);
    }

    @Override
    public void deleteByRoleId(Long... roleId) {
        this.sysUserRoleMapper.delete(
                Wrappers.lambdaQuery(SysUserRole.class)
                        .in(SysUserRole::getRoleId, Arrays.stream(roleId).collect(Collectors.toSet()))
        );
    }

    @Override
    public void deleteByUserId(Long... userId) {
        this.sysUserRoleMapper.delete(
                Wrappers.lambdaQuery(SysUserRole.class)
                        .in(SysUserRole::getUserId, Arrays.stream(userId).collect(Collectors.toSet()))
        );
    }

    @Override
    public List<SysUserRoleDTO> selectByUserId(Long userId) {
        List<SysUserRole> list = this.sysUserRoleMapper.selectList(
                Wrappers.lambdaQuery(SysUserRole.class)
                        .eq(SysUserRole::getUserId, userId)
        );
        return SysUserRoleConvert.INSTANCE.toDto(list);
    }

    @Override
    public List<SysUserRoleDTO> selectByRoleId(Long... roleId) {
        List<SysUserRole> list = this.sysUserRoleMapper.selectList(
                Wrappers.lambdaQuery(SysUserRole.class)
                        .in(SysUserRole::getRoleId, Arrays.stream(roleId).collect(Collectors.toSet()))
        );
        return SysUserRoleConvert.INSTANCE.toDto(list);
    }
}
