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

import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.PropertiesUtil;
import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class SocketAuthorizationListener implements AuthorizationListener {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public SocketAuthorizationListener(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public boolean isAuthorized(HandshakeData data) {
        HttpHeaders headers = data.getHttpHeaders();
        String cookies = headers.get("Cookie");
        Map<String, Object> cookieMap = PropertiesUtil.formatToMap(cookies, Constants.LINE_FEED, Constants.SEPARATOR_EQUAL);
        String token = String.valueOf(cookieMap.get(Constants.SESSION_ID));
        Session session = this.sessionRepository.findById(token);
        return Objects.nonNull(session);
    }
}
