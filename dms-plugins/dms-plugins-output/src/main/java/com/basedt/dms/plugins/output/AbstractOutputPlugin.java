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

import com.basedt.dms.common.enums.FileType;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import org.apache.commons.beanutils.DynaClass;

import java.io.File;
import java.util.Map;

public abstract class AbstractOutputPlugin implements OutputPlugin {

    protected PluginInfo pluginInfo;

    protected File file;

    protected String fileEncoding;

    protected FileType fileType;

    DynaClass columns;

    public AbstractOutputPlugin() {
    }

    public AbstractOutputPlugin(File file, String fileEncoding, DynaClass columns) {
        this.file = file;
        this.fileEncoding = fileEncoding;
        this.columns = columns;
    }

    public AbstractOutputPlugin(Map<String, Object> props) {
        setFile((File) props.get("file"));
        setFileEncoding((String) props.get("fileEncoding"));
        setColumns((DynaClass) props.get("columns"));
    }

    @Override
    public PluginInfo getPluginInfo() {
        return this.pluginInfo;
    }

    @Override
    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Override
    public String getPluginName() {
        return getPluginInfo().getPluginName();
    }

    @Override
    public PluginType getPluginType() {
        return getPluginInfo().getPluginType();
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public String getFileEncoding() {
        return this.fileEncoding;
    }

    @Override
    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    @Override
    public DynaClass getColumns() {
        return this.columns;
    }

    @Override
    public void setColumns(DynaClass columns) {
        this.columns = columns;
    }

    @Override
    public FileType getFileType() {
        return this.fileType;
    }

    @Override
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
}
