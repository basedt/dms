package com.basedt.dms.plugins.datasource.impl.mssql;

import com.basedt.dms.plugins.datasource.dto.CatalogDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcCatalogHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

public class MssqlCatalogHandler extends JdbcCatalogHandler {

    @Override
    public List<CatalogDTO> listCatalogs() throws SQLException {
        CatalogDTO catalogDTO = new CatalogDTO(this.databaseName);
        return new ArrayList<CatalogDTO>() {{
            add(catalogDTO);
        }};
    }

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> list = new ArrayList<String>() {{
            add(TABLE.name());
            add(VIEW.name());
            add(FUNCTION.name());
            add(SEQUENCE.name());
        }};
        return list.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

}
