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
package com.basedt.dms.api.config;

import com.basedt.dms.api.socket.SocketAuthorizationListener;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

    @Value("${socket.io.host}")
    private String host;

    @Value("${socket.io.port}")
    private Integer port;

    @Value("${socket.io.workerThreads}")
    private Integer workerThreads;

    @Value("${socket.io.pingInterval}")
    private Integer pingInterval;

    @Value("${socket.io.pingTimeout}")
    private Integer pingTimeout;

    @Value("${socket.io.maxHttpContentLength}")
    private Integer maxHttpContentLength;

    @Value("${socket.io.maxFramePayloadLength}")
    private Integer maxFramePayloadLength;

    private final SocketAuthorizationListener socketAuthorizationListener;

    public SocketIOConfig(SocketAuthorizationListener socketAuthorizationListener) {
        this.socketAuthorizationListener = socketAuthorizationListener;
    }

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setWorkerThreads(workerThreads);
        config.setPingInterval(pingInterval);
        config.setPingTimeout(pingTimeout);
        config.setMaxHttpContentLength(maxHttpContentLength);
        config.setMaxFramePayloadLength(maxFramePayloadLength);
        config.setAuthorizationListener(socketAuthorizationListener);
        return new SocketIOServer(config);
    }

}
