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
package com.basedt.dms.common.utils;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private final static Logger log = LoggerFactory.getLogger(RedisUtil.class);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public long getExipre(String key) {
        if (StrUtil.isEmpty(key)) {
            return 0;
        }

        Long time = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return time != null ? time : 0L;
    }

    public boolean hasKey(String key) {
        try {
            Boolean flag = redisTemplate.hasKey(key);
            return flag != null ? flag : false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void delKeys(String... keys) {
        if (keys != null && keys.length > 0) {
            redisTemplate.delete(Arrays.asList(keys));
        }
    }

    public String get(String key) {
        return StrUtil.isEmpty(key) ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    public boolean set(String key, String value, long expireTime) {
        try {
            if (expireTime > 0) {
                redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public List<String> scan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>();
        while (cursor.hasNext()) {
            result.add(new String(cursor.next()));
        }
        try {
            RedisConnectionUtils.releaseConnection(rc, factory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public void queuePush(String key, Collection<String> values) {
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void queuePush(String key, String value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String queuePop(String key) {
        if (StrUtil.isEmpty(key)) {
            return null;
        } else {
            return redisTemplate.opsForList().rightPop(key);
        }
    }

    public void queueRightPush(String key, String value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Long getQueueSize(String key) {
        if (StrUtil.isEmpty(key)) {
            return 0L;
        } else {
            return redisTemplate.opsForList().size(key);
        }
    }
}
