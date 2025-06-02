package com.basedt.dms.plugins.datasource.impl.doris;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.dto.MaterializedViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcMaterializedViewHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.MATERIALIZED_VIEW;

public class DorisMaterializedViewHandler extends JdbcMaterializedViewHandler {

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return super.listMViews(catalog, schemaPattern, mViewPattern);
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> mViewList = new ArrayList<>();
        String sql = "show alter table materialized view from " + schemaPattern;
        Connection conn = dataSource.getConnection();
        if (StrUtil.isNotEmpty(mViewPattern)) {
            sql += " where tablename = " + mViewPattern;
        }
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            MaterializedViewDTO mView = new MaterializedViewDTO();
            mView.setCatalogName(catalog);
            mView.setSchemaName(schemaPattern);
            mView.setObjectName(rs.getString("TableName"));
            mView.setObjectType(MATERIALIZED_VIEW.name());
            mView.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("CreateTime")));
            mViewList.add(mView);
        }
        JdbcUtil.close(conn, st, rs);
        return mViewList;
    }
}
