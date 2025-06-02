package com.basedt.dms.plugins.datasource.impl.gaussdb;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.MaterializedViewDTO;
import com.basedt.dms.plugins.datasource.impl.postgre.PostgreMaterializedViewHandler;
import com.basedt.dms.plugins.datasource.impl.postgre.PostgreObjectTypeMapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.MATERIALIZED_VIEW;

public class GaussdbMaterializedViewHandler extends PostgreMaterializedViewHandler {

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
                " join pg_catalog.gs_matviews v " +
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
            sql += " and c.relname = '" + mViewPattern + "'";
        }
        return super.listMViewFromDB(sql);
    }

}
