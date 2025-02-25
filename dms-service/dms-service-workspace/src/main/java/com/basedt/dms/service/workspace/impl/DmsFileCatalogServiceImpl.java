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

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basedt.dms.common.exception.DmsException;
import com.basedt.dms.common.utils.I18nUtil;
import com.basedt.dms.dao.entity.master.workspace.DmsFile;
import com.basedt.dms.dao.entity.master.workspace.DmsFileCatalog;
import com.basedt.dms.dao.mapper.master.workspace.DmsFileCatalogMapper;
import com.basedt.dms.dao.mapper.master.workspace.DmsFileMapper;
import com.basedt.dms.service.workspace.DmsFileCatalogService;
import com.basedt.dms.service.workspace.convert.DmsFileCatalogConvert;
import com.basedt.dms.service.workspace.dto.DmsFileCatalogDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.basedt.dms.common.enums.ResponseCode.ERROR_CATALOG_NOTNULL;

@Service
public class DmsFileCatalogServiceImpl implements DmsFileCatalogService {

    private final DmsFileCatalogMapper dmsFileCatalogMapper;
    private final DmsFileMapper dmsFileMapper;

    public DmsFileCatalogServiceImpl(DmsFileCatalogMapper dmsFileCatalogMapper, DmsFileMapper dmsFileMapper) {
        this.dmsFileCatalogMapper = dmsFileCatalogMapper;
        this.dmsFileMapper = dmsFileMapper;
    }

    @Override
    public void insert(DmsFileCatalogDTO fileCatalogDTO) {
        DmsFileCatalog fileCatalog = DmsFileCatalogConvert.INSTANCE.toDo(fileCatalogDTO);
        this.dmsFileCatalogMapper.insert(fileCatalog);
    }

    @Override
    public void update(DmsFileCatalogDTO fileCatalogDTO) {
        DmsFileCatalog fileCatalog = DmsFileCatalogConvert.INSTANCE.toDo(fileCatalogDTO);
        this.dmsFileCatalogMapper.updateById(fileCatalog);
    }

    @Override
    public void deleteById(Long id) throws DmsException {
        DmsFileCatalog catalog = this.dmsFileCatalogMapper.selectById(id);
        long childFileNum = this.dmsFileMapper.selectCount(Wrappers.lambdaQuery(DmsFile.class)
                .eq(DmsFile::getFileCatalog, catalog.getId())
                .eq(DmsFile::getWorkspaceId, catalog.getWorkspaceId())
        );
        long childCatalogNum = this.dmsFileCatalogMapper.selectCount(Wrappers.lambdaQuery(DmsFileCatalog.class)
                .eq(DmsFileCatalog::getWorkspaceId, catalog.getWorkspaceId())
                .eq(DmsFileCatalog::getPid, catalog.getId())
        );
        if (childFileNum + childCatalogNum > 0) {
            throw new DmsException(ERROR_CATALOG_NOTNULL.getValue(), I18nUtil.get(ERROR_CATALOG_NOTNULL.getLabel()));
        } else {
            this.dmsFileCatalogMapper.deleteById(id);
        }
    }

    @Override
    public List<Tree<Long>> listCatalogTree(Long workspaceId, Long id) {
        List<DmsFileCatalog> catalogs = this.dmsFileCatalogMapper.selectList(Wrappers.lambdaQuery(DmsFileCatalog.class)
                .eq(DmsFileCatalog::getWorkspaceId, workspaceId)
                .notIn(Objects.nonNull(id), DmsFileCatalog::getId, id)
        );
        List<DmsFileCatalogDTO> dtoList = DmsFileCatalogConvert.INSTANCE.toDto(catalogs);
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("key");
        config.setNameKey("title");
        return TreeUtil.build(dtoList, 0L, config, (node, tree) -> {
            tree.setId(node.getId());
            tree.setName(node.getName());
            tree.setParentId(node.getPid());
            tree.setWeight(node.getId());
            tree.putExtra("type", "catalog");
        });
    }
}
