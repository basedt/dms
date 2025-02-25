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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.basedt.dms.common.enums.FileType.XLS;

@AutoService(InputPlugin.class)
public class ExcelInputPlugin extends AbstractInputPlugin {

    private RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);

    private VectorSchemaRoot buffer = null;

    public ExcelInputPlugin() {
        super();
        init();
    }

    public ExcelInputPlugin(File file, String fileEncoding) {
        super(file, fileEncoding);
        init();
    }

    public ExcelInputPlugin(Map<String, Object> props) {
        super(props);
        init();
    }

    /**
     * read data and convert all columns to string
     *
     * @return
     * @throws IOException
     */
    @Override
    public ByteArrayOutputStream read() throws IOException {
        ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream();
        Workbook workbook = null;
        try (FileInputStream in = new FileInputStream(file)) {
            if (getFileType() == XLS) {
                workbook = new HSSFWorkbook(in);
            } else {
                workbook = new XSSFWorkbook(in);
            }
        }
        Sheet sheet = workbook.getSheetAt(0);
        Schema schema = invokeHead(sheet);
        DataFormatter formatter = new DataFormatter();
        buffer = VectorSchemaRoot.create(schema, allocator);
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (Objects.nonNull(row)) {
                for (int colIndex = 0; colIndex < schema.getFields().size(); colIndex++) {
                    ValueVector vector = buffer.getVector(colIndex);
                    Cell cell = row.getCell(colIndex);
                    String value = formatter.formatCellValue(cell);
                    ((VarCharVector) vector).setSafe(rowNum - 1, value.getBytes());

                }
            }
        }
        buffer.setRowCount(sheet.getLastRowNum());
        try (ArrowStreamWriter writer = new ArrowStreamWriter(buffer, null, Channels.newChannel(arrayOutput))) {
            writer.start();
            writer.writeBatch();
            writer.end();
        } finally {
            buffer.close();
            allocator.close();
            workbook.close();
        }
        return arrayOutput;
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.RESOURCE_INPUT.name(), Constants.SEPARATOR_UNDERLINE, FileType.XLSX.getValue()).toUpperCase(),
                PluginType.RESOURCE_INPUT));
        if (Objects.isNull(file)) {
            setFileType(FileType.XLSX);
        } else {
            String fileName = file.getName();
            if (fileName.toUpperCase().endsWith(XLS.name())) {
                setFileType(XLS);
            } else {
                setFileType(FileType.XLSX);
            }
        }
    }

    private Schema invokeHead(Sheet sheet) {
        List<Field> fields = new ArrayList<>();
        Row headRow = sheet.getRow(0);
        for (int i = 0; i < headRow.getLastCellNum(); i++) {
            Cell headCell = headRow.getCell(i);
            Field field = Field.nullable(StrUtil.isBlank(headCell.getStringCellValue()) ? "col_" + i : headCell.getStringCellValue(), ArrowType.Utf8.INSTANCE);
            fields.add(field);
        }
        return new Schema(fields);
    }

}
