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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basedt.dms.dao.entity.log.LogDataTask;
import com.basedt.dms.dao.mapper.log.LogDataTaskMapper;
import com.basedt.dms.service.log.LogDataTaskService;
import com.basedt.dms.service.log.convert.LogDataTaskConvert;
import com.basedt.dms.service.log.dto.LogDataTaskDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogDataTaskServiceImpl implements LogDataTaskService {

    private final LogDataTaskMapper logDataTaskMapper;

    public LogDataTaskServiceImpl(LogDataTaskMapper logDataTaskMapper) {
        this.logDataTaskMapper = logDataTaskMapper;
    }

    @Override
    public int insert(LogDataTaskDTO logDataTaskDTO) {
        LogDataTask logDataTask = LogDataTaskConvert.INSTANCE.toDo(logDataTaskDTO);
        return logDataTaskMapper.insert(logDataTask);
    }

    @Override
    public List<LogDataTaskDTO> listByTask(Long taskId) {
        List<LogDataTask> list = this.logDataTaskMapper.selectList(Wrappers.lambdaQuery(LogDataTask.class)
                .eq(LogDataTask::getTaskId, taskId)
                .orderByAsc(LogDataTask::getCreateTime)
        );
        return LogDataTaskConvert.INSTANCE.toDto(list);
    }
}
