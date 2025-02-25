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

import cn.hutool.core.collection.CollectionUtil;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.security.utils.SecurityUtil;
import com.basedt.dms.service.workspace.DmsFileCatalogService;
import com.basedt.dms.service.workspace.DmsWorkspaceService;
import com.basedt.dms.service.workspace.dto.DmsFileCatalogDTO;
import com.basedt.dms.service.workspace.dto.DmsWorkspaceDTO;
import com.basedt.dms.service.workspace.param.DmsWorkspaceParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping(path = "/api/workspace")
@Tag(name = "WORKSPACE")
public class WorkspaceController {

    private final DmsWorkspaceService dmsWorkspaceService;

    private final DmsFileCatalogService dmsFileCatalogService;

    public WorkspaceController(DmsWorkspaceService dmsWorkspaceService,
                               DmsFileCatalogService dmsFileCatalogService) {
        this.dmsWorkspaceService = dmsWorkspaceService;
        this.dmsFileCatalogService = dmsFileCatalogService;
    }

    @GetMapping
    @AuditLogging
    @Operation(summary = "query workspace info", description = "query workspace info in page")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SPACE_LIST_SHOW)")
    public ResponseEntity<PageDTO<DmsWorkspaceDTO>> listByPage(final DmsWorkspaceParam workspaceParam) {
        PageDTO<DmsWorkspaceDTO> page = this.dmsWorkspaceService.listByPage(workspaceParam);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PostMapping
    @AuditLogging
    @Operation(summary = "insert workspace", description = "insert a new workspace")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SPACE_LIST_ADD)")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResponseVO<Object>> add(@Validated @RequestBody final DmsWorkspaceDTO workspaceDTO) {
        workspaceDTO.setOwner(SecurityUtil.getCurrentUserName());
        this.dmsWorkspaceService.insert(workspaceDTO);
        DmsWorkspaceDTO workspace = this.dmsWorkspaceService.selectOne(workspaceDTO.getWorkspaceCode());
        //create default catalog in workspace
        DmsFileCatalogDTO catalog = new DmsFileCatalogDTO();
        catalog.setWorkspaceId(workspace.getId());
        catalog.setName(workspace.getWorkspaceCode());
        catalog.setPid(0L);
        this.dmsFileCatalogService.insert(catalog);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping
    @AuditLogging
    @Operation(summary = "update workspace", description = "update workspace")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SPACE_LIST_EDIT)")
    public ResponseEntity<ResponseVO<Object>> update(@Validated @RequestBody final DmsWorkspaceDTO workspaceDTO) {
        this.dmsWorkspaceService.update(workspaceDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @DeleteMapping(path = "/{id}")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "delete workspace", description = "delete workspace with id")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SPACE_LIST_DELETE)")
    public ResponseEntity<ResponseVO<Object>> delete(@PathVariable("id") @NotBlank Long id) {
        //TODO delete cascade objects
        this.dmsWorkspaceService.deleteById(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @PostMapping(path = "/batch")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "batch delete workspace", description = "delete workspace with id list")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SPACE_LIST_DELETE)")
    public ResponseEntity<ResponseVO<Object>> delete(@RequestBody final List<Long> idList) {
        if (CollectionUtil.isEmpty(idList)) {
            return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
        }
        //TODO delete cascade objects
        this.dmsWorkspaceService.deleteBatch(idList);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

}
