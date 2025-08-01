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
package com.basedt.dms.plugins.datasource.impl.doris;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.*;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.mysql.MysqlPluginImpl;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * https://doris.apache.org/zh-CN/docs/gettingStarted/quick-start
 */
@AutoService(DataSourcePlugin.class)
public class DorisPluginImpl extends MysqlPluginImpl {

    public DorisPluginImpl() {
        super();
        init();
    }

    public DorisPluginImpl(Properties properties) {
        super(properties);
        init();
    }

    public DorisPluginImpl(String dataSourceName, String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(dataSourceName, hostName, port, databaseName, userName, password, attributes);
        init();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.DORIS.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("com.mysql.jdbc.Driver");
    }

    @Override
    public CatalogHandler getCatalogHandler() {
        DorisCatalogHandler handler = new DorisCatalogHandler();
        handler.initialize(getDataSource(), new HashMap<>(), getDatabaseName());
        return handler;
    }

    @Override
    public FunctionHandler getFunctionHandler() {
        DorisFunctionHandler handler = new DorisFunctionHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public IndexHandler getIndexHandler() {
        DorisIndexHandler handler = new DorisIndexHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public MaterializedViewHandler getMaterializedViewHandler() {
        DorisMaterializedViewHandler handler = new DorisMaterializedViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public TableHandler getTableHandler() {
        DorisTableHandler handler = new DorisTableHandler();
        handler.initialize(getDataSource(), new HashMap<>(), new DorisDataTypeMapper(), getIndexHandler());
        return handler;
    }

    @Override
    public ViewHandler getViewHandler() {
        DorisViewHandler handler = new DorisViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

}
