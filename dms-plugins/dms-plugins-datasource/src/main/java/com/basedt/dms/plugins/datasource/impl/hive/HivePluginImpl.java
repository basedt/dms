package com.basedt.dms.plugins.datasource.impl.hive;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.AbstractDataSourcePlugin;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.google.auto.service.AutoService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@AutoService(DataSourcePlugin.class)
public class HivePluginImpl extends AbstractDataSourcePlugin {

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

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.APACHEHIVE.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("org.apache.hive.jdbc.HiveDriver");
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:hive2://" + getHostName() + Constants.SEPARATOR_COLON + getPort() + "/" + getDatabaseName();
    }

    @Override
    protected void setColumnValue(PreparedStatement ps, ColumnDTO column, String value, int columnIndex) throws SQLException, ParseException {

    }

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        return List.of();
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
}
