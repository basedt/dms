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
import com.basedt.dms.plugins.datasource.AbstractDataSourcePlugin;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.ViewHandler;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import com.google.auto.service.AutoService;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

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
    public ViewHandler getViewHandler() {
        MssqlViewHandler handler = new MssqlViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    @Override
    public List<CatalogDTO> listCatalogs() throws SQLException {
        CatalogDTO catalogDTO = new CatalogDTO(getDatabaseName());
        return new ArrayList<CatalogDTO>() {{
            add(catalogDTO);
        }};
    }

    @Override
    public Boolean isSupportRowEdit() {
        return true;
    }

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(FUNCTION.name());
            add(INDEX.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
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
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o.name as table_name," +
                "    c.name as column_name," +
                "    t1.name as data_type," +
                "    c.max_length as data_length," +
                "    c.precision as data_precision," +
                "    c.scale as data_scale," +
                "    stuff(stuff(dc.definition, 1, 1, ''), len(dc.definition) - 1, 1, '') as default_value," +
                "    c.column_id as column_ordinal," +
                "    ep.value as remark," +
                "    c.is_nullable as is_nullable" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " join sys.all_columns c" +
                " on o.object_id = c.object_id" +
                " left join sys.extended_properties ep" +
                " on c.object_id = ep.major_id" +
                " and c.column_id = ep.minor_id" +
                " and ep.major_id >0" +
                " left join sys.types t1" +
                " on c.user_type_id = t1.user_type_id" +
                " left join sys.default_constraints dc" +
                " on c.default_object_id = dc.object_id " +
                " where o.type in ('U','S','IT','V')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and o.name = '" + tableName + "'";
        }
        return super.listColumnDetails(sql);
    }

    @Override
    public List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException {
        return listIndexDetails(catalog, schemaPattern, tableName);
    }

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    i.name as object_name," +
                "    'INDEX' as object_type," +
                "    i.type_desc as index_type," +
                "    i.is_unique as is_uniqueness," +
                "    o.name as table_name," +
                "    ic.columns as columns," +
                "    null as index_bytes," +
                "    null as create_time," +
                "    null as last_ddl_time" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " join sys.indexes i" +
                " on o.object_id = i.object_id" +
                " join (select string_agg(c.name,',') within group (order by ic.key_ordinal) as columns,ic.object_id,ic.index_id from sys.index_columns ic join sys.columns c on ic.object_id = c.object_id and ic.column_id = c.column_id group by ic.object_id,ic.index_id) ic" +
                " on ic.index_id = i.index_id and ic.object_id = i.object_id" +
                " where i.name is not null";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and o.name = '" + tableName + "'";
        }
        return super.listIndexDetails(sql);
    }

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return listMViewDetails(catalog, schemaPattern, mViewPattern);
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return listSequenceDetails(catalog, schemaPattern, sequencePattern);
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    'SEQUENCE' as object_type," +
                "    o.name as object_name," +
                "    seq.start_value as start_value," +
                "    seq.minimum_value as min_value," +
                "    seq.maximum_value as max_value," +
                "    seq.increment as increment_by," +
                "    seq.is_cycling as is_cycle," +
                "    seq.last_used_value as last_value," +
                "    o.create_date as create_time," +
                "    o.modify_date as last_ddl_time" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " join sys.sequences seq" +
                " on o.object_id = seq.object_id" +
                " and o.schema_id = seq.schema_id" +
                " where 1=1 ";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(sequencePattern)) {
            sql += " and o.name like '%" + sequencePattern + "%'";
        }
        return super.listSequenceDetails(sql);
    }

    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        return listFunctionDetails(catalog, schemaPattern, functionPattern);
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o.name as object_name," +
                "    'FUNCTION' as object_type," +
                "    o.create_date as create_time," +
                "    o.modify_date as last_ddl_time," +
                "    m.definition as source_code" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " left join sys.all_sql_modules m" +
                " on o.object_id = m.object_id" +
                " where o.type in ('FN','IF','FS','AF','TF')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and o.name like '%" + functionPattern + "%'";
        }
        return super.listFunctionDetails(sql);
    }

    @Override
    public List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return getConstraint(catalog, schemaPattern, tableName, PK);
    }

    @Override
    public List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return getConstraint(catalog, schemaPattern, tableName, FK);
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

    private List<ObjectDTO> getConstraint(String catalog, String schemaPattern, String tableName, DbObjectType type) throws SQLException {
        List<ObjectDTO> list = new ArrayList<>();
        String constraintType = "";
        if (PK.equals(type)) {
            constraintType = "PK";
        } else if (FK.equals(type)) {
            constraintType = "F";
        }
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    o1.name as constraint_name," +
                "    o1.type as constraint_type," +
                "    o2.name as table_name," +
                "    o1.create_date as create_time," +
                "    o1.modify_date as last_ddl_time" +
                " from sys.sysconstraints c" +
                " left join sys.all_objects o1" +
                " on c.constid = o1.object_id" +
                " left join sys.schemas s" +
                " on o1.schema_id = s.schema_id" +
                " left join sys.all_objects o2" +
                " on c.id = o2.object_id" +
                " where o1.type = '" + constraintType + "'";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and o2.name = '" + tableName + "'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ObjectDTO obj = new ObjectDTO();
            obj.setCatalogName(rs.getString("catalog_name"));
            obj.setSchemaName(rs.getString("schema_name"));
            obj.setObjectName(rs.getString("constraint_name"));
            obj.setObjectType(type.name());
            obj.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            obj.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            list.add(obj);
        }
        JdbcUtil.close(conn, pstm, rs);
        return list;
    }
}
