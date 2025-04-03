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
package com.basedt.dms.api.controller.workspace;

import cn.hutool.core.lang.tree.Tree;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.workspace.DmsFileCatalogService;
import com.basedt.dms.service.workspace.dto.DmsFileCatalogDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/api/workspace/catalog")
@Tag(name = "FILE_CATALOG")
public class FileCatalogController {

    private final DmsFileCatalogService dmsFileCatalogService;

    public FileCatalogController(DmsFileCatalogService dmsFileCatalogService) {
        this.dmsFileCatalogService = dmsFileCatalogService;
    }

    @GetMapping
    @AuditLogging
    @Operation(summary = "list catalog tree", description = "list catalog tree")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<List<Tree<Long>>>> listCatalog(@NotNull Long workspaceId, Long id) {
        List<Tree<Long>> catalogTree = this.dmsFileCatalogService.listCatalogTree(workspaceId, id);
        return new ResponseEntity<>(ResponseVO.success(catalogTree), HttpStatus.OK);
    }

    @PostMapping
    @AuditLogging
    @Operation(summary = "insert file catalog", description = "create a new file catalog")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> add(@Validated @RequestBody final DmsFileCatalogDTO fileCatalogDTO) {
        this.dmsFileCatalogService.insert(fileCatalogDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping
    @AuditLogging
    @Operation(summary = "update file catalog", description = "update file catalog")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> update(@Validated @RequestBody final DmsFileCatalogDTO fileCatalogDTO) {
        this.dmsFileCatalogService.update(fileCatalogDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @DeleteMapping(path = "/{id}")
    @AuditLogging
    @Operation(summary = "delete file catalog", description = "delete file catalog,throw exception when catalog is not null")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> delete(@PathVariable("id") @NotNull Long id) throws DmsException {
        this.dmsFileCatalogService.deleteById(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }


}
