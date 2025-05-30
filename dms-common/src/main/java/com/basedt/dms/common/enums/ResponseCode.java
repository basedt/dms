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
package com.basedt.dms.common.enums;

import lombok.Getter;

/**
 * @author gleiyu
 */
@Getter
public enum ResponseCode {
    SUCCESS("204", "response.success"),
    ERROR_NO_PRIVILEGE("403", "response.error.no.privilege"),
    ERROR_UNAUTHORIZED("401", "response.error.unauthorized"),
    ERROR("500", "response.error"),
    ERROR_CUSTOM("530", "response.error"),
    ERROR_EMAIL("531", "response.error.email"),
    ERROR_INVALID_ARGUMENT("532", "response.error.invalid.argument"),
    ERROR_DUPLICATE_DATA("533", "response.error.duplicate.data"),
    ERROR_CATALOG_NOTNULL("533", "response.error.file.catalog.notNull"),
    ERROR_DB_TYPE_NOT_SUPPORTED("533", "response.error.db.typeNotSupported"),;

    private final String value;
    private final String label;

    ResponseCode(String value, String label) {
        this.label = label;
        this.value = value;
    }
}
