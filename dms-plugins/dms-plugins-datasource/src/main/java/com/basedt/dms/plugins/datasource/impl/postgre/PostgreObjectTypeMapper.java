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
package com.basedt.dms.plugins.datasource.impl.postgre;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;

import java.util.Objects;

public class PostgreObjectTypeMapper {

    public static final String POSTGRE_TABLE = "r";

    public static final String POSTGRE_VIEW = "v";

    public static final String POSTGRE_FOREIGN_TABLE = "f";

    public static final String POSTGRE_SEQUENCE = "S";

    public static final String POSTGRE_INDEX = "i";

    public static final String POSTGRE_MATERIALIZED_VIEW = "m";

    public static final String POSTGRE_FUNCTION = "F";


    public static String mapToOrigin(DbObjectType type) {
        if (Objects.isNull(type)) {
            return null;
        }
        switch (type) {
            case TABLE:
                return POSTGRE_TABLE;
            case VIEW:
                return POSTGRE_VIEW;
            case FOREIGN_TABLE:
                return POSTGRE_FOREIGN_TABLE;
            case SEQUENCE:
                return POSTGRE_SEQUENCE;
            case INDEX:
                return POSTGRE_INDEX;
            case MATERIALIZED_VIEW:
                return POSTGRE_MATERIALIZED_VIEW;
            case FUNCTION:
                return POSTGRE_FUNCTION;
            default:
                return null;
        }
    }

    public static DbObjectType mapToStandard(String type) {
        if (StrUtil.isEmpty(type)) {
            return null;
        }
        switch (type) {
            case POSTGRE_TABLE:
                return DbObjectType.TABLE;
            case POSTGRE_VIEW:
                return DbObjectType.VIEW;
            case POSTGRE_FOREIGN_TABLE:
                return DbObjectType.FOREIGN_TABLE;
            case POSTGRE_SEQUENCE:
                return DbObjectType.SEQUENCE;
            case POSTGRE_INDEX:
                return DbObjectType.INDEX;
            case POSTGRE_MATERIALIZED_VIEW:
                return DbObjectType.MATERIALIZED_VIEW;
            case POSTGRE_FUNCTION:
                return DbObjectType.FUNCTION;
            default:
                return null;
        }
    }

}
