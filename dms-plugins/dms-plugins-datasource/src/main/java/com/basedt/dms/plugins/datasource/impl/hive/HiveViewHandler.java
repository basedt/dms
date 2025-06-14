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

package com.basedt.dms.plugins.datasource.impl.hive;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.dto.ViewDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcViewHandler;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.VIEW;
import static com.basedt.dms.plugins.datasource.impl.hive.HivePluginImpl.METASTORE_URIS;

public class HiveViewHandler extends JdbcViewHandler {

    @Override
    public List<ViewDTO> listViewDetails(String catalog, String schema, String viewName) throws SQLException {
        try {
            List<ViewDTO> result = new ArrayList<>();
            String uris = this.config.get(METASTORE_URIS);
            Configuration conf = new Configuration();
            conf.set("hive.metastore.uris", uris);
            HiveMetaStoreClient client = new HiveMetaStoreClient(conf);
            List<String> tables = client.getAllTables(null, schema);
            List<Table> tableList = client.getTableObjectsByName(null, schema, tables);
            for (Table table : tableList) {
                if ("VIRTUAL_VIEW".equals(table.getTableType())) {
                    ViewDTO viewDTO = new ViewDTO();
                    viewDTO.setCatalogName(catalog);
                    viewDTO.setSchemaName(table.getDbName());
                    viewDTO.setObjectName(table.getTableName());
                    viewDTO.setObjectType(VIEW.name());
                    viewDTO.setCreateTime(DateTimeUtil.toLocalDateTime(table.getCreateTime()));
                    viewDTO.setQuerySql(table.getViewOriginalText());
                    Map<String, String> params = table.getParameters();
                    viewDTO.setRemark(params.get("comment"));
                    String lastDdlTime = params.get("transient_lastDdlTime");
                    viewDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(Integer.parseInt(Objects.isNull(lastDdlTime) ? "0" : lastDdlTime)));
                    if (Objects.isNull(viewName) || StrUtil.contains(viewDTO.getViewName(), viewName)) {
                        result.add(viewDTO);
                    }
                }
            }
            client.close();
            return result;
        } catch (TException e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public String getViewDdl(String catalog, String schema, String viewName) throws SQLException {
        String ddl = super.getViewDdl(catalog, schema, viewName);
        return SQLUtils.format(ddl, DbType.hive, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION);
    }
}
