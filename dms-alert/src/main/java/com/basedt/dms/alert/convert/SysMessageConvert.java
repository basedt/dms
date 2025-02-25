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
package com.basedt.dms.alert.convert;

import com.basedt.dms.alert.dto.SysMessageDTO;
import com.basedt.dms.dao.entity.master.sys.SysMessage;
import com.basedt.dms.service.base.convert.BaseConvert;
import com.basedt.dms.service.sys.convert.DictVoConvert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {DictVoConvert.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysMessageConvert extends BaseConvert<SysMessage, SysMessageDTO> {

    SysMessageConvert INSTANCE = Mappers.getMapper(SysMessageConvert.class);

    @Override
    @Mapping(target = "messageType", expression = "java(com.basedt.dms.service.sys.convert.DictVoConvert.INSTANCE.toDto(com.basedt.dms.common.constant.DictConstants.MESSAGE_TYPE,entity.getMessageType()))")
    @Mapping(target = "isRead", expression = "java(com.basedt.dms.service.sys.convert.DictVoConvert.INSTANCE.toDto(com.basedt.dms.common.constant.DictConstants.IS_READ,entity.getIsRead()))")
    @Mapping(target = "isDelete", expression = "java(com.basedt.dms.service.sys.convert.DictVoConvert.INSTANCE.toDto(com.basedt.dms.common.constant.DictConstants.BOOL,entity.getIsDelete()))")
    SysMessageDTO toDto(SysMessage entity);
}
