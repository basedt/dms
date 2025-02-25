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
package com.basedt.dms.common.enums;

import com.basedt.dms.common.vo.DictVO;
import lombok.Getter;

@Getter
public enum RegisterChannel {

    REGISTER("01", "REGISTER"),

    BACKGROUND_IMPORT("02", "BACKGROUND_IMPORT");

    private final String value;
    private final String label;

    RegisterChannel(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public DictVO toDict() {
        return new DictVO(this.getValue(), this.getLabel());
    }

}
