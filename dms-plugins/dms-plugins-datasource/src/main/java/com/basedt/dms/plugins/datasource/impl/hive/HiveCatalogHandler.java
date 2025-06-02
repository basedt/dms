package com.basedt.dms.plugins.datasource.impl.hive;

import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcCatalogHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

public class HiveCatalogHandler extends JdbcCatalogHandler {

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(MATERIALIZED_VIEW.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}
