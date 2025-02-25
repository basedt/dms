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
package com.basedt.dms.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "TreeNodeVO", title = "Tree Node VO")
public class TreeNodeVO {

    @Schema(name = "key", title = "key")
    private String key;

    @Schema(name = "title", title = "title")
    private String title;

    @Schema(name = "type", title = "type")
    private String type;

    @Schema(name = "identifier", title = "identifier")
    private String identifier;

    @Schema(name = "parentKey", title = "parent key")
    private String parentKey;

    @Schema(name = "isLeaf", title = "is leaf", description = "is leaf node")
    private Boolean isLeaf;

    @Schema(name = "order", title = "order")
    private String order;

}
