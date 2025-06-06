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

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.sqids.Sqids;

import java.util.Collections;

public class IdGenerateUtil {

    public static String getId() {
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        Sqids sqids = Sqids.builder()
                .minLength(6)
                .alphabet("abcABCxyzXYZdefghijklmnopqrstuvw0123456789DEFGHIJKLMNOPQRSTUVW")
                .build();
        return sqids.encode(Collections.singletonList(snowflake.nextId()));
    }

}
