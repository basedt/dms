package com.basedt.dms.plugins.datasource.impl.polardb;

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
public class PolardbMysqlPluginImpl extends MysqlPluginImpl {

    public PolardbMysqlPluginImpl() {
        init();
    }

    public PolardbMysqlPluginImpl(Properties props) {
        super(props);
        init();
    }

    public PolardbMysqlPluginImpl(String dataSourceName,String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(dataSourceName,hostName, port, databaseName, userName, password, attributes);
        init();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.POLARDB_MYSQL.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("com.mysql.cj.jdbc.Driver");
    }
}
