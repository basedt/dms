package com.basedt.dms.plugins.datasource.impl.mariadb;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.mysql.MysqlPluginImpl;
import com.google.auto.service.AutoService;

import java.util.Map;
import java.util.Properties;

@AutoService(DataSourcePlugin.class)
public class MariadbPluginImpl extends MysqlPluginImpl {

    public MariadbPluginImpl() {
        super();
        init();
    }

    public MariadbPluginImpl(Properties props) {
        super(props);
        init();
    }

    public MariadbPluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mariadb://" + getHostName() + Constants.SEPARATOR_COLON + getPort() + "/" + getDatabaseName() + formatJdbcProps();
    }

    private void init(){
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.MARIADB.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("org.mariadb.jdbc.Driver");
    }

}
