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
package com.basedt.dms.plugins.datasource;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.PropertiesUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.dto.ColumnDTO;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowStreamReader;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractDataSourcePlugin implements DataSourcePlugin {

    protected PluginInfo pluginInfo;

    protected String dataSourceName;

    protected String hostName;

    protected Integer port;

    protected String driverClassName;

    protected String databaseName;

    protected String userName;

    protected String password;

    protected Map<String, String> attributes;

    public AbstractDataSourcePlugin() {
    }

    public AbstractDataSourcePlugin(String dataSourceName, String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        setDataSourceName(dataSourceName);
        setHostName(hostName);
        setPort(port);
        setDatabaseName(databaseName);
        setUserName(userName);
        setPassword(password);
        setAttributes(attributes);
    }

    public AbstractDataSourcePlugin(Properties props) {
        setDataSourceName((String) props.get("dataSourceName"));
        setHostName((String) props.get("hostName"));
        setPort((Integer) props.get("port"));
        setDatabaseName((String) props.get("databaseName"));
        setUserName((String) props.get("userName"));
        setPassword((String) props.get("password"));
        Map<String, String> attrs = JSONUtil.toBean((String) props.get("attrs"),
                new TypeReference<Map<String, String>>() {
                }.getType(), true);
        setAttributes(attrs);
    }

    @Override
    public String getDataSourceName() {
        return this.dataSourceName;
    }

    @Override
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    @Override
    public PluginInfo getPluginInfo() {
        return this.pluginInfo;
    }

    @Override
    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Override
    public String getPluginName() {
        return getPluginInfo().getPluginName();
    }

    @Override
    public PluginType getPluginType() {
        return getPluginInfo().getPluginType();
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getDriverClassName() {
        return driverClassName;
    }

    @Override
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Boolean testConnection() throws Exception {
        Connection conn = JdbcUtil.getConnection(getJdbcUrl(), getDriverClassName(), getUserName(), getPassword(), getJdbcProps());
        return conn != null;
    }

    @Override
    public DataSource getDataSource() {
        return JdbcUtil.getDataSource(getDataSourceName(), getJdbcUrl(), getDriverClassName(), getUserName(), getPassword(), getJdbcProps());
    }

    protected abstract String getJdbcUrl();

    protected Properties getJdbcProps() {
        if (CollectionUtil.isEmpty(this.attributes) || !this.attributes.containsKey(JDBC)) {
            return null;
        }
        Properties props = new Properties();
        String attrs = this.attributes.get(JDBC);
        Map<String, Object> map = PropertiesUtil.formatToMap(attrs, Constants.LINE_FEED, Constants.SEPARATOR_EQUAL);
        map.forEach((k, v) -> {
            props.put(k, (String) v);
        });
        return props;
    }

    protected String formatJdbcProps() {
        Properties props = getJdbcProps();
        StringBuilder builder = new StringBuilder("?");
        if (props != null) {
            props.forEach((k, v) -> {
                builder.append(k).append(Constants.SEPARATOR_EQUAL).append(v).append(Constants.SEPARATOR_AMP);
            });
            return builder.substring(0, builder.length() - 1);
        } else {
            return "";
        }
    }

    @Override
    public Boolean isSupportRowEdit() {
        return false;
    }

    @Override
    public void execute(String sql) throws SQLException {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.execute();
        }
    }

    protected String generateInsertSql(List<String> columns, String schemaName, String tableName) {
        String sql = "insert into " + StrUtil.concat(true, schemaName, Constants.SEPARATOR_DOT, tableName);
        StringBuilder cols = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (String column : columns) {
            cols.append(column).append(",");
            values.append(" ?,");
        }
        return StrUtil.concat(true,
                sql,
                cols.substring(0, cols.length() - 1),
                ") values ",
                values.substring(0, values.length() - 1),
                ")");
    }

    @Override
    public void insertBatch(ArrowStreamReader reader, String catalog, String schemaName, String tableName) throws SQLException, IOException {
        VectorSchemaRoot root = reader.getVectorSchemaRoot();
        Schema schema = root.getSchema();
        List<String> dataCols = schema.getFields().stream().map(Field::getName).toList();
        List<ColumnDTO> tableCols = this.getTableHandler().listColumnsByTable(catalog, schemaName, tableName);
        List<ColumnDTO> columns = tableCols.stream().filter(column -> {
            for (String dataCol : dataCols) {
                if (column.getColumnName().equals(dataCol)) {
                    return true;
                }
            }
            return false;
        }).toList();
        String sql = generateInsertSql(columns.stream().map(ColumnDTO::getColumnName).collect(Collectors.toList()), schemaName, tableName);
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            while (reader.loadNextBatch()) {
                for (int rowNum = 0; rowNum < root.getRowCount(); rowNum++) {
                    int columnIndex = 1;
                    for (ColumnDTO column : columns) {
                        ValueVector vector = root.getVector(column.getColumnName());
                        byte[] strByte = ((VarCharVector) vector).get(rowNum);
                        String value = new String(strByte);
                        setColumnValue(ps, column, value, columnIndex);
                        columnIndex++;
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            }
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    protected abstract void setColumnValue(PreparedStatement ps, ColumnDTO column, String value, int columnIndex) throws SQLException, ParseException;

}
