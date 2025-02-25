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
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basedt.dms.dao.entity.master.sys.SysUser;
import com.basedt.dms.dao.mapper.master.sys.SysUserMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.sys.SysRoleService;
import com.basedt.dms.service.sys.SysUserRoleService;
import com.basedt.dms.service.sys.SysUserService;
import com.basedt.dms.service.sys.convert.SysUserConvert;
import com.basedt.dms.service.sys.dto.SysRoleDTO;
import com.basedt.dms.service.sys.dto.SysUserDTO;
import com.basedt.dms.service.sys.dto.SysUserRoleDTO;
import com.basedt.dms.service.sys.param.SysUserParam;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    private final SysRoleService sysRoleService;

    private final SysUserRoleService sysUserRoleService;

    public SysUserServiceImpl(SysUserMapper sysUserMapper,
                              SysRoleService sysRoleService,
                              SysUserRoleService sysUserRoleService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleService = sysRoleService;
        this.sysUserRoleService = sysUserRoleService;
    }

    @Override
    public void insert(SysUserDTO userDTO) {
        SysUser user = SysUserConvert.INSTANCE.toDo(userDTO);
        this.sysUserMapper.insert(user);
    }

    @Override
    public void update(SysUserDTO userDTO) {
        SysUser user = SysUserConvert.INSTANCE.toDo(userDTO);
        this.sysUserMapper.updateById(user);
    }

    @Override
    public void deleteById(Long id) {
        this.sysUserMapper.deleteById(id);
    }

    @Override
    public void deleteBatch(List<Long> idList) {
        this.sysUserMapper.deleteBatchIds(idList);
    }

    @Override
    public SysUserDTO selectOne(Long id) {
        SysUser user = this.sysUserMapper.selectById(id);
        return SysUserConvert.INSTANCE.toDto(user);
    }

    @Override
    public SysUserDTO selectByUserName(String userName) {
        SysUser user = this.sysUserMapper.selectOne(
                Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUserName, userName)
        );
        return SysUserConvert.INSTANCE.toDto(user);
    }

    @Override
    public SysUserDTO selectByEmail(String email) {
        SysUser user = this.sysUserMapper.selectOne(
                Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getEmail, email)
        );
        return SysUserConvert.INSTANCE.toDto(user);
    }

    @Override
    public PageDTO<SysUserDTO> listByPage(SysUserParam sysUserParam) {
        Page<SysUser> page = this.sysUserMapper.selectPage(
                new PageDTO<>(sysUserParam.getCurrent(), sysUserParam.getPageSize()),
                Wrappers.lambdaQuery(SysUser.class)
                        .like(StrUtil.isNotBlank(sysUserParam.getUserName()), SysUser::getUserName, sysUserParam.getUserName())
                        .eq(StrUtil.isNotBlank(sysUserParam.getUserStatus()), SysUser::getUserStatus, sysUserParam.getUserStatus())
                        .eq(StrUtil.isNotBlank(sysUserParam.getEmail()), SysUser::getEmail, sysUserParam.getEmail())
        );
        PageDTO<SysUserDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(SysUserConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }

    @Override
    public List<SysRoleDTO> listAllPrivilege(String userName) {
        return this.sysRoleService.listRoleByUserName(userName);
    }

    @Override
    public List<SysUserDTO> listUserByRole(Long... roleId) {
        List<SysUserRoleDTO> userRoleList = this.sysUserRoleService.selectByRoleId(roleId);
        if (CollectionUtil.isEmpty(userRoleList)) {
            return null;
        }
        List<SysUser> userList = this.sysUserMapper.selectBatchIds(
                userRoleList.stream()
                        .map(SysUserRoleDTO::getUserId)
                        .collect(Collectors.toList()));
        return SysUserConvert.INSTANCE.toDto(userList);
    }

    @Override
    public List<SysUserDTO> listUserById(Long... idList) {
        List<SysUser> userList = this.sysUserMapper.selectBatchIds(Arrays.stream(idList).collect(Collectors.toSet()));
        return SysUserConvert.INSTANCE.toDto(userList);
    }

    @Override
    public void grantUserToRole(Long roleId, List<Long> userIds) {
        if (CollectionUtil.isEmpty(userIds)) {
            this.sysUserRoleService.deleteByRoleId(roleId);
        } else {
            this.sysUserRoleService.deleteByRoleId(roleId);
            for (Long userId : userIds) {
                SysUserRoleDTO userRoleDTO = new SysUserRoleDTO();
                userRoleDTO.setUserId(userId);
                userRoleDTO.setRoleId(roleId);
                this.sysUserRoleService.insert(userRoleDTO);
            }
        }
    }
}
