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
import com.basedt.dms.dao.entity.master.workspace.DmsDataSource;
import com.basedt.dms.dao.mapper.master.workspace.DmsDataSourceMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.workspace.DmsDataSourceService;
import com.basedt.dms.service.workspace.convert.DmsDataSourceConvert;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;
import com.basedt.dms.service.workspace.param.DmsDataSourceParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DmsDataSourceServiceImpl implements DmsDataSourceService {

    private final DmsDataSourceMapper dmsDataSourceMapper;

    public DmsDataSourceServiceImpl(DmsDataSourceMapper dmsDataSourceMapper) {
        this.dmsDataSourceMapper = dmsDataSourceMapper;
    }


    @Override
    public void insert(DmsDataSourceDTO dataSourceDTO) {
        DmsDataSource dataSource = DmsDataSourceConvert.INSTANCE.toDo(dataSourceDTO);
        this.dmsDataSourceMapper.insert(dataSource);
    }

    @Override
    public void update(DmsDataSourceDTO dataSourceDTO) {
        DmsDataSource dataSource = DmsDataSourceConvert.INSTANCE.toDo(dataSourceDTO);
        this.dmsDataSourceMapper.updateById(dataSource);
    }

    @Override
    public void deleteById(Long id) {
        this.dmsDataSourceMapper.deleteById(id);
    }

    @Override
    public void deleteBatch(List<Long> idList) {
        this.dmsDataSourceMapper.deleteBatchIds(idList);
    }

    @Override
    public DmsDataSourceDTO selectOne(Long id) {
        DmsDataSource dataSource = this.dmsDataSourceMapper.selectById(id);
        return DmsDataSourceConvert.INSTANCE.toDto(dataSource);
    }

    @Override
    public PageDTO<DmsDataSourceDTO> listByPage(DmsDataSourceParam param) {
        Page<DmsDataSource> page = this.dmsDataSourceMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(DmsDataSource.class)
                        .eq(Objects.nonNull(param.getWorkspaceId()), DmsDataSource::getWorkspaceId, param.getWorkspaceId())
                        .like(StrUtil.isNotBlank(param.getDatasourceName()), DmsDataSource::getDatasourceName, param.getDatasourceName())
                        .eq(StrUtil.isNotBlank(param.getDatasourceType()), DmsDataSource::getDatasourceType, param.getDatasourceType())
                        .eq(StrUtil.isNotBlank(param.getHostName()), DmsDataSource::getHostName, param.getHostName())
                        .eq(StrUtil.isNotBlank(param.getDatabaseName()), DmsDataSource::getDatabaseName, param.getDatabaseName())
        );
        PageDTO<DmsDataSourceDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(DmsDataSourceConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }

    @Override
    public List<DmsDataSourceDTO> listByWorkspaceId(Long workspaceId) {
        List<DmsDataSource> list = this.dmsDataSourceMapper.selectList(
                Wrappers.lambdaQuery(DmsDataSource.class).eq(DmsDataSource::getWorkspaceId, workspaceId)
        );
        return DmsDataSourceConvert.INSTANCE.toDto(list);
    }
}
