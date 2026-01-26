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

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.enums.FileEncoding;
import com.basedt.dms.common.enums.FileType;
import com.basedt.dms.common.enums.TaskStatus;
import com.basedt.dms.common.enums.TaskType;
import com.basedt.dms.common.utils.MinioUtil;
import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.log.dto.LogDataTaskDTO;
import com.basedt.dms.service.workspace.DmsDataTaskService;
import com.basedt.dms.service.workspace.dto.DmsDataTaskDTO;
import com.basedt.dms.service.workspace.param.DmsDataTaskParam;
import com.basedt.dms.service.workspace.vo.DmsImportTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static com.basedt.dms.common.enums.FileEncoding.UTF8;

@Slf4j
@RestController
@RequestMapping(path = "/api/workspace/task")
@Tag(name = "TASK")
public class DataTaskController {

    private final DmsDataTaskService dmsDataTaskService;

    private final MinioUtil minioUtil;

    @Value("${minio.bucketName}")
    private String bucketName;

    public DataTaskController(DmsDataTaskService dmsDataTaskService, MinioUtil minioUtil) {
        this.dmsDataTaskService = dmsDataTaskService;
        this.minioUtil = minioUtil;
    }

    @GetMapping
    @AuditLogging
    @Operation(summary = "list data tasks", description = "list data tasks")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<PageDTO<DmsDataTaskDTO>> listByPage(DmsDataTaskParam param) {
        PageDTO<DmsDataTaskDTO> page = this.dmsDataTaskService.listByPage(param);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/export")
    @AuditLogging
    @Operation(summary = "new data export task", description = "new data export task")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> newExportTask(@Validated @RequestBody final DmsDataTaskDTO dataTaskDTO) throws SQLException {
        if (Objects.isNull(dataTaskDTO.getFileEncoding())) {
            dataTaskDTO.setFileEncoding(UTF8.toDict());
        }
        dataTaskDTO.setTaskStatus(TaskStatus.WAIT.toDict());
        dataTaskDTO.setTaskType(TaskType.EXPORT.toDict());
        Long taskId = this.dmsDataTaskService.insert(dataTaskDTO);
        this.dmsDataTaskService.createExportTask(taskId, dataTaskDTO.getSqlScript());
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PostMapping("/import")
    @AuditLogging
    @Operation(summary = "new data import task", description = "new data import task")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> newImportTask(@Validated DmsImportTaskVO importTaskVO, @RequestPart("file") MultipartFile file) throws IOException, SQLException {
        if (file.isEmpty()) {
            return new ResponseEntity<>(ResponseVO.error("Uploaded file cannot be empty"), HttpStatus.BAD_REQUEST);
        }
        DmsDataTaskDTO dmsDataTaskDTO = new DmsDataTaskDTO();
        dmsDataTaskDTO.setWorkspaceId(importTaskVO.getWorkspaceId());
        dmsDataTaskDTO.setDatasourceId(importTaskVO.getDatasourceId());
        dmsDataTaskDTO.setFileName(file.getOriginalFilename());
        dmsDataTaskDTO.setFileType(getFileType(importTaskVO.getFileType()));
        dmsDataTaskDTO.setFileEncoding(getFileEncoding(importTaskVO.getFileEncoding()));
        dmsDataTaskDTO.setFileSize(file.getSize());
        dmsDataTaskDTO.setTaskStatus(TaskStatus.WAIT.toDict());
        dmsDataTaskDTO.setTaskType(TaskType.IMPORT.toDict());
        if (dmsDataTaskDTO.getFileSize() > 200 * 1024 * 1024) {
            return new ResponseEntity<>(ResponseVO.error("Uploaded file size cannot exceed 200MB"), HttpStatus.BAD_REQUEST);
        }
        if (!("csv".equalsIgnoreCase(dmsDataTaskDTO.getFileType().getValue())
                || "xls".equalsIgnoreCase(dmsDataTaskDTO.getFileType().getValue())
                || "xlsx".equalsIgnoreCase(dmsDataTaskDTO.getFileType().getValue())
        )) {
            return new ResponseEntity<>(ResponseVO.error("File types are not supported"), HttpStatus.BAD_REQUEST);
        }
        Long taskId = this.dmsDataTaskService.insert(dmsDataTaskDTO);
        // upload file to minio
        String objectName = StrUtil.concat(true, "import/", String.valueOf(taskId), "/", file.getOriginalFilename());
        minioUtil.uploadObject(this.bucketName, objectName, file.getInputStream());
        this.dmsDataTaskService.createImportTask(taskId, importTaskVO, objectName);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @GetMapping(path = "/download/{taskId}")
    @AuditLogging
    @Operation(summary = "download file", description = "download file")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> download(@PathVariable("taskId") @NotNull Long taskId, HttpServletResponse response) throws IOException {
        DmsDataTaskDTO dmsDataTaskDTO = this.dmsDataTaskService.selectOne(taskId);
        String filePath = dmsDataTaskDTO.getFileUrl();
        if (dmsDataTaskDTO.getTaskType().getValue().equals(TaskType.EXPORT.getValue())) {
            String fileName = URLEncoder.encode(StrUtil.concat(true, dmsDataTaskDTO.getFileName(), ".zip"), StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName.replace("+", "%20"));
        } else {
            String fileName = URLEncoder.encode(dmsDataTaskDTO.getFileName(), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName.replace("+", "%20"));
        }
        try (InputStream inputStream = minioUtil.downloadObject(minioUtil.getBucketName(filePath), minioUtil.getObjectName(filePath))) {
            OutputStream outputStream = response.getOutputStream();
            FileCopyUtils.copy(inputStream, outputStream);
            outputStream.flush();
        }
        return ResponseEntity.ok(ResponseVO.success());
    }

    @GetMapping(path = "/log/{taskId}")
    @AuditLogging
    @Operation(summary = "view download file log", description = "view download file log")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<List<LogDataTaskDTO>>> viewLog(@PathVariable("taskId") @NotNull Long taskId) throws IOException {
        List<LogDataTaskDTO> logList = this.dmsDataTaskService.getLogDetail(taskId);
        return ResponseEntity.ok(ResponseVO.success(logList));
    }

    private DictVO getFileType(String fileType) {
        if (StrUtil.isEmpty(fileType)) {
            return null;
        }
        try {
            FileType type = FileType.valueOf(fileType.toUpperCase());
            return type.toDict();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private DictVO getFileEncoding(String fileEncoding) {
        if (StrUtil.isEmpty(fileEncoding)) {
            return null;
        }
        try {
            FileEncoding encoding = FileEncoding.valueOf(fileEncoding.replace("-", "").toUpperCase());
            return encoding.toDict();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
