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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {

    public static Properties format(String str, String lineSeparator, String kvSeparator) {
        Properties props = new Properties();
        if (StrUtil.isNotBlank(str)) {
            String[] arr = str.split(lineSeparator);
            for (int i = 0; i < arr.length; i++) {
                String[] kv = arr[i].split(kvSeparator);
                if (kv.length == 2) {
                    props.put(kv[0], kv[1]);
                }
            }
        }
        return props;
    }

    public static Map<String, Object> formatToMap(String str, String lineSeparator, String kvSeparator) {
        Map<String, Object> map = new HashMap<>();
        Properties properties = format(str, lineSeparator, kvSeparator);
        if (!properties.isEmpty()) {
            properties.forEach((k, v) -> {
                map.put(String.valueOf(k), v);
            });
        }
        return map;
    }

    public static String toFormatStr(Properties prop, String lineSeparator, String kvSeparator) {
        if (prop.isEmpty()) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        prop.forEach((k, v) -> {
            buffer.append(k).append(kvSeparator).append(v).append(lineSeparator);
        });
        return buffer.toString();
    }

    public static String toFormatStr(Map<String, Object> propMap, String lineSeparator, String kvSeparator) {
        if (CollectionUtil.isEmpty(propMap)) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        propMap.forEach((k, v) -> {
            buffer.append(k).append(kvSeparator).append(v).append(lineSeparator);
        });
        return buffer.toString();
    }
}
