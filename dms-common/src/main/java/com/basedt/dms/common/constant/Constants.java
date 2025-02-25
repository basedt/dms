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
package com.basedt.dms.common.constant;

public class Constants {

    public static final String DATASOURCE_DYNAMIC = "dynamicDataSource";

    public static final String DATASOURCE_MASTER = "masterDataSource";

    public static final String TRANSACTION_MANAGER_MASTER = "transactionManagerMaster";

    public static final String DATASOURCE_LOG = "logDataSource";

    public static final String SEPARATOR_COLON = ":";

    public static final String SEPARATOR_DOT = ".";

    public static final String SEPARATOR_SEMICOLON = ";";

    public static final String SEPARATOR_UNDERLINE = "_";

    public static final String SEPARATOR_DASHED = "-";

    public static final String SEPARATOR_EQUAL = "=";

    public static final String SEPARATOR_AMP = "&";

    public static final String LINE_FEED = "\n";

    public static final String REGEX_EXCLUDE_SPECIAL_CHAR = "[^\\s\\\\\\.\\[\\]\\{\\}\\(\\)\\<\\>\\*\\+\\-\\=\\!\\?\\^\\$\\|~、/,【】《》。·`：；“”\"':;]+";

    public static final String REGEX_WORD_CHAR = "[\\w.]+$";

    public static final String AUTH_CODE_KEY = "authCode-key_";

    public static final String SESSION_ID = "_sid";

    public static final String SESSION_USER = "_u";

    public static final String ROLE_SUPER_ADMIN = "_super_admin";

    public static final String ROLE_NORMAL = "_normal";

    public static final String DMS_CONFIG_ADMIN_INIT = "admin.init";

    public static final String ROOT_PRIVILEGE_CODE = "root";

    public static final String ROOT_CATALOG_CODE = "CATALOG_ROOT";

    public static final String CFG_EMAIL_CODE = "email";

    public static final String CODEC_STR_PREFIX = "Encrypted:";

    public static final String DATASOURCE_ATTR_JDBC = "jdbc";

    public static final String DATASOURCE_ATTR_POOL = "pool";

    public static final String JOB_PREFIX = "JOB_";

    public static final String JOB_GROUP_PREFIX = "JOB_GRP_";

    public static final String TRIGGER_PREFIX = "TRI_";

    public static final String TRIGGER_GROUP_PREFIX = "TRI_GRP_";

    public static final String TMP_RESOURCE_QUEUE_PREFIX = "RESOURCE_QUEUE_";

    public static final String ARROW_MMAP_FILE_PATH = "arrow_mmap_file_";

}
