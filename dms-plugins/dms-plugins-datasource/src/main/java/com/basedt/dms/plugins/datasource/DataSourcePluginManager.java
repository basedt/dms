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
package com.basedt.dms.plugins.datasource;

import cn.hutool.json.JSONUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginLoader;

import java.util.Map;
import java.util.Properties;

public class DataSourcePluginManager {

    public static DataSourcePlugin newInstance(PluginInfo pluginInfo, Properties props) {
        return newInstance(pluginInfo.getPluginName(), props);
    }

    public static DataSourcePlugin newInstance(String pluginName, Properties props) {
        PluginLoader<DataSourcePlugin> loader = new PluginLoader<>(DataSourcePlugin.class);
        return loader.newInstance(pluginName, props);
    }

    public static DataSourcePlugin newInstance(PluginInfo pluginInfo, String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        return newInstance(pluginInfo.getPluginName(), hostName, port, databaseName, userName, password, attributes);
    }

    public static DataSourcePlugin newInstance(String pluginName, String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        Properties props = new Properties();
        props.put("hostName", hostName);
        props.put("port", port);
        props.put("databaseName", databaseName);
        props.put("userName", userName);
        props.put("password", password);
        props.put("attrs", JSONUtil.toJsonStr(attributes));
        return newInstance(pluginName, props);
    }

}
