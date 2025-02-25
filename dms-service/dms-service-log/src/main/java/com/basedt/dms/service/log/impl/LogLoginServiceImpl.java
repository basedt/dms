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
import com.basedt.dms.dao.entity.log.LogLogin;
import com.basedt.dms.dao.mapper.log.LogLoginMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.log.LogLoginService;
import com.basedt.dms.service.log.convert.LogLoginConvert;
import com.basedt.dms.service.log.dto.LogLoginDTO;
import com.basedt.dms.service.log.param.LogLoginParam;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LogLoginServiceImpl implements LogLoginService {

    private final LogLoginMapper logLoginMapper;

    public LogLoginServiceImpl(LogLoginMapper logLoginMapper) {
        this.logLoginMapper = logLoginMapper;
    }

    @Override
    public int insert(LogLoginDTO logLoginDTO) {
        LogLogin log = LogLoginConvert.INSTANCE.toDo(logLoginDTO);
        return this.logLoginMapper.insert(log);
    }

    @Override
    public PageDTO<LogLoginDTO> listByPage(LogLoginParam param) {
        Page<LogLogin> page = this.logLoginMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(LogLogin.class)
                        .eq(StrUtil.isNotBlank(param.getUserName()), LogLogin::getUserName, param.getUserName())
                        .ge(Objects.nonNull(param.getLoginTime()), LogLogin::getLoginTime, param.getLoginTime())
                        .orderByDesc(LogLogin::getLoginTime)
        );
        PageDTO<LogLoginDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(LogLoginConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }
}
