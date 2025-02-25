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
import com.basedt.dms.dao.entity.master.sys.SysRolePrivilege;
import com.basedt.dms.dao.mapper.master.sys.SysRolePrivilegeMapper;
import com.basedt.dms.service.sys.SysRolePrivilegeService;
import com.basedt.dms.service.sys.convert.SysRolePrivilegeConvert;
import com.basedt.dms.service.sys.dto.SysRolePrivilegeDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class SysRolePrivilegeServiceImpl implements SysRolePrivilegeService {

    private final SysRolePrivilegeMapper sysRolePrivilegeMapper;

    public SysRolePrivilegeServiceImpl(SysRolePrivilegeMapper sysRolePrivilegeMapper) {
        this.sysRolePrivilegeMapper = sysRolePrivilegeMapper;
    }

    @Override
    public void insert(SysRolePrivilegeDTO rolePrivilegeDTO) {
        SysRolePrivilege rolePrivilege = SysRolePrivilegeConvert.INSTANCE.toDo(rolePrivilegeDTO);
        this.sysRolePrivilegeMapper.insert(rolePrivilege);
    }

    @Override
    public void deleteByRoleId(Long... roleId) {
        this.sysRolePrivilegeMapper.delete(
                Wrappers.lambdaQuery(SysRolePrivilege.class)
                        .in(SysRolePrivilege::getRoleId, Arrays.stream(roleId).collect(Collectors.toSet()))
        );
    }

}
