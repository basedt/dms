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
package com.basedt.dms.plugins.input.impl;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.FileType;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.input.AbstractInputPlugin;
import com.basedt.dms.plugins.input.InputPlugin;
import com.google.auto.service.AutoService;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowStreamWriter;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AutoService(InputPlugin.class)
public class CsvInputPlugin extends AbstractInputPlugin {

    private String separator;

    private RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);

    private VectorSchemaRoot buffer = null;

    public CsvInputPlugin() {
        super();
        init();
    }

    public CsvInputPlugin(File file, String fileEncoding) {
        super(file, fileEncoding);
        init();
    }

    public CsvInputPlugin(Map<String, Object> props) {
        super(props);
        String sep = props.get("separator").toString();
        setSeparator(StrUtil.isEmpty(sep) ? "," : sep);
        init();
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public ByteArrayOutputStream read() throws IOException {
        ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream();
        try (Reader reader = new FileReader(getFile());
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setDelimiter(getSeparator())
                     .setSkipHeaderRecord(true)
                     .build())
        ) {
            Schema schema = invokeHead(parser.getHeaderNames());
            buffer = VectorSchemaRoot.create(schema, allocator);
            for (CSVRecord record : parser) {
                int rowNum = Math.toIntExact(record.getRecordNumber());
                for (Field field : schema.getFields()) {
                    ValueVector vector = buffer.getVector(field.getName());
                    String value = record.get(field.getName());
                    ((VarCharVector) vector).setSafe(rowNum - 1, value.getBytes());
                }
            }
            buffer.setRowCount(Math.toIntExact(parser.getRecordNumber()));
        }
        try (ArrowStreamWriter writer = new ArrowStreamWriter(buffer, null, Channels.newChannel(arrayOutput))) {
            writer.start();
            writer.writeBatch();
            writer.end();
        } finally {
            buffer.close();
            allocator.close();
        }
        return arrayOutput;
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.RESOURCE_INPUT.name(), Constants.SEPARATOR_UNDERLINE, FileType.CSV.getValue()).toUpperCase(),
                PluginType.RESOURCE_INPUT));
        setFileType(FileType.CSV);
    }

    private Schema invokeHead(List<String> headerNames) {
        List<Field> fields = new ArrayList<>();
        for (int i = 0; i < headerNames.size(); i++) {
            String headerName = headerNames.get(i);
            Field field = Field.nullable(StrUtil.isBlank(headerName) ? "col_" + i : headerName, ArrowType.Utf8.INSTANCE);
            fields.add(field);
        }
        return new Schema(fields);
    }
}
