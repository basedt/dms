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
public enum PageCode {
    DEFAULT("dft", "dms.p.page.dft"),
    SYS_DICT("dic", "dms.p.sys.dic.dft.show"),
    SYS_USER("usr", "dms.p.sys.user.dft.show"),
    SYS_SETTING("set", "dms.p.sys.set.dft.show"),
    SYS_ROLE("rol", "dms.p.sys.rol.dft.show"),
    WORKSPACE_OVERVIEW("wso", "dms.p.ws.wso.dft.show"),
    WORKSPACE_SPACE("wss", "dms.p.ws.wss.dft.show"),
    WORKSPACE_DATASOURCE("wsd", "dms.p.ws.wsd.dft.show"),
    WORKSPACE_HISTORY("wsh", "dms.p.ws.wsh.dft.show"),
    WORKSPACE_EXPORT("wse", "dms.p.ws.wse.dft.show"),
    WORKSPACE_IMPORT("wsi", "dms.p.ws.wsi.dft.show"),
    ;

    private final String value;
    private final String label;

    PageCode(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
