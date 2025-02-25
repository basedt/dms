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

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.FileType;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.output.AbstractOutputPlugin;
import com.basedt.dms.plugins.output.OutputPlugin;
import com.google.auto.service.AutoService;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.orc.CompressionKind;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.apache.orc.storage.common.type.HiveIntervalDayTime;
import org.apache.orc.storage.ql.exec.vector.*;
import org.apache.orc.storage.serde2.io.HiveDecimalWritable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AutoService(OutputPlugin.class)
public class OrcOutputPlugin extends AbstractOutputPlugin {

    private Writer writer;

    public OrcOutputPlugin() {
        super();
        init();
    }

    public OrcOutputPlugin(File file, String fileEncoding, DynaClass columns) throws IOException {
        super(file, fileEncoding, columns);
        init();
        configWriter();
    }

    public OrcOutputPlugin(Map<String, Object> props) throws IOException {
        super(props);
        init();
        configWriter();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.RESOURCE_OUTPUT.name(), Constants.SEPARATOR_UNDERLINE, FileType.ORC.getValue()).toUpperCase(),
                PluginType.RESOURCE_OUTPUT));
        setFileType(FileType.ORC);
    }

    private void configWriter() throws IOException {
        String filePath = getFile().getCanonicalPath();
        writer = OrcFile.createWriter(new Path(filePath),
                OrcFile.writerOptions(new Configuration())
                        .setSchema(configSchema(getColumns()))
                        .stripeSize(64L * 1024 * 1024)
                        .blockSize(256L * 1024 * 1024)
                        .bufferSize(256 * 1024)
                        .rowIndexStride(10000)
                        .blockPadding(true)
                        .compress(CompressionKind.ZLIB)
        );
    }

    @Override
    public void write(List<List<Object>> data) throws IOException {
        VectorizedRowBatch batch = configSchema(getColumns()).createRowBatch();
        ColumnVector[] columns = batch.cols;
        for (List<Object> row : data) {
            int rowNum = batch.size++;
            for (int i = 0; i < row.size(); i++) {
                if (columns[i] instanceof LongColumnVector) {
                    ((LongColumnVector) columns[i]).vector[rowNum] = NumberUtil.parseLong(row.get(i).toString());
                } else if (columns[i] instanceof BytesColumnVector) {
                    ((BytesColumnVector) columns[i]).setVal(rowNum, StrUtil.bytes(row.get(i).toString()));
                } else if (columns[i] instanceof DateColumnVector) {
                    ((DateColumnVector) columns[i]).vector[rowNum] = NumberUtil.parseLong(row.get(i).toString());
                } else if (columns[i] instanceof Decimal64ColumnVector) {
                    ((Decimal64ColumnVector) columns[i]).vector[rowNum] = NumberUtil.parseLong(row.get(i).toString());
                } else if (columns[i] instanceof DecimalColumnVector) {
                    ((DecimalColumnVector) columns[i]).vector[rowNum] = new HiveDecimalWritable((String) row.get(i));
                } else if (columns[i] instanceof DoubleColumnVector) {
                    ((DoubleColumnVector) columns[i]).vector[rowNum] = Double.parseDouble(row.get(i).toString());
                } else if (columns[i] instanceof TimestampColumnVector) {
                    LocalDateTime timestamp = DateTimeUtil.toLocalDateTime(row.get(i).toString(), DateTimeUtil.NORMAL_DATETIME_MS_PATTERN);
                    if (Objects.nonNull(timestamp)) {
                        ((TimestampColumnVector) columns[i]).set(rowNum, new Timestamp(timestamp.toInstant(ZoneOffset.UTC).toEpochMilli()));
                    } else {
                        ((TimestampColumnVector) columns[i]).set(rowNum, null);
                    }
                } else if (columns[i] instanceof IntervalDayTimeColumnVector) {
                    ((IntervalDayTimeColumnVector) columns[i]).set(rowNum, new HiveIntervalDayTime(new BigDecimal((Long) row.get(i))));
                }
            }
            if (batch.size == batch.getMaxSize()) {
                writer.addRowBatch(batch);
                batch.reset();
            }
        }
        if (batch.size >= 0) {
            writer.addRowBatch(batch);
            batch.reset();
        }
    }

    @Override
    public void finish() throws IOException {
        writer.close();
    }

    private TypeDescription configSchema(DynaClass dynaClass) {
        TypeDescription schema = TypeDescription.createStruct();
        if (Objects.nonNull(dynaClass)) {
            DynaProperty[] dynaProperties = dynaClass.getDynaProperties();
            for (DynaProperty dynaProperty : dynaProperties) {
                String name = dynaProperty.getName();
                schema.addField(name, typeMapping(dynaProperty));
            }
        }
        return schema;
    }

    private TypeDescription typeMapping(DynaProperty dynaProperty) {
        Class<?> type = dynaProperty.getType();
        if (Boolean.class.getName().equals(type.getName())) {
            return TypeDescription.createBoolean();
        } else if (Byte.class.getName().equals(type.getName())) {
            return TypeDescription.createByte();
        } else if (Short.class.getName().equals(type.getName())) {
            return TypeDescription.createShort();
        } else if (Integer.class.getName().equals(type.getName())) {
            return TypeDescription.createInt();
        } else if (Long.class.getName().equals(type.getName())) {
            return TypeDescription.createLong();
        } else if (Float.class.getName().equals(type.getName())) {
            return TypeDescription.createFloat();
        } else if (Double.class.getName().equals(type.getName())) {
            return TypeDescription.createDouble();
        } else if (String.class.getName().equals(type.getName())) {
            return TypeDescription.createString();
        } else if (Date.class.getName().equals(type.getName())) {
            return TypeDescription.createDate();
        } else if (Timestamp.class.getName().equals(type.getName())) {
            return TypeDescription.createTimestamp();
        } else {
            return TypeDescription.createString();
        }
    }
}
