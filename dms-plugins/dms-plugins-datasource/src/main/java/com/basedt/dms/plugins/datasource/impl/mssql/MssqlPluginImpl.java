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

package com.basedt.dms.plugins.datasource.impl.mssql;

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
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcMaterializedViewHandler;
import com.google.auto.service.AutoService;

import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@AutoService(DataSourcePlugin.class)
public class MssqlPluginImpl extends AbstractDataSourcePlugin {

    public MssqlPluginImpl() {
        super();
        init();
    }

    public MssqlPluginImpl(Properties props) {
        super(props);
        init();
    }

    public MssqlPluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.MSSQL.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    /**
     * https://learn.microsoft.com/zh-cn/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver16&redirectedfrom=MSDN
     */

    @Override
    protected String getJdbcUrl() {
        return "jdbc:sqlserver://" + getHostName() + Constants.SEPARATOR_COLON + getPort() + ";databaseName=" + getDatabaseName() + formatJdbcProps();
    }

    @Override
    public CatalogHandler getCatalogHandler() {
        MssqlCatalogHandler handler = new MssqlCatalogHandler();
        handler.initialize(getDataSource(), new HashMap<>(), getDatabaseName());
        return handler;
    }

    @Override
    public TableHandler getTableHandler() {
        MssqlTableHandler handler = new MssqlTableHandler();
        handler.initialize(getDataSource(), new HashMap<>(),new JdbcDataTypeMapper());
        return handler;
    }

    @Override
    public ViewHandler getViewHandler() {
        MssqlViewHandler handler = new MssqlViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
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
        MssqlFunctionHandler handler = new MssqlFunctionHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public IndexHandler getIndexHandler() {
        MssqlIndexHandler handler = new MssqlIndexHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public MaterializedViewHandler getMaterializedViewHandler() {
        JdbcMaterializedViewHandler handler = new JdbcMaterializedViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public SequenceHandler getSequenceHandler() {
        MssqlSequenceHandler handler = new MssqlSequenceHandler();
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
            case "int":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.NULL);
                } else {
                    ps.setInt(columnIndex, Integer.parseInt(value));
                }
                break;
            case "smallint":
            case "tinyint":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.NULL);
                } else {
                    ps.setShort(columnIndex, Short.parseShort(value));
                }
                break;
            case "bigint":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.NULL);
                } else {
                    ps.setLong(columnIndex, Long.parseLong(value));
                }
                break;
            case "bit":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.BOOLEAN);
                } else {
                    ps.setBoolean(columnIndex, Boolean.parseBoolean(value));
                }
                break;
            case "date":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.DATE);
                } else {
                    Long dValue = DateTimeUtil.toTimeStamp(value);
                    ps.setDate(columnIndex, Objects.isNull(dValue) ? null : new Date(dValue));
                }
                break;
            case "datetime":
            case "datetime2":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.TIMESTAMP);
                } else {
                    Long tValue = DateTimeUtil.toTimeStamp(value);
                    ps.setTimestamp(columnIndex, Objects.isNull(tValue) ? null : new Timestamp(tValue));
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
            case "numeric":
            case "decimal":
            case "smallmoney":
            case "money":
                ps.setBigDecimal(columnIndex, StrUtil.isBlank(value) ? null : BigDecimal.valueOf(Double.parseDouble(value)));
                break;
            case "float":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.FLOAT);
                } else {
                    ps.setFloat(columnIndex, Float.parseFloat(value));
                }
                break;
            case "real":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.REAL);
                } else {
                    ps.setDouble(columnIndex, Double.parseDouble(value));
                }
                break;
            case "sysname":
            case "uniqueidentifier":
            case "text":
            case "ntext":
            case "char":
            case "nchar":
            case "varchar":
            case "nvarchar":
            case "xml":
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
