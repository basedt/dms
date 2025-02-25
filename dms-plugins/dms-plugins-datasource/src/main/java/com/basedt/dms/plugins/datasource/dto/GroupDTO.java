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

@Data
public class GroupDTO {

    private static final long serialVersionUID = 1L;

    private String groupKey;

    private String groupLabel;

    private String groupType;

    private String groupOrder;

    public GroupDTO(String groupKey, String groupLabel, String groupType, String groupOrder) {
        this.groupKey = groupKey;
        this.groupLabel = groupLabel;
        this.groupType = groupType;
        this.groupOrder = groupOrder;
    }
}
