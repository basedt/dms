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
package com.basedt.dms.service.base.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(name = "BaseDTO", title = "Base DTO")
public class BaseDTO implements Serializable {

    @Schema(name = "id", title = "id")
    private Long id;

    @Length(max = 32)
    @Schema(name = "creator", title = "creator")
    private String creator;

    @Schema(name = "createTime", title = "create time")
    private LocalDateTime createTime;

    @Length(max = 32)
    @Schema(name = "editor", title = "editor")
    private String editor;

    @Schema(name = "updateTime", title = "update time")
    private LocalDateTime updateTime;
}
