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
package com.basedt.dms.service.log.convert;

import com.basedt.dms.dao.entity.log.LogSqlHistory;
import com.basedt.dms.service.base.convert.BaseConvert;
import com.basedt.dms.service.log.dto.LogSqlHistoryDTO;
import com.basedt.dms.service.sys.convert.DictVoConvert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {DictVoConvert.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LogSqlHistoryConvert extends BaseConvert<LogSqlHistory, LogSqlHistoryDTO> {

    LogSqlHistoryConvert INSTANCE = Mappers.getMapper(LogSqlHistoryConvert.class);

    @Override
    @Mapping(target = "sqlStatus", expression = "java(com.basedt.dms.service.sys.convert.DictVoConvert.INSTANCE.toDto(com.basedt.dms.common.constant.DictConstants.SQL_STATUS,entity.getSqlStatus()))")
    LogSqlHistoryDTO toDto(LogSqlHistory entity);
}
