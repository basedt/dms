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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.api.vo.meta.TableEditVO;
import com.basedt.dms.api.vo.meta.TableInfoConvert;
import com.basedt.dms.api.vo.meta.TableInfoVO;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.ResponseCode;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.MetaDataService;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.dto.TypeInfoDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.enums.DmlType;
import com.basedt.dms.plugins.datasource.param.MetaObjectParam;
import com.basedt.dms.plugins.datasource.param.TableInfoParam;
import com.basedt.dms.service.workspace.DmsDataSourceService;
import com.basedt.dms.service.workspace.convert.DataSourceConvert;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;
import com.basedt.dms.service.workspace.param.DmsSqlExecParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.simpleframework.xml.core.Validate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    @GetMapping(path = "/types/{dataSourceId}")
    @Operation(summary = "list types in database", description = "list types in database")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<List<TypeInfoDTO>>> listTypeInfo(@PathVariable("dataSourceId") @NotNull Long dataSourceId) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        List<TypeInfoDTO> types = this.metaDataService.listTypeInfo(DataSourceConvert.toDataSource(dto));
        return new ResponseEntity<>(ResponseVO.success(types), HttpStatus.OK);
    }


    @AuditLogging
    @GetMapping(path = "/table")
    @Operation(summary = "get table info", description = "get table info")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<TableInfoVO>> getTableInfo(@Validate final TableInfoParam param) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(param.getDataSourceId());
        TableDTO table = metaDataService.getTableInfo(DataSourceConvert.toDataSource(dto), param.getCatalog(), param.getSchemaName(), param.getTableName());
        TableInfoVO tableInfo = TableInfoConvert.toTableVO(table);
        return new ResponseEntity<>(ResponseVO.success(tableInfo), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/table/dml")
    @Operation(summary = "generate dml script", description = "generate dml script")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<String>> generateDml(Long dataSourceId, String catalog, String schemaName, String objectName, String dmlType) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        String sql = metaDataService.generateDml(DataSourceConvert.toDataSource(dto), catalog, schemaName, objectName, DmlType.valueOf(dmlType));
        return new ResponseEntity<>(ResponseVO.success(sql), HttpStatus.OK);
    }

    /**
     * todo 判断是普通表还是外部表
     *
     * @param param
     * @return
     * @throws DmsException
     */
    @AuditLogging
    @PutMapping(path = "table/ddl")
    @Operation(summary = "get table info", description = "get table info")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<String>> getTableDDL(@Validate @RequestBody final TableEditVO param) throws DmsException {
        String sqlScript = "";
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(param.getDataSourceId());
        if (Objects.isNull(param.getOriginTable()) && Objects.nonNull(param.getNewTable())) {
            sqlScript = metaDataService.getTableDDL(DataSourceConvert.toDataSource(dto), TableInfoConvert.toTableDTO(param.getNewTable()));
        } else if (Objects.nonNull(param.getOriginTable()) && (Objects.isNull(param.getNewTable()))) {
            //get origin table ddl
            TableDTO originTable = TableInfoConvert.toTableDTO(param.getOriginTable());
            sqlScript = metaDataService.getTableDDL(DataSourceConvert.toDataSource(dto), originTable.getCatalogName(), originTable.getSchemaName(), originTable.getTableName());
        } else if (Objects.nonNull(param.getOriginTable())) {
            //edit table
            TableDTO originTable = TableInfoConvert.toTableDTO(param.getOriginTable());
            TableDTO newTable = TableInfoConvert.toTableDTO(param.getNewTable());
            sqlScript = metaDataService.getTableDDL(DataSourceConvert.toDataSource(dto), originTable, newTable);
        }
        return new ResponseEntity<>(ResponseVO.success(sqlScript), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/obj/table/rename")
    @Operation(summary = "rename table object", description = "rename table object")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> renameTableObject(Long dataSourceId, String catalog, String schemaName, String tableName, String objectName, String objectType, String newName) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        metaDataService.renameTableObject(DataSourceConvert.toDataSource(dto), catalog, schemaName, tableName, objectType, objectName, newName);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/obj/table/drop")
    @Operation(summary = "drop table object", description = "drop table object")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> dropTableObject(Long dataSourceId, String catalog, String schemaName, String tableName, String objectName, String objectType) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        metaDataService.dropTableObject(DataSourceConvert.toDataSource(dto), catalog, schemaName, tableName, objectName, objectType);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/obj/table/ddl")
    @Operation(summary = "get table object ddl", description = "get table object ddl")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<String>> viewTableObjectDdl(Long dataSourceId, String catalog, String schemaName, String tableName, String objectName, String objectType) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        String ddl = metaDataService.generateDDL(DataSourceConvert.toDataSource(dto), catalog, schemaName, tableName, objectName, DbObjectType.valueOf(objectType));
        return new ResponseEntity<>(ResponseVO.success(ddl), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/obj/rename")
    @Operation(summary = "rename database object", description = "rename database object")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> renameDbObject(Long dataSourceId, String catalog, String schemaName, String objectName, String objectType, String newName) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        metaDataService.renameDbObject(DataSourceConvert.toDataSource(dto), catalog, schemaName, objectType, objectName, newName);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/obj/drop")
    @Operation(summary = "drop database object", description = "drop database object")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> dropDbObject(Long dataSourceId, String catalog, String schemaName, String objectName, String objectType) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        metaDataService.dropDbObject(DataSourceConvert.toDataSource(dto), catalog, schemaName, objectName, objectType);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/obj/ddl")
    @Operation(summary = "get object ddl", description = "get object ddl")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<String>> viewObjectDdl(Long dataSourceId, String catalog, String schemaName, String objectName, String objectType) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        String ddl = metaDataService.generateDDL(DataSourceConvert.toDataSource(dto), catalog, schemaName, objectName, DbObjectType.valueOf(objectType));
        return new ResponseEntity<>(ResponseVO.success(ddl), HttpStatus.OK);
    }

    @AuditLogging
    @PostMapping(path = "/ddl/exec")
    @Operation(summary = "get table object ddl", description = "get table object ddl")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> executeDDL(@Validate @RequestBody final DmsSqlExecParam param) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(param.getDataSourceId());
        DataSourcePlugin plugin = metaDataService.getDataSourcePluginInstance(DataSourceConvert.toDataSource(dto));
        List<String> sqlArray = Arrays.stream(param.getScript().split(Constants.SEPARATOR_SEMICOLON)).filter(StrUtil::isNotBlank).toList();
        try {
            if (CollectionUtil.isNotEmpty(sqlArray)) {
                Connection conn = plugin.getDataSource().getConnection();
                for (String sql : sqlArray) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.execute();
                }
            }
        } catch (SQLException e) {
            throw new DmsException(ResponseCode.ERROR_CUSTOM.getValue(), e.getMessage());
        }
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

}
