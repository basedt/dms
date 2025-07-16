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
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.types.Type;
import lombok.Data;

@Data
public class ColumnDTO {

    private String id;

    private String catalogName;

    private String schemaName;

    private String tableName;

    private String columnName;

    private String dataType;

    private Integer sqlType;

    private Type type;

    private Integer dataLength;

    private Integer dataPrecision;

    private Integer dataScale;

    private String defaultValue;

    private Integer columnOrdinal;

    private String remark;

    private Boolean isNullable;

    public TreeNodeVO toTreeNodeVO(TreeNodeVO parent) {
        TreeNodeVO nodeVO = new TreeNodeVO();
        String keyStr = getColumnName().toLowerCase();
        String key = DigestUtil.md5Hex(parent.getKey() + keyStr);
        nodeVO.setKey(key);
        nodeVO.setTitle(keyStr);
        nodeVO.setType(DbObjectType.COLUMN.name());
        nodeVO.setIdentifier(StrUtil.join(Constants.SEPARATOR_DOT, parent.getIdentifier(), keyStr));
        nodeVO.setParentKey(parent.getKey());
        nodeVO.setOrder(StrUtil.fillBefore(String.valueOf(columnOrdinal), '0', 5));
        nodeVO.setIsLeaf(true);
        return nodeVO;
    }

}
