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
import com.basedt.dms.plugins.datasource.AbstractDataSourcePlugin;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import com.google.auto.service.AutoService;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;
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
        String sql = "select name from system.databases where name not in ('system','information_schema','INFORMATION_SCHEMA')";
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
                "    t.table_catalog as catalog_name," +
                "    t.table_schema as schema_name," +
                "    t.table_name as object_name," +
                "    case when t.table_type = 'SYSTEM VIEW' then 'VIEW' when t.table_type='BASE TABLE' then 'TABLE' else t.table_type end as object_type," +
                "    t.table_rows as table_rows," +
                "    t.data_length as data_bytes," +
                "    t.table_comment as remark," +
                "    st.metadata_modification_time as create_time," +
                "    st.metadata_modification_time as last_ddl_time," +
                "    toDateTime('1970-01-01 00:00:00') as last_access_time" +
                " from information_schema.tables t" +
                " left join system.tables st" +
                " on t.table_schema = st.database" +
                " and t.table_name = st.name" +
                " where t.table_type in ('BASE TABLE')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and t.table_name like '%" + tablePattern + "%'";
        }
        return super.listTableDetails(sql);
    }

    @Override
    public List<ViewDTO> listViews(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        return listViewDetails(catalog, schemaPattern, viewPattern);
    }

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        String sql = "select" +
                "    v.table_catalog as catalog_name," +
                "    v.table_schema as schema_name," +
                "    v.table_name as object_name," +
                "    'VIEW' as object_type," +
                "    t.table_comment as remark," +
                "    st.metadata_modification_time as create_time," +
                "    st.metadata_modification_time as last_ddl_time," +
                "    v.view_definition as query_sql" +
                " from information_schema.views v" +
                " left join information_schema.tables t" +
                " on v.table_catalog = t.table_catalog" +
                " and v.table_schema = t.table_schema" +
                " and v.table_name = t.table_name" +
                " left join system.tables st" +
                " on v.table_schema = st.database" +
                " and v.table_name = st.name" +
                " where st.engine <> 'MaterializedView'";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and v.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(viewPattern)) {
            sql += " and v.table_name like '%" + viewPattern + "%'";
        }
        return super.listViewDetails(sql);
    }

    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        String sql = " select" +
                "    t.table_catalog as catalog_name," +
                "    t.table_schema as schema_name," +
                "    t.table_name as object_name," +
                "    'FOREIGN_TABLE' as object_type," +
                "    t.table_comment as remark," +
                "    st.metadata_modification_time as create_time," +
                "    st.metadata_modification_time as last_ddl_time" +
                " from information_schema.tables t" +
                " left join system.tables st" +
                " on t.table_schema = st.database" +
                " and t.table_name = st.name" +
                " where t.table_type in ('FOREIGN TABLE')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and t.table_name like '%" + tablePattern + "%'";
        }
        return super.listForeignTables(sql);
    }

    @Override
    public List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException {
        return listIndexDetails(catalog, schemaPattern, tableName);
    }

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return listMViewDetails(catalog, schemaPattern, mViewPattern);
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        String sql = "select" +
                "    t.table_catalog as catalog_name," +
                "    t.table_schema as schema_name," +
                "    t.table_name as object_name," +
                "    'MATERIALIZED_VIEW'  as object_type," +
                "    t.table_comment as remark," +
                "    st.create_table_query as query_sql," +
                "    t.data_length as data_bytes," +
                "    st.metadata_modification_time as create_time," +
                "    st.metadata_modification_time as last_ddl_time" +
                " from information_schema.tables t" +
                " left join system.tables st" +
                " on t.table_schema = st.database" +
                " and t.table_name = st.name" +
                " where st.engine = 'MaterializedView'";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(mViewPattern)) {
            sql += " and t.table_name like '%" + mViewPattern + "%'";
        }
        return super.listMViewDetails(sql);
    }

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return listSequenceDetails(catalog, schemaPattern, sequencePattern);
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select" + catalog +
                "     as catalog_name," + schemaPattern +
                "     as schema_name," +
                "    t.name as object_name," +
                "    'FUNCTION' as object_type," +
                "    '' as  source_code," +
                "    toDateTime('1970-01-01 00:00:00') as create_time," +
                "    toDateTime('1970-01-01 00:00:00') as last_ddl_time," +
                "    t.description as remark" +
                " from system.functions t " +
                " where 1=1 ";
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and t.name like '%" + functionPattern + "%'";
        }
        return super.listFunctionDetails(sql);
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

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(MATERIALIZED_VIEW.name());
            add(FOREIGN_TABLE.name());
            add(FUNCTION.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}
