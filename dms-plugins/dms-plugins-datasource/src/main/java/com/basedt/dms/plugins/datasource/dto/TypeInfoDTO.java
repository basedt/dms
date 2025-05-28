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

package com.basedt.dms.plugins.datasource.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Data
public class TypeInfoDTO implements Comparable<TypeInfoDTO> {

    private String typeName;

    //java.sql.Types
    private Integer dataType;

    private Integer precision;

    private Boolean autoIncrement;

    private String localTypeName;

    public TypeInfoDTO() {
    }

    public TypeInfoDTO(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public int compareTo(@NotNull TypeInfoDTO o) {
        if (Objects.isNull(o.getTypeName())) {
            return 1;
        }
        return this.typeName.compareTo(o.getTypeName());
    }
}
