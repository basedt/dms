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
package com.basedt.dms.service.sys;

import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.sys.dto.SysRoleDTO;
import com.basedt.dms.service.sys.dto.SysUserDTO;
import com.basedt.dms.service.sys.param.SysUserParam;

import java.util.List;

public interface SysUserService {

    void insert(SysUserDTO userDTO);

    void update(SysUserDTO userDTO);

    void deleteById(Long id);

    void deleteBatch(List<Long> idList);

    SysUserDTO selectOne(Long id);

    SysUserDTO selectByUserName(String userName);

    SysUserDTO selectByEmail(String email);

    PageDTO<SysUserDTO> listByPage(SysUserParam sysUserParam);

    List<SysRoleDTO> listAllPrivilege(String userName);

    List<SysUserDTO> listUserByRole(Long... roleId);

    List<SysUserDTO> listUserById(Long... idList);

    void grantUserToRole(Long roleId, List<Long> userIds);
}
