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
package com.basedt.dms.plugins.datasource.impl.hive;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.*;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.jdbc.*;
import com.google.auto.service.AutoService;

import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@AutoService(DataSourcePlugin.class)
public class HivePluginImpl extends AbstractDataSourcePlugin {

    public static final String METASTORE_URIS = "hmsUris";

    public HivePluginImpl() {
        super();
        init();
    }

    public HivePluginImpl(Properties props) {
        super(props);
        init();
    }

    public HivePluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:hive2://" + getHostName() + Constants.SEPARATOR_COLON + getPort() + "/" + getDatabaseName() + formatJdbcProps();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.APACHEHIVE.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("org.apache.hive.jdbc.HiveDriver");
    }

    @Override
    public Boolean isSupportRowEdit() {
        return false;
    }


    @Override
    public CatalogHandler getCatalogHandler() {
        HiveCatalogHandler handler = new HiveCatalogHandler();
        handler.initialize(getDataSource(), new HashMap<>(), getDatabaseName());
        return handler;
    }

    @Override
    public TableHandler getTableHandler() {
        HiveTableHandler handler = new HiveTableHandler();
        Map<String, String> config = new HashMap<>();
        config.put(METASTORE_URIS, this.attributes.get(METASTORE_URIS));
        handler.initialize(getDataSource(), config,new JdbcDataTypeMapper());
        return handler;
    }

    @Override
    public ViewHandler getViewHandler() {
        HiveViewHandler handler = new HiveViewHandler();
        Map<String, String> config = new HashMap<>();
        config.put(METASTORE_URIS, this.attributes.get(METASTORE_URIS));
        handler.initialize(getDataSource(), config);
        return handler;
    }

    @Override
    public ForeignTableHandler getForeignTableHandler() {
        JdbcForeignTableHandler handler = new JdbcForeignTableHandler();
        handler.initialize(getDataSource(), new HashMap<>(),new JdbcDataTypeMapper());
        return handler;
    }

    @Override
    public FunctionHandler getFunctionHandler() {
        JdbcFunctionHandler handler = new JdbcFunctionHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public IndexHandler getIndexHandler() {
        JdbcIndexHandler handler = new JdbcIndexHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public MaterializedViewHandler getMaterializedViewHandler() {
        HiveMaterializedViewHandler handler = new HiveMaterializedViewHandler();
        Map<String, String> config = new HashMap<>();
        config.put(METASTORE_URIS, this.attributes.get(METASTORE_URIS));
        handler.initialize(getDataSource(), config);
        return handler;
    }

    @Override
    public SequenceHandler getSequenceHandler() {
        JdbcSequenceHandler handler = new JdbcSequenceHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    protected void setColumnValue(PreparedStatement ps, ColumnDTO column, String value, int columnIndex) throws SQLException, ParseException {
        switch (column.getDataType()) {
            case "TINYINT":
            case "SMALLINT":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.NULL);
                } else {
                    ps.setShort(columnIndex, Short.parseShort(value));
                }
                break;
            case "INT":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.NULL);
                } else {
                    ps.setInt(columnIndex, Integer.parseInt(value));
                }
                break;
            case "BIGINT":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.NULL);
                } else {
                    ps.setLong(columnIndex, Long.parseLong(value));
                }
                break;
            case "FLOAT":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.REAL);
                } else {
                    ps.setFloat(columnIndex, Float.parseFloat(value));
                }
                break;
            case "DOUBLE":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.REAL);
                } else {
                    ps.setDouble(columnIndex, Double.parseDouble(value));
                }
                break;
            case "DECIMAL":
                ps.setBigDecimal(columnIndex, StrUtil.isBlank(value) ? null : BigDecimal.valueOf(Double.parseDouble(value)));
                break;
            case "BOOLEAN":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.BOOLEAN);
                } else {
                    ps.setBoolean(columnIndex, Boolean.parseBoolean(value));
                }
                break;
            case "DATE":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.DATE);
                } else {
                    Long dValue = DateTimeUtil.toTimeStamp(value);
                    ps.setDate(columnIndex, Objects.isNull(dValue) ? null : new Date(dValue));
                }
                break;
            case "TIMESTAMP":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.TIMESTAMP);
                } else {
                    Long tValue = DateTimeUtil.toTimeStamp(value);
                    ps.setTimestamp(columnIndex, Objects.isNull(tValue) ? null : new Timestamp(tValue));
                }
                break;
            case "STRING":
            case "VARCHAR":
            case "CHAR":
            default:
                ps.setString(columnIndex, value);

        }
    }

    @Override
    protected String formatJdbcProps() {
        Properties props = getJdbcProps();
        StringBuilder builder = new StringBuilder();
        if (props != null) {
            props.forEach((k, v) -> {
                builder.append(Constants.SEPARATOR_SEMICOLON).append(k).append(Constants.SEPARATOR_EQUAL).append(v);
            });
            return builder.toString();
        } else {
            return builder.toString();
        }
    }

}
