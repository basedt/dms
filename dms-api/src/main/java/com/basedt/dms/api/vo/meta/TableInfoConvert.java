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

package com.basedt.dms.api.vo.meta;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TableInfoConvert {

    public static TableInfoVO toTableVO(TableDTO dto) {
        if (Objects.isNull(dto)) {
            return null;
        }
        TableInfoVO vo = new TableInfoVO();
        vo.setCatalog(dto.getCatalogName());
        vo.setSchemaName(dto.getSchemaName());
        vo.setTableName(dto.getTableName());
        vo.setComment(dto.getRemark());
        vo.setColumns(getColumnInfo(dto));
        vo.setIndexes(getIndexInfo(dto));
        vo.setPartitions(getPartitionInfo(dto));
        return vo;
    }

    public static TableDTO toTableDTO(TableInfoVO vo) {
        if (Objects.isNull(vo)) {
            return null;
        }
        TableDTO dto = new TableDTO();
        //todo implement

        return dto;
    }

    public static List<ColumnInfoVO> getColumnInfo(TableDTO dto) {
        if (Objects.isNull(dto)) {
            return null;
        }
        List<ColumnInfoVO> columns = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dto.getColumns())) {
            for (ColumnDTO colDTO : dto.getColumns()) {
                ColumnInfoVO colVO = new ColumnInfoVO();
                colVO.setId(UUID.fastUUID().toString());
                colVO.setOrdinal(colDTO.getColumnOrdinal());
                colVO.setColumnName(colDTO.getColumnName());
                colVO.setDataType(formatColumnType(colDTO));
                colVO.setDefaultValue(colDTO.getDefaultValue());
                colVO.setComment(colDTO.getRemark());
                colVO.setNullable(colDTO.getIsNullable());
                columns.add(colVO);
            }
        }
        return columns;
    }

    /**
     * format data type
     *
     * @param dto
     * @return
     */
    private static String formatColumnType(ColumnDTO dto) {
        if (Objects.isNull(dto) || StrUtil.isEmpty(dto.getDataType())) {
            return null;
        } else if (("decimal".equalsIgnoreCase(dto.getDataType()) ||
                "numeric".equalsIgnoreCase(dto.getDataType()) ||
                "number".equalsIgnoreCase(dto.getDataType())) &&
                Objects.nonNull(dto.getDataScale()) &&
                dto.getDataScale() > 0) {
            return dto.getDataType() + "(" + dto.getDataPrecision() + "," + dto.getDataScale() + ")";
        } else if (Objects.isNull(dto.getDataLength()) || dto.getDataLength() <= 0) {
            return dto.getDataType();
        } else {
            return dto.getDataType() + "(" + dto.getDataLength() + ")";
        }


    }

    public static List<IndexInfoVO> getIndexInfo(TableDTO dto) {
        if (Objects.isNull(dto)) {
            return null;
        }
        List<IndexInfoVO> indexes = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dto.getIndexes())) {
            for (IndexDTO idxDTO : dto.getIndexes()) {
                IndexInfoVO idxVO = new IndexInfoVO();
                idxVO.setId(UUID.fastUUID().toString());
                idxVO.setIndexName(idxDTO.getIndexName());
                idxVO.setIndexType(idxDTO.getIndexType());
                idxVO.setUniqueness(idxDTO.getIsUniqueness());
                idxVO.setColumns(Arrays.stream(idxDTO.getColumns().split(",")).toList());
                idxVO.setPk(isPrimaryKey(idxDTO.getIndexName(), dto));
                idxVO.setFk(isForeignKey(idxDTO.getIndexName(), dto));
                indexes.add(idxVO);
            }
        }
        return indexes;
    }

    private static boolean isPrimaryKey(String indexName, TableDTO dto) {
        List<ObjectDTO> pks = dto.getPks();
        if (CollectionUtils.isEmpty(pks)) {
            return false;
        }
        for (ObjectDTO objDTO : pks) {
            if (objDTO.getObjectName().equals(indexName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isForeignKey(String indexName, TableDTO dto) {
        List<ObjectDTO> pks = dto.getFks();
        if (CollectionUtils.isEmpty(pks)) {
            return false;
        }
        for (ObjectDTO objDTO : pks) {
            if (objDTO.getObjectName().equals(indexName)) {
                return true;
            }
        }
        return false;
    }

    public static List<PartitionInfoVO> getPartitionInfo(TableDTO dto) {
        if (Objects.isNull(dto)) {
            return null;
        }
        List<PartitionInfoVO> partitions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dto.getPartitions())) {
            for (PartitionDTO ptDTO : dto.getPartitions()) {
                PartitionInfoVO ptVO = new PartitionInfoVO();
                ptVO.setId(UUID.fastUUID().toString());
                ptVO.setPartitionName(ptDTO.getPartitionName());
                ptVO.setPartitionExpr(ptDTO.getPartitionExpr());
                ptVO.setRows(ptDTO.getPartitionRows());
                ptVO.setSize(ptDTO.getPartitionBytes());
                ptVO.setCreateTime(ptDTO.getCreateTime());
                ptVO.setUpdateTime(ptDTO.getLastDdlTime());
                partitions.add(ptVO);
            }
        }
        return partitions;
    }
}
