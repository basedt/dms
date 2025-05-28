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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.ErrorShowType;
import com.basedt.dms.common.enums.ResponseCode;
import com.basedt.dms.common.utils.I18nUtil;
import com.basedt.dms.common.utils.PropertiesUtil;
import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.DataSourcePluginManager;
import com.basedt.dms.plugins.datasource.MetaDataService;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.workspace.DmsDataSourceService;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;
import com.basedt.dms.service.workspace.param.DmsDataSourceParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.*;

import static com.basedt.dms.plugins.datasource.DataSourcePlugin.JDBC;

@RestController
@RequestMapping(path = "/api/workspace/ds")
@Tag(name = "DATASOURCE")
public class DataSourceController {

    private final DmsDataSourceService dmsDataSourceService;

    public DataSourceController(DmsDataSourceService dmsDataSourceService) {
        this.dmsDataSourceService = dmsDataSourceService;
    }

    @GetMapping(path = "/{id}")
    @AuditLogging
    @Operation(summary = "get datasource info by id", description = "get datasource info by id")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_DATASOURCE_SHOW)")
    public ResponseEntity<ResponseVO<DmsDataSourceDTO>> getDataSource(@PathVariable("id") @NotNull Long id) {
        DmsDataSourceDTO dataSourceDTO = this.dmsDataSourceService.selectOne(id);
        return new ResponseEntity<>(ResponseVO.success(dataSourceDTO), HttpStatus.OK);
    }

    @GetMapping
    @AuditLogging
    @Operation(summary = "query datasource info", description = "query datasource info in page")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_DATASOURCE_SHOW)")
    public ResponseEntity<PageDTO<DmsDataSourceDTO>> listAllDataSources(final DmsDataSourceParam dataSourceParam) {
        PageDTO<DmsDataSourceDTO> page = this.dmsDataSourceService.listByPage(dataSourceParam);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PostMapping
    @AuditLogging
    @Operation(summary = "insert datasource", description = "insert a new datasource")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_DATASOURCE_ADD)")
    public ResponseEntity<ResponseVO<Object>> add(@Validated @RequestBody final DmsDataSourceDTO dataSourceDTO) {
        if (StrUtil.isNotEmpty(dataSourceDTO.getPassword())) {
            String password = Base64.encode(dataSourceDTO.getPassword());
            dataSourceDTO.setPassword(password);
        }
        formatJdbcProps(dataSourceDTO);
        this.dmsDataSourceService.insert(dataSourceDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping
    @AuditLogging
    @Operation(summary = "update datasource", description = "update datasource")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_DATASOURCE_EDIT)")
    public ResponseEntity<ResponseVO<Object>> update(@Validated @RequestBody final DmsDataSourceDTO dataSourceDTO) {
        if (StrUtil.isNotEmpty(dataSourceDTO.getPassword()) && dataSourceDTO.getIsPasswordChange()) {
            String password = Base64.encode(dataSourceDTO.getPassword());
            dataSourceDTO.setPassword(password);
        } else {
            dataSourceDTO.setPassword(null);
        }
        formatJdbcProps(dataSourceDTO);
        this.dmsDataSourceService.update(dataSourceDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @DeleteMapping(path = "/{id}")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "delete datasource", description = "delete datasource with id")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_DATASOURCE_DELETE)")
    public ResponseEntity<ResponseVO<Object>> delete(@PathVariable("id") @NotNull Long id) {
        //TODO check if datasource is used
        this.dmsDataSourceService.deleteById(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @PostMapping(path = "/batch")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "batch delete datasource", description = "delete datasource with id list")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_DATASOURCE_DELETE)")
    public ResponseEntity<ResponseVO<Object>> delete(@RequestBody final List<Long> idList) {
        if (CollectionUtil.isEmpty(idList)) {
            return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
        }
        //TODO check if datasource is used
        this.dmsDataSourceService.deleteBatch(idList);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AuditLogging
    @PostMapping(path = "/test")
    @Operation(summary = "test connection", description = "test connection")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_DATASOURCE_SHOW)")
    public ResponseEntity<ResponseVO<Object>> testConnection(@Validated @RequestBody final DmsDataSourceDTO dataSourceDTO) {
        try {
            String datasourceType = dataSourceDTO.getDatasourceType().getValue();
            if (dataSourceDTO.getId() != null && !dataSourceDTO.getIsPasswordChange()) {
                DmsDataSourceDTO ds = this.dmsDataSourceService.selectOne(dataSourceDTO.getId());
                dataSourceDTO.setPassword(Base64.decodeStr(ds.getPassword()));
            }
            Map<String, String> attrs = new HashMap<>();
            if (CollectionUtil.isNotEmpty(dataSourceDTO.getAttrs()) && dataSourceDTO.getAttrs().containsKey(JDBC)) {
                String jdbcAttr = (String) dataSourceDTO.getAttrs().get(JDBC);
                Map<String, Object> map = PropertiesUtil.formatToMap(jdbcAttr, Constants.LINE_FEED, Constants.SEPARATOR_EQUAL);
                map.forEach((k, v) -> {
                    attrs.put(k, (String) v);
                });
            }
            DataSourcePlugin dataSource = DataSourcePluginManager.newInstance(
                    StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, datasourceType).toUpperCase(),
                    dataSourceDTO.getHostName(),
                    dataSourceDTO.getPort(),
                    dataSourceDTO.getDatabaseName(),
                    dataSourceDTO.getUserName(),
                    dataSourceDTO.getPassword(),
                    attrs);
            if (dataSource != null) {
                boolean result = dataSource.testConnection();
                return new ResponseEntity<>(ResponseVO.success(result), HttpStatus.OK);
            } else {
                throw new NullPointerException("datasource is null");
            }
        } catch (SQLException | ClassNotFoundException | NullPointerException e) {
            return new ResponseEntity<>(ResponseVO.error(ResponseCode.ERROR_CUSTOM.getValue(),
                    I18nUtil.get("response.error.datasource.connection") + " : " + e.getMessage(),
                    ErrorShowType.NOTIFICATION),
                    HttpStatus.OK);
        }
    }

    @GetMapping(path = "/list/{workspaceId}")
    @AuditLogging
    @Operation(summary = "list datasources in workspace", description = "list datasources in workspace")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).WORKSPACE_WS_DATASOURCE_SHOW)")
    public ResponseEntity<ResponseVO<List<DictVO>>> listAllDataSources(@PathVariable("workspaceId") @NotNull Long workspaceId) {
        List<DmsDataSourceDTO> list = this.dmsDataSourceService.listByWorkspaceId(workspaceId);
        List<DictVO> dsList = new ArrayList<>();
        if (CollectionUtil.isEmpty(list)) {
            return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
        }
        for (DmsDataSourceDTO ds : list) {
            DictVO vo = new DictVO(String.valueOf(ds.getId()), ds.getDatasourceType().getValue() + Constants.SEPARATOR_DASHED + ds.getDatasourceName());
            dsList.add(vo);
        }
        return new ResponseEntity<>(ResponseVO.success(dsList), HttpStatus.OK);
    }

    private void formatJdbcProps(DmsDataSourceDTO dataSourceDTO) {
        Map<String, Object> attrs = dataSourceDTO.getAttrs();
        if (CollectionUtil.isNotEmpty(attrs) && attrs.containsKey(JDBC)) {
            String jdbcProps = (String) attrs.get(JDBC);
            Properties props = PropertiesUtil.format(jdbcProps, Constants.LINE_FEED, Constants.SEPARATOR_EQUAL);
            attrs.put(JDBC, PropertiesUtil.toFormatStr(props, Constants.LINE_FEED, Constants.SEPARATOR_EQUAL));
        }
    }
}
