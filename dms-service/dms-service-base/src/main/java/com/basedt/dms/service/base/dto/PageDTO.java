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


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "PageDTO", title = "Common Page DTO")
public class PageDTO<T> extends Page<T> {

    @Schema(name = "data", title = "data list")
    private List<T> data;

    public PageDTO() {

    }

    public PageDTO(Long current, Long pageSize) {
        this(current, pageSize, 0L);
    }

    public PageDTO(Long current, Long pageSize, Long total) {
        super(current, pageSize, total);
    }

    public Long getPageSize() {
        return getSize();
    }

    public void setPageSize(Long pageSize) {
        setSize(pageSize);
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
