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
package com.basedt.dms.plugins.core;

import cn.hutool.core.util.StrUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PluginLoader<T extends Plugin> {

    private volatile Map<String, T> pluginMap = null;

    public PluginLoader(Class<T> clazz) {
        ServiceLoader<T> spiServices = ServiceLoader.load(clazz);
        Map<String, T> map = new HashMap<>();
        for (T t : spiServices) {
            map.put(t.getPluginName(), t);
        }
        this.pluginMap = map;
    }

    public T newInstance(String pluginName, Properties props) {
        if (pluginMap.containsKey(pluginName)) {
            T t = pluginMap.get(pluginName);
            try {
                final Class<T> clazz = (Class<T>) Class.forName(t.getClass().getName());
                return clazz.getConstructor(Properties.class).newInstance(props);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException(StrUtil.format("plugin {} is not supported", pluginName));
        }
    }

    public T newInstance(String pluginName, Map<String, Object> props) {
        if (pluginMap.containsKey(pluginName)) {
            T t = pluginMap.get(pluginName);
            try {
                final Class<T> clazz = (Class<T>) Class.forName(t.getClass().getName());
                return clazz.getConstructor(Map.class).newInstance(props);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException(StrUtil.format("plugin {} is not supported", pluginName));
        }
    }


    public T newInstance(String pluginName, Object... props) {
        if (pluginMap.containsKey(pluginName)) {
            T t = pluginMap.get(pluginName);
            try {
                final Class<T> clazz = (Class<T>) Class.forName(t.getClass().getName());
                Class<?>[] clazzInfo = getClazzInfo(props);
                if (clazzInfo == null) {
                    return clazz.getConstructor().newInstance();
                } else {
                    return clazz.getConstructor(clazzInfo).newInstance(props);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException(StrUtil.format("plugin {} is not supported", pluginName));
        }
    }

    private Class<?>[] getClazzInfo(Object... props) {
        if (Objects.isNull(props) || props.length == 0) {
            return null;
        }
        List<Class<?>> classes = new ArrayList<>();
        for (Object prop : props) {
            Class<?> clazz = prop.getClass();
            classes.add(clazz);
        }
        return classes.toArray(new Class<?>[0]);
    }
}
