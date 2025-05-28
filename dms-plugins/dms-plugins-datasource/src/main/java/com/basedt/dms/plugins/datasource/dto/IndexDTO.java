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

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.vo.TreeNodeVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.INDEX;

@EqualsAndHashCode(callSuper = true)
@Data
public class IndexDTO extends ObjectDTO {

    private static final long serialVersionUID = 1L;

    private String tableName;

    private String indexType;

    private Boolean isUniqueness;

    private LocalDateTime lastAnalyzedTime;

    private Long indexBytes;

    private String columns;

    public String getIndexName() {
        return this.getObjectName();
    }

    @Override
    public TreeNodeVO toTreeNodeVO(TreeNodeVO parent) {
        TreeNodeVO vo = new TreeNodeVO();
        String keyStr = StrUtil.join(Constants.SEPARATOR_DOT, getTableName(), getIndexName()).toLowerCase();
        String key = DigestUtil.md5Hex(parent.getKey() + keyStr);
        vo.setKey(key);
        vo.setTitle(keyStr);
        vo.setType(INDEX.name());
        vo.setIdentifier(StrUtil.join(Constants.SEPARATOR_DOT, parent.getIdentifier(), keyStr));
        vo.setParentKey(parent.getKey());
        vo.setOrder(keyStr);
        vo.setIsLeaf(true);
        return vo;
    }
}
