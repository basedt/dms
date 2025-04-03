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
package com.basedt.dms.api.controller.meta;

import cn.hutool.core.lang.tree.Tree;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.plugins.datasource.MetaDataService;
import com.basedt.dms.plugins.datasource.dto.SuggestionDTO;
import com.basedt.dms.plugins.datasource.param.MetaObjectParam;
import com.basedt.dms.service.workspace.DmsDataSourceService;
import com.basedt.dms.service.workspace.convert.DataSourceConvert;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping(path = "/api/meta")
@Tag(name = "META")
public class MetaDataController {

    private final MetaDataService metaDataService;

    private final DmsDataSourceService dmsDataSourceService;

    public MetaDataController(MetaDataService metaDataService, DmsDataSourceService dmsDataSourceService) {
        this.metaDataService = metaDataService;
        this.dmsDataSourceService = dmsDataSourceService;
    }

    @AuditLogging
    @GetMapping(path = "/schema/{dataSourceId}")
    @Operation(summary = "list catalog and schemas", description = "list catalog and schemas")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<List<Tree<String>>>> listCatalogTree(@PathVariable("dataSourceId") @NotNull Long dataSourceId) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        List<Tree<String>> treeList = metaDataService.listSchemasTree(DataSourceConvert.toDataSource(dto));
        return new ResponseEntity<>(ResponseVO.success(treeList), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/schema/child")
    @Operation(summary = "list child node info", description = "list child node info")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<List<Tree<String>>>> listChildNode(@Validated final MetaObjectParam param) throws DmsException, SQLException {
        String type = param.getType();
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(param.getDataSourceId());
        List<Tree<String>> treeList = metaDataService.listChildNode(DataSourceConvert.toDataSource(dto), param.getIdentifier(), param.getKey(), type);
        return new ResponseEntity<>(ResponseVO.success(treeList), HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "get code suggestions", description = "get code suggestions")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<List<SuggestionDTO>>> getSuggestions(@NotNull Long workspaceId, @NotNull Long dataSourceId, @NotNull String keyword, String tableName) {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        List<SuggestionDTO> suggestions = this.metaDataService.listSuggestion(DataSourceConvert.toDataSource(dto), keyword, tableName);
        return new ResponseEntity<>(ResponseVO.success(suggestions), HttpStatus.OK);
    }
}
