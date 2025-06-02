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
package com.basedt.dms.api.socket;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.api.vo.ResultSetVO;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.SqlStatus;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.common.utils.RedisUtil;
import com.basedt.dms.plugins.datasource.DataSourcePlugin;
import com.basedt.dms.plugins.datasource.MetaDataService;
import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import com.basedt.dms.service.log.LogSqlHistoryService;
import com.basedt.dms.service.log.dto.LogSqlHistoryDTO;
import com.basedt.dms.service.security.utils.SecurityUtil;
import com.basedt.dms.service.workspace.DmsDataSourceService;
import com.basedt.dms.service.workspace.convert.DataSourceConvert;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;
import com.basedt.dms.service.workspace.param.DmsSqlExecParam;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SqlScriptHandler {

    private static final String EVENT_EXEC = "exec";

    private static final String EVENT_STOP = "stop";

    private static final String EVENT_INFO = "info";

    private static final String EVENT_ERROR = "error";

    private static final String EVENT_RESULT_SET = "resultSet";

    private static final String EVENT_FINISHED = "finished";

    private final RedisUtil redisUtil;

    private final SocketIONamespace namespace;

    private final MetaDataService metaDataService;

    private final LogSqlHistoryService logSqlHistoryService;

    private final DmsDataSourceService dmsDataSourceService;

    public SqlScriptHandler(RedisUtil redisUtil, SocketIOServer server, MetaDataService metaDataService, LogSqlHistoryService logSqlHistoryService, DmsDataSourceService dmsDataSourceService) {
        this.redisUtil = redisUtil;
        this.namespace = server.addNamespace("/sql");
        this.logSqlHistoryService = logSqlHistoryService;
        this.dmsDataSourceService = dmsDataSourceService;
        this.namespace.addConnectListener(onConnected());
        this.namespace.addDisconnectListener(onDisconnected());
        this.namespace.addEventListener(EVENT_EXEC, DmsSqlExecParam.class, onSqlExec());
        this.namespace.addEventListener(EVENT_STOP, Object.class, onStopExec());
        this.metaDataService = metaDataService;
    }

    private ConnectListener onConnected() {
        return client -> {
            HandshakeData handshakeData = client.getHandshakeData();
            log.debug("Client[{}] - Connected to chat module through '{}'", client.getSessionId().toString(), handshakeData.getUrl());
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            log.debug("Client[{}] - Disconnected from chat module.", client.getSessionId().toString());
        };
    }

    private DataListener<DmsSqlExecParam> onSqlExec() {
        return (client, data, ackSender) -> {
            String socketId = client.getSessionId().toString();
            if (Objects.nonNull(data.getWorkspaceId()) && Objects.nonNull(data.getDataSourceId()) && Objects.nonNull(data.getScript())) {
                DmsDataSourceDTO dto = this.dmsDataSourceService.selectOne(data.getDataSourceId());
                DataSourcePlugin plugin = metaDataService.getDataSourcePluginInstance(DataSourceConvert.toDataSource(dto));
                List<String> sqlArray = Arrays.stream(data.getScript().split(Constants.SEPARATOR_SEMICOLON)).filter(StrUtil::isNotBlank).collect(Collectors.toList());
                redisUtil.queuePush(socketId, sqlArray);
                Connection conn = plugin.getDataSource().getConnection();
                while (redisUtil.getQueueSize(socketId) > 0) {
                    String sql = redisUtil.queuePop(socketId);
                    LogSqlHistoryDTO sqlLog = new LogSqlHistoryDTO();
                    sqlLog.setWorkspaceId(data.getWorkspaceId());
                    sqlLog.setDatasourceId(data.getDataSourceId());
                    sqlLog.setSqlScript(sql);
                    String currentUser = SecurityUtil.getCurrentUserName();
                    sqlLog.setCreator(currentUser);
                    sqlLog.setEditor(currentUser);
                    sqlLog.setStartTime(LocalDateTime.now());
                    sendMsgEvent(client, StrUtil.format("[{}] execute sql : {}",
                            DateTimeUtil.toChar(sqlLog.getStartTime(), DateTimeUtil.NORMAL_DATETIME_PATTERN),
                            sql), EVENT_INFO);
                    try {
                        PreparedStatement psm = conn.prepareStatement(sql);
                        boolean flag = psm.execute();
                        if (flag) {
                            ResultSet rs = psm.getResultSet();
                            ResultSetVO result = new ResultSetVO(rs, plugin.isSupportRowEdit());
                            result.setSql(sql);
                            JdbcUtil.close(null, psm, rs);
                            sqlLog.setEndTime(LocalDateTime.now());
                            sendMsgEvent(client, StrUtil.format("[{}] completed in {} ms",
                                    DateTimeUtil.toChar(LocalDateTime.now(), DateTimeUtil.NORMAL_DATETIME_PATTERN),
                                    DateTimeUtil.getTimeInterval(sqlLog.getEndTime(), sqlLog.getStartTime())), EVENT_INFO);
                            sendResultSetEvent(client, result);
                        } else {
                            int result = psm.getUpdateCount();
                            sqlLog.setEndTime(LocalDateTime.now());
                            JdbcUtil.close(null, psm, null);
                            sendMsgEvent(client, StrUtil.format("[{}] completed in {} ms , ({} row affected)",
                                    DateTimeUtil.toChar(LocalDateTime.now(), DateTimeUtil.NORMAL_DATETIME_PATTERN),
                                    DateTimeUtil.getTimeInterval(sqlLog.getEndTime(), sqlLog.getStartTime()),
                                    result), EVENT_INFO);
                        }
                        sqlLog.setSqlStatus(SqlStatus.SUCCESS.toDict());
                        logSqlHistoryService.insert(sqlLog);
                    } catch (Exception e) {
                        String errorMsg = e.getMessage();
                        sqlLog.setEndTime(LocalDateTime.now());
                        sqlLog.setSqlStatus(SqlStatus.FAILURE.toDict());
                        sqlLog.setRemark(errorMsg);
                        sendMsgEvent(client, StrUtil.format("[{}] sql run failed , {}",
                                DateTimeUtil.toChar(LocalDateTime.now(), DateTimeUtil.NORMAL_DATETIME_PATTERN),
                                errorMsg), EVENT_ERROR);
                        redisUtil.delKeys(socketId);
                        logSqlHistoryService.insert(sqlLog);
                        JdbcUtil.close(conn);
                    }
                }
                sendMsgEvent(client, StrUtil.format("[{}] execute finished",
                        DateTimeUtil.toChar(LocalDateTime.now(), DateTimeUtil.NORMAL_DATETIME_PATTERN)), EVENT_FINISHED);
                JdbcUtil.close(conn);
            }
        };
    }

    private DataListener<Object> onStopExec() {
        return (client, data, ackSender) -> {
            String socketId = client.getSessionId().toString();
            redisUtil.delKeys(socketId);
            sendMsgEvent(client, "stop finished", EVENT_STOP);
        };
    }

    private void sendResultSetEvent(SocketIOClient client, ResultSetVO result) {
        namespace.getClient(client.getSessionId()).sendEvent(EVENT_RESULT_SET, result);
    }

    private void sendMsgEvent(SocketIOClient client, String msg, String eventKey) {
        namespace.getClient(client.getSessionId()).sendEvent(eventKey, msg);
    }

}
