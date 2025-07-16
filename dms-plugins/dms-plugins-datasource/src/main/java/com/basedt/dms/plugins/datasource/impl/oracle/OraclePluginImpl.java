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
package com.basedt.dms.plugins.datasource.impl.oracle;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.*;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcForeignTableHandler;
import com.google.auto.service.AutoService;

import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@AutoService(DataSourcePlugin.class)
public class OraclePluginImpl extends AbstractDataSourcePlugin {

    public OraclePluginImpl() {
        init();
    }

    public OraclePluginImpl(Properties props) {
        super(props);
        init();
    }

    public OraclePluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    protected String getJdbcUrl() {
        return "jdbc:oracle:thin:@//" + getHostName() + ":" + getPort() + "/" + getDatabaseName();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.ORACLE.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("oracle.jdbc.driver.OracleDriver");
    }

    @Override
    public CatalogHandler getCatalogHandler() {
        OracleCatalogHandler handler = new OracleCatalogHandler();
        handler.initialize(getDataSource(), new HashMap<>(), getDatabaseName());
        return handler;
    }

    @Override
    public TableHandler getTableHandler() {
        OracleTableHandler handler = new OracleTableHandler();
        handler.initialize(getDataSource(), new HashMap<>(),new JdbcDataTypeMapper(),getIndexHandler());
        return handler;
    }

    @Override
    public ViewHandler getViewHandler() {
        OracleViewHandler handler = new OracleViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    /**
     * all_external_tables
     */
    @Override
    public ForeignTableHandler getForeignTableHandler() {
        JdbcForeignTableHandler handler = new JdbcForeignTableHandler();
        handler.initialize(getDataSource(), new HashMap<>(),new JdbcDataTypeMapper(),getIndexHandler());
        return handler;
    }

    @Override
    public FunctionHandler getFunctionHandler() {
        OracleFunctionHandler handler = new OracleFunctionHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public IndexHandler getIndexHandler() {
        OracleIndexHandler handler = new OracleIndexHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public MaterializedViewHandler getMaterializedViewHandler() {
        OracleMaterializedViewHandler handler = new OracleMaterializedViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public SequenceHandler getSequenceHandler() {
        OracleSequenceHandler handler = new OracleSequenceHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }


    @Override
    public Boolean isSupportRowEdit() {
        return true;
    }

    @Override
    protected void setColumnValue(PreparedStatement ps, ColumnDTO column, String value, int columnIndex) throws SQLException, ParseException {
        switch (column.getDataType()) {
            case "VARCHAR2":
            case "CHAR":
                ps.setString(columnIndex, value);
                break;
            case "NUMBER":
                if (column.getDataScale() == 0 && column.getDataPrecision() <= 9) {
                    if (StrUtil.isBlank(value)) {
                        ps.setNull(columnIndex, Types.INTEGER);
                    } else {
                        ps.setInt(columnIndex, Integer.parseInt(value));
                    }
                } else if (column.getDataScale() == 0 && column.getDataPrecision() <= 18) {
                    if (StrUtil.isBlank(value)) {
                        ps.setNull(columnIndex, Types.BIGINT);
                    } else {
                        ps.setLong(columnIndex, Long.parseLong(value));
                    }
                } else if (column.getDataScale() > 0) {
                    if (StrUtil.isBlank(value)) {
                        ps.setNull(columnIndex, Types.DOUBLE);
                    } else {
                        ps.setDouble(columnIndex, Double.parseDouble(value));
                    }
                } else {
                    ps.setBigDecimal(columnIndex, StrUtil.isBlank(value) ? null : BigDecimal.valueOf(Double.parseDouble(value)));
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
            case "TIMESTAMP WITH TIME ZONE":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.TIMESTAMP);
                } else {
                    Long tValue = DateTimeUtil.toTimeStamp(value);
                    ps.setTimestamp(columnIndex, Objects.isNull(tValue) ? null : new Timestamp(tValue));
                }
                break;
            case "BINARY_FLOAT":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.FLOAT);
                } else {
                    ps.setFloat(columnIndex, Float.parseFloat(value));
                }
                break;
            case "BINARY_DOUBLE":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.DOUBLE);
                } else {
                    ps.setDouble(columnIndex, Double.parseDouble(value));
                }
                break;
            default:
                ps.setString(columnIndex, value);
        }
    }


}
