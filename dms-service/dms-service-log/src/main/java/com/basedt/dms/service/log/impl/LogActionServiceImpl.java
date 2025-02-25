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
package com.basedt.dms.service.log.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basedt.dms.dao.entity.log.LogAction;
import com.basedt.dms.dao.mapper.log.LogActionMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.log.LogActionService;
import com.basedt.dms.service.log.convert.LogActionConvert;
import com.basedt.dms.service.log.dto.LogActionDTO;
import com.basedt.dms.service.log.param.LogActionParam;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LogActionServiceImpl implements LogActionService {

    private final LogActionMapper logActionMapper;

    public LogActionServiceImpl(LogActionMapper logActionMapper) {
        this.logActionMapper = logActionMapper;
    }

    @Override
    public int insert(LogActionDTO logActionDTO) {
        LogAction log = LogActionConvert.INSTANCE.toDo(logActionDTO);
        return this.logActionMapper.insert(log);
    }

    @Override
    public PageDTO<LogActionDTO> listByPage(LogActionParam param) {
        Page<LogAction> page = this.logActionMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(LogAction.class)
                        .eq(StrUtil.isNotBlank(param.getUserName()), LogAction::getUserName, param.getUserName())
                        .ge(Objects.nonNull(param.getActionTime()), LogAction::getActionTime, param.getActionTime())
                        .orderByDesc(LogAction::getActionTime)
        );
        PageDTO<LogActionDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(LogActionConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }
}
