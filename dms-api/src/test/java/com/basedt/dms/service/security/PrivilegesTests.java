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
package com.basedt.dms.service.security;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.DmsApplication;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.service.security.annotation.PrivilegeDesc;
import com.basedt.dms.service.security.enums.*;
import com.basedt.dms.service.security.utils.SecurityUtil;
import com.basedt.dms.service.sys.SysPrivilegeService;
import com.basedt.dms.service.sys.dto.SysPrivilegeDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@SpringBootTest(classes = DmsApplication.class)
public class PrivilegesTests {

    @Autowired
    private SysPrivilegeService sysPrivilegeService;

    /**
     * generate privilege codes to sql script
     */
    @Test
    public void initPrivilegesTest() throws FileNotFoundException {
        log.info("begin generate privilege codes ...");
        Class<DmsPrivileges> clazz = DmsPrivileges.class;
        Set<SysPrivilegeDTO> privileges = new HashSet<>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(PrivilegeDesc.class)) {
                PrivilegeDesc desc = field.getAnnotation(PrivilegeDesc.class);
                SysPrivilegeDTO privilege = toPrivilege(desc.module(), desc.page(), desc.block(), desc.action(), desc.parent());
                privilege.setCreator("sys");
                privilege.setEditor("sys");
                privileges.add(privilege);
            }
        }

        File file = ResourceUtils.getFile("classpath:privilege.txt");
        log.info("write privileges to file {} with sql", file.getAbsolutePath());
        FileUtil.writeUtf8String("------------- sql -------------\n", file);
        FileUtil.appendUtf8String("truncate table sys_privilege;\n", file);
        privileges.forEach(privilege -> {
            String sql = "insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) " +
                    "values ('" + privilege.getPrivilegeCode() + "','"
                    + privilege.getPrivilegeName() + "','"
                    + privilege.getParentCode() + "','"
                    + privilege.getLevel() + "','"
                    + privilege.getCreator() + "','"
                    + privilege.getEditor() + "');\n";
            FileUtil.appendUtf8String(sql, file);
        });
        log.info("write privileges to file {} with json ", file);
        FileUtil.appendUtf8String("------------- json -------------\n", file);
        FileUtil.appendUtf8String("roleSuperAdmin: '" + Constants.ROLE_SUPER_ADMIN + "',\n", file);
        privileges.forEach(privilege -> {
            String code = StrUtil.toCamelCase(privilege.getPrivilegeName().replace("dms.p.", "").replaceAll("\\.", "_"));
            String value = privilege.getPrivilegeCode();
            FileUtil.appendUtf8String(code + ":'" + value + "',\n", file);
        });
        log.info("insert into database ");
        sysPrivilegeService.truncate();
        privileges.forEach(privilege -> {
            sysPrivilegeService.insert(privilege);
        });
    }

    private SysPrivilegeDTO toPrivilege(ModuleCode module, PageCode page, BlockCode block, ActionCode action, DmsPrivileges parent) {
        SysPrivilegeDTO privilege = new SysPrivilegeDTO();
        privilege.setPrivilegeCode(SecurityUtil.getPrivilegeCode(module, page, block, action));
        privilege.setPrivilegeName(SecurityUtil.getPrivilegeName(module, page, block, action));
        privilege.setParentCode(parent.getCode());
        int level;
        if (PageCode.DEFAULT.equals(page)) {
            level = 1;
        } else if (BlockCode.DEFAULT.equals(block)) {
            level = 2;
        } else {
            level = 3;
        }
        privilege.setLevel(level);
        return privilege;
    }
}
