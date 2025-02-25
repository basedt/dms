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
package com.basedt.dms.service.workspace.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.FileStatus;
import com.basedt.dms.common.vo.TreeNodeVO;
import com.basedt.dms.dao.entity.master.workspace.DmsFile;
import com.basedt.dms.dao.entity.master.workspace.DmsFileCatalog;
import com.basedt.dms.dao.mapper.master.workspace.DmsFileCatalogMapper;
import com.basedt.dms.dao.mapper.master.workspace.DmsFileMapper;
import com.basedt.dms.service.security.utils.SecurityUtil;
import com.basedt.dms.service.workspace.DmsFileService;
import com.basedt.dms.service.workspace.convert.DmsFileConvert;
import com.basedt.dms.service.workspace.dto.DmsFileDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DmsFileServiceImpl implements DmsFileService {

    private final DmsFileMapper dmsFileMapper;

    private final DmsFileCatalogMapper dmsFileCatalogMapper;

    public DmsFileServiceImpl(DmsFileMapper dmsFileMapper,
                              DmsFileCatalogMapper dmsFileCatalogMapper) {
        this.dmsFileMapper = dmsFileMapper;
        this.dmsFileCatalogMapper = dmsFileCatalogMapper;
    }

    @Override
    public void save(DmsFileDTO dmsFileDTO) {
        DmsFile file = DmsFileConvert.INSTANCE.toDo(dmsFileDTO);
        String currentUser = SecurityUtil.getCurrentUserName();
        if (FileStatus.DRAFT.getValue().equals(file.getFileStatus())) {
            if (Objects.isNull(file.getId())) {
                file.setOwner(currentUser);
                this.dmsFileMapper.insert(file);
            } else {
                this.dmsFileMapper.updateById(file);
            }
        } else if (FileStatus.PUBLISH.getValue().equals(file.getFileStatus())) {
            //clone row and version +1
            Integer newVersion = file.getVersion() + 1;
            file.setId(null);
            file.setVersion(newVersion);
            file.setFileStatus(FileStatus.DRAFT.getValue());
            this.dmsFileMapper.insert(file);
        }
    }

    @Override
    public void publish(Long id) {
        DmsFile file = new DmsFile();
        file.setId(id);
        file.setFileStatus(FileStatus.PUBLISH.getValue());
        this.dmsFileMapper.updateById(file);
    }

    @Override
    public void deleteById(Long id) {
        DmsFile file = this.dmsFileMapper.selectById(id);
        if (Objects.nonNull(file)) {
            this.dmsFileMapper.deleteByFileName(file.getWorkspaceId(), file.getFileCatalog(), file.getFileName());
        }
    }

    @Override
    public void renameFile(Long fileId, String newFileName) {
        DmsFile file = this.dmsFileMapper.selectById(fileId);
        this.dmsFileMapper.renameFile(file.getWorkspaceId(), file.getFileCatalog(), file.getFileName(), newFileName);
    }

    @Override
    public void moveCatalog(Long fileId, Long newFileCatalog) {
        DmsFile file = this.dmsFileMapper.selectById(fileId);
        this.dmsFileMapper.moveCatalog(file.getWorkspaceId(), file.getFileCatalog(), file.getFileName(), newFileCatalog);
    }

    @Override
    public DmsFileDTO selectOne(Long id) {
        DmsFile file = this.dmsFileMapper.selectById(id);
        return DmsFileConvert.INSTANCE.toDto(file);
    }

    @Override
    public DmsFileDTO selectLastVersion(Long workspaceId, Long catalogId, String fileName) {
        DmsFile file = this.dmsFileMapper.selectLatestFile(workspaceId, catalogId, fileName);
        return DmsFileConvert.INSTANCE.toDto(file);
    }

    @Override
    public List<Tree<String>> listFileTree(Long workspaceId, Long datasourceId) {
        List<TreeNodeVO> nodeList = new ArrayList<>();
        List<DmsFile> fileList = this.dmsFileMapper.listAllByWorkspaceAndDatasource(workspaceId, datasourceId);
        List<DmsFileCatalog> catalogList = this.dmsFileCatalogMapper.selectList(Wrappers.lambdaQuery(DmsFileCatalog.class)
                .eq(DmsFileCatalog::getWorkspaceId, workspaceId)
        );
        if (CollectionUtil.isNotEmpty(catalogList)) {
            for (DmsFileCatalog catalog : catalogList) {
                TreeNodeVO node = new TreeNodeVO();
                node.setKey(String.valueOf(catalog.getId()));
                node.setTitle(catalog.getName());
                node.setType("catalog");
                node.setParentKey(String.valueOf(catalog.getPid()));
                node.setIdentifier(node.getType() + Constants.SEPARATOR_DOT + node.getKey());
                node.setIsLeaf(false);
                node.setOrder(node.getKey());
                nodeList.add(node);
            }
        }
        if (CollectionUtil.isNotEmpty(fileList)) {
            for (DmsFile file : fileList) {
                TreeNodeVO node = new TreeNodeVO();
                node.setKey(file.getFileType() + Constants.SEPARATOR_DOT + file.getId());
                node.setTitle(file.getFileName());
                node.setParentKey(String.valueOf(file.getFileCatalog()));
                node.setType(file.getFileType());
                node.setIdentifier(node.getKey());
                node.setIsLeaf(true);
                node.setOrder(file.getFileName());
                nodeList.add(node);
            }
        }
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("key");
        config.setNameKey("title");
        return TreeUtil.build(nodeList, "0", config, (node, tree) -> {
            tree.setId(node.getKey());
            tree.setName(node.getTitle());
            tree.setParentId(node.getParentKey());
            tree.setWeight(node.getOrder());
            tree.putExtra("type", node.getType());
            tree.putExtra("isLeaf", node.getIsLeaf());
            tree.putExtra("identifier", node.getIdentifier());
        });
    }

}
