package com.basedt.dms.plugins.datasource.impl.clickhouse;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.MaterializedViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcMaterializedViewHandler;

import java.sql.SQLException;
import java.util.List;

public class ClickHouseMaterializedViewHandler extends JdbcMaterializedViewHandler {

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
            sql += " and t.table_name = '" + mViewPattern + "'";
        }
        return super.listMViewFromDB(sql);
    }

    @Override
    public String getMViewDdl(String catalog, String schema, String mViewName) throws SQLException {
        if (StrUtil.isEmpty(mViewName)) {
            return "";
        }
        MaterializedViewDTO mv = getMViewDetail(catalog, schema, mViewName);
        return mv.getQuerySql();
    }

}
