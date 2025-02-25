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
package com.basedt.dms.api.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RegisterInfoVO {

    @NotBlank
    @Length(max = 30, min = 5)
    @Pattern(regexp = "\\w+$")
    private String userName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Length(min = 6, max = 32)
    private String password;

    @NotBlank
    @Length(min = 6, max = 32)
    private String confirmPassword;

    @NotBlank
    @Length(min = 5, max = 5)
    private String authCode;

    @NotBlank
    private String uuid;

}
