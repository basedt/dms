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
package com.basedt.dms.service.security.enums;

import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.service.security.annotation.PrivilegeDesc;
import lombok.Getter;

import static com.basedt.dms.service.security.enums.ActionCode.*;
import static com.basedt.dms.service.security.enums.BlockCode.*;
import static com.basedt.dms.service.security.enums.ModuleCode.SYS;
import static com.basedt.dms.service.security.enums.ModuleCode.WORKSPACE;
import static com.basedt.dms.service.security.enums.PageCode.*;
import static com.basedt.dms.service.security.utils.SecurityUtil.getPrivilegeCode;

@Getter
public enum DmsPrivileges {

    ROOT(Constants.ROOT_PRIVILEGE_CODE),
    /**
     * module privileges
     */
    @PrivilegeDesc(module = SYS, page = PageCode.DEFAULT, block = BlockCode.DEFAULT, action = SHOW, parent = ROOT)
    SYS_SHOW(getPrivilegeCode(SYS, PageCode.DEFAULT, BlockCode.DEFAULT, SHOW)),
    @PrivilegeDesc(module = WORKSPACE, page = PageCode.DEFAULT, block = BlockCode.DEFAULT, action = SHOW, parent = ROOT)
    WORKSPACE_SHOW(getPrivilegeCode(WORKSPACE, PageCode.DEFAULT, BlockCode.DEFAULT, SHOW)),

    /**
     * page privileges
     */
    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = BlockCode.DEFAULT, action = SHOW, parent = SYS_SHOW)
    SYS_DICT_SHOW(getPrivilegeCode(SYS, SYS_DICT, BlockCode.DEFAULT, SHOW)),
    @PrivilegeDesc(module = SYS, page = SYS_USER, block = BlockCode.DEFAULT, action = SHOW, parent = SYS_SHOW)
    SYS_USER_SHOW(getPrivilegeCode(SYS, SYS_USER, BlockCode.DEFAULT, SHOW)),
    @PrivilegeDesc(module = SYS, page = SYS_ROLE, block = BlockCode.DEFAULT, action = SHOW, parent = SYS_SHOW)
    SYS_ROLE_SHOW(getPrivilegeCode(SYS, SYS_ROLE, BlockCode.DEFAULT, SHOW)),
    @PrivilegeDesc(module = SYS, page = SYS_SETTING, block = BlockCode.DEFAULT, action = SHOW, parent = SYS_SHOW)
    SYS_SETTING_SHOW(getPrivilegeCode(SYS, SYS_SETTING, BlockCode.DEFAULT, SHOW)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_OVERVIEW, block = BlockCode.DEFAULT, action = SHOW, parent = WORKSPACE_SHOW)
    WORKSPACE_OVERVIEW_SHOW(getPrivilegeCode(WORKSPACE, WORKSPACE_OVERVIEW, BlockCode.DEFAULT, SHOW)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_SPACE, block = BlockCode.DEFAULT, action = SHOW, parent = WORKSPACE_SHOW)
    WORKSPACE_SPACE_SHOW(getPrivilegeCode(WORKSPACE, WORKSPACE_SPACE, BlockCode.DEFAULT, SHOW)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_DATASOURCE, block = BlockCode.DEFAULT, action = SHOW, parent = WORKSPACE_SHOW)
    WORKSPACE_DATASOURCE_SHOW(getPrivilegeCode(WORKSPACE, WORKSPACE_DATASOURCE, BlockCode.DEFAULT, SHOW)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_HISTORY, block = BlockCode.DEFAULT, action = SHOW, parent = WORKSPACE_SHOW)
    WORKSPACE_HISTORY_SHOW(getPrivilegeCode(WORKSPACE, WORKSPACE_HISTORY, BlockCode.DEFAULT, SHOW)),
    /**
     * action privileges
     */

    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_TYPE, action = SHOW, parent = SYS_DICT_SHOW)
    SYS_DICT_TYPE_SHOW(getPrivilegeCode(SYS, SYS_DICT, DICT_TYPE, SHOW)),
    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_TYPE, action = ADD, parent = SYS_DICT_SHOW)
    SYS_DICT_TYPE_ADD(getPrivilegeCode(SYS, SYS_DICT, DICT_TYPE, ADD)),
    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_TYPE, action = EDIT, parent = SYS_DICT_SHOW)
    SYS_DICT_TYPE_EDIT(getPrivilegeCode(SYS, SYS_DICT, DICT_TYPE, EDIT)),
    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_TYPE, action = DELETE, parent = SYS_DICT_SHOW)
    SYS_DICT_TYPE_DELETE(getPrivilegeCode(SYS, SYS_DICT, DICT_TYPE, DELETE)),
    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_TYPE, action = DETAIL, parent = SYS_DICT_SHOW)
    SYS_DICT_TYPE_DETAIL(getPrivilegeCode(SYS, SYS_DICT, DICT_TYPE, DETAIL)),

    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_DATA, action = SHOW, parent = SYS_DICT_SHOW)
    SYS_DICT_DATA_SHOW(getPrivilegeCode(SYS, SYS_DICT, DICT_DATA, SHOW)),
    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_DATA, action = ADD, parent = SYS_DICT_SHOW)
    SYS_DICT_DATA_ADD(getPrivilegeCode(SYS, SYS_DICT, DICT_DATA, ADD)),
    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_DATA, action = EDIT, parent = SYS_DICT_SHOW)
    SYS_DICT_DATA_EDIT(getPrivilegeCode(SYS, SYS_DICT, DICT_DATA, EDIT)),
    @PrivilegeDesc(module = SYS, page = SYS_DICT, block = DICT_DATA, action = DELETE, parent = SYS_DICT_SHOW)
    SYS_DICT_DATA_DELETE(getPrivilegeCode(SYS, SYS_DICT, DICT_DATA, DELETE)),

    @PrivilegeDesc(module = SYS, page = SYS_ROLE, block = ROLE_LIST, action = SHOW, parent = SYS_ROLE_SHOW)
    SYS_SYS_ROLE_SHOW(getPrivilegeCode(SYS, SYS_ROLE, ROLE_LIST, SHOW)),
    @PrivilegeDesc(module = SYS, page = SYS_ROLE, block = ROLE_LIST, action = ADD, parent = SYS_ROLE_SHOW)
    SYS_SYS_ROLE_ADD(getPrivilegeCode(SYS, SYS_ROLE, ROLE_LIST, ADD)),
    @PrivilegeDesc(module = SYS, page = SYS_ROLE, block = ROLE_LIST, action = EDIT, parent = SYS_ROLE_SHOW)
    SYS_SYS_ROLE_EDIT(getPrivilegeCode(SYS, SYS_ROLE, ROLE_LIST, EDIT)),
    @PrivilegeDesc(module = SYS, page = SYS_ROLE, block = ROLE_LIST, action = DELETE, parent = SYS_ROLE_SHOW)
    SYS_SYS_ROLE_DELETE(getPrivilegeCode(SYS, SYS_ROLE, ROLE_LIST, DELETE)),
    @PrivilegeDesc(module = SYS, page = SYS_ROLE, block = ROLE_LIST, action = GRANT, parent = SYS_ROLE_SHOW)
    SYS_SYS_ROLE_GRANT(getPrivilegeCode(SYS, SYS_ROLE, ROLE_LIST, GRANT)),

    @PrivilegeDesc(module = SYS, page = SYS_USER, block = USER_LIST, action = SHOW, parent = SYS_USER_SHOW)
    SYS_SYS_USER_SHOW(getPrivilegeCode(SYS, SYS_USER, USER_LIST, SHOW)),
    @PrivilegeDesc(module = SYS, page = SYS_USER, block = USER_LIST, action = ADD, parent = SYS_USER_SHOW)
    SYS_SYS_USER_ADD(getPrivilegeCode(SYS, SYS_USER, USER_LIST, ADD)),
    @PrivilegeDesc(module = SYS, page = SYS_USER, block = USER_LIST, action = EDIT, parent = SYS_USER_SHOW)
    SYS_SYS_USER_EDIT(getPrivilegeCode(SYS, SYS_USER, USER_LIST, EDIT)),
    @PrivilegeDesc(module = SYS, page = SYS_USER, block = USER_LIST, action = DELETE, parent = SYS_USER_SHOW)
    SYS_SYS_USER_DELETE(getPrivilegeCode(SYS, SYS_USER, USER_LIST, DELETE)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_SPACE, block = WORKSPACE_LIST, action = SHOW, parent = WORKSPACE_SPACE_SHOW)
    WORKSPACE_SPACE_LIST_SHOW(getPrivilegeCode(WORKSPACE, WORKSPACE_SPACE, WORKSPACE_LIST, SHOW)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_SPACE, block = WORKSPACE_LIST, action = ADD, parent = WORKSPACE_SPACE_SHOW)
    WORKSPACE_SPACE_LIST_ADD(getPrivilegeCode(WORKSPACE, WORKSPACE_SPACE, WORKSPACE_LIST, ADD)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_SPACE, block = WORKSPACE_LIST, action = EDIT, parent = WORKSPACE_SPACE_SHOW)
    WORKSPACE_SPACE_LIST_EDIT(getPrivilegeCode(WORKSPACE, WORKSPACE_SPACE, WORKSPACE_LIST, EDIT)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_SPACE, block = WORKSPACE_LIST, action = DELETE, parent = WORKSPACE_SPACE_SHOW)
    WORKSPACE_SPACE_LIST_DELETE(getPrivilegeCode(WORKSPACE, WORKSPACE_SPACE, WORKSPACE_LIST, DELETE)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_DATASOURCE, block = DATASOURCE_LIST, action = SHOW, parent = WORKSPACE_DATASOURCE_SHOW)
    WORKSPACE_WS_DATASOURCE_SHOW(getPrivilegeCode(WORKSPACE, WORKSPACE_DATASOURCE, DATASOURCE_LIST, SHOW)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_DATASOURCE, block = DATASOURCE_LIST, action = ADD, parent = WORKSPACE_DATASOURCE_SHOW)
    WORKSPACE_WS_DATASOURCE_ADD(getPrivilegeCode(WORKSPACE, WORKSPACE_DATASOURCE, DATASOURCE_LIST, ADD)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_DATASOURCE, block = DATASOURCE_LIST, action = EDIT, parent = WORKSPACE_DATASOURCE_SHOW)
    WORKSPACE_WS_DATASOURCE_EDIT(getPrivilegeCode(WORKSPACE, WORKSPACE_DATASOURCE, DATASOURCE_LIST, EDIT)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_DATASOURCE, block = DATASOURCE_LIST, action = DELETE, parent = WORKSPACE_DATASOURCE_SHOW)
    WORKSPACE_WS_DATASOURCE_DELETE(getPrivilegeCode(WORKSPACE, WORKSPACE_DATASOURCE, DATASOURCE_LIST, DELETE)),
    @PrivilegeDesc(module = WORKSPACE, page = WORKSPACE_HISTORY, block = HISTORY_LIST, action = SHOW, parent = WORKSPACE_HISTORY_SHOW)
    WORKSPACE_WS_HISTORY_SHOW(getPrivilegeCode(WORKSPACE, WORKSPACE_HISTORY, HISTORY_LIST, SHOW));

    private final String code;

    DmsPrivileges(String code) {
        this.code = code;
    }
}
