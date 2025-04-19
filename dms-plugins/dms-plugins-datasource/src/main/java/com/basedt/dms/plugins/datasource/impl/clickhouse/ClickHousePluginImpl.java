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
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.AbstractDataSourcePlugin;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import com.google.auto.service.AutoService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

@AutoService(DataSourcePlugin.class)
public class ClickHousePluginImpl extends AbstractDataSourcePlugin {

    public ClickHousePluginImpl() {
        init();
    }

    public ClickHousePluginImpl(Properties props) {
        super(props);
        init();
    }

    public ClickHousePluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.CLICKHOUSE.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
    }

    @Override
    public List<CatalogDTO> listCatalogs() {
        CatalogDTO catalog = new CatalogDTO();
        catalog.setCatalogName(getDatabaseName());
        return new ArrayList<CatalogDTO>() {{
            add(catalog);
        }};
    }

    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        List<SchemaDTO> resultList = new ArrayList<>();
//        String sql = "select name from system.databases where name not in ('system','information_schema','INFORMATION_SCHEMA')";
        String sql = "select name from system.databases ";
        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String name = rs.getString("name");
            resultList.add(new SchemaDTO(name));
        }
        JdbcUtil.close(connection, ps, rs);
        return resultList;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:clickhouse://" + getHostName() + ":" + getPort() + "/" + getDatabaseName() + formatJdbcProps();
    }

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return listTableDetails(catalog, schemaPattern, tablePattern, TABLE);
    }

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        return listTableDetails(catalog, schemaPattern, tablePattern, type);
    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        String sql = " select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o.name as object_name," +
                "    'TABLE' as object_type," +
                "    p.rows as table_rows," +
                "    p.size as data_bytes," +
                "    ep.value as remark," +
                "    o.create_date as create_time," +
                "    o.modify_date as last_ddl_time," +
                "    ius.last_user_scan as last_access_time" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " left join sys.dm_db_index_usage_stats ius" +
                " on o.object_id = ius.object_id" +
                " and ius.index_id in (0,1)" +
                " left join sys.extended_properties ep" +
                " on o.object_id = ep.major_id" +
                " and ep.minor_id = 0" +
                " left join (select s.object_id,sum(s.rows) as rows,sum(a.total_pages) * 8 as size from sys.partitions s inner join sys.allocation_units a on s.partition_id = a.container_id where index_id < 2 group by object_id) p " +
                " on o.object_id = p.object_id" +
                " where o.type in ('U','S','IT')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and o.name like '%" + tablePattern + "%'";
        }
        return super.listTableDetails(sql);
    }

    @Override
    protected void setColumnValue(PreparedStatement ps, ColumnDTO column, String value, int columnIndex) throws SQLException, ParseException {

    }

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        return List.of();
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
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
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
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(MATERIALIZED_VIEW.name());
            add(FOREIGN_TABLE.name());
            add(FUNCTION.name());
            add(INDEX.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}
