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

package com.basedt.dms.plugins.datasource.impl.gaussdb;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.MaterializedViewHandler;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.postgre.PostgrePluginImpl;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@AutoService(DataSourcePlugin.class)
public class GaussdbPluginImpl extends PostgrePluginImpl {

    public GaussdbPluginImpl() {
        super();
        init();
    }

    public GaussdbPluginImpl(Properties properties) {
        super(properties);
        init();
    }

    public GaussdbPluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }


    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.GAUSSDB.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("org.postgresql.Driver");
    }

    @Override
    public MaterializedViewHandler getMaterializedViewHandler() {
        GaussdbMaterializedViewHandler handler = new GaussdbMaterializedViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }
}
