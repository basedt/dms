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
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TableDTO extends ObjectDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long tableRows;

    private Long dataBytes;

    private LocalDateTime lastAccessTime;

    private String remark;

    private List<ColumnDTO> columns;

    private List<IndexDTO> indexes;

    private List<PartitionDTO> partitions;

    private List<ObjectDTO> pks;

    private List<ObjectDTO> fks;

    public String getTableName() {
        return this.getObjectName();
    }

}
