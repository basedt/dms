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
package com.basedt.dms.plugins.datasource.impl.mysql;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.ResponseCode;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.*;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.google.auto.service.AutoService;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * https://dev.mysql.com/doc/refman/8.4/en/
 */
@AutoService(DataSourcePlugin.class)
public class MysqlPluginImpl extends AbstractDataSourcePlugin {

    public MysqlPluginImpl() {
        super();
        init();
    }

    public MysqlPluginImpl(Properties props) {
        super(props);
        init();
    }

    public MysqlPluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mysql://" + getHostName() + Constants.SEPARATOR_COLON + getPort() + "/" + getDatabaseName() + formatJdbcProps();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.MYSQL.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("com.mysql.cj.jdbc.Driver");
    }

    @Override
    public CatalogHandler getCatalogHandler() {
        MysqlCatalogHandler handler = new MysqlCatalogHandler();
        handler.initialize(getDataSource(), new HashMap<>(), getDatabaseName());
        return handler;
    }

    @Override
    public TableHandler getTableHandler() {
        MysqlTableHandler handler = new MysqlTableHandler();
        handler.initialize(getDataSource(), new HashMap<>(),new JdbcDataTypeMapper());
        return handler;
    }

    @Override
    public ViewHandler getViewHandler() {
        MysqlViewHandler handler = new MysqlViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @SneakyThrows
    @Override
    public ForeignTableHandler getForeignTableHandler() {
        throw new DmsException(ResponseCode.ERROR_DB_TYPE_NOT_SUPPORTED);
    }

    @Override
    public FunctionHandler getFunctionHandler() {
        MysqlFunctionHandler handler = new MysqlFunctionHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public IndexHandler getIndexHandler() {
        MysqlIndexHandler handler = new MysqlIndexHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @SneakyThrows
    @Override
    public MaterializedViewHandler getMaterializedViewHandler() {
        throw new DmsException(ResponseCode.ERROR_DB_TYPE_NOT_SUPPORTED);
    }

    @SneakyThrows
    @Override
    public SequenceHandler getSequenceHandler() {
        throw new DmsException(ResponseCode.ERROR_DB_TYPE_NOT_SUPPORTED);
    }


    @Override
    public Boolean isSupportRowEdit() {
        return true;
    }

    /**
     * @param ps
     * @param column
     * @param value
     * @param columnIndex
     * @throws SQLException
     * @throws ParseException
     */
    @Override
    protected void setColumnValue(PreparedStatement ps, ColumnDTO column, String value, int columnIndex) throws SQLException, ParseException {
        switch (column.getDataType()) {
            case "varchar":
            case "longtext":
            case "enum":
            case "json":
            case "text":
            case "mediumtext":
            case "char":
                ps.setString(columnIndex, value);
                break;
            case "int":
            case "tinyint":
            case "smallint":
            case "mediumint":
            case "integer":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.INTEGER);
                } else if ("true".equalsIgnoreCase(value)) {
                    ps.setBoolean(columnIndex, true);
                } else if ("false".equalsIgnoreCase(value)) {
                    ps.setBoolean(columnIndex, false);
                } else {
                    ps.setInt(columnIndex, Integer.parseInt(value));
                }
                break;
            case "bigint":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.BIGINT);
                } else {
                    ps.setLong(columnIndex, Long.parseLong(value));
                }
                break;
            case "float":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.FLOAT);
                } else {
                    ps.setFloat(columnIndex, Float.parseFloat(value));
                }
                break;
            case "double":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.DOUBLE);
                } else {
                    ps.setDouble(columnIndex, Double.parseDouble(value));
                }
                break;
            case "decimal":
            case "numeric":
                ps.setBigDecimal(columnIndex, StrUtil.isBlank(value) ? null : BigDecimal.valueOf(Double.parseDouble(value)));
                break;
            case "timestamp":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.TIMESTAMP);
                } else {
                    Long tValue = DateTimeUtil.toTimeStamp(value);
                    ps.setTimestamp(columnIndex, Objects.isNull(tValue) ? null : new Timestamp(tValue));
                }
                break;
            case "date":
            case "datetime":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.DATE);
                } else {
                    Long dValue = DateTimeUtil.toTimeStamp(value);
                    ps.setDate(columnIndex, Objects.isNull(dValue) ? null : new Date(dValue));
                }
                break;
            case "time":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.TIME);
                } else {
                    Long t = DateTimeUtil.toTimeStamp(value);
                    ps.setTime(columnIndex, Objects.isNull(t) ? null : new Time(t));
                }
                break;
            default:
                ps.setString(columnIndex, value);
        }
    }

}
