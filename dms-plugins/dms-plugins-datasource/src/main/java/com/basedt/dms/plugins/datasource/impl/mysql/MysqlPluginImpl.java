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

import cn.hutool.core.collection.CollectionUtil;
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
import java.sql.Date;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

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
    public List<CatalogDTO> listCatalogs() throws SQLException {
        CatalogDTO catalog = new CatalogDTO();
        catalog.setCatalogName(getDatabaseName());
        return new ArrayList<CatalogDTO>() {{
            add(catalog);
        }};
    }

    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        List<CatalogDTO> schemaList = listCatalogs();
        List<SchemaDTO> resultList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(schemaList)) {
            for (CatalogDTO catalogDTO : schemaList) {
                if (!catalogDTO.getCatalogName().equalsIgnoreCase("performance_schema") &&
                        !catalogDTO.getCatalogName().equalsIgnoreCase("information_schema") &&
                        !catalogDTO.getCatalogName().equalsIgnoreCase("mysql")
                ) {
                    resultList.add(new SchemaDTO(catalogDTO.getCatalogName()));
                }
            }
        }
        return resultList;
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
        String sql = "select " +
                " null as catalog_name," +
                " t.table_schema as schema_name," +
                " t.table_name as object_name," +
                " case when t.table_type = 'SYSTEM VIEW' then 'VIEW' when t.table_type = 'BASE TABLE' then 'TABLE' else t.table_type end as object_type," +
                " t.table_rows as table_rows," +
                " t.data_length as data_bytes," +
                " t.table_comment as remark," +
                " t.create_time as create_time, " +
                " t.create_time as last_ddl_time," +
                " t.update_time as last_access_time" +
                " from information_schema.tables t " +
                " where t.table_type = 'BASE TABLE'";
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
                " null as catalog_name," +
                " v.table_schema as schema_name," +
                " v.table_name as object_name," +
                " 'VIEW' as object_type," +
                " t.table_comment as remark," +
                " t.create_time as create_time," +
                " t.update_time as last_ddl_time," +
                " v.view_definition as query_sql" +
                " from information_schema.views v" +
                " join information_schema.tables t" +
                " on v.table_schema = t.table_schema " +
                " and v.table_name = t.table_name " +
                " where t.table_type not in ('BASE TABLE')";
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
        return List.of();
    }

    @Override
    public List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException {
        return listIndexDetails(catalog, schemaPattern, tableName);
    }

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " t.index_schema as schema_name," +
                " t.index_name as object_name," +
                " 'INDEX' as object_type," +
                " t.index_type as index_type," +
                " t.table_name as table_name, " +
                " case when max(t.non_unique) >=1 then 0 else 1 end as is_uniqueness," +
                " group_concat(t.column_name order by t.seq_in_index) as columns,"+
                " null as index_bytes," +
                " null as create_time," +
                " null as last_ddl_time" +
                " from information_schema.statistics t" +
                " where 1 = 1";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.index_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and t.table_name = '" + tableName + "'";
        }
        sql += " group by t.index_schema,t.index_name,t.index_type,t.table_name";
        return super.listIndexDetails(sql);
    }

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return null;
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return null;
    }

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return null;
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return null;
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " r.routine_schema as schema_name," +
                " r.routine_name as object_name," +
                " 'FUNCTION' as object_type," +
                " r.routine_definition as source_code," +
                " r.created as create_time," +
                " r.last_altered as last_ddl_time," +
                " r.routine_comment as remark" +
                " from information_schema.routines r" +
                " where 1=1 ";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and r.routine_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and r.routine_name like '%" + functionPattern + "%'";
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
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " t.table_schema as schema_name," +
                " t.table_name as table_name," +
                " t.column_name  as column_name," +
                " t.data_type as data_type," +
                " t.character_maximum_length as data_length," +
                " t.numeric_precision as data_precision," +
                " t.numeric_scale as data_scale," +
                " t.column_default as default_value," +
                " t.ordinal_position as column_ordinal," +
                " t.column_comment as remark," +
                " case when t.is_nullable = 'YES' then 1 else 0 end as is_nullable" +
                " from information_schema.columns t" +
                " where 1=1 ";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and t.table_name = '" + tableName + "'";
        }
        return super.listColumnDetails(sql);
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

    private List<ObjectDTO> getConstraint(String catalog, String schemaPattern, String tableName, DbObjectType type) throws SQLException {
        List<ObjectDTO> constraints = new ArrayList<>();
        String constraintType = "";
        if (PK.equals(type)) {
            constraintType = "PRIMARY KEY";
        } else if (FK.equals(type)) {
            constraintType = "FOREIGN KEY";
        }
        String sql = "select " +
                " null as catalog_name," +
                " t.constraint_schema as schema_name," +
                " t.constraint_name as object_name," +
                " t.table_name as table_name," +
                " t.constraint_type " +
                " from information_schema.table_constraints t" +
                " where t.constraint_type = '" + constraintType + "'";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.constraint_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and t.table_name = '" + tableName + "'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ObjectDTO obj = new ObjectDTO();
            obj.setCatalogName(rs.getString("catalog_name"));
            obj.setSchemaName(rs.getString("schema_name"));
            obj.setObjectName(rs.getString("object_name"));
            obj.setObjectType(type.name());
            constraints.add(obj);
        }
        JdbcUtil.close(conn, pstm, rs);
        return constraints;
    }

    @Override
    public String renameTable(String catalog, String schemaPattern, String tableName, String newTableName) {
        String originName = schemaPattern + Constants.SEPARATOR_DOT + tableName;
        return StrUtil.format("rename table {} to {}", originName, newTableName);
    }
}
