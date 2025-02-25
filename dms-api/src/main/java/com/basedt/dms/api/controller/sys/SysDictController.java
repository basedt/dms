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
package com.basedt.dms.api.controller.sys;

import cn.hutool.core.collection.CollectionUtil;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.sys.SysDictService;
import com.basedt.dms.service.sys.SysDictTypeService;
import com.basedt.dms.service.sys.cache.DictCache;
import com.basedt.dms.service.sys.dto.SysDictDTO;
import com.basedt.dms.service.sys.dto.SysDictTypeDTO;
import com.basedt.dms.service.sys.param.SysDictParam;
import com.basedt.dms.service.sys.param.SysDictTypeParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/sys/dict")
@Tag(name = "SYS-DICT")
public class SysDictController {

    @Resource
    private SysDictTypeService sysDictTypeService;

    @Resource
    private SysDictService sysDictService;

    @GetMapping(path = "/type")
    @AuditLogging
    @Operation(summary = "query sys dict type", description = "query sys dict type in page")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_TYPE_SHOW)")
    public ResponseEntity<PageDTO<SysDictTypeDTO>> listDictType(final SysDictTypeParam param) {
        PageDTO<SysDictTypeDTO> page = this.sysDictTypeService.listByPage(param);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PostMapping(path = "/type")
    @AuditLogging
    @Operation(summary = "insert sys dict type", description = "insert sys dict type")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_TYPE_ADD)")
    public ResponseEntity<ResponseVO<Object>> addDictType(@Validated @RequestBody final SysDictTypeDTO sysDictTypeDTO) {
        sysDictTypeService.insert(sysDictTypeDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping(path = "/type")
    @AuditLogging
    @Operation(summary = "update sys dict type", description = "update sys dict type")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_TYPE_EDIT)")
    public ResponseEntity<ResponseVO<Object>> updateDictType(@Validated @RequestBody final SysDictTypeDTO sysDictTypeDTO) {
        sysDictTypeService.update(sysDictTypeDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @DeleteMapping(path = "/type/{id}")
    @AuditLogging
    @Operation(summary = "delete one dict type", description = "delete dict type by id")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_TYPE_DELETE)")
    public ResponseEntity<ResponseVO<Object>> deleteDictType(@PathVariable("id") Long id) {
        sysDictTypeService.deleteById(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PostMapping(path = "/type/batch")
    @AuditLogging
    @Operation(summary = "delete dict type in batch", description = "delete dict type with ids")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_TYPE_DELETE)")
    public ResponseEntity<ResponseVO<Object>> deleteDictType(@RequestBody final List<Long> idList) {
        sysDictTypeService.deleteBatch(idList);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @GetMapping(path = "/data")
    @AuditLogging
    @Operation(summary = "query sys dict", description = "query sys dict in page")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_DATA_SHOW)")
    public ResponseEntity<PageDTO<SysDictDTO>> listDict(final SysDictParam param) {
        PageDTO<SysDictDTO> page = this.sysDictService.listByPage(param);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping(path = "/data/{type}")
    @AuditLogging
    @Operation(summary = "list sys dict by type", description = "list sys dict by type from cache")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_DATA_SHOW)")
    public ResponseEntity<ResponseVO<List<DictVO>>> listDictByType(@PathVariable("type") String type) {
        SysDictTypeDTO typeDTO = DictCache.getValueByKey(type);
        List<DictVO> result = new ArrayList<>();
        if (typeDTO != null && CollectionUtil.isNotEmpty(typeDTO.getSysDictList())) {
            typeDTO.getSysDictList().forEach(item -> {
                result.add(new DictVO(item.getDictCode(), item.getDictValue()));
            });
        }
        return new ResponseEntity<>(ResponseVO.success(result), HttpStatus.OK);
    }

    @PostMapping(path = "/data")
    @AuditLogging
    @Operation(summary = "insert sys dict", description = "insert sys dict")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_DATA_ADD)")
    public ResponseEntity<ResponseVO<Object>> addDictData(@Validated @RequestBody final SysDictDTO sysDictDTO) {
        sysDictService.insert(sysDictDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping(path = "/data")
    @AuditLogging
    @Operation(summary = "update sys dict", description = "update sys dict")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_DATA_EDIT)")
    public ResponseEntity<ResponseVO<Object>> updateDictData(@Validated @RequestBody final SysDictDTO sysDictDTO) {
        sysDictService.update(sysDictDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @DeleteMapping(path = "/data/{id}")
    @AuditLogging
    @Operation(summary = "delete one dict", description = "delete dict by id")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_DATA_DELETE)")
    public ResponseEntity<ResponseVO<Object>> deleteDictData(@PathVariable("id") Long id) {
        sysDictService.deleteById(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PostMapping(path = "/data/batch")
    @AuditLogging
    @Operation(summary = "delete dict in batch", description = "delete dict with ids")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_DICT_DATA_DELETE)")
    public ResponseEntity<ResponseVO<Object>> deleteDictData(@RequestBody final List<Long> idList) {
        sysDictService.deleteBatch(idList);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }
}
