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
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.dao.entity.log.LogSqlHistory;
import com.basedt.dms.dao.mapper.log.LogSqlHistoryMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.log.LogSqlHistoryService;
import com.basedt.dms.service.log.convert.LogSqlHistoryConvert;
import com.basedt.dms.service.log.dto.LogSqlHistoryDTO;
import com.basedt.dms.service.log.param.LogSqlHistoryParam;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LogSqlHistoryServiceImpl implements LogSqlHistoryService {

    private final LogSqlHistoryMapper logSqlHistoryMapper;

    public LogSqlHistoryServiceImpl(LogSqlHistoryMapper logSqlHistoryMapper) {
        this.logSqlHistoryMapper = logSqlHistoryMapper;
    }

    @Override
    public int insert(LogSqlHistoryDTO logSqlHistoryDTO) {
        LogSqlHistory logSqlHistory = LogSqlHistoryConvert.INSTANCE.toDo(logSqlHistoryDTO);
        return this.logSqlHistoryMapper.insert(logSqlHistory);
    }

    @Override
    public PageDTO<LogSqlHistoryDTO> listByPage(LogSqlHistoryParam param) {
        Page<LogSqlHistory> page = this.logSqlHistoryMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(LogSqlHistory.class)
                        .eq(StrUtil.isNotBlank(param.getCreator()), LogSqlHistory::getCreator, param.getCreator())
                        .eq(Objects.nonNull(param.getDatasourceId()), LogSqlHistory::getDatasourceId, param.getDatasourceId())
                        .eq(Objects.nonNull(param.getWorkspaceId()), LogSqlHistory::getWorkspaceId, param.getWorkspaceId())
                        .eq(StrUtil.isNotBlank(param.getSqlStatus()), LogSqlHistory::getSqlStatus, param.getSqlStatus())
                        .ge(StrUtil.isNotBlank(param.getStartTimeFrom()), LogSqlHistory::getStartTime, DateTimeUtil.toLocalDateTime(param.getStartTimeFrom(), DateTimeUtil.NORMAL_DATETIME_PATTERN))
                        .le(StrUtil.isNotBlank(param.getStartTimeTo()), LogSqlHistory::getStartTime, DateTimeUtil.toLocalDateTime(param.getStartTimeTo(), DateTimeUtil.NORMAL_DATETIME_PATTERN))
                        .ge(StrUtil.isNotBlank(param.getEndTimeFrom()), LogSqlHistory::getEndTime, DateTimeUtil.toLocalDateTime(param.getEndTimeFrom(), DateTimeUtil.NORMAL_DATETIME_PATTERN))
                        .le(StrUtil.isNotBlank(param.getEndTimeTo()), LogSqlHistory::getEndTime, DateTimeUtil.toLocalDateTime(param.getEndTimeTo(), DateTimeUtil.NORMAL_DATETIME_PATTERN))
        );
        PageDTO<LogSqlHistoryDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(LogSqlHistoryConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }
}
