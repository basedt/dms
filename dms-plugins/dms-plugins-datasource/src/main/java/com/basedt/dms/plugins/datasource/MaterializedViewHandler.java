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

package com.basedt.dms.plugins.datasource;

import com.basedt.dms.plugins.datasource.dto.MaterializedViewDTO;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface MaterializedViewHandler {

    void initialize(DataSource dataSource, Map<String, String> config);

    List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException;

    List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException;

    MaterializedViewDTO getMViewDetail(String catalog, String schemaPattern, String mViewPattern) throws SQLException;

    void dropMView(String schema, String mViewName) throws SQLException;

    void renameMView(String schema, String mViewName, String newName) throws SQLException;

    String getMViewDdl(String catalog,String schema,String mViewName) throws SQLException;

    String getDropDDL(String schema, String mViewName) throws SQLException;

    String getRenameDDL(String schema, String mViewName, String newName) throws SQLException;
}
