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
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.basedt.dms.alert.DmsAlert;
import com.basedt.dms.alert.dto.SysMessageDTO;
import com.basedt.dms.api.annotation.AuditLogging;
import com.basedt.dms.api.vo.EmailBindVO;
import com.basedt.dms.api.vo.LoginInfoVO;
import com.basedt.dms.api.vo.PwdChangeVO;
import com.basedt.dms.api.vo.RegisterInfoVO;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.*;
import com.basedt.dms.common.utils.I18nUtil;
import com.basedt.dms.common.utils.RedisUtil;
import com.basedt.dms.common.vo.ResponseVO;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.security.DmsSecurityService;
import com.basedt.dms.service.security.UserDetailsInfo;
import com.basedt.dms.service.security.annotation.AnonymousAccess;
import com.basedt.dms.service.security.utils.CookieUtil;
import com.basedt.dms.service.security.utils.SecurityUtil;
import com.basedt.dms.service.sys.SysRoleService;
import com.basedt.dms.service.sys.SysUserService;
import com.basedt.dms.service.sys.dto.SysRoleDTO;
import com.basedt.dms.service.sys.dto.SysUserDTO;
import com.basedt.dms.service.sys.param.SysUserParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(path = "/api")
@Tag(name = "USER")
public class UserController {

    private final RedisUtil redisUtil;

    private final SysUserService sysUserService;

    private final PasswordEncoder passwordEncoder;

    private final DmsSecurityService dmsSecurityService;

    private final SysRoleService sysRoleService;

    private final DmsAlert alert;

    @Value("${dms.session.timeout.short}")
    private int shortSessionTimeout;

    @Value("${dms.session.timeout.long}")
    private int longSessionTimeout;

    @Value("${spring.application.name}")
    private String appName;

    public UserController(RedisUtil redisUtil,
                          SysUserService sysUserService,
                          PasswordEncoder passwordEncoder,
                          DmsSecurityService dmsSecurityService,
                          SysRoleService sysRoleService,
                          DmsAlert alert) {
        this.redisUtil = redisUtil;
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.dmsSecurityService = dmsSecurityService;
        this.sysRoleService = sysRoleService;
        this.alert = alert;
    }

    @AnonymousAccess
    @PostMapping(path = "/user/login")
    public ResponseEntity<ResponseVO<SysUserDTO>> login(@Validated @RequestBody LoginInfoVO loginInfo, HttpServletRequest request, HttpServletResponse response) {
        String authCode = this.redisUtil.get(loginInfo.getUuid());
        redisUtil.delKeys(loginInfo.getUuid());
        if (StrUtil.isNotEmpty(authCode) && authCode.equalsIgnoreCase(loginInfo.getAuthCode())) {
            try {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginInfo.getUserName(), loginInfo.getPassword());
                Authentication authentication = dmsSecurityService.authentication(authenticationToken);
                dmsSecurityService.disableSessionByUserName(loginInfo.getUserName());
                final UserDetailsInfo userDetails = (UserDetailsInfo) authentication.getPrincipal();
                SecurityContextHolder.getContext().setAuthentication(authentication);
                userDetails.setLoginIp(request.getRemoteAddr());
                userDetails.setLoginTime(LocalDateTime.now());
                HttpSession session = request.getSession();
                int timeout = loginInfo.getAutoLogin() ? longSessionTimeout : shortSessionTimeout;
                session.setMaxInactiveInterval(timeout);
                session.setAttribute(Constants.SESSION_USER, userDetails);
                response.addCookie(CookieUtil.createCookie(Constants.SESSION_ID, session.getId(), timeout));
                return new ResponseEntity<>(ResponseVO.success(userDetails.getUser()), HttpStatus.OK);
            } catch (BadCredentialsException e) {
                return new ResponseEntity<>(ResponseVO.error(ResponseCode.ERROR_CUSTOM.getValue(),
                        I18nUtil.get("response.error.login.password"),
                        ErrorShowType.ERROR_MESSAGE),
                        HttpStatus.OK);
            } catch (AccountStatusException e) {
                return new ResponseEntity<>(ResponseVO.error(ResponseCode.ERROR_CUSTOM.getValue(),
                        I18nUtil.get("response.error.login.disable"),
                        ErrorShowType.ERROR_MESSAGE),
                        HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(ResponseVO.error(ResponseCode.ERROR_CUSTOM.getValue(),
                    I18nUtil.get("response.error.authCode"),
                    ErrorShowType.ERROR_MESSAGE),
                    HttpStatus.OK);
        }
    }

    @AnonymousAccess
    @PostMapping(path = "/user/register")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResponseVO<Object>> register(@Validated @RequestBody RegisterInfoVO registerInfo, HttpServletRequest request) {
        String authCode = this.redisUtil.get(registerInfo.getUuid());
        redisUtil.delKeys(registerInfo.getUuid());
        if (StrUtil.isNotEmpty(authCode) && authCode.equalsIgnoreCase(registerInfo.getAuthCode())) {
            if (registerInfo.getPassword().equals(registerInfo.getConfirmPassword())) {

                SysUserDTO userDTO = new SysUserDTO();
                userDTO.setUserName(registerInfo.getUserName());
                userDTO.setEmail(registerInfo.getEmail());
                userDTO.setPassword(passwordEncoder.encode(registerInfo.getPassword()));
                userDTO.setUserStatus(UserStatus.NORMAL.toDict());
                userDTO.setRegisterChannel(RegisterChannel.REGISTER.toDict());
                userDTO.setRegisterIp(request.getRemoteAddr());
                userDTO.setRegisterTime(LocalDateTime.now());
                sysUserService.insert(userDTO);
                grantNormalRoleToUser(userDTO.getUserName());
                sendRegisterMail(userDTO, registerInfo.getPassword());
                return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(ResponseVO.error(ResponseCode.ERROR_CUSTOM.getValue(), I18nUtil.get("response.error.notSamePassword"), ErrorShowType.SILENT), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(ResponseVO.error(ResponseCode.ERROR_CUSTOM.getValue(), I18nUtil.get("response.error.authCode"), ErrorShowType.ERROR_MESSAGE), HttpStatus.OK);
        }
    }

    @AnonymousAccess
    @GetMapping(path = "/user/current")
    public ResponseEntity<ResponseVO<SysUserDTO>> getCurrentUser(boolean profile) {
        UserDetailsInfo userDetailsInfo = SecurityUtil.getCurrentUser();
        if (Objects.nonNull(userDetailsInfo) && Objects.nonNull(userDetailsInfo.getUser())) {
            if (profile) {
                SysUserDTO user = sysUserService.selectByUserName(userDetailsInfo.getUsername());
                return new ResponseEntity<>(ResponseVO.success(user), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(ResponseVO.success(userDetailsInfo.getUser()), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AnonymousAccess
    @GetMapping(path = "/user/logout")
    public ResponseEntity<ResponseVO<Object>> logout(HttpServletResponse response) {
        UserDetailsInfo userDetailsInfo = SecurityUtil.getCurrentUser();
        if (userDetailsInfo != null) {
            this.dmsSecurityService.disableSessionByUserName(userDetailsInfo.getUsername());
        }
        response.addCookie(CookieUtil.invalidCookie(Constants.SESSION_ID));
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @AnonymousAccess
    @GetMapping(path = "/user/validate/userName")
    public ResponseEntity<Boolean> isUserExists(@NotNull String userName) {
        SysUserDTO user = this.sysUserService.selectByUserName(userName);
        return new ResponseEntity<>(user == null, HttpStatus.OK);
    }

    @AnonymousAccess
    @GetMapping(path = "/user/validate/email")
    public ResponseEntity<Boolean> isEmailExists(@NotNull String email) {
        SysUserDTO user = this.sysUserService.selectByEmail(email);
        return new ResponseEntity<>(user == null, HttpStatus.OK);
    }

    @GetMapping(path = "/sys/user")
    @AuditLogging
    @Operation(summary = "list users in page", description = "list users in page")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_USER_SHOW)")
    public ResponseEntity<PageDTO<SysUserDTO>> listByPage(final SysUserParam param) {
        PageDTO<SysUserDTO> page = sysUserService.listByPage(param);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PostMapping(path = "/sys/user")
    @AuditLogging
    @Operation(summary = "add user", description = "add user")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_USER_ADD)")
    public ResponseEntity<ResponseVO<Object>> add(@Validated @RequestBody final SysUserDTO userDTO, HttpServletRequest request) {
        String password = RandomUtil.randomString(10);
        userDTO.setPassword(this.passwordEncoder.encode(password));
        userDTO.setUserStatus(UserStatus.NORMAL.toDict());
        userDTO.setRegisterChannel(RegisterChannel.BACKGROUND_IMPORT.toDict());
        userDTO.setRegisterIp(request.getRemoteAddr());
        userDTO.setRegisterTime(LocalDateTime.now());
        this.sysUserService.insert(userDTO);
        grantNormalRoleToUser(userDTO.getUserName());
        sendRegisterMail(userDTO, password);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PutMapping(path = "/sys/user")
    @AuditLogging
    @Operation(summary = "update user", description = "update user")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_USER_EDIT)")
    public ResponseEntity<ResponseVO<Object>> update(@Validated @RequestBody final SysUserDTO userDTO) {
        this.sysUserService.update(userDTO);
        if (userDTO.getRoles() != null || userDTO.getUserStatus() != null) {
            dmsSecurityService.disableSessionByUserName(userDTO.getUserName());
        }
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @DeleteMapping(path = "/sys/user/{id}")
    @AuditLogging
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "delete a user", description = "disable a user")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_USER_DELETE)")
    public ResponseEntity<ResponseVO<Object>> delete(@PathVariable("id") @NotNull Long id) {
        SysUserDTO userDTO = new SysUserDTO();
        userDTO.setId(id);
        userDTO.setUserStatus(UserStatus.FORBIDDEN.toDict());
        dmsSecurityService.disableSessionByUserId(id);
        this.sysUserService.update(userDTO);
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PostMapping(path = "/sys/user/batch")
    @AuditLogging
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "batch delete user", description = "disable users with id list")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_USER_DELETE)")
    public ResponseEntity<ResponseVO<Object>> deleteBatch(@RequestBody final List<Long> idList) {
        if (CollectionUtil.isNotEmpty(idList)) {
            idList.forEach(id -> {
                SysUserDTO userDTO = new SysUserDTO();
                userDTO.setId(id);
                userDTO.setUserStatus(UserStatus.FORBIDDEN.toDict());
                this.sysUserService.update(userDTO);
            });
            dmsSecurityService.disableSessionByUserId(idList.toArray(new Long[0]));
        }
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PostMapping(path = "/sys/user/enable")
    @AuditLogging
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "enable users", description = "enable users with id list")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_USER_EDIT)")
    public ResponseEntity<ResponseVO<Object>> userEnableBatch(@RequestBody final List<Long> idList) {
        if (CollectionUtil.isNotEmpty(idList)) {
            idList.forEach(id -> {
                SysUserDTO userDTO = new SysUserDTO();
                userDTO.setId(id);
                userDTO.setUserStatus(UserStatus.NORMAL.toDict());
                this.sysUserService.update(userDTO);
            });
        }
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @PostMapping(path = "/sys/user/role/{userId}")
    @AuditLogging
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "grant roles to user", description = "grant roles to user")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_GRANT)")
    public ResponseEntity<ResponseVO<Object>> grantRoleToUser(@PathVariable("userId") @NotNull Long userId,
                                                              @RequestBody final List<Long> roleIds) {
        SysUserDTO userInfo = this.sysUserService.selectOne(userId);
        this.sysRoleService.grantRoleToUser(userId, roleIds);
        this.dmsSecurityService.clearPrivilegeByUser(userInfo.getUserName());
        return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
    }

    @GetMapping(path = "/sys/user/{roleId}")
    @AuditLogging
    @Operation(summary = "list user with role or without role", description = "list user with role or without role")
    @PreAuthorize("@sec.validate(T(com.basedt.dms.service.security.enums.DmsPrivileges).SYS_SYS_ROLE_GRANT)")
    public ResponseEntity<ResponseVO<List<SysUserDTO>>> listUserByRole(@PathVariable("roleId") @NotNull Long roleId) {
        List<SysUserDTO> users = this.sysUserService.listUserByRole(roleId);
        return new ResponseEntity<>(ResponseVO.success(users), HttpStatus.OK);
    }

    @AnonymousAccess
    @PostMapping(path = "/sys/user/pwd/edit")
    @Operation(summary = "change user password", description = "change user password")
    public ResponseEntity<ResponseVO<Object>> changePassword(@RequestBody @Validated PwdChangeVO pwdVo) {
        String password = pwdVo.getPassword();
        String oldPassword = pwdVo.getOldPassword();
        String confirmPassword = pwdVo.getConfirmPassword();
        String userName = SecurityUtil.getCurrentUserName();
        if (StrUtil.isNotEmpty(userName)) {
            if (password.equals(confirmPassword)) {
                SysUserDTO user = this.sysUserService.selectByUserName(userName);
                if (this.passwordEncoder.matches(oldPassword, user.getPassword())) {
                    SysUserDTO userDTO = new SysUserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setPassword(this.passwordEncoder.encode(password));
                    this.sysUserService.update(userDTO);
                    return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(
                            ResponseVO.error(I18nUtil.get("response.error.oldPassword")),
                            HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(
                        ResponseVO.error(I18nUtil.get("response.error.notSamePassword")),
                        HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(
                    ResponseVO.error(ResponseCode.ERROR_CUSTOM), HttpStatus.OK);
        }
    }

    @AnonymousAccess
    @GetMapping(path = "/sys/user/mail/auth")
    @Operation(summary = "get email auth code", description = "get email auth code")
    public ResponseEntity<ResponseVO<Object>> getEmailAuthCode(@NotNull String email) {
        String userName = SecurityUtil.getCurrentUserName();
        if (StrUtil.isNotEmpty(userName)) {
            String code = RandomUtil.randomNumbers(6);
            redisUtil.set(userName + Constants.SEPARATOR_UNDERLINE + email, code, 10 * 60);
            SysMessageDTO message = new SysMessageDTO();
            message.setReceiver(email);
            message.setTitle(this.appName + I18nUtil.get("dms.mail.user.bind.title"));
            message.setContent("<html><body><p>" + I18nUtil.get("dms.mail.user.bind.content.p1") +
                    "：" + userName + "<br/><br/>" + I18nUtil.get("dms.mail.user.bind.content.p2") +
                    "<br/><h3>" + code + "</h3><br/> " + I18nUtil.get("dms.mail.user.bind.content.p3") +
                    "</p></body></html>");
            alert.send(message, AlertType.EMAIL);
            return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    ResponseVO.error(ResponseCode.ERROR_CUSTOM), HttpStatus.OK);
        }
    }

    @AnonymousAccess
    @PostMapping(path = "/sys/user/mail/bind")
    @Operation(summary = "bind user email", description = "bind user email")
    public ResponseEntity<ResponseVO<Object>> bindEmail(@RequestBody @Validated EmailBindVO bindInfo) {
        String userName = SecurityUtil.getCurrentUserName();
        if (StrUtil.isNotEmpty(userName)) {
            String code = redisUtil.get(userName + Constants.SEPARATOR_UNDERLINE + bindInfo.getEmail());
            if (StrUtil.isNotEmpty(code) && code.equals(bindInfo.getAuthCode())) {
                SysUserDTO userDTO = this.sysUserService.selectByUserName(userName);
                userDTO.setEmail(bindInfo.getEmail());
                this.sysUserService.update(userDTO);
                return new ResponseEntity<>(ResponseVO.success(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(
                ResponseVO.error(ResponseCode.ERROR_EMAIL), HttpStatus.OK);
    }

    private void grantNormalRoleToUser(String userName) {
        SysUserDTO userInfo = this.sysUserService.selectByUserName(userName);
        SysRoleDTO normalRole = this.sysRoleService.selectOne(Constants.ROLE_NORMAL);
        this.sysRoleService.grantRoleToUser(userInfo, normalRole);
    }

    private void sendRegisterMail(SysUserDTO userDTO, String password) {
        SysMessageDTO message = new SysMessageDTO();
        message.setReceiver(userDTO.getEmail());
        message.setTitle(this.appName + I18nUtil.get("dms.mail.user.register.title"));
        message.setContent("<html><body><p>" + I18nUtil.get("dms.mail.user.register.content.p1") +
                this.appName +
                I18nUtil.get("dms.mail.user.register.content.p2") + "<br/>" +
                I18nUtil.get("dms.mail.user.register.content.p3") + ": " + userDTO.getUserName() +
                I18nUtil.get("dms.mail.user.register.content.p4") + ": " + password + "<br/>" +
                I18nUtil.get("dms.mail.user.register.content.p5") + "</p></body></html>");
        alert.send(message, AlertType.EMAIL);
    }
}
