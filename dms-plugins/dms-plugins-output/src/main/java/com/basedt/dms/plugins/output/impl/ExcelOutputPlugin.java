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
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.FileType;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.output.AbstractOutputPlugin;
import com.basedt.dms.plugins.output.OutputPlugin;
import com.google.auto.service.AutoService;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AutoService(OutputPlugin.class)
public class ExcelOutputPlugin extends AbstractOutputPlugin {

    private final String DEFAULT_SHEET_NAME = "data";

    private ExcelWriter excelWriter;

    private WriteSheet writeSheet = EasyExcel.writerSheet(DEFAULT_SHEET_NAME).build();

    public ExcelOutputPlugin() {
        super();
        init();
    }

    public ExcelOutputPlugin(File file, String fileEncoding, DynaClass columns) {
        super(file, fileEncoding, columns);
        init();
        configWriter();
    }

    public ExcelOutputPlugin(Map<String, Object> props) {
        super(props);
        init();
        configWriter();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.RESOURCE_OUTPUT.name(), Constants.SEPARATOR_UNDERLINE, FileType.XLSX.getValue()).toUpperCase(),
                PluginType.RESOURCE_OUTPUT));
        setFileType(FileType.XLSX);
    }

    private void configWriter() {
        excelWriter = EasyExcel.write(file)
                .head(configHeader(getColumns()))
                .excelType(ExcelTypeEnum.XLSX)
                .charset(Charset.forName(getFileEncoding()))
                .registerWriteHandler(new SimpleColumnWidthStyleStrategy(20)) //set default column width
                .build();
    }

    @Override
    public void write(List<List<Object>> data) throws IOException {
        excelWriter.write(data, writeSheet);
    }

    @Override
    public void finish() throws IOException {
        excelWriter.finish();
    }

    protected List<List<String>> configHeader(DynaClass dynaClass) {
        List<List<String>> headers = new ArrayList<>();
        if (Objects.nonNull(dynaClass)) {
            DynaProperty[] cols = dynaClass.getDynaProperties();
            for (DynaProperty dynaProperty : cols) {
                String columnName = dynaProperty.getName();
                List<String> head = new ArrayList<>();
                head.add(columnName);
                headers.add(head);
            }
        }
        return headers;
    }
}
