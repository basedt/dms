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
package com.basedt.dms.plugins.datasource.impl.doris;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.core.PluginInfo;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.dto.*;
import com.basedt.dms.plugins.datasource.enums.DataSourceType;
import com.basedt.dms.plugins.datasource.impl.mysql.MysqlPluginImpl;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import com.google.auto.service.AutoService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.*;

@AutoService(DataSourcePlugin.class)
public class DorisPluginImpl extends MysqlPluginImpl {

    public DorisPluginImpl() {
        super();
        init();
    }

    public DorisPluginImpl(Properties properties) {
        super(properties);
        init();
    }

    public DorisPluginImpl(String hostName, Integer port, String databaseName, String userName, String password, Map<String, String> attributes) {
        super(hostName, port, databaseName, userName, password, attributes);
        init();
    }

    private void init() {
        setPluginInfo(new PluginInfo(StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, DataSourceType.DORIS.getValue()).toUpperCase(),
                PluginType.DATASOURCE));
        setDriverClassName("com.mysql.jdbc.Driver");
    }

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

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> mViewList = new ArrayList<>();
        String sql = "show alter table materialized view from " + schemaPattern;
        Connection conn = this.getConnection();
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

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        return listMViewDetails(catalog, schemaPattern, mViewPattern);
    }

    @Override
    public List<FunctionDTO> listFunctionDetails(String catalog, String schemaPattern, String functionPattern) throws SQLException {
        List<FunctionDTO> functionList = new ArrayList<>();
        String sql = "show global full functions";
        Connection connection = this.getConnection();
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

    @Override
    public List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException {
        return List.of();
    }

    @Override
    public List<ObjectDTO> listFkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public List<ObjectDTO> listPkByTable(String catalog, String schemaPattern, String tableName) throws SQLException {
        return List.of();
    }

    @Override
    public List<IndexDTO> listIndexDetails(String catalog, String schemaPattern, String tableName) throws SQLException {
        List<IndexDTO> indexList = new ArrayList<>();
        String sql = "show index from " + StrUtil.concat(true, schemaPattern, Constants.SEPARATOR_DOT, tableName);
        Connection conn = this.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            IndexDTO index = new IndexDTO();
            index.setCatalogName(catalog);
            index.setSchemaName(schemaPattern);
            index.setTableName(tableName);
            index.setObjectName(rs.getString("Key_name"));
            index.setIndexType(rs.getString("Index_type"));
            index.setIsUniqueness(false);
            indexList.add(index);
        }
        JdbcUtil.close(conn, st, rs);
        return indexList;
    }
}
