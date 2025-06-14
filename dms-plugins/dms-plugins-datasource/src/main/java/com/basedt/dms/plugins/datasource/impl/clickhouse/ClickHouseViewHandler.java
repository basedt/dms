package com.basedt.dms.plugins.datasource.impl.clickhouse;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.datasource.dto.ViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcViewHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ClickHouseViewHandler extends JdbcViewHandler {

    @Override
    public List<ViewDTO> listViews(String catalog, String schema, String viewName) throws SQLException {
        return listViewDetails(catalog, schema, viewName);
    }

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schema, String viewName) throws SQLException {
        String sql = "select" +
                "    v.table_catalog as catalog_name," +
                "    v.table_schema as schema_name," +
                "    v.table_name as object_name," +
                "    'VIEW' as object_type," +
                "    t.table_comment as remark," +
                "    st.metadata_modification_time as create_time," +
                "    st.metadata_modification_time as last_ddl_time," +
                "    v.view_definition as query_sql" +
                " from information_schema.views v" +
                " left join information_schema.tables t" +
                " on v.table_catalog = t.table_catalog" +
                " and v.table_schema = t.table_schema" +
                " and v.table_name = t.table_name" +
                " left join system.tables st" +
                " on v.table_schema = st.database" +
                " and v.table_name = st.name" +
                " where st.engine <> 'MaterializedView'";
        if (StrUtil.isNotEmpty(schema)) {
            sql += " and v.table_schema = '" + schema + "'";
        }
        if (StrUtil.isNotEmpty(viewName)) {
            sql += " and v.table_name = '" + viewName + "'";
        }
        return super.listViewFromDB(sql);
    }

    @Override
    protected String generateRenameSQL(String schema, String viewName, String newName) {
        return StrUtil.format("RENAME TABLE {}.{} TO {}", schema, viewName, newName);
    }

    @Override
    public String getViewDdl(String catalog, String schema, String viewName) throws SQLException {
        ViewDTO viewInfo = getViewDetail(catalog, schema, viewName);
        if (Objects.nonNull(viewInfo)) {
            StringBuilder ddlBuilder = new StringBuilder();
            ddlBuilder.append("CREATE VIEW ")
                    .append(viewInfo.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(viewInfo.getViewName())
                    .append("\n AS \n");
            if (StrUtil.isNotEmpty(viewInfo.getQuerySql())) {
                ddlBuilder.append(viewInfo.getQuerySql());
            }
            return ddlBuilder.toString();
        } else {
            throw new SQLException(StrUtil.format("view {} does not exist in {}", viewName, schema));
        }
    }
}
