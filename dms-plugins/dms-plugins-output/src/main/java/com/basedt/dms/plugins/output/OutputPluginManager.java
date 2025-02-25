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
package com.basedt.dms.plugins.output;

import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginLoader;
import org.apache.commons.beanutils.DynaClass;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class OutputPluginManager {

    public static OutputPlugin newInstance(PluginInfo pluginInfo, Map<String, Object> props) {
        return newInstance(pluginInfo.getPluginName(), props);
    }

    public static OutputPlugin newInstance(String pluginName, Map<String, Object> props) {
        PluginLoader<OutputPlugin> loader = new PluginLoader<>(OutputPlugin.class);
        return loader.newInstance(pluginName, props);
    }

    public static OutputPlugin newInstance(String pluginName, File file, String fileEncoding, DynaClass columns) {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("file", file);
        props.put("fileEncoding", fileEncoding);
        props.put("columns", columns);
        return newInstance(pluginName, props);
    }
}
