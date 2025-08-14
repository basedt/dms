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
package com.basedt.dms.service.workspace;

import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.log.dto.LogDataTaskDTO;
import com.basedt.dms.service.workspace.dto.DmsDataTaskDTO;
import com.basedt.dms.service.workspace.param.DmsDataTaskParam;
import com.basedt.dms.service.workspace.vo.DmsImportTaskVO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface DmsDataTaskService {

    Long insert(DmsDataTaskDTO dmsDataTaskDTO);

    void update(DmsDataTaskDTO dmsDataTaskDTO);

    void deleteById(Long id);

    void deleteBatch(List<Long> idList);

    DmsDataTaskDTO selectOne(Long id);

    PageDTO<DmsDataTaskDTO> listByPage(DmsDataTaskParam param);

    void createImportTask(Long taskId, DmsImportTaskVO dmsImportTaskVO, String objectName) throws IOException, SQLException;

    void createExportTask(Long taskId, String script) throws SQLException;

    List<LogDataTaskDTO> getLogDetail(Long taskId) throws IOException;
}
