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
package com.basedt.dms.api.init;

import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.Bool;
import com.basedt.dms.common.enums.RegisterChannel;
import com.basedt.dms.common.enums.UserStatus;
import com.basedt.dms.service.llm.DmsChatClient;
import com.basedt.dms.service.sys.SysConfigService;
import com.basedt.dms.service.sys.SysDictTypeService;
import com.basedt.dms.service.sys.SysRoleService;
import com.basedt.dms.service.sys.SysUserService;
import com.basedt.dms.service.sys.cache.DictCache;
import com.basedt.dms.service.sys.dto.SysDictTypeDTO;
import com.basedt.dms.service.sys.dto.SysRoleDTO;
import com.basedt.dms.service.sys.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.basedt.dms.service.sys.cache.CaffeineCacheConfig.UnBoundedCaches.CACHE_DICT;


@Order(2)
@Slf4j
@Component
public class DmsApplicationRunner implements ApplicationRunner {

    private final SysConfigService sysConfigService;

    private final PasswordEncoder passwordEncoder;

    private final SysUserService sysUserService;

    private final SysRoleService sysRoleService;

    private final SysDictTypeService sysDictTypeService;

    public DmsApplicationRunner(SysConfigService sysConfigService,
                                PasswordEncoder passwordEncoder,
                                SysUserService sysUserService,
                                SysRoleService sysRoleService,
                                SysDictTypeService sysDictTypeService
    ) {
        this.sysConfigService = sysConfigService;
        this.passwordEncoder = passwordEncoder;
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
        this.sysDictTypeService = sysDictTypeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initialize system dictionary cache begin...");
        initDictCache();
        log.info("Initialize system dictionary cache finish.");

        log.info("Initialize system data begin ...");
        initSuperAdmin();
        log.info("Initialize system data finish.");
        DmsChatClient.newInstance();
    }

    private void initSuperAdmin() {
        String isAdminInit = sysConfigService.selectValueByKey(Constants.CFG_ADMIN_INIT);
        if (Bool.YES.getValue().equals(isAdminInit)) {
            log.info("init super admin account ...");
            SysUserDTO userDTO = new SysUserDTO();
            userDTO.setUserName("admin");
            userDTO.setNickName("super admin");
            userDTO.setEmail("admin@dms.com");
            userDTO.setPassword(passwordEncoder.encode("123456"));
            userDTO.setUserStatus(UserStatus.NORMAL.toDict());
            userDTO.setRegisterChannel(RegisterChannel.BACKGROUND_IMPORT.toDict());
            userDTO.setRegisterIp("localhost");
            userDTO.setRegisterTime(LocalDateTime.now());
            sysUserService.insert(userDTO);
            log.info("grant {} role to {}", Constants.ROLE_SUPER_ADMIN, userDTO.getUserName());
            SysUserDTO admin = sysUserService.selectByUserName(userDTO.getUserName());
            SysRoleDTO superAdminRole = sysRoleService.selectOne(Constants.ROLE_SUPER_ADMIN);
            this.sysRoleService.grantRoleToUser(admin, superAdminRole);
            log.info("reset system config key {} to {}", Constants.CFG_ADMIN_INIT, Bool.NO.getValue());
            this.sysConfigService.update(Constants.CFG_ADMIN_INIT, Bool.NO.getValue());
        }
    }

    private void initDictCache() {
        log.info("initializing cache {}", CACHE_DICT);
        List<SysDictTypeDTO> list = this.sysDictTypeService.listAll();
        DictCache.clearCache();
        DictCache.updateCache(list);
    }
}
