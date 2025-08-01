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

import com.basedt.dms.plugins.datasource.dto.SequenceDTO;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface SequenceHandler {

    void initialize(DataSource dataSource, Map<String, String> config);

    List<SequenceDTO> listSequences(String catalog, String schemaPattern, String sequencePattern) throws SQLException;

    List<SequenceDTO> listSequenceDetails(String catalog, String schemaPattern, String sequencePattern) throws SQLException;

    SequenceDTO getSequenceDetail(String catalog, String schemaPattern, String sequencePattern) throws SQLException;

    void dropSequence(String schema, String sequenceName) throws SQLException;

    void renameSequence(String schema, String sequenceName, String newName) throws SQLException;

    String getSequenceDDL(String catalog, String schema, String seuenceName) throws SQLException;

    String getDropDDL(String schema, String sequenceName) throws SQLException;

    String getRenameDDL(String schema, String sequenceName, String newName) throws SQLException;
}
