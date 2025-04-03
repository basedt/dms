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
package com.basedt.dms.alert.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basedt.dms.alert.SysMessageService;
import com.basedt.dms.alert.convert.SysMessageConvert;
import com.basedt.dms.alert.dto.SysMessageDTO;
import com.basedt.dms.alert.param.SysMessageParam;
import com.basedt.dms.common.enums.Bool;
import com.basedt.dms.dao.entity.master.sys.SysMessage;
import com.basedt.dms.dao.mapper.master.sys.SysMessageMapper;
import com.basedt.dms.service.base.dto.PageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MessageAlertServiceImpl implements SysMessageService {

    private final SysMessageMapper sysMessageMapper;


    public MessageAlertServiceImpl(SysMessageMapper sysMessageMapper) {
        this.sysMessageMapper = sysMessageMapper;
    }

    @Override
    public PageDTO<SysMessageDTO> listMessageByPage(SysMessageParam param) {
        Page<SysMessage> page = this.sysMessageMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(SysMessage.class)
                        .eq(StrUtil.isNotBlank(param.getReceiver()), SysMessage::getReceiver, param.getReceiver())
                        .eq(StrUtil.isNotBlank(param.getIsRead()), SysMessage::getIsRead, param.getIsRead())
                        .eq(StrUtil.isNotBlank(param.getIsDelete()), SysMessage::getIsDelete, param.getIsDelete())
                        .eq(StrUtil.isNotBlank(param.getMessageType()), SysMessage::getMessageType, param.getMessageType())
                        .orderByDesc(SysMessage::getCreateTime)
        );
        PageDTO<SysMessageDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(SysMessageConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }

    @Override
    public void insert(SysMessageDTO messageDTO) {
        SysMessage message = SysMessageConvert.INSTANCE.toDo(messageDTO);
        this.sysMessageMapper.insert(message);
    }

    @Override
    public void update(SysMessageDTO messageDTO) {
        SysMessage message = SysMessageConvert.INSTANCE.toDo(messageDTO);
        this.sysMessageMapper.updateById(message);
    }

    @Override
    public List<SysMessageDTO> listByIds(Set<Long> ids) {
        List<SysMessage> list = this.sysMessageMapper.selectByIds(ids);
        return SysMessageConvert.INSTANCE.toDto(list);
    }

    @Override
    public Long countUnReadMsg(String receiver) {
        return this.sysMessageMapper.selectCount(
                Wrappers.lambdaQuery(SysMessage.class)
                        .eq(SysMessage::getReceiver, receiver)
                        .eq(SysMessage::getIsRead, Bool.NO.getValue())
        );
    }
}
