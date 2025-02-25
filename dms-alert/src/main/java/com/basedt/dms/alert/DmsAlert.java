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
package com.basedt.dms.alert;

import com.basedt.dms.alert.dto.SysMessageDTO;
import com.basedt.dms.alert.impl.EmailAlertServiceImpl;
import com.basedt.dms.common.enums.AlertType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DmsAlert {

    private final EmailAlertServiceImpl emailAlertService;

    private final SysMessageService sysMessageService;


    public DmsAlert(EmailAlertServiceImpl emailAlertService,
                    SysMessageService sysMessageService) {
        this.emailAlertService = emailAlertService;
        this.sysMessageService = sysMessageService;
    }

    public void send(SysMessageDTO message, AlertType... types) {
        if (Objects.nonNull(types)) {
            for (AlertType type : types) {
                switch (type) {
                    case EMAIL:
                        emailAlertService.send(message.getReceiver(), message.getTitle(), message.getContent());
                        break;
                    case MESSAGE:
                        sysMessageService.insert(message);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    ;
}
