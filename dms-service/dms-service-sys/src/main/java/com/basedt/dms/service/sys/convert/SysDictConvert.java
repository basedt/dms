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
package com.basedt.dms.service.sys.convert;

import com.basedt.dms.dao.entity.master.sys.SysDict;
import com.basedt.dms.dao.entity.master.sys.SysDictType;
import com.basedt.dms.service.base.convert.BaseConvert;
import com.basedt.dms.service.sys.dto.SysDictDTO;
import com.basedt.dms.service.sys.dto.SysDictTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {SysDictTypeConvert.class, DictVoConvert.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysDictConvert extends BaseConvert<SysDict, SysDictDTO> {

    SysDictConvert INSTANCE = Mappers.getMapper(SysDictConvert.class);

    @Override
    default SysDictDTO toDto(SysDict entity) {
        if (entity == null) {
            return null;
        }

        SysDictDTO sysDictDTO = new SysDictDTO();

        sysDictDTO.setId(entity.getId());
        sysDictDTO.setCreator(entity.getCreator());
        sysDictDTO.setCreateTime(entity.getCreateTime());
        sysDictDTO.setEditor(entity.getEditor());
        sysDictDTO.setUpdateTime(entity.getUpdateTime());
        SysDictTypeDTO typeDTO = new SysDictTypeDTO();
        if (entity.getSysDictType() != null) {
            typeDTO.setId(entity.getSysDictType().getId());
            typeDTO.setDictTypeCode(entity.getSysDictType().getDictTypeCode());
            typeDTO.setDictTypeName(entity.getSysDictType().getDictTypeName());
        }
        sysDictDTO.setSysDictType(typeDTO);
        sysDictDTO.setDictCode(entity.getDictCode());
        sysDictDTO.setDictValue(entity.getDictValue());
        sysDictDTO.setRemark(entity.getRemark());

        return sysDictDTO;
    }

    @Override
    default SysDict toDo(SysDictDTO dto) {
        if (dto == null) {
            return null;
        }

        SysDict sysDict = new SysDict();
        sysDict.setId(dto.getId());
        sysDict.setCreator(dto.getCreator());
        sysDict.setCreateTime(dto.getCreateTime());
        sysDict.setEditor(dto.getEditor());
        sysDict.setUpdateTime(dto.getUpdateTime());
        sysDict.setDictCode(dto.getDictCode());
        sysDict.setDictValue(dto.getDictValue());
        sysDict.setRemark(dto.getRemark());
        SysDictType type = new SysDictType();
        if (dto.getSysDictType() != null) {
            type.setId(dto.getSysDictType().getId());
            type.setDictTypeCode(dto.getSysDictType().getDictTypeCode());
            type.setDictTypeName(dto.getSysDictType().getDictTypeName());
            sysDict.setDictTypeCode(dto.getSysDictType().getDictTypeCode());
        }
        sysDict.setSysDictType(type);

        return sysDict;
    }

}
