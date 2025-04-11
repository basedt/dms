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
package com.basedt.dms.plugins.datasource.impl.postgre;

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
public class PostgreDataSourcePluginImpl extends AbstractDataSourcePlugin {

    public PostgreDataSourcePluginImpl() {
        init();
    }

    public PostgreDataSourcePluginImpl(Properties props) {
        super(props);
        init();
    }

    public PostgreDataSourcePluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:postgresql://" + getHostName() + ":" + getPort() + "/" + getDatabaseName() + formatJdbcProps();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.POSTGRESQL.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("org.postgresql.Driver");
    }

    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        List<SchemaDTO> list = super.listSchemas(catalog, schemaPattern);
        return list.stream().filter(s -> {
            if ("pg_catalog".equalsIgnoreCase(s.getSchemaName()) ||
                    "information_schema".equalsIgnoreCase(s.getSchemaName())) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        String sql = "select " +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " case c.relkind when 'r' then 'TABLE' when 'i' then 'INDEX' when 'v' then 'VIEW' when 'S' then 'SEQUENCE'" +
                " when 'f' then 'FOREIGN_TABLE' when 'm' then 'MATERIALIZED_VIEW' else null end as object_type," +
                " d.description as remark," +
                " pg_table_size(concat_ws('.',n.nspname, c.relname)) as data_bytes," +
                " c.reltuples as table_rows," +
                " null as create_time," +
                " null as last_ddl_time," +
                " null as last_access_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c " +
                " on n.oid = c.relnamespace  " +
                " left join pg_catalog.pg_description d " +
                " on c.oid  = d.objoid " +
                " and d.objsubid = 0" +
                " and d.classoid  = 'pg_class'::regclass" +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(type) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and c.relname like '%" + tablePattern + "%'";
        }
        return super.listTableDetails(sql);
    }

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        String sql = " select " +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " 'VIEW'as object_type," +
                " d.description as remark," +
                " v.definition as query_sql," +
                " null as create_time," +
                " null as last_ddl_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c " +
                " on n.oid = c.relnamespace  " +
                " join pg_catalog.pg_views v " +
                " on n.nspname = v.schemaname " +
                " and c.relname = v.viewname " +
                " left join pg_catalog.pg_description d " +
                " on c.oid  = d.objoid " +
                " and d.objsubid = 0" +
                " and d.classoid  = 'pg_class'::regclass" +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(VIEW) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(viewPattern)) {
            sql += " and c.relname like '%" + viewPattern + "%'";
        }
        return super.listViewDetails(sql);
    }

    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return listTableDetails(catalog, schemaPattern, tablePattern, DbObjectType.FOREIGN_TABLE);
    }

    @Override
    public List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException {
        return listIndexDetails(catalog, schemaPattern, tableName);
    }

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        String sql = " select" +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " 'INDEX'as object_type," +
                " i.tablename as table_name," +
                " pg_relation_size(pgi.indexrelid::regclass) as index_bytes," +
                " pgi.indisunique as is_uniqueness," +
                " null as index_type," +
                " null as create_time," +
                " null as last_ddl_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c" +
                " on n.oid = c.relnamespace" +
                " join pg_catalog.pg_indexes i" +
                " on n.nspname = i.schemaname" +
                " and c.relname = i.indexname" +
                " join pg_catalog.pg_index pgi" +
                " on c.oid = pgi.indexrelid" +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(INDEX) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and i.tablename = '" + tableName + "'";
        }
        return super.listIndexDetails(sql);
    }

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return listMViewDetails(catalog, schemaPattern, mViewPattern);
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> viewList = new ArrayList<>();
        String sql = " select " +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " 'MATERIALIZED_VIEW'as object_type," +
                " d.description as remark," +
                " v.definition as query_sql," +
                " pg_table_size(concat_ws('.',n.nspname, c.relname)) as data_bytes," +
                " null as create_time," +
                " null as last_ddl_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c " +
                " on n.oid = c.relnamespace  " +
                " join pg_catalog.pg_matviews v " +
                " on n.nspname = v.schemaname " +
                " and c.relname = v.matviewname " +
                " left join pg_catalog.pg_description d " +
                " on c.oid  = d.objoid " +
                " and d.objsubid = 0" +
                " and d.classoid  = 'pg_class'::regclass" +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(MATERIALIZED_VIEW) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(mViewPattern)) {
            sql += " and c.relname like '%" + mViewPattern + "%'";
        }
        return super.listMViewDetails(sql);
    }

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return listSequenceDetails(catalog, schemaPattern, sequencePattern);
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        String sql = " select " +
                " null as catalog_name," +
                " n.nspname as schema_name," +
                " c.relname as object_name," +
                " 'SEQUENCE'as object_type," +
                " s.start_value," +
                " s.min_value," +
                " s.max_value," +
                " s.increment_by," +
                " s.cycle as is_cycle," +
                " s.last_value," +
                " null as create_time," +
                " null as last_ddl_time" +
                " from pg_catalog.pg_namespace n" +
                " join pg_catalog.pg_class c " +
                " on n.oid = c.relnamespace  " +
                " join pg_catalog.pg_sequences s" +
                " on n.nspname = s.schemaname " +
                " and c.relname = s.sequencename " +
                " where c.relkind in ('" + PostgreObjectTypeMapper.mapToOrigin(SEQUENCE) + "')";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and n.nspname = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(sequencePattern)) {
            sql += " and c.relname like '%" + sequencePattern + "%'";
        }
        return super.listSequenceDetails(sql);
    }

    @Override
    public List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        return listFunctionDetails(catalog, schemaPattern, functionPattern);
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        String sql = "select" +
                "    catalog_name," +
                "    schema_name," +
                "    function_name as object_name," +
                "    object_type," +
                "    source_code," +
                "    null as create_time," +
                "    null as last_ddl_time" +
                " from" +
                "    (" +
                "        select" +
                "            null as catalog_name," +
                "            n.nspname as schema_name," +
                "            p.proname as function_name," +
                "            'FUNCTION' as object_type," +
                "            p.prosrc as source_code," +
                "            row_number() over (partition by n.nspname,p.proname order by p.oid) as rn" +
                "        from pg_catalog.pg_namespace n" +
                "            join pg_catalog.pg_proc p" +
                "            on p.pronamespace = n.oid" +
                "        where p.prokind = 'f'" +
                "    ) tt" +
                " where tt.rn = 1 ";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and tt.schema_name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and tt.function_name like '%" + functionPattern + "%'";
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
        String sql = "select" +
                " t.table_catalog as catalog_name," +
                " t.table_schema as schema_name," +
                " t.table_name as table_name," +
                " t.column_name as column_name," +
                " t.data_type as data_type," +
                " t.character_maximum_length as data_length," +
                " t.numeric_precision as data_precision," +
                " t.numeric_scale as data_scale," +
                " t.column_default as default_value," +
                " t.ordinal_position as column_ordinal," +
                " col_description(attr.attrelid, attr.attnum) as remark," +
                " t.is_nullable as is_nullable" +
                " from pg_catalog.pg_class c" +
                " join pg_catalog.pg_namespace n" +
                " on n.oid = c.relnamespace" +
                " join pg_catalog.pg_attribute attr" +
                " on attr.attrelid = c.oid" +
                " join information_schema.columns t" +
                " on n.nspname = t.table_schema" +
                " and c.relname = t.table_name" +
                " and attr.attname = t.column_name" +
                " where c.relkind in ('r','v')" +
                " and attr.attnum > 0";
        if (StrUtil.isNotEmpty(catalog)) {
            sql += " and t.table_catalog ='" + catalog + "'";
        }
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
            add(MATERIALIZED_VIEW.name());
            add(FOREIGN_TABLE.name());
            add(FUNCTION.name());
            add(INDEX.name());
            add(SEQUENCE.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public Boolean isSupportRowEdit() {
        return true;
    }

    private List<ObjectDTO> getConstraint(String catalog, String schemaPattern, String tableName, DbObjectType type) throws SQLException {
        List<ObjectDTO> pkList = new ArrayList<>();
        String constraintType = "";
        if (PK.equals(type)) {
            constraintType = "PRIMARY KEY";
        } else if (FK.equals(type)) {
            constraintType = "FOREIGN KEY";
        }
        String sql = "select" +
                "    t.table_catalog," +
                "    t.table_schema," +
                "    t.table_name," +
                "    t.constraint_name," +
                "    t.constraint_type" +
                " from information_schema.table_constraints t" +
                " where t.constraint_type = '" + constraintType + "'";
        if (StrUtil.isNotEmpty(catalog)) {
            sql += " and t.table_catalog ='" + catalog + "'";
        }
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and t.table_schema = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and t.table_name = '" + tableName + "'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ObjectDTO obj = new ObjectDTO();
            obj.setCatalogName(rs.getString("table_catalog"));
            obj.setSchemaName(rs.getString("table_schema"));
            obj.setObjectName(rs.getString("constraint_name"));
            obj.setObjectType(type.name());
            pkList.add(obj);
        }
        JdbcUtil.close(conn, pstm, rs);
        return pkList;
    }


    @Override
    protected void setColumnValue(PreparedStatement ps, ColumnDTO column, String value, int columnIndex) throws SQLException, ParseException {
        switch (column.getDataType()) {
            case "smallint":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.SMALLINT);
                } else {
                    ps.setShort(columnIndex, Short.parseShort(value));
                }
                break;
            case "integer":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.INTEGER);
                } else {
                    ps.setInt(columnIndex, Integer.parseInt(value));
                }
                break;
            case "bigint":
            case "oid":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.BIGINT);
                } else {
                    ps.setLong(columnIndex, Long.parseLong(value));
                }
                break;
            case "name":
            case "inet":
            case "varchar":
            case "text":
            case "\"char\"":
            case "character varying":
                ps.setString(columnIndex, value);
                break;
            case "boolean":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.BOOLEAN);
                } else {
                    ps.setBoolean(columnIndex, Boolean.parseBoolean(value));
                }
                break;
            case "numeric":
                ps.setBigDecimal(columnIndex, StrUtil.isBlank(value) ? null : BigDecimal.valueOf(Double.parseDouble(value)));
                break;
            case "timestamp with time zone":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.TIMESTAMP);
                } else {
                    Long tValue = DateTimeUtil.toTimeStamp(value);
                    ps.setTimestamp(columnIndex, Objects.isNull(tValue) ? null : new Timestamp(tValue));
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
            case "double precision":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.DOUBLE);
                } else {
                    ps.setDouble(columnIndex, Double.parseDouble(value));
                }
                break;
            case "real":
                if (StrUtil.isBlank(value)) {
                    ps.setNull(columnIndex, Types.REAL);
                } else {
                    ps.setFloat(columnIndex, Float.parseFloat(value));
                }
                break;
            default:
                ps.setString(columnIndex, value);
        }
    }

}
