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
import com.basedt.dms.api.vo.meta.TableInfoConvert;
import com.basedt.dms.api.vo.meta.TableInfoVO;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.plugins.datasource.MetaDataService;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.dto.TypeInfoDTO;
import com.basedt.dms.plugins.datasource.enums.DmlType;
import com.basedt.dms.plugins.datasource.param.MetaObjectParam;
import com.basedt.dms.plugins.datasource.param.TableInfoParam;
import com.basedt.dms.service.workspace.DmsDataSourceService;
import com.basedt.dms.service.workspace.convert.DataSourceConvert;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.simpleframework.xml.core.Validate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
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
    @PutMapping(path = "/table")
    @Operation(summary = "create new table", description = "create new table")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> newTable(@Validate @RequestBody final TableInfoVO tableInfo) throws DmsException {
        //TODO implement
        return new ResponseEntity<>(ResponseVO.success(true), HttpStatus.OK);
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
    @GetMapping(path = "/obj/rename")
    @Operation(summary = "rename database object", description = "rename database object")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> renameDbObject(Long dataSourceId, String catalog, String schemaName, String objectName, String objectType, String newName) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        metaDataService.renameDbObject(DataSourceConvert.toDataSource(dto), catalog, schemaName, objectType, objectName, newName);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
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
    @GetMapping(path = "/obj/drop")
    @Operation(summary = "drop database object", description = "drop database object")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> dropDbObject(Long dataSourceId, String catalog, String schemaName, String objectName, String objectType) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        metaDataService.dropDbObject(DataSourceConvert.toDataSource(dto), catalog, schemaName, objectName, objectType);
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
    @GetMapping(path = "/table/dml")
    @Operation(summary = "generate dml script", description = "generate dml script")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<String>> generateDml(Long dataSourceId, String catalog, String schemaName, String objectName, String dmlType) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dataSourceId);
        String sql = metaDataService.generateDml(DataSourceConvert.toDataSource(dto), catalog, schemaName, objectName, DmlType.valueOf(dmlType));
        return new ResponseEntity<>(ResponseVO.success(sql), HttpStatus.OK);
    }

    @AuditLogging
    @GetMapping(path = "/obj/ddl")
    @Operation(summary = "get object ddl", description = "get object ddl")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<String>> viewDdl(Long dataSourceId, String catalog, String schemaName, String objectName, String objectType) throws DmsException {
        //todo
        return new ResponseEntity<>(ResponseVO.success(""), HttpStatus.OK);
    }

    @AuditLogging
    @PutMapping(path = "script/ddl")
    @Operation(summary = "get table info", description = "get table info")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_SHOW)")
    public ResponseEntity<ResponseVO<String>> getTableDdlScript(@Validate @RequestBody final TableInfoParam param) throws DmsException {
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(param.getDataSourceId());
        TableDTO originTable = metaDataService.getTableInfo(DataSourceConvert.toDataSource(dto), param.getCatalog(), param.getSchemaName(), param.getTableName());
        if (Objects.isNull(param.getTableInfo())) {
            // TODO  没有变更信息，返回原始的表结构ddl
        } else if (Objects.isNull(originTable)) {
            // TODO 当前数据库中没有相关表，判定为新建表，生成建表语句
        } else {
            // TODO 判定为修改表，如果对比后发现没有修改任何内容，则返回原始表的DDL语句


//             需要注意顺序，例如先处理表名修改，再处理列的增删，然后是索引和分区
//             1. 改表名
//             2. 改表注释
//             3. 新增列
//             4. 删除列
//             5. 调整列顺序 -- 不支持
//             6. 修改字段名称
//             7. 调整字段类型
//             8. 调整字段空值属性
//             9. 调整字段默认值
//             10. 调整字段注释
//             11. 新建索引
//             12. 删除索引
//             13. 修改索引名称
//             14. 修改索引列或者类型  =》 删除重建
//             15. 分区操作 暂不支持后续再说

        }
        String sqlScript = "";
        return new ResponseEntity<>(ResponseVO.success(sqlScript), HttpStatus.OK);
    }

}
