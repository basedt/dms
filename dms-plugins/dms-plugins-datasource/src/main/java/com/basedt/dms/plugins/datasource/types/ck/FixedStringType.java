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

package com.basedt.dms.plugins.datasource.types.ck;

import com.basedt.dms.plugins.datasource.enums.DbDataType;
import com.basedt.dms.plugins.datasource.types.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FixedStringType extends Type.STRING {

    private Integer length;

    public FixedStringType(Integer length) {
        this.length = length;
    }

    public static FixedStringType get(Integer length) {
        return new FixedStringType(length);
    }

    @Override
    public DbDataType type() {
        return DbDataType.CHAR;
    }

    @Override
    public String name() {
        return "FixedString";
    }

    @Override
    public String formatString() {
        return "FixedString(" + length + ")";
    }
}
