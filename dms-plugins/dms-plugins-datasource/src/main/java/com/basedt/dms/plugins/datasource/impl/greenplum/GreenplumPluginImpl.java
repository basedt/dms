package com.basedt.dms.plugins.datasource.impl.greenplum;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.MaterializedViewHandler;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.postgre.PostgrePluginImpl;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-greenplum/7/greenplum-database/ref_guide-system_catalogs-catalog_ref.html
 */
@AutoService(DataSourcePlugin.class)
public class GreenplumPluginImpl extends PostgrePluginImpl {

    public GreenplumPluginImpl() {
        init();
    }

    public GreenplumPluginImpl(Properties props) {
        super(props);
        init();
    }

    public GreenplumPluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.GREENPLUM.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("org.postgresql.Driver");
    }

    @Override
    public MaterializedViewHandler getMaterializedViewHandler() {
        GreenplumMaterializedViewHandler handler = new GreenplumMaterializedViewHandler();
        handler.initialize(getDataSource(), new HashMap<>());
        return handler;
    }

    public static final String DISTRIBUTED_SQL = "select " +
            "  case when tt.distributed_key is not null then concat('DISTRIBUTED BY (',tt.distributed_key,')')" +
            "       when tt.distributed_key is null and tt.policytype = 'p' then 'DISTRIBUTED RANDOMLY'" +
            "       when tt.distributed_key is null and tt.policytype = 'r' then 'DISTRIBUTED REPLICATED'" +
            "  end as distributed_ddl " +
            " from " +
            " (" +
            "  select " +
            "    n.nspname," +
            "    c.relname," +
            "    d.policytype," +
            "    string_agg(t.attname,',' order by t.attnum) as distributed_key" +
            "  from pg_namespace n" +
            "  join pg_class c" +
            "  on n.oid = c.relnamespace" +
            "  left join gp_distribution_policy d " +
            "  on c.oid = d.localoid" +
            "  left join pg_attribute t " +
            "  on c.oid = t.attrelid" +
            "  and t.attnum = any(d.distkey) " +
            "  where n.nspname = ?" +
            "  and c.relname = ?" +
            "  group by n.nspname,c.relname,d.policytype" +
            " ) tt";
}
