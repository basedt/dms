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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basedt.dms.common.enums.RoleType;
import com.basedt.dms.common.utils.I18nUtil;
import com.basedt.dms.dao.entity.BaseDO;
import com.basedt.dms.dao.entity.master.sys.SysRole;
import com.basedt.dms.dao.mapper.master.sys.SysRoleMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.sys.SysPrivilegeService;
import com.basedt.dms.service.sys.SysRolePrivilegeService;
import com.basedt.dms.service.sys.SysRoleService;
import com.basedt.dms.service.sys.SysUserRoleService;
import com.basedt.dms.service.sys.convert.SysRoleConvert;
import com.basedt.dms.service.sys.dto.SysRoleDTO;
import com.basedt.dms.service.sys.dto.SysUserDTO;
import com.basedt.dms.service.sys.dto.SysUserRoleDTO;
import com.basedt.dms.service.sys.param.SysRoleParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.basedt.dms.common.enums.RoleType.ROLE_USER;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    private final SysUserRoleService sysUserRoleService;

    private final SysRolePrivilegeService sysRolePrivilegeService;

    private final SysPrivilegeService sysPrivilegeService;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper,
                              SysUserRoleService sysUserRoleService,
                              SysRolePrivilegeService sysRolePrivilegeService,
                              SysPrivilegeService sysPrivilegeService) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleService = sysUserRoleService;
        this.sysRolePrivilegeService = sysRolePrivilegeService;
        this.sysPrivilegeService = sysPrivilegeService;
    }

    @Override
    public void insert(SysRoleDTO roleDTO) {
        roleDTO.setRoleCode(UUID.randomUUID().toString());
        roleDTO.setRoleType(ROLE_USER.toDict());
        SysRole role = SysRoleConvert.INSTANCE.toDo(roleDTO);
        this.sysRoleMapper.insert(role);
    }

    @Override
    public void update(SysRoleDTO roleDTO) {
        Assert.notNull(roleDTO.getId(), I18nUtil.get("response.error.illegalArgument.notnull.id"));
        SysRole role = SysRoleConvert.INSTANCE.toDo(roleDTO);
        this.sysRoleMapper.updateById(role);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        deleteBatch(new ArrayList<Long>() {{
            add(id);
        }});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> idList) {
        List<SysRole> userRoles = listUserRoles(idList.toArray(new Long[0]));
        if (CollectionUtil.isNotEmpty(userRoles)) {
            Set<Long> userRoleList = userRoles.stream().map(BaseDO::getId).collect(Collectors.toSet());
            this.sysRoleMapper.delete(
                    Wrappers.lambdaQuery(SysRole.class)
                            .in(SysRole::getId, userRoleList)
            );
            this.sysUserRoleService.deleteByRoleId(userRoleList.toArray(new Long[0]));
            this.sysRolePrivilegeService.deleteByRoleId(userRoleList.toArray(new Long[0]));
        }
    }

    @Override
    public SysRoleDTO selectOne(Long id) {
        SysRole role = this.sysRoleMapper.selectById(id);
        return SysRoleConvert.INSTANCE.toDto(role);
    }

    @Override
    public SysRoleDTO selectOne(String roleCode) {
        SysRole role = this.sysRoleMapper.selectOne(
                Wrappers.lambdaQuery(SysRole.class)
                        .eq(SysRole::getRoleCode, roleCode)
        );
        return SysRoleConvert.INSTANCE.toDto(role);
    }

    @Override
    public PageDTO<SysRoleDTO> listByPage(SysRoleParam param) {
        Page<SysRole> page = this.sysRoleMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(SysRole.class)
                        .like(StrUtil.isNotBlank(param.getRoleCode()), SysRole::getRoleCode, param.getRoleCode())
                        .like(StrUtil.isNotBlank(param.getRoleName()), SysRole::getRoleName, param.getRoleName())
                        .eq(StrUtil.isNotBlank(param.getRoleStatus()), SysRole::getRoleStatus, param.getRoleStatus())
        );
        PageDTO<SysRoleDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(SysRoleConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }

    @Override
    public void grantRoleToUser(SysUserDTO user, SysRoleDTO role) {
        if (Objects.nonNull(user) && Objects.nonNull(role)) {
            SysUserRoleDTO userRoleDTO = new SysUserRoleDTO();
            userRoleDTO.setUserId(user.getId());
            userRoleDTO.setRoleId(role.getId());
            this.sysUserRoleService.insert(userRoleDTO);
        }
    }

    @Override
    public void grantRoleToUser(Long userId, List<Long> roleIds) {
        if (CollectionUtil.isEmpty(roleIds)) {
            this.sysUserRoleService.deleteByUserId(userId);
        } else {
            this.sysUserRoleService.deleteByUserId(userId);
            for (Long roleId : roleIds) {
                SysUserRoleDTO userRoleDTO = new SysUserRoleDTO();
                userRoleDTO.setRoleId(roleId);
                userRoleDTO.setUserId(userId);
                this.sysUserRoleService.insert(userRoleDTO);
            }
        }
    }

    @Override
    public List<SysRoleDTO> listRoleByUserName(String userName) {
        List<SysRole> list = this.sysRoleMapper.selectByUserName(userName);
        return SysRoleConvert.INSTANCE.toDto(list);
    }

    @Override
    public List<SysRoleDTO> listAll() {
        List<SysRole> list = this.sysRoleMapper.selectList(Wrappers.emptyWrapper());
        return SysRoleConvert.INSTANCE.toDto(list);
    }

    private List<SysRole> listUserRoles(Long... idList) {
        return this.sysRoleMapper.selectList(
                Wrappers.lambdaQuery(SysRole.class)
                        .notIn(SysRole::getRoleType, RoleType.ROLE_SYS.getValue())
                        .in(SysRole::getId, Arrays.stream(idList).collect(Collectors.toSet()))
        );
    }
}
