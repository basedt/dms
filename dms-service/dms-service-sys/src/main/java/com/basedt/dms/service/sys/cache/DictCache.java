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
package com.basedt.dms.service.sys.cache;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.service.sys.dto.SysDictDTO;
import com.basedt.dms.service.sys.dto.SysDictTypeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.basedt.dms.service.sys.cache.CaffeineCacheConfig.UNBOUNDED_CACHE_MANAGER;
import static com.basedt.dms.service.sys.cache.CaffeineCacheConfig.UnBoundedCaches.CACHE_DICT;

@Slf4j
@Component
public class DictCache {
    private static Cache dictCache;

    private final CacheManager cacheManager;

    public DictCache(@Qualifier(UNBOUNDED_CACHE_MANAGER) CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        DictCache.dictCache = cacheManager.getCache(CACHE_DICT);
    }

    public synchronized static void clearCache() {
        dictCache.clear();
    }

    public synchronized static void updateCache(List<SysDictTypeDTO> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            for (SysDictTypeDTO dictType : list) {
                updateCache(dictType);
            }
        }
    }

    public synchronized static void updateCache(SysDictTypeDTO dict) {
        if (StrUtil.isAllNotEmpty(dict.getDictTypeCode())) {
            evictCache(dict.getDictTypeCode());
            dictCache.put(dict.getDictTypeCode(), dict);
        }
    }

    public synchronized static void evictCache(String typeCode) {
        dictCache.evict(typeCode);
    }


    public static SysDictTypeDTO getValueByKey(String key) {
        return dictCache.get(key, SysDictTypeDTO.class);
    }

    public static DictVO getValueByKey(String dictTypeCode, String dictCode) {
        SysDictTypeDTO typeDTO = getValueByKey(dictTypeCode);
        if (Objects.nonNull(typeDTO) && CollectionUtil.isNotEmpty(typeDTO.getSysDictList())) {
            List<SysDictDTO> dictList = typeDTO.getSysDictList();
            Map<String, String> map = dictList.stream().collect(Collectors.toMap(SysDictDTO::getDictCode, SysDictDTO::getDictValue));
            String value = map.getOrDefault(dictCode, null);
            if (StrUtil.isNotEmpty(value)) {
                return new DictVO(dictCode, value);
            }
        }
        return null;
    }

}
