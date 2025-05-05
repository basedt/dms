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
import com.basedt.dms.plugins.datasource.AbstractDataSourcePlugin;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.google.auto.service.AutoService;
import lombok.SneakyThrows;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

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

    @SneakyThrows
    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        List<TableDTO> result = new ArrayList<>();
        HiveMetaStoreClient client = getHmsClient();
        List<String> tables = client.getAllTables(null, schemaPattern);
        List<Table> tableList = client.getTableObjectsByName(null, schemaPattern, tables);
        for (Table table : tableList) {
            if ("MANAGED_TABLE".equals(table.getTableType()) || "EXTERNAL_TABLE".equals(table.getTableType())) {
                TableDTO tableDTO = new TableDTO();
                tableDTO.setCatalogName(catalog);
                tableDTO.setSchemaName(table.getDbName());
                tableDTO.setObjectName(table.getTableName());
                tableDTO.setObjectType(TABLE.name());
                tableDTO.setCreateTime(DateTimeUtil.toLocalDateTime(table.getCreateTime()));
                tableDTO.setLastAccessTime(DateTimeUtil.toLocalDateTime(table.getLastAccessTime()));
                Map<String, String> params = table.getParameters();
                tableDTO.setRemark(params.get("comment"));
                tableDTO.setTableRows(Long.parseLong(Objects.isNull(params.get("numRows")) ? "0" : params.get("numRows")));
                tableDTO.setDataBytes(Long.parseLong(Objects.isNull(params.get("totalSize")) ? "0" : params.get("totalSize")));
                String lastDdlTime = params.get("transient_lastDdlTime");
                tableDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(Integer.parseInt(Objects.isNull(lastDdlTime) ? "0" : lastDdlTime)));
                if (Objects.isNull(tablePattern) || StrUtil.contains(tableDTO.getTableName(), tablePattern)) {
                    result.add(tableDTO);
                }
            }
        }
        client.close();
        return result;
    }

    @SneakyThrows
    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        List<ViewDTO> result = new ArrayList<>();
        HiveMetaStoreClient client = getHmsClient();
        List<String> tables = client.getAllTables(null, schemaPattern);
        List<Table> tableList = client.getTableObjectsByName(null, schemaPattern, tables);
        for (Table table : tableList) {
            if ("VIRTUAL_VIEW".equals(table.getTableType())) {
                ViewDTO viewDTO = new ViewDTO();
                viewDTO.setCatalogName(catalog);
                viewDTO.setSchemaName(table.getDbName());
                viewDTO.setObjectName(table.getTableName());
                viewDTO.setObjectType(VIEW.name());
                viewDTO.setCreateTime(DateTimeUtil.toLocalDateTime(table.getCreateTime()));
                viewDTO.setQuerySql(table.getViewOriginalText());
                Map<String, String> params = table.getParameters();
                viewDTO.setRemark(params.get("comment"));
                String lastDdlTime = params.get("transient_lastDdlTime");
                viewDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(Integer.parseInt(Objects.isNull(lastDdlTime) ? "0" : lastDdlTime)));
                if (Objects.isNull(viewPattern) || StrUtil.contains(viewDTO.getViewName(), viewPattern)) {
                    result.add(viewDTO);
                }
            }
        }
        client.close();
        return result;
    }

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> result = new ArrayList<>();
        List<TableDTO> tables = super.listTables(catalog, schemaPattern, mViewPattern, MATERIALIZED_VIEW);
        for (TableDTO table : tables) {
            MaterializedViewDTO mvDTO = new MaterializedViewDTO();
            mvDTO.setCatalogName(catalog);
            mvDTO.setSchemaName(table.getSchemaName());
            mvDTO.setObjectName(table.getObjectName());
            mvDTO.setObjectType(MATERIALIZED_VIEW.name());
            result.add(mvDTO);
        }
        return result;
    }

    @SneakyThrows
    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> result = new ArrayList<>();
        HiveMetaStoreClient client = getHmsClient();
        List<String> tables = client.getAllTables(null, schemaPattern);
        List<Table> tableList = client.getTableObjectsByName(null, schemaPattern, tables);
        for (Table table : tableList) {
            if ("MATERIALIZED_VIEW".equals(table.getTableType())) {
                MaterializedViewDTO mvDTO = new MaterializedViewDTO();
                mvDTO.setCatalogName(catalog);
                mvDTO.setSchemaName(table.getDbName());
                mvDTO.setObjectName(table.getTableName());
                mvDTO.setObjectType(MATERIALIZED_VIEW.name());
                mvDTO.setCreateTime(DateTimeUtil.toLocalDateTime(table.getCreateTime()));
                mvDTO.setQuerySql(table.getViewOriginalText());
                Map<String, String> params = table.getParameters();
                mvDTO.setRemark(params.get("comment"));
                mvDTO.setDataBytes(Long.parseLong(Objects.isNull(params.get("totalSize")) ? "0" : params.get("totalSize")));
                String lastDdlTime = params.get("transient_lastDdlTime");
                mvDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(Integer.parseInt(Objects.isNull(lastDdlTime) ? "0" : lastDdlTime)));
                if (Objects.isNull(mViewPattern) || StrUtil.contains(mvDTO.getMViewName(), mViewPattern)) {
                    result.add(mvDTO);
                }
            }
        }
        client.close();
        return result;
    }

    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
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

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(MATERIALIZED_VIEW.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    private HiveMetaStoreClient getHmsClient() throws MetaException {
        String uris = this.attributes.get(METASTORE_URIS);
        Configuration conf = new Configuration();
        conf.set("hive.metastore.uris", uris);
        HiveMetaStoreClient client = new HiveMetaStoreClient(conf);
        return client;
    }

}
