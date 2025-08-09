package com.basedt.dms.plugins.datasource.impl.clickhouse;

import com.basedt.dms.plugins.datasource.dto.CatalogDTO;
import com.basedt.dms.plugins.datasource.dto.SchemaDTO;
import com.basedt.dms.plugins.datasource.dto.TypeInfoDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcCatalogHandler;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

public class ClickHouseCatalogHandler extends JdbcCatalogHandler {

    @Override
    public List<CatalogDTO> listCatalogs() throws SQLException {
        CatalogDTO catalog = new CatalogDTO();
        catalog.setCatalogName(this.databaseName);
        return new ArrayList<CatalogDTO>() {{
            add(catalog);
        }};
    }

    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        List<SchemaDTO> resultList = new ArrayList<>();
        String sql = "select name from system.databases where name not in ('system','information_schema','INFORMATION_SCHEMA')";
        Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String name = rs.getString("name");
            resultList.add(new SchemaDTO(name));
        }
        JdbcUtil.close(connection, ps, rs);
        return resultList;
    }

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(MATERIALIZED_VIEW.name());
            add(FOREIGN_TABLE.name());
            add(FUNCTION.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public Map<String, TypeInfoDTO> listDataType() throws SQLException {
        Map<String, TypeInfoDTO> map = new HashMap<>();
        Connection conn = dataSource.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTypeInfo();
        while (rs.next()) {
            TypeInfoDTO typeInfo = new TypeInfoDTO();
            typeInfo.setTypeName(rs.getString("TYPE_NAME"));
            typeInfo.setDataType(rs.getInt("DATA_TYPE"));
            typeInfo.setPrecision(rs.getInt("PRECISION"));
            typeInfo.setLocalTypeName(rs.getString("LOCAL_TYPE_NAME"));
            typeInfo.setAutoIncrement(rs.getBoolean("AUTO_INCREMENT"));
            map.put(typeInfo.getTypeName(), typeInfo);
        }
        JdbcUtil.close(conn, rs);
        return map;
    }
}
