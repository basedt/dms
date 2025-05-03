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
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
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
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.TABLE;
import static com.basedt.dms.plugins.datasource.enums.DbObjectType.VIEW;

@Slf4j
public abstract class AbstractDataSourcePlugin implements DataSourcePlugin {

    protected PluginInfo pluginInfo;

    protected String hostName;

    protected Integer port;

    protected String driverClassName;

    protected String databaseName;

    protected String userName;

    protected String password;

    protected Map<String, String> attributes;

    public AbstractDataSourcePlugin() {
    }

    public AbstractDataSourcePlugin(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        setHostName(hostName);
        setPort(port);
        setDatabaseName(databaseName);
        setUserName(userName);
        setPassword(password);
        setAttributes(attributes);
    }

    public AbstractDataSourcePlugin(Properties props) {
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
    public Boolean testConnection() throws SQLException, ClassNotFoundException {
        Connection conn = JdbcUtil.getConnection(getJdbcUrl(), getDriverClassName(), getUserName(), getPassword(), getJdbcProps());
        return conn != null;
    }

    @Override
    public Connection getConnection() {
        return JdbcUtil.getConnectionSilently(getJdbcUrl(), getDriverClassName(), getUserName(), getPassword(), getAttributes());
    }

    @Override
    public DataSource getDataSource() {
        return JdbcUtil.getDataSource(getJdbcUrl(), getDriverClassName(), getUserName(), getPassword(), getAttributes());
    }

    protected abstract String getJdbcUrl();

    protected Properties getJdbcProps() {
        if (this.attributes == null) {
            return null;
        }
        Properties props = new Properties();
        props.putAll(this.attributes);
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

    /**
     * get catalog list
     *
     * @return catalog list
     * @throws SQLException
     */
    @Override
    public List<CatalogDTO> listCatalogs() throws SQLException {
        Connection conn = getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getCatalogs();
        List<CatalogDTO> result = new ArrayList<>();
        while (rs.next()) {
            CatalogDTO catalog = new CatalogDTO(rs.getString("TABLE_CAT"));
            result.add(catalog);
        }
        JdbcUtil.close(conn, rs);
        if (CollectionUtil.isEmpty(result)) {
            result.add(new CatalogDTO(getDatabaseName()));
        }
        return result;
    }

    /**
     * get schemas with catalog and schema pattern
     *
     * @param catalog       database catalog
     * @param schemaPattern filter schema with pattern
     * @return schema list
     * @throws SQLException
     */
    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        Connection conn = getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getSchemas(catalog, schemaPattern);
        List<SchemaDTO> result = new ArrayList<>();
        while (rs.next()) {
            SchemaDTO schema = new SchemaDTO(rs.getString("TABLE_SCHEM"));
            result.add(schema);
        }
        JdbcUtil.close(conn, rs);
        return result;
    }

    /**
     * get object types
     *
     * @return object type list
     * @throws SQLException
     */
    @Override
    public List<String> listObjectTypes() throws SQLException {
        Connection conn = getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTableTypes();
        List<String> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getString("TABLE_TYPE"));
        }
        JdbcUtil.close(conn, rs);
        return result;
    }

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern) throws SQLException {
        return listTables(catalog, schemaPattern, tablePattern, TABLE);
    }

    @Override
    public List<TableDTO> listTables(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        List<ObjectDTO> list = this.listTables(catalog, schemaPattern, tablePattern, new String[]{type.name()});
        List<TableDTO> result = new ArrayList<>();
        for (ObjectDTO obj : list) {
            TableDTO table = new TableDTO();
            table.setCatalogName(obj.getCatalogName());
            table.setSchemaName(obj.getSchemaName());
            table.setObjectName(obj.getObjectName());
            table.setObjectType(type.name());
            result.add(table);
        }
        return result;
    }

    /**
     * TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     */
    private List<ObjectDTO> listTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) throws SQLException {
        Connection conn = getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(catalog, schemaPattern, tableNamePattern, types);
        List<ObjectDTO> result = new ArrayList<>();
        while (rs.next()) {
            ObjectDTO table = new ObjectDTO();
            table.setCatalogName(rs.getString("TABLE_CAT"));
            table.setSchemaName(rs.getString("TABLE_SCHEM"));
            table.setObjectName(rs.getString("TABLE_NAME"));
            table.setObjectType(rs.getString("TABLE_TYPE"));
            result.add(table);
        }
        JdbcUtil.close(conn, rs);
        return result;
    }

    protected List<TableDTO> listTableDetails(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<TableDTO> result = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
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
            tableDTO.setLastAccessTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_access_time")));
            result.add(tableDTO);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }

    protected List<ViewDTO> listViewDetails(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<ViewDTO> result = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
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
            result.add(view);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }

    protected List<TableDTO> listForeignTables(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<TableDTO> result = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
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
            result.add(tableDTO);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }

    protected List<IndexDTO> listIndexDetails(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<IndexDTO> result = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        while (rs.next()) {
            IndexDTO index = new IndexDTO();
            index.setCatalogName(rs.getString("catalog_name"));
            index.setSchemaName(rs.getString("schema_name"));
            index.setObjectName(rs.getString("object_name"));
            index.setObjectType(rs.getString("object_type"));
            index.setTableName(rs.getString("table_name"));
            index.setIndexBytes(rs.getLong("index_bytes"));
            index.setIndexType(rs.getString("index_type"));
            index.setIsUniqueness(rs.getBoolean("is_uniqueness"));
            index.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            index.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            result.add(index);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }

    protected List<MaterializedViewDTO> listMViewDetails(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<MaterializedViewDTO> result = new ArrayList<>();
        Connection conn = this.getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
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
            result.add(matView);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }

    protected List<SequenceDTO> listSequenceDetails(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<SequenceDTO> result = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
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
            result.add(sequence);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }

    protected List<FunctionDTO> listFunctionDetails(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<FunctionDTO> result = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
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
            result.add(function);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }

    protected List<ColumnDTO> listColumnDetails(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<ColumnDTO> result = new ArrayList<>();
        Connection conn = getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
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
            result.add(column);
        }
        JdbcUtil.close(conn, pstm, rs);
        return result;
    }


    @Override
    public TableDTO getTableDetail(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        List<TableDTO> tables = listTableDetails(catalog, schemaPattern, tablePattern, type);
        if (CollectionUtil.isNotEmpty(tables)) {
            return tables.get(0);
        } else {
            return null;
        }
    }


    @Override
    public List<ViewDTO> listViews(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        List<ObjectDTO> list = this.listTables(catalog, schemaPattern, viewPattern, new String[]{VIEW.name()});
        List<ViewDTO> result = new ArrayList<>();
        for (ObjectDTO obj : list) {
            ViewDTO view = new ViewDTO();
            view.setCatalogName(obj.getCatalogName());
            view.setSchemaName(obj.getSchemaName());
            view.setObjectName(obj.getObjectName());
            view.setObjectType(obj.getObjectType());
            result.add(view);
        }
        return result;
    }

    @Override
    public ViewDTO getViewDetail(String catalog, String schemaPattern, String viewPattern) throws SQLException {
        List<ViewDTO> views = listViewDetails(catalog, schemaPattern, viewPattern);
        if (CollectionUtil.isNotEmpty(views)) {
            return views.get(0);
        } else {
            return null;
        }
    }

    @Override
    public IndexDTO getIndexDetail(String catalog, String schemaPattern, String tableName) throws SQLException {
        List<IndexDTO> indexes = listIndexDetails(catalog, schemaPattern, tableName);
        if (CollectionUtil.isNotEmpty(indexes)) {
            return indexes.get(0);
        } else {
            return null;
        }
    }

    @Override
    public MaterializedViewDTO getMViewDetail(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> mvs = listMViewDetails(catalog, schemaPattern, mViewPattern);
        if (CollectionUtil.isNotEmpty(mvs)) {
            return mvs.get(0);
        } else {
            return null;
        }
    }

    @Override
    public SequenceDTO getSequenceDetail(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        List<SequenceDTO> seqs = listSequenceDetails(catalog, schemaPattern, sequencePattern);
        if (CollectionUtil.isNotEmpty(seqs)) {
            return seqs.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<FunctionDTO> listFunctions(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> funcs = new ArrayList<>();
        Connection conn = getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getFunctions(catalog, schemaPattern, functionPattern);
        while (rs.next()) {
            FunctionDTO function = new FunctionDTO();
            function.setCatalogName(rs.getString("FUNCTION_CAT"));
            function.setSchemaName(rs.getString("FUNCTION_SCHEM"));
            function.setObjectName(rs.getString("FUNCTION_NAME"));
            function.setRemark(rs.getString("REMARKS"));
            function.setObjectType(DbObjectType.FUNCTION.name());
            funcs.add(function);
        }
        JdbcUtil.close(conn, rs);
        return funcs;
    }

    @Override
    public FunctionDTO getFunctionDetail(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> funcs = listFunctionDetails(catalog, schemaPattern, functionPattern);
        if (CollectionUtil.isNotEmpty(funcs)) {
            return funcs.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<ColumnDTO> listColumnsByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        Connection conn = getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getColumns(catalog, schemaPattern, tableName, null);
        List<ColumnDTO> result = new ArrayList<>();
        while (rs.next()) {
            ColumnDTO column = new ColumnDTO();
            column.setCatalogName(rs.getString("TABLE_CAT"));
            column.setSchemaName(rs.getString("TABLE_SCHEM"));
            column.setTableName(rs.getString("TABLE_NAME"));
            column.setColumnName(rs.getString("COLUMN_NAME"));
            column.setDataType(String.valueOf(rs.getInt("DATA_TYPE")));
            column.setDataLength(rs.getInt("COLUMN_SIZE"));
            column.setRemark(rs.getString("REMARKS"));
            column.setDefaultValue(rs.getString("COLUMN_DEF"));
            column.setColumnOrdinal(rs.getInt("ORDINAL_POSITION"));
            column.setIsNullable(formatString2Bool(rs.getString("IS_NULLABLE")));
            result.add(column);
        }
        JdbcUtil.close(conn, rs);
        return result;
    }

    private Boolean formatString2Bool(String str) {
        if ("YES".equalsIgnoreCase(str)) {
            return true;
        } else if ("NO".equalsIgnoreCase(str)) {
            return false;
        } else {
            return null;
        }
    }

    @Override
    public Boolean isSupportRowEdit() {
        return false;
    }

    @Override
    public void execute(String sql) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)
        ) {
            pstm.execute();
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
        List<String> dataCols = schema.getFields().stream().map(Field::getName).collect(Collectors.toList());
        List<ColumnDTO> tableCols = this.listColumnsByTable(catalog, schemaName, tableName);
        List<ColumnDTO> columns = tableCols.stream().filter(column -> {
            for (String dataCol : dataCols) {
                if (column.getColumnName().equals(dataCol)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
        String sql = generateInsertSql(columns.stream().map(ColumnDTO::getColumnName).collect(Collectors.toList()), schemaName, tableName);
        try (Connection conn = getConnection();
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
