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
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
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
        dto.setCatalogName(vo.getCatalog());
        dto.setSchemaName(vo.getSchemaName());
        dto.setObjectName(vo.getTableName());
        dto.setObjectType(DbObjectType.TABLE.name());
        dto.setRemark(vo.getComment());
        dto.setColumns(getColumnList(vo));
        dto.setIndexes(getIndexList(vo));
        dto.setPks(getPkList(vo));
        dto.setFks(getFkList(vo));
//        dto.setPartitions();
        return dto;
    }

    public static List<ColumnDTO> getColumnList(TableInfoVO vo) {
        if (Objects.isNull(vo) || Objects.isNull(vo.getColumns())) {
            return null;
        }
        List<ColumnDTO> list = new ArrayList<>();
        for (ColumnInfoVO col : vo.getColumns()) {
            ColumnDTO column = new ColumnDTO();
            column.setColumnName(col.getColumnName());
            column.setDataType(col.getDataType());
            column.setDefaultValue(col.getDefaultValue());
            column.setRemark(col.getComment());
            column.setIsNullable(col.getNullable());
            column.setColumnOrdinal(col.getOrdinal());
            list.add(column);
        }
        return list;
    }

    public static List<IndexDTO> getIndexList(TableInfoVO vo) {
        if (Objects.isNull(vo) || Objects.isNull(vo.getIndexes())) {
            return null;
        }
        List<IndexDTO> list = new ArrayList<>();
        for (IndexInfoVO idx : vo.getIndexes()) {
            IndexDTO idxDTO = new IndexDTO();
            idxDTO.setCatalogName(vo.getCatalog());
            idxDTO.setSchemaName(vo.getSchemaName());
            idxDTO.setTableName(vo.getTableName());
            idxDTO.setObjectName(idx.getIndexName());
            idxDTO.setObjectType(DbObjectType.INDEX.name());
            idxDTO.setIndexType(idx.getIndexType());
            idxDTO.setIsUniqueness(idx.getUniqueness());
            idxDTO.setColumns(String.join(",", idx.getColumns()));
        }
        return list;
    }

    public static List<ObjectDTO> getPkList(TableInfoVO vo) {
        if (Objects.isNull(vo) || Objects.isNull(vo.getIndexes())) {
            return null;
        }
        List<ObjectDTO> list = new ArrayList<>();
        for (IndexInfoVO idx : vo.getIndexes()) {
            if (idx.getPk()) {
                ObjectDTO objDTO = new ObjectDTO();
                objDTO.setCatalogName(vo.getCatalog());
                objDTO.setSchemaName(vo.getSchemaName());
                objDTO.setObjectName(idx.getIndexName());
                objDTO.setObjectType(DbObjectType.PK.name());
                list.add(objDTO);
            }
        }
        return list;
    }

    public static List<ObjectDTO> getFkList(TableInfoVO vo) {
        if (Objects.isNull(vo) || Objects.isNull(vo.getIndexes())) {
            return null;
        }
        List<ObjectDTO> list = new ArrayList<>();
        for (IndexInfoVO idx : vo.getIndexes()) {
            if (idx.getFk()) {
                ObjectDTO objDTO = new ObjectDTO();
                objDTO.setCatalogName(vo.getCatalog());
                objDTO.setSchemaName(vo.getSchemaName());
                objDTO.setObjectName(idx.getIndexName());
                objDTO.setObjectType(DbObjectType.FK.name());
                list.add(objDTO);
            }
        }
        return list;
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
                colVO.setDataType(colDTO.getType().formatString());
                colVO.setDefaultValue(colDTO.getDefaultValue());
                colVO.setComment(colDTO.getRemark());
                colVO.setNullable(colDTO.getIsNullable());
                columns.add(colVO);
            }
        }
        return columns;
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
