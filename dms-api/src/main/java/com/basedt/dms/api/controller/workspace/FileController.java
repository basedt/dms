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
import com.basedt.dms.service.workspace.DmsFileService;
import com.basedt.dms.service.workspace.dto.DmsFileDTO;
import com.basedt.dms.service.workspace.vo.DmsFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

import static com.basedt.dms.common.enums.FileStatus.DRAFT;

@RestController
@RequestMapping(path = "/api/workspace/file")
@Tag(name = "FILE")
public class FileController {

    private final DmsFileService dmsFileService;

    public FileController(DmsFileService dmsFileService) {
        this.dmsFileService = dmsFileService;
    }


    @GetMapping(path = "/{id}")
    @AuditLogging
    @Operation(summary = "get file info by id", description = "get file info by id")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<DmsFileDTO>> getFile(@PathVariable("id") @NotNull Long fileId) {
        DmsFileDTO file = this.dmsFileService.selectOne(fileId);
        return new ResponseEntity<>(ResponseVO.success(file), HttpStatus.OK);
    }

    @GetMapping
    @AuditLogging
    @Operation(summary = "get latest file info", description = "get the latest version file information")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<DmsFileDTO>> getLatestFile(@NotNull Long workspaceId, @NotNull Long catalogId, @NotBlank String fileName) {
        DmsFileDTO file = this.dmsFileService.selectLastVersion(workspaceId, catalogId, fileName);
        return new ResponseEntity<>(ResponseVO.success(file), HttpStatus.OK);
    }


    @GetMapping(path = "/list")
    @AuditLogging
    @Operation(summary = "list file tree", description = "list file tree")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<List<Tree<String>>>> listFileTree(@NotNull Long workspaceId, @NotNull Long datasourceId) {
        List<Tree<String>> fileTree = this.dmsFileService.listFileTree(workspaceId, datasourceId);
        return new ResponseEntity<>(ResponseVO.success(fileTree), HttpStatus.OK);
    }

    @PostMapping
    @AuditLogging
    @Operation(summary = "save file", description = "create a new script file or update file")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> add(@Validated @RequestBody final DmsFileDTO fileDTO) throws DmsException {
        if (Objects.isNull(fileDTO.getFileStatus())) {
            fileDTO.setFileStatus(DRAFT.toDict());
        }
        this.dmsFileService.save(fileDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping(path = "/rename")
    @AuditLogging
    @Operation(summary = "rename file", description = "rename file")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> renameFile(@Validated @RequestBody final DmsFileVO file) throws DmsException {
        this.dmsFileService.renameFile(file.getId(), file.getNewFileName());
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping(path = "/move")
    @AuditLogging
    @Operation(summary = "move file catalog", description = "move file catalog")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> moveFileCatalog(@Validated @RequestBody final DmsFileVO file) throws DmsException {
        this.dmsFileService.moveCatalog(file.getId(), file.getNewFileCatalog());
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping(path = "/{id}")
    @AuditLogging
    @Operation(summary = "publish file ", description = "publish file")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> publish(@PathVariable("id") @NotNull Long id) {
        this.dmsFileService.publish(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @DeleteMapping(path = "/{id}")
    @AuditLogging
    @Operation(summary = "delete file", description = "delete file and file history version")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> delete(@PathVariable("id") @NotNull Long id) {
        this.dmsFileService.deleteById(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

}
