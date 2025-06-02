package com.basedt.dms.plugins.datasource.impl.doris;

import com.basedt.dms.plugins.datasource.dto.SchemaDTO;
import com.basedt.dms.plugins.datasource.impl.mysql.MysqlCatalogHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

public class DorisCatalogHandler extends MysqlCatalogHandler {

    @Override
    public List<SchemaDTO> listSchemas(String catalog, String schemaPattern) throws SQLException {
        return super.listSchemas(catalog, schemaPattern).stream().filter(s -> {
            if ("__internal_schema".equalsIgnoreCase(s.getSchemaName())) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> listObjectTypes() throws SQLException {
        List<String> objectTypes = super.listObjectTypes();
        objectTypes.add(FOREIGN_TABLE.name());
        objectTypes.add(MATERIALIZED_VIEW.name());
        return objectTypes.stream().filter(s -> {
            if (INDEX.name().equalsIgnoreCase(s)) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());
    }
}
