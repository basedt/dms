package com.basedt.dms.plugins.datasource.impl.doris;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.dto.FunctionDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcFunctionHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.FUNCTION;

public class DorisFunctionHandler extends JdbcFunctionHandler {

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> functionList = new ArrayList<>();
        String sql = "show global full functions";
        Connection connection = dataSource.getConnection();
        if (StrUtil.isNotEmpty(functionPattern)) {
            sql += StrUtil.concat(true, " like ", functionPattern, "%");
        }
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            FunctionDTO function = new FunctionDTO();
            function.setCatalogName(catalog);
            function.setSchemaName(schemaPattern);
            function.setObjectName(rs.getString("Signature"));
            function.setObjectType(FUNCTION.name());
            functionList.add(function);
        }
        JdbcUtil.close(connection, st, rs);
        return functionList;
    }
}
