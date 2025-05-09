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
package com.basedt.dms.dao.mapper.master.workspace;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basedt.dms.dao.entity.master.workspace.DmsFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DmsFileMapper extends BaseMapper<DmsFile> {

    void deleteByFileName(@Param("workspaceId") Long workspaceId,
                          @Param("fileCatalog") Long fileCatalog,
                          @Param("fileName") String fileName);

    void renameFile(@Param("workspaceId") Long workspaceId,
                    @Param("fileCatalog") Long fileCatalog,
                    @Param("fileName") String fileName,
                    @Param("newFileName") String newFileName
    );

    void moveCatalog(@Param("workspaceId") Long workspaceId,
                     @Param("fileCatalog") Long fileCatalog,
                     @Param("fileName") String fileName,
                     @Param("newFileCatalog") Long newFileCatalog);

    List<DmsFile> listAllByWorkspaceAndDatasource(@Param("workspaceId") Long workspaceId, @Param("datasourceId") Long datasourceId);

    DmsFile selectLatestFile(@Param("workspaceId") Long workspaceId,
                             @Param("fileCatalog") Long fileCatalog,
                             @Param("fileName") String fileName);
}
