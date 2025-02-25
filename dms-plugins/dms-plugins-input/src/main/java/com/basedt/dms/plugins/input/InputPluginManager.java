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
package com.basedt.dms.plugins.input;

import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class InputPluginManager {

    public static InputPlugin newInstance(PluginInfo pluginInfo, Map<String, Object> props) {
        return newInstance(pluginInfo.getPluginName(), props);
    }

    public static InputPlugin newInstance(String pluginName, Map<String, Object> props) {
        PluginLoader<InputPlugin> loader = new PluginLoader<>(InputPlugin.class);
        return loader.newInstance(pluginName, props);
    }

    public static InputPlugin newInstance(String pluginName, File file, String fileEncoding) {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("file", file);
        props.put("fileEncoding", fileEncoding);
        return newInstance(pluginName, props);
    }
}
