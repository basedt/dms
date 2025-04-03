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
package com.basedt.dms.service.workspace.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basedt.dms.dao.entity.master.workspace.DmsWorkspace;
import com.basedt.dms.dao.mapper.master.workspace.DmsWorkspaceMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.workspace.DmsWorkspaceService;
import com.basedt.dms.service.workspace.convert.DmsWorkspaceConvert;
import com.basedt.dms.service.workspace.dto.DmsWorkspaceDTO;
import com.basedt.dms.service.workspace.param.DmsWorkspaceParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DmsWorkspaceServiceImpl implements DmsWorkspaceService {

    private final DmsWorkspaceMapper dmsWorkspaceMapper;

    public DmsWorkspaceServiceImpl(DmsWorkspaceMapper dmsWorkspaceMapper) {
        this.dmsWorkspaceMapper = dmsWorkspaceMapper;
    }

    @Override
    public void insert(DmsWorkspaceDTO workspaceDTO) {
        DmsWorkspace workspace = DmsWorkspaceConvert.INSTANCE.toDo(workspaceDTO);
        this.dmsWorkspaceMapper.insert(workspace);
    }

    @Override
    public void update(DmsWorkspaceDTO workspaceDTO) {
        DmsWorkspace workspace = DmsWorkspaceConvert.INSTANCE.toDo(workspaceDTO);
        this.dmsWorkspaceMapper.updateById(workspace);
    }

    @Override
    public void deleteById(Long id) {
        this.dmsWorkspaceMapper.deleteById(id);
    }

    @Override
    public void deleteBatch(List<Long> idList) {
        this.dmsWorkspaceMapper.deleteByIds(idList);
    }

    @Override
    public DmsWorkspaceDTO selectOne(Long id) {
        DmsWorkspace workspace = this.dmsWorkspaceMapper.selectById(id);
        return DmsWorkspaceConvert.INSTANCE.toDto(workspace);
    }

    @Override
    public DmsWorkspaceDTO selectOne(String workspaceCode) {
        DmsWorkspace workspace = this.dmsWorkspaceMapper.selectOne(Wrappers.lambdaQuery(DmsWorkspace.class)
                .eq(DmsWorkspace::getWorkspaceCode, workspaceCode)
        );
        return DmsWorkspaceConvert.INSTANCE.toDto(workspace);
    }

    @Override
    public PageDTO<DmsWorkspaceDTO> listByPage(DmsWorkspaceParam param) {
        Page<DmsWorkspace> page = this.dmsWorkspaceMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(DmsWorkspace.class)
                        .like(StrUtil.isNotBlank(param.getWorkspaceCode()), DmsWorkspace::getWorkspaceCode, param.getWorkspaceCode())
                        .like(StrUtil.isNotBlank(param.getWorkspaceName()), DmsWorkspace::getWorkspaceName, param.getWorkspaceName())
                        .eq(StrUtil.isNotBlank(param.getOwner()), DmsWorkspace::getOwner, param.getOwner())
        );
        PageDTO<DmsWorkspaceDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(DmsWorkspaceConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }
}
