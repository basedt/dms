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
package com.basedt.dms.service.workspace.dto;

import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.vo.DictVO;
import com.basedt.dms.service.base.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DmsDataSourceDTO", title = "Dms DataSource DTO")
public class DmsDataSourceDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(name = "workspaceId", title = "workspace id")
    private Long workspaceId;

    @NotBlank
    @Length(max = 64)
    @Pattern(regexp = Constants.REGEX_WORD_CHAR)
    @Schema(name = "datasourceName", title = "datasource name")
    private String datasourceName;

    @NotNull
    @Schema(name = "datasourceType", title = "datasource type")
    private DictVO datasourceType;

    @Length(max = 256)
    @Schema(name = "hostName", title = "host name")
    private String hostName;

    @Length(max = 64)
    @Schema(name = "databaseName", title = "database name")
    private String databaseName;

    @Schema(name = "port", title = "port")
    private Integer port;

    @Length(max = 256)
    @Schema(name = "userName", title = "user name")
    private String userName;

    @Length(max = 256)
    @Schema(name = "password", title = "password")
    private String password;

    @Length(max = 512)
    @Schema(name = "remark", title = "remark")
    private String remark;

    @Schema(name = "attrs", title = "datasource extend attrs")
    private Map<String, Object> attrs;

    @Schema(name = "isPasswordChange", title = "is Password Changed true/false")
    private Boolean isPasswordChange;

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

}
