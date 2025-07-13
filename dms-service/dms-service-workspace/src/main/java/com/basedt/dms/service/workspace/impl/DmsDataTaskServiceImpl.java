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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.FileType;
import com.basedt.dms.common.enums.TaskStatus;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.common.utils.MinioUtil;
import com.basedt.dms.dao.entity.master.workspace.DmsDataTask;
import com.basedt.dms.dao.mapper.master.workspace.DmsDataTaskMapper;
import com.basedt.dms.plugins.core.PluginType;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.DataSourcePluginManager;
import com.basedt.dms.plugins.datasource.MetaDataService;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import com.basedt.dms.plugins.input.InputPlugin;
import com.basedt.dms.plugins.input.InputPluginManager;
import com.basedt.dms.plugins.output.OutputPlugin;
import com.basedt.dms.plugins.output.OutputPluginManager;
import com.basedt.dms.service.base.dto.PageDTO;
import com.basedt.dms.service.log.LogDataTaskService;
import com.basedt.dms.service.log.dto.LogDataTaskDTO;
import com.basedt.dms.service.workspace.DmsDataSourceService;
import com.basedt.dms.service.workspace.DmsDataTaskService;
import com.basedt.dms.service.workspace.convert.DataSourceConvert;
import com.basedt.dms.service.workspace.convert.DmsDataTaskConvert;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;
import com.basedt.dms.service.workspace.dto.DmsDataTaskDTO;
import com.basedt.dms.service.workspace.param.DmsDataTaskParam;
import com.basedt.dms.service.workspace.vo.DmsImportTaskVO;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.ipc.ArrowStreamReader;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.ResultSetDynaClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.sql.Date;
import java.util.*;

@Service
public class DmsDataTaskServiceImpl implements DmsDataTaskService {

    private final DmsDataTaskMapper dmsDataTaskMapper;

    private final MetaDataService metaDataService;

    private final DmsDataSourceService dmsDataSourceService;

    private final LogDataTaskService logDataTaskService;

    private final MinioUtil minioUtil;

    private final Integer BUFFER_SIZE = 5000;

    @Value("${minio.bucketName}")
    private String bucketName;

    public DmsDataTaskServiceImpl(DmsDataTaskMapper dmsDataTaskMapper, MetaDataService metaDataService, DmsDataSourceService dmsDataSourceService, LogDataTaskService logDataTaskService, MinioUtil minioUtil) {
        this.dmsDataTaskMapper = dmsDataTaskMapper;
        this.metaDataService = metaDataService;
        this.dmsDataSourceService = dmsDataSourceService;
        this.logDataTaskService = logDataTaskService;
        this.minioUtil = minioUtil;
    }

    @Override
    public Long insert(DmsDataTaskDTO dmsDataTaskDTO) {
        DmsDataTask dmsDataTask = DmsDataTaskConvert.INSTANCE.toDo(dmsDataTaskDTO);
        this.dmsDataTaskMapper.insert(dmsDataTask);
        return dmsDataTask.getId();
    }

    @Override
    public void update(DmsDataTaskDTO dmsDataTaskDTO) {
        DmsDataTask dmsDataTask = DmsDataTaskConvert.INSTANCE.toDo(dmsDataTaskDTO);
        this.dmsDataTaskMapper.updateById(dmsDataTask);
    }

    @Override
    public void deleteById(Long id) {
        this.dmsDataTaskMapper.deleteById(id);
    }

    @Override
    public void deleteBatch(List<Long> idList) {
        this.dmsDataTaskMapper.deleteByIds(idList);
    }

    @Override
    public DmsDataTaskDTO selectOne(Long id) {
        DmsDataTask dmsDataTask = this.dmsDataTaskMapper.selectById(id);
        return DmsDataTaskConvert.INSTANCE.toDto(dmsDataTask);
    }

    @Override
    public PageDTO<DmsDataTaskDTO> listByPage(DmsDataTaskParam param) {
        Page<DmsDataTask> page = this.dmsDataTaskMapper.selectPage(
                new PageDTO<>(param.getCurrent(), param.getPageSize()),
                Wrappers.lambdaQuery(DmsDataTask.class)
                        .eq(DmsDataTask::getWorkspaceId, param.getWorkspaceId())
                        .eq(DmsDataTask::getTaskType, param.getTaskType())
                        .eq(Objects.nonNull(param.getDatasourceId()), DmsDataTask::getDatasourceId, param.getDatasourceId())
                        .like(StrUtil.isNotBlank(param.getFileName()), DmsDataTask::getFileName, param.getFileName())
                        .eq(StrUtil.isNotBlank(param.getTaskStatus()), DmsDataTask::getTaskStatus, param.getTaskStatus())
                        .like(StrUtil.isNotBlank(param.getCreator()), DmsDataTask::getCreator, param.getCreator())
                        .ge(StrUtil.isNotBlank(param.getCreateTimeFrom()), DmsDataTask::getCreateTime, DateTimeUtil.toLocalDateTime(param.getCreateTimeFrom(), DateTimeUtil.NORMAL_DATETIME_PATTERN))
                        .le(StrUtil.isNotBlank(param.getCreateTimeTo()), DmsDataTask::getCreateTime, DateTimeUtil.toLocalDateTime(param.getCreateTimeTo(), DateTimeUtil.NORMAL_DATETIME_PATTERN))
                        .orderByDesc(DmsDataTask::getCreateTime)
        );
        PageDTO<DmsDataTaskDTO> result = new PageDTO<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setData(DmsDataTaskConvert.INSTANCE.toDto(page.getRecords()));
        return result;
    }

    @Override
    @Async("asyncExecutor")
    public void createImportTask(Long taskId, DmsImportTaskVO dmsImportTaskVO, MultipartFile file) {
        DmsDataTaskDTO dmsDataTaskDTO = this.selectOne(taskId);
        try {
            dmsDataTaskDTO.setTaskStatus(TaskStatus.RUNNING.toDict());
            this.update(dmsDataTaskDTO);
            this.logDataTaskService.insert(new LogDataTaskDTO(taskId, "data import task start..."));
            //1. upload file to minio
            String objectName = StrUtil.concat(true, "import/", String.valueOf(taskId), "/", file.getOriginalFilename());
            minioUtil.uploadObject(this.bucketName, objectName, file.getInputStream());
            String tmpFilePath = System.getProperty("java.io.tmpdir");
            tmpFilePath += "dms" + File.separator + "import" + File.separator + taskId;
            FileUtil.mkdir(tmpFilePath);
            this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("local temp file folder is {}", tmpFilePath)));
            File tmpFile = FileUtil.file(tmpFilePath, dmsDataTaskDTO.getFileName());
            Files.copy(minioUtil.downloadObject(this.bucketName, objectName), Paths.get(tmpFilePath, dmsDataTaskDTO.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = minioUtil.getObjectURI(this.bucketName, objectName);
            dmsDataTaskDTO.setFileUrl(fileUrl);
            this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("upload file to minio {}", fileUrl)));
            //2. read file
            this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("begin read file {}", tmpFile.getCanonicalPath())));
            Map<String, Object> propMap = new HashMap<>();
            propMap.put("file", tmpFile);
            propMap.put("fileEncoding", dmsImportTaskVO.getFileEncoding());
            if (FileType.CSV.name().equalsIgnoreCase(dmsImportTaskVO.getFileType())) {
                propMap.put("separator", dmsImportTaskVO.getSeparator());
            }
            InputPlugin inputPlugin = InputPluginManager.newInstance(StrUtil.concat(true, PluginType.RESOURCE_INPUT.name(), Constants.SEPARATOR_UNDERLINE, dmsDataTaskDTO.getFileType().getValue()).toUpperCase(),
                    propMap);
            DmsDataSourceDTO dataSourceDTO = dmsDataSourceService.selectOne(dmsDataTaskDTO.getDatasourceId());
            Map<String, String> attrs = new HashMap<>();
            if (CollectionUtil.isNotEmpty(dataSourceDTO.getAttrs())) {
                dataSourceDTO.getAttrs().forEach((k, v) -> {
                    attrs.put(k, (String) v);
                });
            }
            try (ByteArrayOutputStream outputStream = inputPlugin.read();
                 ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                 RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);
                 ArrowStreamReader reader = new ArrowStreamReader(inputStream, allocator)) {
                DataSourcePlugin dataSourcePlugin = DataSourcePluginManager.newInstance(
                        StrUtil.concat(true, PluginType.DATASOURCE.name(), Constants.SEPARATOR_UNDERLINE, dataSourceDTO.getDatasourceType().getValue()).toUpperCase(),
                        dataSourceDTO.getHostName(),
                        dataSourceDTO.getPort(),
                        dataSourceDTO.getDatabaseName(),
                        dataSourceDTO.getUserName(),
                        Base64.decodeStr(dataSourceDTO.getPassword()),
                        attrs
                );
                this.logDataTaskService.insert(new LogDataTaskDTO(taskId, "connected to the database success."));
                String tableName = StrUtil.concat(true, dmsImportTaskVO.getSchema(), Constants.SEPARATOR_DOT, dmsImportTaskVO.getTableName());
                if (dmsImportTaskVO.getIsTruncate()) {
                    String sql = StrUtil.concat(true, "truncate table ", tableName);
                    this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("execute sql : {}", sql)));
                    dataSourcePlugin.execute(sql);
                }
                this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("start import data to table {}", tableName)));
                dataSourcePlugin.insertBatch(reader, dmsImportTaskVO.getCatalog(), dmsImportTaskVO.getSchema(), dmsImportTaskVO.getTableName());
                this.logDataTaskService.insert(new LogDataTaskDTO(taskId, "data import completed."));
                FileUtil.del(tmpFilePath);
                dmsDataTaskDTO.setTaskStatus(TaskStatus.SUCCESS.toDict());
            }
        } catch (Exception e) {
            this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("[{}] exception:{}", e.getMessage())));
            dmsDataTaskDTO.setTaskStatus(TaskStatus.FAILURE.toDict());
        } finally {
            this.update(dmsDataTaskDTO);
        }
    }

    @Override
    @Async("asyncExecutor")
    public void createExportTask(Long taskId, String script) throws SQLException {
        DmsDataTaskDTO dmsDataTaskDTO = this.selectOne(taskId);
        dmsDataTaskDTO.setSqlScript(script);
        // 1. create local tmp folder
        String tmpFilePath = System.getProperty("java.io.tmpdir");
        tmpFilePath += "dms" + File.separator + "export" + File.separator + taskId;
        FileUtil.mkdir(tmpFilePath);
        //2.set task status running
        String sql = dmsDataTaskDTO.getSqlScript();
        dmsDataTaskDTO.setTaskStatus(TaskStatus.RUNNING.toDict());
        this.update(dmsDataTaskDTO);
        this.logDataTaskService.insert(new LogDataTaskDTO(taskId, "data export task start..."));
        this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("local temp file folder is {}", tmpFilePath)));
        this.logDataTaskService.insert(new LogDataTaskDTO(taskId, "begin init datasource."));
        DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(dmsDataTaskDTO.getDatasourceId());
        DataSourcePlugin plugin = this.metaDataService.getDataSourcePluginInstance(DataSourceConvert.toDataSource(dto));
        Connection connection = plugin.getDataSource().getConnection();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        this.logDataTaskService.insert(new LogDataTaskDTO(taskId, "connected to the database success."));
        List<List<Object>> bufferList = new ArrayList<>();
        try {
            pstm = connection.prepareStatement(sql);
            this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("execute query {}", sql)));
            rs = pstm.executeQuery();
            ResultSetDynaClass resultSetDynaClass = new ResultSetDynaClass(rs);
            Iterator<DynaBean> iterator = resultSetDynaClass.iterator();
            if (Objects.nonNull(dmsDataTaskDTO.getSplitRow()) && dmsDataTaskDTO.getSplitRow() > 0) {
                //split file
                int rowNum = 0;
                int fileNum = 0;
                Long splitRow = dmsDataTaskDTO.getSplitRow();
                File tmpFile = FileUtil.file(FileUtil.file(tmpFilePath), dmsDataTaskDTO.getFileName() + Constants.SEPARATOR_UNDERLINE + fileNum + "." + dmsDataTaskDTO.getFileType().getValue());
                OutputPlugin outputPlugin = OutputPluginManager.newInstance(
                        StrUtil.concat(true, PluginType.RESOURCE_OUTPUT.name(), Constants.SEPARATOR_UNDERLINE, dmsDataTaskDTO.getFileType().getValue()).toUpperCase(),
                        tmpFile,
                        dmsDataTaskDTO.getFileEncoding().getValue(),
                        resultSetDynaClass
                );
                while (iterator.hasNext()) {
                    DynaBean dynaBean = iterator.next();
                    bufferList.add(bean2Json(dynaBean));
                    rowNum++;
                    if (rowNum % BUFFER_SIZE == 0) {
                        outputPlugin.write(bufferList);
                        this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("exported rows {}", rowNum)));
                        bufferList.clear();
                    }
                    if (rowNum % splitRow == 0) {
                        outputPlugin.write(bufferList);
                        outputPlugin.finish();
                        this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("exported rows {}", rowNum)));
                        bufferList.clear();
                        fileNum++;
                        tmpFile = FileUtil.file(FileUtil.file(tmpFilePath), dmsDataTaskDTO.getFileName() + Constants.SEPARATOR_UNDERLINE + fileNum + "." + dmsDataTaskDTO.getFileType().getValue());
                        outputPlugin = OutputPluginManager.newInstance(
                                StrUtil.concat(true, PluginType.RESOURCE_OUTPUT.name(), Constants.SEPARATOR_UNDERLINE, dmsDataTaskDTO.getFileType().getValue()).toUpperCase(),
                                tmpFile,
                                dmsDataTaskDTO.getFileEncoding().getValue(),
                                resultSetDynaClass
                        );
                    }
                }
                if (CollectionUtil.isNotEmpty(bufferList)) {
                    this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("exported rows {}", rowNum)));
                    outputPlugin.write(bufferList);
                } else if (CollectionUtil.isEmpty(bufferList) && rowNum > 0) {
                    FileUtil.del(tmpFile);
                } else if (rowNum == 0) {
                    //write head
                    outputPlugin.write(bufferList);
                }
                outputPlugin.finish();
            } else {
                //write single file
                int rowNum = 0;
                File tmpFile = FileUtil.file(FileUtil.file(tmpFilePath), dmsDataTaskDTO.getFileName() + "." + dmsDataTaskDTO.getFileType().getValue());
                OutputPlugin outputPlugin = OutputPluginManager.newInstance(
                        StrUtil.concat(true, PluginType.RESOURCE_OUTPUT.name(), Constants.SEPARATOR_UNDERLINE, dmsDataTaskDTO.getFileType().getValue()).toUpperCase(),
                        tmpFile,
                        dmsDataTaskDTO.getFileEncoding().getValue(),
                        resultSetDynaClass
                );
                while (iterator.hasNext()) {
                    DynaBean bean = iterator.next();
                    rowNum++;
                    bufferList.add(bean2Json(bean));
                    if (bufferList.size() >= BUFFER_SIZE) {
                        outputPlugin.write(bufferList);
                        this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("exported rows {}", rowNum)));
                        bufferList.clear();
                    }
                }
                if (!bufferList.isEmpty()) {
                    outputPlugin.write(bufferList);
                } else if (rowNum == 0) {
                    //write head
                    outputPlugin.write(bufferList);
                }
                outputPlugin.finish();
                this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("exported rows {}", rowNum)));
                logDataTaskService.insert(new LogDataTaskDTO(taskId, "zip local file and upload to file server"));
            }
            File zipFile = ZipUtil.zip(tmpFilePath);
            String objectName = StrUtil.concat(true, "export/", String.valueOf(taskId), "/", zipFile.getName());
            minioUtil.uploadObject(this.bucketName, objectName, FileUtil.getInputStream(zipFile));
            logDataTaskService.insert(new LogDataTaskDTO(taskId, "clear temp files."));
            dmsDataTaskDTO.setFileSize(Files.size(zipFile.toPath()));
            FileUtil.del(zipFile);
            FileUtil.del(tmpFilePath);
            logDataTaskService.insert(new LogDataTaskDTO(taskId, "data export task end."));
            dmsDataTaskDTO.setFileUrl(minioUtil.getObjectURI(this.bucketName, objectName));
            dmsDataTaskDTO.setTaskStatus(TaskStatus.SUCCESS.toDict());
        } catch (Exception e) {
            this.logDataTaskService.insert(new LogDataTaskDTO(taskId, StrUtil.format("[{}] exception:{}", e.getMessage())));
            dmsDataTaskDTO.setTaskStatus(TaskStatus.FAILURE.toDict());
        } finally {
            JdbcUtil.closeSilently(connection, pstm, rs);
            this.update(dmsDataTaskDTO);
        }
    }

    @Override
    public List<LogDataTaskDTO> getLogDetail(Long taskId) throws IOException {
        return this.logDataTaskService.listByTask(taskId);
    }


    private List<Object> bean2Json(DynaBean bean) {
        if (Objects.isNull(bean)) {
            return null;
        }
        List<Object> list = new ArrayList<>();
        DynaProperty[] cols = bean.getDynaClass().getDynaProperties();
        for (DynaProperty col : cols) {
            if (col.getType().getName().equals(Timestamp.class.getName())) {
                Timestamp value = (Timestamp) bean.get(col.getName());
                if (value != null) {
                    list.add(DateTimeUtil.toChar(value.getTime(), DateTimeUtil.NORMAL_DATETIME_MS_PATTERN));
                } else {
                    list.add(null);
                }
            } else if (col.getType().getName().equals(Date.class.getName())) {
                Date value = (Date) bean.get(col.getName());
                if (value != null) {
                    list.add(DateTimeUtil.toChar(value.getTime(), DateTimeUtil.NORMAL_DATE_PATTERN));
                } else {
                    list.add(null);
                }
            } else {
                Object value = bean.get(col.getName());
                list.add(value);
            }
        }
        return list;
    }

}
