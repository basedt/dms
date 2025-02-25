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
package com.basedt.dms.service.base.datasource;

import cn.hutool.core.util.StrUtil;

import static com.basedt.dms.common.constant.Constants.DATASOURCE_MASTER;

public class RoutingDataSourceContext {
    static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    public static void setDataSourceRoutingKey(String key) {
        THREAD_LOCAL.set(key);
    }

    public static String getDataSourceRoutingKey() {
        String key = THREAD_LOCAL.get();
        return StrUtil.isEmpty(key) ? DATASOURCE_MASTER : key;
    }

    public static void clearDataSourceRoutingKey() {
        THREAD_LOCAL.remove();
    }
}
