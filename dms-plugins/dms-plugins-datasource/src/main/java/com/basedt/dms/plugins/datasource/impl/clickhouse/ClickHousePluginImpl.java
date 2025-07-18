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

package com.basedt.dms.plugins.datasource.impl.clickhouse;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.*;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcIndexHandler;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcSequenceHandler;
import com.google.auto.service.AutoService;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * https://clickhouse.com/docs/data-modeling/overview
 */
@AutoService(DataSourcePlugin.class)
public class ClickHousePluginImpl extends AbstractDataSourcePlugin {

    public ClickHousePluginImpl() {
        init();
    }

    public ClickHousePluginImpl(Properties props) {
        super(props);
        init();
    }

    public ClickHousePluginImpl(String dataSourceName, String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(dataSourceName, hostName, port, databaseName, userName, password, attributes);
        init();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.CLICKHOUSE.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
    }

    @Override
    public CatalogHandler getCatalogHandler() {
        ClickHouseCatalogHandler handler = new ClickHouseCatalogHandler();
        handler.initialize(getDataSource(), new HashMap<>(), getDatabaseName());
        return handler;
    }

    @Override
    public TableHandler getTableHandler() {
        ClickHouseTableHandler handler = new ClickHouseTableHandler();
        handler.initialize(getDataSource(), new HashMap<>(), new JdbcDataTypeMapper(), getIndexHandler());
        return handler;
    }

    @Override
    public ViewHandler getViewHandler() {
        ClickHouseViewHandler handler = new ClickHouseViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public ForeignTableHandler getForeignTableHandler() {
        ClickHouseFgnTableHandler handler = new ClickHouseFgnTableHandler();
        handler.initialize(getDataSource(), new HashMap<>(), new JdbcDataTypeMapper(), getIndexHandler());
        return handler;
    }

    @Override
    public FunctionHandler getFunctionHandler() {
        ClickHouseFunctionHandler handler = new ClickHouseFunctionHandler();
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
        ClickHouseMaterializedViewHandler handler = new ClickHouseMaterializedViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public SequenceHandler getSequenceHandler() {
        JdbcSequenceHandler handler = new JdbcSequenceHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:clickhouse://" + getHostName() + ":" + getPort() + "/" + getDatabaseName() + formatJdbcProps();
    }

    @Override
    protected void setColumnValue(PreparedStatement ps, ColumnDTO column, String value, int columnIndex) throws SQLException, ParseException {
        switch (column.getDataType()) {
            case "INT":
            case "INT SIGNED":
            case "INT UNSIGNED":
            case "INT1":
            case "INT1 SIGNED":
            case "INT1 UNSIGNED":
            case "INTEGER":
            case "INTEGER SIGNED":
            case "INTEGER UNSIGNED":
            case "MEDIUMINT":
            case "MEDIUMINT SIGNED":
            case "MEDIUMINT UNSIGNED":
            case "SMALLINT":
            case "SMALLINT SIGNED":
            case "SMALLINT UNSIGNED":
            case "TINYINT":
            case "TINYINT SIGNED":
            case "TINYINT UNSIGNED":
            case "Int8":
            case "Int16":
            case "Int32":
            case "UInt8":
            case "UInt16":
            case "UInt32":
            case "BYTE":
            case "Nullable(Int8)":
            case "Nullable(Int16)":
            case "Nullable(Int32)":
            case "Nullable(UInt8)":
            case "Nullable(UInt16)":
            case "Nullable(UInt32)":
                if (StrUtil.isNotEmpty(value)) {
                    ps.setNull(columnIndex, Types.INTEGER);
                } else {
                    ps.setInt(columnIndex, Integer.parseInt(value));
                }
                break;
            case "BIGINT":
            case "BIGINT SIGNED":
            case "BIGINT UNSIGNED":
            case "BIT":
            case "Int64":
            case "Int128":
            case "Int256":
            case "UInt64":
            case "UInt128":
            case "UInt256":
            case "SIGNED":
            case "UNSIGNED":
            case "TIME":
            case "Nullable(Int64)":
            case "Nullable(Int128)":
            case "Nullable(Int256)":
            case "Nullable(UInt64)":
            case "Nullable(UInt128)":
            case "Nullable(UInt256)":
                if (StrUtil.isNotEmpty(value)) {
                    ps.setNull(columnIndex, Types.NULL);
                } else {
                    ps.setLong(columnIndex, Long.parseLong(value));
                }
                break;
            case "Float32":
            case "FLOAT":
            case "REAL":
            case "SINGLE":
            case "Nullable(Float32)":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.FLOAT);
                } else {
                    ps.setFloat(columnIndex, Float.parseFloat(value));
                }
                break;
            case "DOUBLE":
            case "Float64":
            case "DOUBLE PRECISION":
            case "Nullable(Float64)":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.DOUBLE);
                } else {
                    ps.setDouble(columnIndex, Double.parseDouble(value));
                }
                break;
            case "DEC":
            case "Decimal":
            case "Decimal32":
            case "Decimal64":
            case "Decimal128":
            case "Decimal256":
            case "Nullable(Decimal)":
            case "Nullable(Decimal32)":
            case "Nullable(Decimal64)":
            case "Nullable(Decimal128)":
            case "Nullable(Decimal256)":
                ps.setBigDecimal(columnIndex, StrUtil.isBlank(value) ? null : BigDecimal.valueOf(Double.parseDouble(value)));
                break;
            case "Bool":
            case "bool":
            case "boolean":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.BOOLEAN);
                } else {
                    ps.setBoolean(columnIndex, Boolean.parseBoolean(value));
                }
                break;
            case "Date":
            case "Date32":
            case "DateTime":
            case "DateTime32":
            case "DateTime64":
            case "TIMESTAMP":
            case "Nullable(Date)":
            case "Nullable(Date32)":
            case "Nullable(DateTime)":
            case "Nullable(DateTime32)":
            case "Nullable(DateTime64)":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.DATE);
                } else {
                    Long dValue = DateTimeUtil.toTimeStamp(value);
                    ps.setDate(columnIndex, Objects.isNull(dValue) ? null : new Date(dValue));
                }
                break;
            case "BINARY":
            case "BINARY LARGE OBJECT":
            case "BINARY VARYING":
            case "BLOB":
            case "CHAR":
            case "FixedString":
            case "String":
            case "TEXT":
            case "Nullable(String)":
            default:
                ps.setString(columnIndex, value);

        }
    }

}
