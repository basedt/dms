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
package com.basedt.dms.service.security.enums;

import lombok.Getter;

@Getter
public enum ActionCode {
    SHOW("0", "dms.p.action.show"),
    ADD("1", "dms.p.action.add"),
    EDIT("2", "dms.p.action.edit"),
    DELETE("3", "dms.p.action.delete"),
    GRANT("4", "dms.p.action.grant"),
    SECURITY("5", "dms.p.action.security"),
    DOWNLOAD("6", "dms.p.action.download"),
    DETAIL("7", "dms.p.action.detail"),
    ;

    private final String value;
    private final String label;

    ActionCode(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
