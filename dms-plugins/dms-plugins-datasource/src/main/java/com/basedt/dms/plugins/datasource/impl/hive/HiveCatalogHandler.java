package com.basedt.dms.plugins.datasource.impl.hive;

import com.basedt.dms.plugins.datasource.dto.TypeInfoDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcCatalogHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    public Map<String, TypeInfoDTO> listDataType() throws SQLException {
        Map<String, TypeInfoDTO> map = super.listDataType();
        map.entrySet().removeIf((entry) ->
                entry.getKey().equalsIgnoreCase("void") ||
                        entry.getKey().equalsIgnoreCase("interval_day_time") ||
                        entry.getKey().equalsIgnoreCase("interval_year_month"));
        return map;
    }
}
