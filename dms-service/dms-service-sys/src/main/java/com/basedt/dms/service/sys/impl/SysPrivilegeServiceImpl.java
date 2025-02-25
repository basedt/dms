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
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.dao.entity.master.sys.SysPrivilege;
import com.basedt.dms.dao.mapper.master.sys.SysPrivilegeMapper;
import com.basedt.dms.service.sys.SysPrivilegeService;
import com.basedt.dms.service.sys.SysRolePrivilegeService;
import com.basedt.dms.service.sys.convert.SysPrivilegeConvert;
import com.basedt.dms.service.sys.dto.SysPrivilegeDTO;
import com.basedt.dms.service.sys.dto.SysRolePrivilegeDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SysPrivilegeServiceImpl implements SysPrivilegeService {

    private final SysPrivilegeMapper sysPrivilegeMapper;

    private final SysRolePrivilegeService sysRolePrivilegeService;

    public SysPrivilegeServiceImpl(SysPrivilegeMapper sysPrivilegeMapper,
                                   SysRolePrivilegeService sysRolePrivilegeService) {
        this.sysPrivilegeMapper = sysPrivilegeMapper;
        this.sysRolePrivilegeService = sysRolePrivilegeService;
    }

    @Override
    public List<Tree<String>> listPrivilegeTree() {
        List<SysPrivilege> list = this.sysPrivilegeMapper.selectList(Wrappers.emptyWrapper());
        List<SysPrivilegeDTO> privilegeList = SysPrivilegeConvert.INSTANCE.toDto(list);
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("key");
        config.setNameKey("title");
        return TreeUtil.build(privilegeList, Constants.ROOT_PRIVILEGE_CODE, config, (node, tree) -> {
            tree.setId(node.getPrivilegeCode());
            tree.setName(node.getPrivilegeName());
            tree.setParentId(node.getParentCode());
            tree.setWeight(node.getPrivilegeCode());
            tree.putExtra("level", node.getLevel());
        });
    }

    @Override
    public void insert(SysPrivilegeDTO privilege) {
        SysPrivilege sysPrivilege = SysPrivilegeConvert.INSTANCE.toDo(privilege);
        this.sysPrivilegeMapper.insert(sysPrivilege);
    }

    @Override
    public void truncate() {
        this.sysPrivilegeMapper.delete(Wrappers.emptyWrapper());
    }

    @Override
    public List<String> listPrivilegeByRole(Long roleId) {
        List<SysPrivilege> list = this.sysPrivilegeMapper.listPrivilegeByRole(roleId);
        if (CollectionUtil.isNotEmpty(list)) {
            return list.stream().map(SysPrivilege::getPrivilegeCode).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void grantPrivilegeToRole(Long roleId, List<String> privilegeCode) {
        if (CollectionUtil.isEmpty(privilegeCode)) {
            this.sysRolePrivilegeService.deleteByRoleId(roleId);
        } else {
            this.sysRolePrivilegeService.deleteByRoleId(roleId);
            for (String code : privilegeCode) {
                SysPrivilege privilege = this.sysPrivilegeMapper.selectOne(
                        Wrappers
                                .lambdaQuery(SysPrivilege.class)
                                .eq(SysPrivilege::getPrivilegeCode, code));
                if (Objects.nonNull(privilege)) {
                    SysRolePrivilegeDTO rolePrivilege = new SysRolePrivilegeDTO();
                    rolePrivilege.setRoleId(roleId);
                    rolePrivilege.setPrivilegeId(privilege.getId());
                    this.sysRolePrivilegeService.insert(rolePrivilege);
                }
            }
        }
    }
}
