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
import com.basedt.dms.dao.entity.master.sys.SysDict;
import com.basedt.dms.dao.mapper.master.sys.SysDictMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.sys.SysDictService;
import com.basedt.dms.service.sys.SysDictTypeService;
import com.basedt.dms.service.sys.convert.SysDictConvert;
import com.basedt.dms.service.sys.dto.SysDictDTO;
import com.basedt.dms.service.sys.param.SysDictParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public class SysDictServiceImpl implements SysDictService {

    private final SysDictMapper sysDictMapper;

    private final SysDictTypeService sysDictTypeService;

    public SysDictServiceImpl(SysDictMapper sysDictMapper, SysDictTypeService sysDictTypeService) {
        this.sysDictMapper = sysDictMapper;
        this.sysDictTypeService = sysDictTypeService;
    }

    @Override
    public void insert(SysDictDTO sysDictDTO) {
        SysDict dict = SysDictConvert.INSTANCE.toDo(sysDictDTO);
        this.sysDictMapper.insert(dict);
        this.sysDictTypeService.refreshDictCache(dict.getDictTypeCode());
    }

    @Override
    public void update(SysDictDTO sysDictDTO) {
        Assert.notNull(sysDictDTO.getId(), I18nUtil.get("response.error.illegalArgument.notnull.id"));
        SysDict dict = SysDictConvert.INSTANCE.toDo(sysDictDTO);
        this.sysDictMapper.updateById(dict);
        this.sysDictTypeService.refreshDictCache(dict.getDictTypeCode());
    }

    @Override
    public void deleteById(@NotNull Long id) {
        SysDictDTO dict = selectOne(id);
        this.sysDictMapper.deleteById(id);
        this.sysDictTypeService.refreshDictCache(dict.getSysDictType().getDictTypeCode());
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
    public SysDictDTO selectOne(Long id) {
        SysDict sysDict = this.sysDictMapper.selectById(id);
        return SysDictConvert.INSTANCE.toDto(sysDict);
    }

    @Override
    public PageDTO<SysDictDTO> listByPage(SysDictParam param) {
        Page<SysDict> page = this.sysDictMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(SysDict.class)
                        .eq(StrUtil.isNotBlank(param.getDictTypeCode()), SysDict::getDictTypeCode, param.getDictTypeCode())
                        .like(StrUtil.isNotBlank(param.getDictCode()), SysDict::getDictCode, param.getDictCode())
                        .like(StrUtil.isNotBlank(param.getDictValue()), SysDict::getDictValue, param.getDictValue())
        );
        PageDTO<SysDictDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(SysDictConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }
}
