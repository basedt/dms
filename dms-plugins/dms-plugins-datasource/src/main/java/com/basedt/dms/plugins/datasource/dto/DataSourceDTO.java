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
package com.basedt.dms.plugins.datasource.dto;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.PropertiesUtil;
import com.basedt.dms.common.vo.DictVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.basedt.dms.plugins.datasource.DataSourcePlugin.JDBC;

@Data
public class DataSourceDTO {

    @Schema(name = "id", title = "id")
    private Long id;

    @Schema(name = "datasourceName", title = "datasource name")
    private String datasourceName;

    @Schema(name = "datasourceType", title = "datasource type")
    private DictVO datasourceType;

    @Schema(name = "hostName", title = "host name")
    private String hostName;

    @Schema(name = "databaseName", title = "database name")
    private String databaseName;

    @Schema(name = "port", title = "port")
    private Integer port;

    @Schema(name = "userName", title = "user name")
    private String userName;

    @Schema(name = "password", title = "password")
    private String password;

    @Schema(name = "attrs", title = "datasource extend attrs")
    private Map<String, Object> attrs;

    public Properties toProperties() {
        Properties props = new Properties();
        props.put("hostName", this.hostName);
        props.put("port", this.port);
        props.put("databaseName", this.databaseName);
        props.put("userName", this.userName);
        props.put("password", this.password);
        Map<String, String> attrMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(attrs)){
            for (Map.Entry<String, Object> entry : this.attrs.entrySet()) {
                attrMap.put(entry.getKey(),entry.getValue().toString());
            }
        }
        props.put("attrs", JSONUtil.toJsonStr(attrMap));
        return props;
    }
}
