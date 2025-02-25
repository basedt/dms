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
public class OracleDataSourcePluginImpl extends AbstractDataSourcePlugin {

    public OracleDataSourcePluginImpl() {
        init();
    }

    public OracleDataSourcePluginImpl(Properties props) {
        super(props);
        init();
    }

    public OracleDataSourcePluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
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

    public List<CatalogDTO> listCatalogs() throws SQLException {
        CatalogDTO catalog = new CatalogDTO();
        catalog.setCatalogName(getDatabaseName());
        return new ArrayList<CatalogDTO>() {{
            add(catalog);
        }};
    }

    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        List<SchemaDTO> schemas = new ArrayList<>();
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement("select username" +
                " from all_users" +
                " where username not in ('SYS'," +
                "                       'AUDSYS', 'SYSTEM', 'SYSBACKUP', 'SYSDG', 'SYSKM', 'SYSRAC', 'OUTLN', 'REMOTE_SCHEDULER_AGENT'," +
                "                       'XS$NULL', 'GSMADMIN_INTERNAL', 'GSMUSER', 'DIP', 'DBSFWUSER', 'ORACLE_OCM', 'SYS$UMF', 'DBSNMP'," +
                "                       'APPQOSSYS', 'GSMCATUSER', 'GGSYS', 'XDB', 'ANONYMOUS', 'OLAPSYS', 'WMSYS', 'OJVMSYS', 'CTXSYS'," +
                "                       'ORDSYS', 'ORDDATA', 'ORDPLUGINS', 'SI_INFORMTN_SCHEMA', 'MDSYS', 'DVSYS', 'MDDATA', 'LBACSYS'," +
                "                       'DVF'" +
                "    )");
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            SchemaDTO schema = new SchemaDTO();
            schema.setSchemaName(rs.getString("username"));
            schemas.add(schema);
        }
        JdbcUtil.close(conn, pstm, rs);
        return schemas;
    }

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return super.listTables(
                StrUtil.isNotEmpty(catalog) ? catalog.toUpperCase() : null,
                StrUtil.isNotEmpty(schemaPattern) ? schemaPattern.toUpperCase() : null,
                StrUtil.isNotEmpty(tablePattern) ? tablePattern.toUpperCase() : null);
    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        List<TableDTO> tableList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    o.object_type as  object_type," +
                "    c.comments as remark," +
                "    d.total_data_bytes + i.total_index_bytes as data_bytes," +
                "    nvl(p.num_rows,t.num_rows) as table_rows," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_tables t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.table_name" +
                " left join all_tab_comments c" +
                " on t.table_name = c.table_name" +
                " and t.owner = c.owner" +
                " left join" +
                "    (" +
                "        select" +
                "            s.owner," +
                "            s.segment_name," +
                "            sum(nvl(s.bytes,0) + nvl(b.lob_data_bytes,0)) as total_data_bytes" +
                "        from dba_segments s" +
                "        left join (select" +
                "                       b.owner," +
                "                       b.table_name," +
                "                       sum(t.bytes) as lob_data_bytes" +
                "                    from dba_lobs b" +
                "                    join dba_segments t" +
                "                    on b.owner = t.owner" +
                "                    and  t.segment_name = b.segment_name" +
                "                    group by b.owner, b.table_name" +
                "                    ) b" +
                "        on s.owner = b.owner" +
                "        and s.segment_name = b.table_name" +
                "        where s.segment_type in ('TABLE', 'TABLE PARTITION', 'TABLE SUBPARTITION')" +
                "        group by s.owner, s.segment_name" +
                "    ) d" +
                " on t.table_name = d.segment_name" +
                " and t.owner = d.owner" +
                " left join" +
                "    (" +
                "        select" +
                "            t1.table_owner as owner," +
                "            t1.table_name," +
                "            sum(t2.bytes) as total_index_bytes" +
                "        from" +
                "            (" +
                "                select" +
                "                    t.table_owner," +
                "                    t.table_name," +
                "                    t.index_name" +
                "                from all_indexes t" +
                "                union" +
                "                select" +
                "                    t.owner," +
                "                    t.table_name," +
                "                    t.index_name" +
                "                from dba_lobs t" +
                "            ) t1" +
                "        join dba_segments t2" +
                "        on t1.table_owner = t2.owner" +
                "        and t1.index_name = t2.segment_name" +
                "      group by t1.table_owner, t1.table_name) i" +
                " on t.table_name = i.table_name" +
                " and t.owner = i.owner" +
                " left join" +
                "     (" +
                "         select" +
                "             table_owner as owner," +
                "             table_name," +
                "             nvl(sum(num_rows), 0) as num_rows" +
                "         from all_tab_partitions o" +
                "         group by table_owner, table_name) p" +
                " on t.table_name = p.table_name" +
                " and t.owner = p.owner" +
                " where o.owner in (?)";
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and o.object_name like '%" + tablePattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            TableDTO tableDTO = new TableDTO();
            tableDTO.setCatalogName(rs.getString("catalog_name"));
            tableDTO.setSchemaName(rs.getString("schema_name"));
            tableDTO.setObjectName(rs.getString("object_name"));
            tableDTO.setObjectType(rs.getString("object_type"));
            tableDTO.setRemark(rs.getString("remark"));
            tableDTO.setDataBytes(rs.getLong("data_bytes"));
            tableDTO.setTableRows(rs.getLong("table_rows"));
            tableDTO.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            tableDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            tableList.add(tableDTO);
        }
        JdbcUtil.close(conn, pstm, rs);
        return tableList;
    }

    @Override
    public List<ViewDTO> listViews(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        return super.listViews(StrUtil.isNotEmpty(catalog) ? catalog.toUpperCase() : null,
                StrUtil.isNotEmpty(schemaPattern) ? schemaPattern.toUpperCase() : null,
                StrUtil.isNotEmpty(viewPattern) ? viewPattern.toUpperCase() : null);
    }

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        List<ViewDTO> viewList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    t.owner as schema_name," +
                "    t.view_name as object_name," +
                "    'VIEW' as object_type," +
                "    c.comments as remark," +
                "    t.text as query_sql," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_views t" +
                " left join all_tab_comments c on" +
                " t.owner = c.owner" +
                " and t.view_name = c.table_name" +
                " left join all_objects o" +
                " on t.owner = o.owner" +
                " and t.view_name = o.object_name" +
                " where t.owner = ?";
        if (StrUtil.isNotEmpty(viewPattern)) {
            sql += " and t.view_name like '%" + viewPattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ViewDTO view = new ViewDTO();
            view.setCatalogName(rs.getString("catalog_name"));
            view.setSchemaName(rs.getString("schema_name"));
            view.setObjectName(rs.getString("object_name"));
            view.setObjectType(rs.getString("object_type"));
            view.setRemark(rs.getString("remark"));
            view.setQuerySql(rs.getString("query_sql"));
            view.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            view.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            viewList.add(view);
        }
        JdbcUtil.close(conn, pstm, rs);
        return viewList;
    }

    @Override
    public List<TableDTO> listForeignTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        List<TableDTO> tableList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    t.owner as schema_name," +
                "    t.object_name as object_name," +
                "    'FOREIGN_TABLE' as object_type," +
                "    c.comments as remark," +
                "    t.created as create_time," +
                "    t.last_ddl_time as last_ddl_time" +
                " from all_objects t" +
                " join all_external_tables et" +
                " on t.owner = et.owner" +
                " left join all_tab_comments c" +
                " on t.owner = c.owner" +
                " and t.object_name = c.table_name" +
                " where t.owner = ?";
        if (StrUtil.isNotEmpty(tablePattern)) {
            sql += " and t.object_name like '%" + tablePattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            TableDTO tableDTO = new TableDTO();
            tableDTO.setCatalogName(rs.getString("catalog_name"));
            tableDTO.setSchemaName(rs.getString("schema_name"));
            tableDTO.setObjectName(rs.getString("object_name"));
            tableDTO.setObjectType(rs.getString("object_type"));
            tableDTO.setRemark(rs.getString("remark"));
            tableDTO.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            tableDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            tableList.add(tableDTO);
        }
        JdbcUtil.close(conn, pstm, rs);
        return tableList;
    }

    @Override
    public List<IndexDTO> listIndexes(String catalog, String schemaPattern, String tableName) throws SQLException {
        return listIndexDetails(catalog, schemaPattern, tableName);
    }

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        List<IndexDTO> indexList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    i.owner as schema_name," +
                "    i.index_name as object_name," +
                "    'INDEX' as object_type," +
                "    i.table_name as table_name," +
                "    i.index_type as index_type," +
                "    decode(i.uniqueness,'UNIQUE',1,'NONUNIQUE',0,0) as is_uniqueness," +
                "    d.bytes as index_bytes," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_indexes i" +
                " on o.owner = i.owner" +
                " and o.object_name = i.index_name" +
                " and o.object_type = 'INDEX'" +
                " left join dba_segments d" +
                " on o.owner = d.owner" +
                " and o.object_name = d.segment_name " +
                " where o.owner = ?";
        if (StrUtil.isNotEmpty(tableName)) {
            sql += " and o.object_name like '%" + tableName.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            IndexDTO index = new IndexDTO();
            index.setCatalogName(rs.getString("catalog_name"));
            index.setSchemaName(rs.getString("schema_name"));
            index.setObjectName(rs.getString("object_name"));
            index.setObjectType(rs.getString("object_type"));
            index.setTableName(rs.getString("table_name"));
            index.setIndexBytes(rs.getLong("index_bytes"));
            index.setIsUniqueness(rs.getBoolean("is_uniqueness"));
            index.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            index.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            indexList.add(index);
        }
        JdbcUtil.close(conn, pstm, rs);
        return indexList;
    }

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> viewList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    'MATERIALIZED_VIEW' as object_type," +
                "    t.mview_name as table_name," +
                "    t.query as query_sql," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_mviews t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.mview_name" +
                " where o.owner = ? ";
        if (StrUtil.isNotEmpty(mViewPattern)) {
            sql += " and o.object_name like '%" + mViewPattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            MaterializedViewDTO matView = new MaterializedViewDTO();
            matView.setCatalogName(rs.getString("catalog_name"));
            matView.setSchemaName(rs.getString("schema_name"));
            matView.setObjectName(rs.getString("object_name"));
            matView.setObjectType(rs.getString("object_type"));
            matView.setQuerySql(rs.getString("query_sql"));
            matView.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            matView.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            viewList.add(matView);
        }
        JdbcUtil.close(conn, pstm, rs);
        return viewList;
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> viewList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    'MATERIALIZED_VIEW' as object_type," +
                "    t.mview_name as table_name," +
                "    t.query as query_sql," +
                "    d.bytes as data_bytes," +
                "    c.comments as remark," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_mviews t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.mview_name" +
                " left join dba_segments d" +
                " on o.owner = d.owner" +
                " and o.object_name = d.segment_name" +
                " left join all_tab_comments c" +
                " on t.owner = c.owner" +
                " and t.mview_name = c.table_name " +
                " where o.owner = ? ";
        if (StrUtil.isNotEmpty(mViewPattern)) {
            sql += " and o.object_name like '%" + mViewPattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            MaterializedViewDTO matView = new MaterializedViewDTO();
            matView.setCatalogName(rs.getString("catalog_name"));
            matView.setSchemaName(rs.getString("schema_name"));
            matView.setObjectName(rs.getString("object_name"));
            matView.setObjectType(rs.getString("object_type"));
            matView.setRemark(rs.getString("remark"));
            matView.setQuerySql(rs.getString("query_sql"));
            matView.setDataBytes(rs.getLong("data_bytes"));
            matView.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            matView.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            viewList.add(matView);
        }
        JdbcUtil.close(conn, pstm, rs);
        return viewList;
    }

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        List<SequenceDTO> sequenceList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    'SEQUENCE' as object_type," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_sequences s" +
                " on o.owner = s.sequence_owner" +
                " and o.object_name = s.sequence_name" +
                " where o.owner = ? ";
        if (StrUtil.isNotEmpty(sequencePattern)) {
            sql += " and o.object_name like '%" + sequencePattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            SequenceDTO sequence = new SequenceDTO();
            sequence.setCatalogName(rs.getString("catalog_name"));
            sequence.setSchemaName(rs.getString("schema_name"));
            sequence.setObjectName(rs.getString("object_name"));
            sequence.setObjectType(rs.getString("object_type"));
            sequence.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            sequence.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            sequenceList.add(sequence);
        }
        JdbcUtil.close(conn, pstm, rs);
        return sequenceList;
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        List<SequenceDTO> sequenceList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    'SEQUENCE' as object_type," +
                "    s.min_value as start_value," +
                "    s.min_value as min_value," +
                "    s.max_value as max_value," +
                "    s.increment_by as increment_by," +
                "    s.cycle_flag as is_cycle," +
                "    s.last_number as last_value," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_sequences s" +
                " on o.owner = s.sequence_owner" +
                " and o.object_name = s.sequence_name" +
                " where o.owner = ? ";
        if (StrUtil.isNotEmpty(sequencePattern)) {
            sql += " and o.object_name like '%" + sequencePattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            SequenceDTO sequence = new SequenceDTO();
            sequence.setCatalogName(rs.getString("catalog_name"));
            sequence.setSchemaName(rs.getString("schema_name"));
            sequence.setObjectName(rs.getString("object_name"));
            sequence.setObjectType(rs.getString("object_type"));
            sequence.setStartValue(rs.getLong("start_value"));
            sequence.setMinValue(rs.getLong("min_value"));
            try {
                sequence.setMaxValue(rs.getLong("max_value"));
            } catch (Exception e) {
                sequence.setMaxValue(Long.MAX_VALUE);
            }
            sequence.setIncrementBy(rs.getLong("increment_by"));
            sequence.setIsCycle(rs.getBoolean("is_cycle"));
            sequence.setLastValue(rs.getLong("last_value"));
            sequence.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            sequence.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            sequenceList.add(sequence);
        }
        JdbcUtil.close(conn, pstm, rs);
        return sequenceList;
    }

    @Override
    public List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> functionList = new ArrayList<>();
        String sql = "select" +
                "     null as catalog_name," +
                "     o.owner as schema_name," +
                "     o.object_name as object_name," +
                "     'FUNCTION' as object_type" +
                " from all_objects o" +
                " join all_procedures s" +
                " on o.owner = s.owner" +
                " and o.object_name = s.object_name" +
                " where o.owner = ? " +
                " and o.object_type = 'FUNCTION'";
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and o.object_name like '%" + functionPattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            FunctionDTO function = new FunctionDTO();
            function.setCatalogName(rs.getString("catalog_name"));
            function.setSchemaName(rs.getString("schema_name"));
            function.setObjectName(rs.getString("object_name"));
            function.setObjectType(rs.getString("object_type"));
            functionList.add(function);
        }
        JdbcUtil.close(conn, pstm, rs);
        return functionList;
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> functionList = new ArrayList<>();
        String sql = "select" +
                "     null as catalog_name," +
                "     o.owner as schema_name," +
                "     o.object_name as object_name," +
                "     'FUNCTION' as object_type," +
                "     source.source_code as source_code," +
                "     o.created as create_time," +
                "     o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join all_procedures s" +
                " on o.owner = s.owner" +
                " and o.object_name = s.procedure_name" +
                " left join" +
                "     (" +
                "         select" +
                "             owner," +
                "             name," +
                "             xmlagg(xmlelement(e,text) order by line).extract('//text()')  source_code" +
                "         from all_source group by owner,name" +
                "     ) source" +
                " on o.owner = source.owner" +
                " and o.object_name = source.name" +
                " where o.owner = ? " +
                " and o.object_type = 'FUNCTION'";
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += " and o.object_name like '%" + functionPattern.toUpperCase() + "%'";
        }
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            FunctionDTO function = new FunctionDTO();
            function.setCatalogName(rs.getString("catalog_name"));
            function.setSchemaName(rs.getString("schema_name"));
            function.setObjectName(rs.getString("object_name"));
            function.setObjectType(rs.getString("object_type"));
            function.setSourceCode(rs.getString("source_code"));
            function.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            function.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            functionList.add(function);
        }
        JdbcUtil.close(conn, pstm, rs);
        return functionList;
    }

    @Override
    public List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return this.getConstraint(catalog, schemaPattern, tableName, "P");
    }

    @Override
    public List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return this.getConstraint(catalog, schemaPattern, tableName, "F");
    }

    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        List<ColumnDTO> columnList = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    t.owner as schema_name," +
                "    t.table_name as table_name," +
                "    t.column_name as column_name," +
                "    t.data_type as data_type," +
                "    t.data_length as data_length," +
                "    t.data_precision as data_precision," +
                "    t.data_scale as data_scale," +
                "    decode(t.nullable,'Y',1,'N',0,0) as is_nullable," +
                "    t.data_default as default_value," +
                "    t.column_id as column_ordinal," +
                "    c.comments as remark" +
                " from all_tab_columns t" +
                " left join all_tab_comments c" +
                " on t.owner = c.owner" +
                " and t.table_name = c.table_name" +
                " and t.column_name = c.table_name" +
                " where t.owner = ? " +
                " and t.table_name = ? ";
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        pstm.setString(2, tableName.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ColumnDTO column = new ColumnDTO();
            column.setCatalogName(rs.getString("catalog_name"));
            column.setSchemaName(rs.getString("schema_name"));
            column.setTableName(rs.getString("table_name"));
            column.setColumnName(rs.getString("column_name"));
            column.setDataType(rs.getString("data_type"));
            column.setDataLength(rs.getInt("data_length"));
            column.setDataPrecision(rs.getInt("data_precision"));
            column.setDataScale(rs.getInt("data_scale"));
            column.setDefaultValue(rs.getString("default_value"));
            column.setColumnOrdinal(rs.getInt("column_ordinal"));
            column.setRemark(rs.getString("remark"));
            column.setIsNullable(rs.getBoolean("is_nullable"));
            columnList.add(column);
        }
        JdbcUtil.close(conn, pstm, rs);
        return columnList;
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
                Long dValue = DateTimeUtil.toTimeStamp(value);
                ps.setDate(columnIndex, Objects.isNull(dValue) ? null : new Date(dValue));
                break;
            case "TIMESTAMP":
            case "TIMESTAMP WITH TIME ZONE":
                Long tValue = DateTimeUtil.toTimeStamp(value);
                ps.setTimestamp(columnIndex, Objects.isNull(tValue) ? null : new Timestamp(tValue));
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

    /**
     * https://docs.oracle.com/en/database/oracle/oracle-database/21/refrn/ALL_CONSTRAINTS.html
     *
     * @param catalog
     * @param schemaPattern
     * @param tableName
     * @param type
     * @return
     * @throws SQLException
     */
    private List<ObjectDTO> getConstraint(String catalog, String schemaPattern, String tableName, String type) throws SQLException {
        List<ObjectDTO> list = new ArrayList<>();
        String sql = "select" +
                "    null as catalog_name," +
                "    o.owner as schema_name," +
                "    o.object_name as object_name," +
                "    o.object_type as object_type," +
                "    o.created as create_time," +
                "    o.last_ddl_time as last_ddl_time" +
                " from all_objects o" +
                " join  all_constraints t" +
                " on o.owner = t.owner" +
                " and o.object_name = t.constraint_name" +
                " where o.owner = ?" +
                " and t.table_name = ? " +
                " and t.constraint_type = ? ";
        ;
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, schemaPattern.toUpperCase());
        pstm.setString(2, tableName.toUpperCase());
        pstm.setString(3, type.toUpperCase());
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            ObjectDTO object = new ObjectDTO();
            object.setCatalogName(rs.getString("catalog_name"));
            object.setSchemaName(rs.getString("schema_name"));
            object.setObjectName(rs.getString("object_name"));
            if ("P".equals(type)) {
                object.setObjectType(PK.name());
            } else if ("F".equals(type)) {
                object.setObjectType(FK.name());
            } else {
                object.setObjectType(rs.getString("object_type"));
            }
            object.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            object.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            list.add(object);
        }
        JdbcUtil.close(conn, pstm, rs);
        return list;
    }
}
