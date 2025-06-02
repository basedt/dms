package com.basedt.dms.plugins.datasource.impl.doris;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.datasource.dto.IndexDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcIndexHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DorisIndexHandler extends JdbcIndexHandler {

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        List<IndexDTO> indexList = new ArrayList<>();
        String sql = "show index from " + StrUtil.concat(true, schemaPattern, Constants.SEPARATOR_DOT, tableName);
        Connection conn = dataSource.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            IndexDTO index = new IndexDTO();
            index.setCatalogName(catalog);
            index.setSchemaName(schemaPattern);
            index.setTableName(tableName);
            index.setObjectName(rs.getString("key_name"));
            index.setIndexType(rs.getString("index_type"));
            index.setColumns(rs.getString("column_name"));
            index.setIsUniqueness(false);
            indexList.add(index);
        }
        JdbcUtil.close(conn, st, rs);
        return indexList;
    }
}
