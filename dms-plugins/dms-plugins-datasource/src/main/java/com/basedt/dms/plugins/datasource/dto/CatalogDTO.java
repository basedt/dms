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

import cn.hutool.crypto.digest.DigestUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.vo.TreeNodeVO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CatalogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String catalogName;

    private List<SchemaDTO> schemas;

    public CatalogDTO() {
    }

    public CatalogDTO(String catalogName) {
        this.catalogName = catalogName;
    }

    public TreeNodeVO toTreeNodeVO() {
        TreeNodeVO vo = new TreeNodeVO();
        String keyStr = getCatalogName().toLowerCase();
        vo.setKey(DigestUtil.md5Hex(keyStr));
        vo.setTitle(keyStr);
        vo.setType(DbObjectType.CATALOG.name());
        vo.setIdentifier(keyStr);
        vo.setParentKey(DigestUtil.md5Hex(Constants.ROOT_CATALOG_CODE));
        vo.setIsLeaf(false);
        vo.setOrder(keyStr);
        return vo;
    }
}
