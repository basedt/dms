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

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.dao.entity.master.sys.SysConfig;
import com.basedt.dms.dao.mapper.master.sys.SysConfigMapper;
import com.basedt.dms.service.sys.SysConfigService;
import com.basedt.dms.service.sys.convert.SysConfigConvert;
import com.basedt.dms.service.sys.dto.LLMConfigDTO;
import com.basedt.dms.service.sys.dto.SysConfigDTO;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public SysConfigServiceImpl(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    @Override
    public void insert(SysConfigDTO config) {
        SysConfig sysConfig = SysConfigConvert.INSTANCE.toDo(config);
        this.sysConfigMapper.insert(sysConfig);
    }

    @Override
    public void update(SysConfigDTO config) {
        SysConfig sysConfig = SysConfigConvert.INSTANCE.toDo(config);
        this.sysConfigMapper.updateById(sysConfig);
    }

    @Override
    public void update(String key, String value) {
        SysConfig config = new SysConfig();
        config.setCfgCode(key);
        config.setCfgValue(value);
        this.sysConfigMapper.update(config,
                Wrappers
                        .lambdaUpdate(SysConfig.class)
                        .eq(SysConfig::getCfgCode, config.getCfgCode()));
    }

    @Override
    public void deleteByKey(String key) {
        this.sysConfigMapper.delete(
                Wrappers
                        .lambdaQuery(SysConfig.class)
                        .eq(SysConfig::getCfgCode, key)
        );
    }

    @Override
    public String selectValueByKey(String key) {
        SysConfig sysConfig = this.sysConfigMapper.selectOne(
                Wrappers.lambdaQuery(SysConfig.class)
                        .eq(SysConfig::getCfgCode, key)
        );
        if (Objects.nonNull(sysConfig)) {
            return sysConfig.getCfgValue();
        } else {
            return null;
        }
    }

    @Override
    public LLMConfigDTO getLLMConfig() {
        String llmConfig = selectValueByKey(Constants.CFG_LLM_CODE);
        LLMConfigDTO config;
        if (StrUtil.isNotBlank(llmConfig)) {
            config = JSONUtil.toBean(llmConfig, LLMConfigDTO.class);
        } else {
            config = new LLMConfigDTO();
        }
        return config;
    }
}
