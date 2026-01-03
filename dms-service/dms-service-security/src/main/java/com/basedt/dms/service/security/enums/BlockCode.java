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
public enum BlockCode {
    DEFAULT("dft", "dms.p.block.dft"),
    DICT_TYPE("dct", "dms.p.sys.dic.dct.show"),
    DICT_DATA("dcd", "dms.p.sys.dic.dcd.show"),
    ROLE_LIST("rli", "dms.p.sys.rol.rli.show"),
    USER_LIST("uli", "dms.p.sys.usr.uli.show"),
    WORKSPACE_LIST("wpi", "dms.p.ws.wss.wpi.show"),
    DATASOURCE_LIST("wdl", "dms.p.ws.wsd.wdl.show"),
    HISTORY_LIST("whl", "dms.p.ws.wsh.whl.show"),
    EXPORT_LIST("whe", "dms.p.ws.wse.whe.show"),
    IMPORT_LIST("whi", "dms.p.ws.wsi.whi.show"),
    ;

    private final String value;
    private final String label;

    BlockCode(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
