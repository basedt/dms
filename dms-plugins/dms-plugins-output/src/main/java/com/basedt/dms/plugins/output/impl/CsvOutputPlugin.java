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
package com.basedt.dms.plugins.output.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.FileType;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.output.OutputPlugin;
import com.google.auto.service.AutoService;
import org.apache.commons.beanutils.DynaClass;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

@AutoService(OutputPlugin.class)
public class CsvOutputPlugin extends ExcelOutputPlugin {

    private ExcelWriter excelWriter;

    public CsvOutputPlugin() {
        super();
        init();
    }

    public CsvOutputPlugin(File file, String fileEncoding, DynaClass columns) {
        super(file, fileEncoding, columns);
        init();
        configWriter();
    }

    public CsvOutputPlugin(Map<String, Object> props) {
        super(props);
        init();
        configWriter();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.RESOURCE_OUTPUT.name(), Constants.SEPARATOR_UNDERLINE, FileType.CSV.getValue()).toUpperCase(),
                PluginType.RESOURCE_OUTPUT));
        setFileType(FileType.CSV);
    }

    private void configWriter() {
        excelWriter = EasyExcel.write(file)
                .head(configHeader(getColumns()))
                .excelType(ExcelTypeEnum.CSV)
                .charset(Charset.forName(getFileEncoding()))
                .registerWriteHandler(new SimpleColumnWidthStyleStrategy(20)) //set default column width
                .build();
    }


}
