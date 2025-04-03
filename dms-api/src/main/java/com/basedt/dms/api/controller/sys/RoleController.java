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
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.security.DmsSecurityService;
import com.basedt.dms.service.sys.SysRolePrivilegeService;
import com.basedt.dms.service.sys.SysRoleService;
import com.basedt.dms.service.sys.SysUserService;
import com.basedt.dms.service.sys.dto.SysRoleDTO;
import com.basedt.dms.service.sys.param.SysRoleParam;
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
import java.util.List;

@RestController
@RequestMapping(path = "/api/sys/role")
@Tag(name = "ROLE")
public class RoleController {

    private final SysRoleService sysRoleService;

    private final SysUserService sysUserService;

    private final SysRolePrivilegeService sysRolePrivilegeService;

    private final DmsSecurityService dmsSecurityService;

    public RoleController(SysRoleService sysRoleService,
                          SysUserService sysUserService,
                          SysRolePrivilegeService sysRolePrivilegeService,
                          DmsSecurityService dmsSecurityService) {
        this.sysRoleService = sysRoleService;
        this.sysUserService = sysUserService;
        this.sysRolePrivilegeService = sysRolePrivilegeService;
        this.dmsSecurityService = dmsSecurityService;
    }

    @GetMapping
    @AuditLogging
    @Operation(summary = "query role info", description = "query role info in page")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_SHOW)")
    public ResponseEntity<PageDTO<SysRoleDTO>> listByPage(final SysRoleParam roleParam) {
        PageDTO<SysRoleDTO> page = this.sysRoleService.listByPage(roleParam);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PostMapping
    @AuditLogging
    @Operation(summary = "insert role", description = "insert a new role")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_ADD)")
    public ResponseEntity<ResponseVO<Object>> add(@Validated @RequestBody final SysRoleDTO roleDTO) {
        this.sysRoleService.insert(roleDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping
    @AuditLogging
    @Operation(summary = "update role", description = "update role")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_EDIT)")
    public ResponseEntity<ResponseVO<Object>> update(@Validated @RequestBody final SysRoleDTO roleDTO) {
        this.sysRoleService.update(roleDTO);
        this.dmsSecurityService.clearPrivilegeByRole(roleDTO.getId());
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @DeleteMapping(path = "/{id}")
    @AuditLogging
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "delete role", description = "delete role with id")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_DELETE)")
    public ResponseEntity<ResponseVO<Object>> delete(@PathVariable("id") @NotBlank Long id) {
        this.sysRoleService.deleteById(id);
        dmsSecurityService.clearPrivilegeByRole(id);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PostMapping(path = "/batch")
    @AuditLogging
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "batch delete role", description = "delete role with id list")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_DELETE)")
    public ResponseEntity<ResponseVO<Object>> delete(@RequestBody final List<Long> idList) {
        if (CollectionUtil.isEmpty(idList)) {
            return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
        }
        this.sysRoleService.deleteBatch(idList);
        dmsSecurityService.clearPrivilegeByRole(idList.toArray(new Long[0]));
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @GetMapping(path = "/{userName}")
    @AuditLogging
    @Operation(summary = "list roles by user ", description = "list roles by user")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_GRANT)")
    public ResponseEntity<ResponseVO<List<SysRoleDTO>>> listRoleByUser(@NotNull @PathVariable("userName") String userName) {
        List<SysRoleDTO> list = this.sysRoleService.listRoleByUserName(userName);
        return new ResponseEntity<>(ResponseVO.success(list), HttpStatus.OK);
    }

    @GetMapping(path = "/all")
    @AuditLogging
    @Operation(summary = "list all roles ", description = "list all roles ")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_GRANT)")
    public ResponseEntity<ResponseVO<List<SysRoleDTO>>> listAll() {
        List<SysRoleDTO> list = this.sysRoleService.listAll();
        return new ResponseEntity<>(ResponseVO.success(list), HttpStatus.OK);
    }

    @PostMapping(path = "/{roleId}")
    @AuditLogging
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "grant users to role", description = "grant users to role")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_GRANT)")
    public ResponseEntity<ResponseVO<Object>> grantRoleToUser(@PathVariable("roleId") @NotNull Long roleId,
                                                              @RequestBody final List<Long> userIds) {
        this.sysUserService.grantUserToRole(roleId, userIds);
        this.dmsSecurityService.clearPrivilegeByRole(roleId);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }
}
