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

import cn.hutool.core.lang.tree.Tree;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.security.DmsSecurityService;
import com.basedt.dms.service.sys.SysPrivilegeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/sys/privilege")
@Tag(name = "PRIVILEGE")
public class PrivilegeController {

    private final SysPrivilegeService sysPrivilegeService;

    private final DmsSecurityService dmsSecurityService;

    public PrivilegeController(SysPrivilegeService sysPrivilegeService,
                               DmsSecurityService dmsSecurityService) {
        this.sysPrivilegeService = sysPrivilegeService;
        this.dmsSecurityService = dmsSecurityService;
    }

    @GetMapping
    @AuditLogging
    @Operation(summary = "list all privileges", description = "list all privileges")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_GRANT)")
    public ResponseEntity<ResponseVO<List<Tree<String>>>> listAllPrivileges() {
        List<Tree<String>> modules = this.sysPrivilegeService.listPrivilegeTree();
        return new ResponseEntity<>(ResponseVO.success(modules), HttpStatus.OK);
    }

    @GetMapping(path = "/{roleId}")
    @AuditLogging
    @Operation(summary = "list privileges by role", description = "list privileges by role")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_GRANT)")
    public ResponseEntity<ResponseVO<List<String>>> listPrivilegeByRole(@PathVariable("roleId") Long roleId) {
        List<String> privileges = this.sysPrivilegeService.listPrivilegeByRole(roleId);
        return new ResponseEntity<>(ResponseVO.success(privileges), HttpStatus.OK);
    }

    @PostMapping(path = "/{roleId}")
    @AuditLogging
    @Operation(summary = "grant privileges to role", description = "grant privileges to role")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_GRANT)")
    public ResponseEntity<ResponseVO<Object>> grantPrivilegeToRole(@PathVariable("roleId") Long roleId, @RequestBody List<String> privileges) {
        this.sysPrivilegeService.grantPrivilegeToRole(roleId, privileges);
        this.dmsSecurityService.clearPrivilegeByRole(roleId);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }
}
