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

import cn.hutool.core.lang.tree.Tree;
import com.basedt.dms.service.workspace.dto.DmsFileDTO;

import java.util.List;

public interface DmsFileService {

    void save(DmsFileDTO dmsFileDTO);

    void publish(Long id);

    /**
     * delete all file version
     *
     * @param id
     */
    void deleteById(Long id);

    void renameFile(Long fileId, String newFileName);

    void moveCatalog(Long fileId, Long newFileCatalog);

    DmsFileDTO selectOne(Long id);

    DmsFileDTO selectLastVersion(Long workspaceId, Long catalogId, String fileName);

    List<Tree<String>> listFileTree(Long workspaceId, Long datasourceId);

}
