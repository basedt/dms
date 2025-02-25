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

import java.util.Objects;

@Getter
public enum FileEncoding {
    GBK("GBK", "GBK"),
    UTF8("UTF-8", "UTF-8");

    private final String value;
    private final String label;

    FileEncoding(String value, String label) {
        this.label = label;
        this.value = value;
    }

    public boolean equalsAsDict(DictVO dictVO) {
        if (Objects.nonNull(dictVO)) {
            return this.getValue().equals(dictVO.getValue());
        }
        return false;
    }

    public DictVO toDict() {
        return new DictVO(this.getValue(), this.getLabel());
    }
}
