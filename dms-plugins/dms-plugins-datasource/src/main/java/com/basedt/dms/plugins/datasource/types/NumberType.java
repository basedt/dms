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

package com.basedt.dms.plugins.datasource.types;

import com.basedt.dms.plugins.datasource.enums.DbDataType;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class NumberType extends Type.NUMBER {

    private Integer precision;

    private Integer scale;

    public NumberType(Integer precision, Integer scale) {
        this.precision = precision;
        this.scale = scale;
    }

    public static NumberType get(Integer precision, Integer scale) {
        return new NumberType(precision, scale);
    }

    @Override
    public DbDataType type() {
        return DbDataType.NUMBER;
    }

    @Override
    public String name() {
        return "number";
    }

    @Override
    public String formatString() {
        if (Objects.nonNull(this.precision) && Objects.nonNull(this.scale) && this.precision > 0) {
            return name() + "(" + precision + "," + scale + ")";
        } else if (Objects.nonNull(this.precision) && this.precision > 0) {
            return name() + "(" + precision + ",0" + ")";
        } else {
            return name();
        }
    }
}
