package com.basedt.dms.plugins.datasource.impl.mssql;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.plugins.datasource.dto.SequenceDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcSequenceHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class MssqlSequenceHandler extends JdbcSequenceHandler {

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        String sql = "select" +
                "    db_name() as catalog_name," +
                "    s.name as schema_name," +
                "    'SEQUENCE' as object_type," +
                "    o.name as object_name," +
                "    seq.start_value as start_value," +
                "    seq.minimum_value as min_value," +
                "    seq.maximum_value as max_value," +
                "    seq.increment as increment_by," +
                "    seq.is_cycling as is_cycle," +
                "    seq.cache_size as cache_size," +
                "    seq.last_used_value as last_value," +
                "    o.create_date as create_time," +
                "    o.modify_date as last_ddl_time" +
                " from sys.all_objects o" +
                " join sys.schemas s" +
                " on o.schema_id = s.schema_id" +
                " join sys.sequences seq" +
                " on o.object_id = seq.object_id" +
                " and o.schema_id = seq.schema_id" +
                " where 1=1 ";
        if (StrUtil.isNotEmpty(schemaPattern)) {
            sql += " and s.name = '" + schemaPattern + "'";
        }
        if (StrUtil.isNotEmpty(sequencePattern)) {
            sql += " and o.name = '" + sequencePattern + "'";
        }
        return super.listSequenceFromDB(sql);
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return listSequences(catalog, schemaPattern, sequencePattern);
    }

    @Override
    protected String generateRenameSQL(String schema, String sequenceName, String newName) {
        return StrUtil.format("exec sp_rename '{}.{}',{},'OBJECT'", schema, sequenceName, newName);
    }

    @Override
    public String getSequenceDDL(String catalog, String schema, String sequenceName) throws SQLException {
        StringBuilder builder = new StringBuilder();
        SequenceDTO sequence = getSequenceDetail(catalog, schema, sequenceName);
        if (Objects.nonNull(sequence)) {
            builder.append("CREATE SEQUENCE ")
                    .append(sequence.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(sequence.getSequenceName())
                    .append("\n START WITH ")
                    .append(sequence.getStartValue())
                    .append("\n INCREMENT BY ")
                    .append(sequence.getIncrementBy())
                    .append("\n MINVALUE ")
                    .append(sequence.getMinValue())
                    .append("\n MAXVALUE ")
                    .append(sequence.getMaxValue())
                    .append(sequence.getIsCycle() ? "\n CYCLE" : "\n NO CYCLE")
                    .append(Objects.nonNull(sequence.getCacheSize()) && sequence.getCacheSize() > 0L ? "\n CACHE " + sequence.getCacheSize() : "\n NO CACHE")
                    .append(";");
            return builder.toString();
        }
        return "";
    }
}
