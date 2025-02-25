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
import com.basedt.dms.common.utils.I18nUtil;
import com.basedt.dms.dao.entity.master.sys.SysDictType;
import com.basedt.dms.dao.mapper.master.sys.SysDictMapper;
import com.basedt.dms.dao.mapper.master.sys.SysDictTypeMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.sys.SysDictTypeService;
import com.basedt.dms.service.sys.cache.DictCache;
import com.basedt.dms.service.sys.convert.SysDictTypeConvert;
import com.basedt.dms.service.sys.dto.SysDictDTO;
import com.basedt.dms.service.sys.dto.SysDictTypeDTO;
import com.basedt.dms.service.sys.param.SysDictTypeParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysDictTypeServiceImpl implements SysDictTypeService {

    private final SysDictTypeMapper sysDictTypeMapper;

    private final SysDictMapper sysDictMapper;

    public SysDictTypeServiceImpl(SysDictTypeMapper sysDictTypeMapper, SysDictMapper sysDictMapper) {
        this.sysDictTypeMapper = sysDictTypeMapper;
        this.sysDictMapper = sysDictMapper;
    }

    @Override
    public void insert(SysDictTypeDTO sysDictTypeDTO) {
        SysDictType sysDictType = SysDictTypeConvert.INSTANCE.toDo(sysDictTypeDTO);
        this.sysDictTypeMapper.insert(sysDictType);
        refreshDictCache(sysDictTypeDTO.getDictTypeCode());
    }

    @Override
    public void update(SysDictTypeDTO sysDictTypeDTO) {
        Assert.notNull(sysDictTypeDTO.getId(), I18nUtil.get("response.error.illegalArgument.notnull.id"));
        SysDictType sysDictType = SysDictTypeConvert.INSTANCE.toDo(sysDictTypeDTO);
        this.sysDictTypeMapper.updateById(sysDictType);
        refreshDictCache(sysDictTypeDTO.getDictTypeCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(@NotNull Long id) {
        SysDictTypeDTO dictType = selectOne(id);
        if (dictType != null && CollectionUtil.isNotEmpty(dictType.getSysDictList())) {
            List<Long> idList = dictType.getSysDictList()
                    .stream()
                    .map(SysDictDTO::getId)
                    .collect(Collectors.toList());
            this.sysDictMapper.deleteBatchIds(idList);
            DictCache.evictCache(dictType.getDictTypeCode());
        }
        this.sysDictTypeMapper.deleteById(id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> idList) {
        if (CollectionUtil.isNotEmpty(idList)) {
            for (Long id : idList) {
                deleteById(id);
            }
        }
    }

    @Override
    public SysDictTypeDTO selectOne(Long id) {
        SysDictType sysDictType = this.sysDictTypeMapper.selectById(id);
        return SysDictTypeConvert.INSTANCE.toDto(sysDictType);
    }

    @Override
    public SysDictTypeDTO selectOne(String dictTypeCode) {
        SysDictType sysDictType = this.sysDictTypeMapper.selectOne(
                Wrappers.lambdaQuery(SysDictType.class).eq(SysDictType::getDictTypeCode, dictTypeCode)
        );
        return SysDictTypeConvert.INSTANCE.toDto(sysDictType);
    }

    @Override
    public PageDTO<SysDictTypeDTO> listByPage(SysDictTypeParam params) {
        Page<SysDictType> page = this.sysDictTypeMapper.selectPage(
                new PageDTO<>(params.getCurrent(), params.getPageSize()),
                Wrappers.lambdaQuery(SysDictType.class)
                        .like(StrUtil.isNotBlank(params.getDictTypeCode()), SysDictType::getDictTypeCode, params.getDictTypeCode())
                        .like(StrUtil.isNotBlank(params.getDictTypeName()), SysDictType::getDictTypeName, params.getDictTypeName())
        );
        PageDTO<SysDictTypeDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(SysDictTypeConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }

    @Override
    public List<SysDictTypeDTO> listAll() {
        List<SysDictType> list = this.sysDictTypeMapper.selectList(Wrappers.emptyWrapper());
        return SysDictTypeConvert.INSTANCE.toDto(list);
    }

    public void refreshDictCache(String dictCodeType) {
        SysDictTypeDTO dictType = selectOne(dictCodeType);
        DictCache.updateCache(dictType);
    }
}
