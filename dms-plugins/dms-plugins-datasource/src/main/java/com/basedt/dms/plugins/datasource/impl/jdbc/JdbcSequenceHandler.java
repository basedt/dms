/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.basedt.dms.plugins.datasource.impl.jdbc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.SequenceHandler;
import com.basedt.dms.plugins.datasource.dto.SequenceDTO;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JdbcSequenceHandler implements SequenceHandler {

    protected DataSource dataSource;

    protected Map<String, String> config;

    @Override
    public void initialize(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.config = config;
    }

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return listSequenceDetails(catalog, schemaPattern, sequencePattern);
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return List.of();
    }

    @Override
    public SequenceDTO getSequenceDetail(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        List<SequenceDTO> sequences = listSequenceDetails(catalog, schemaPattern, sequencePattern);
        if (CollectionUtil.isNotEmpty(sequences)) {
            return sequences.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void dropSequence(String schema, String sequenceName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateDropSQL(schema, sequenceName));
        }
    }

    @Override
    public void renameSequence(String schema, String sequenceName, String newName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            JdbcUtil.execute(conn, generateRenameSQL(schema, sequenceName, newName));
        }
    }

    @Override
    public String getSequenceDDL(String catalog, String schema, String sequenceName) throws SQLException {
        StringBuilder builder = new StringBuilder();
        SequenceDTO sequence = getSequenceDetail(catalog, schema, sequenceName);
        if (Objects.nonNull(sequence)){
            builder.append("CREATE SEQUENCE ")
                    .append(sequence.getSchemaName())
                    .append(Constants.SEPARATOR_DOT)
                    .append(sequence.getSequenceName())
                    .append("\n INCREMENT BY ")
                    .append(sequence.getIncrementBy())
                    .append("\n MINVALUE ")
                    .append(sequence.getMinValue())
                    .append("\n MAXVALUE ")
                    .append(sequence.getMaxValue())
                    .append("\n START WITH ")
                    .append(sequence.getStartValue())
                    .append("\n CACHE ")
                    .append(sequence.getCacheSize())
                    .append(sequence.getIsCycle()? " \n CYCLE" : " \n NO CYCLE")
                    .append(";");
            return builder.toString();
        }
        return "";
    }

    @Override
    public String getDropDDL(String schema, String sequenceName) throws SQLException {
        return generateDropSQL(schema, sequenceName);
    }

    @Override
    public String getRenameDDL(String schema, String sequenceName, String newName) throws SQLException {
        return generateRenameSQL(schema, sequenceName, newName);
    }

    protected List<SequenceDTO> listSequenceFromDB(String sql) throws SQLException {
        if (StrUtil.isBlank(sql)) {
            return List.of();
        }
        List<SequenceDTO> result = new ArrayList<>();
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            SequenceDTO sequence = new SequenceDTO();
            sequence.setCatalogName(rs.getString("catalog_name"));
            sequence.setSchemaName(rs.getString("schema_name"));
            sequence.setObjectName(rs.getString("object_name"));
            sequence.setObjectType(rs.getString("object_type"));
            sequence.setStartValue(rs.getLong("start_value"));
            sequence.setMinValue(rs.getLong("min_value"));
            try {
                sequence.setMaxValue(rs.getLong("max_value"));
            } catch (Exception e) {
                sequence.setMaxValue(Long.MAX_VALUE);
            }
            sequence.setIncrementBy(rs.getLong("increment_by"));
            sequence.setIsCycle(rs.getBoolean("is_cycle"));
            sequence.setLastValue(rs.getLong("last_value"));
            sequence.setCacheSize(rs.getLong("cache_size"));
            sequence.setCreateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("create_time")));
            sequence.setLastDdlTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("last_ddl_time")));
            result.add(sequence);
        }
        JdbcUtil.close(conn, ps, rs);
        return result;
    }

    protected String generateDropSQL(String schema, String sequenceName) {
        return StrUtil.format("DROP SEQUENCE {}.{}", schema, sequenceName);
    }

    protected String generateRenameSQL(String schema, String sequenceName, String newName) {
        return StrUtil.format("ALTER SEQUENCE {}.{} RENAME TO {}", schema, sequenceName, newName);
    }
}
