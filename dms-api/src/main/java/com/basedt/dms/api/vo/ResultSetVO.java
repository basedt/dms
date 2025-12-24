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
package com.basedt.dms.api.vo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.SqlUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.enums.DataType;
import com.basedt.dms.common.utils.DateTimeUtil;
import lombok.Data;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.ResultSetDynaClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ResultSetVO {

    private String id;

    private String sql;

    private List<ColumnVO> columns;

    private List<JSONObject> data;

    public ResultSetVO(ResultSet rs, Boolean editable) throws SQLException {
        ResultSetDynaClass resultSetDynaClass = new ResultSetDynaClass(rs, true, true);
        DynaProperty[] cols = resultSetDynaClass.getDynaProperties();
        this.columns = new ArrayList<>(cols.length);
        for (DynaProperty col : cols) {
            ColumnVO columnVO = new ColumnVO();
            String name = formatColumnName(col.getName());
            Class<?> type = col.getType();
            columnVO.setKey(name);
            columnVO.setTitle(name);
            columnVO.setDataIndex(name);
            columnVO.setDataType(convertDataType(type));
            columnVO.setIsReadOnly(!editable);
            this.columns.add(columnVO);
        }
        this.data = convert2JsonNode(resultSetDynaClass.iterator());
    }

    public List<ColumnVO> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnVO> columns) {
        this.columns = columns;
    }

    public List<JSONObject> getData() {
        return data;
    }

    public void setData(List<JSONObject> data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    private DataType convertDataType(Class<?> type) {
        if (Long.class.getName().equals(type.getName())
                || Integer.class.getName().equals(type.getName())
                || Short.class.getName().equals(type.getName())
                || Byte.class.getName().equals(type.getName())
                || Float.class.getName().equals(type.getName())
                || Double.class.getName().equals(type.getName())
        ) {
            return DataType.NUMBER;
        } else if (Boolean.class.getName().equals(type.getName())) {
            return DataType.TEXT;
        } else if (Timestamp.class.getName().equals(type.getName())) {
            return DataType.TIMESTAMP;
        } else if (Date.class.getName().equals(type.getName())) {
            return DataType.DATESTRING;
        }
        return DataType.TEXT;
    }

    private List<JSONObject> convert2JsonNode(Iterator<DynaBean> iterator) {
        List<JSONObject> resultList = new ArrayList<>();
        while (iterator.hasNext()) {
            DynaBean bean = iterator.next();
            DynaProperty[] cols = bean.getDynaClass().getDynaProperties();
            JSONObject node = JSONUtil.createObj();
            for (DynaProperty col : cols) {
                String colName = formatColumnName(col.getName());
                if (col.getType().getName().equals(Timestamp.class.getName())) {
                    Timestamp value = (Timestamp) bean.get(col.getName());
                    if (Objects.isNull(value)) {
                        node.set(colName, null);
                    } else {
                        node.set(colName, DateTimeUtil.toChar(value.getTime(), DateTimeUtil.NORMAL_DATETIME_MS_PATTERN));
                    }
                } else if (col.getType().getName().equals(Date.class.getName())) {
                    Date value = (Date) bean.get(col.getName());
                    if (Objects.isNull(value)) {
                        node.set(colName, null);
                    } else {
                        node.set(colName, DateTimeUtil.toChar(value.getTime(), DateTimeUtil.NORMAL_DATE_PATTERN));
                    }
                } else if (col.getType().getName().equals(java.sql.Array.class.getName())) {
                    Object value = bean.get(col.getName());
                    if (Objects.isNull(value)) {
                        node.set(colName, null);
                    } else {
                        node.set(colName, String.valueOf(value));
                    }
                } else if (col.getType().getName().equals("oracle.jdbc.OracleClob")) {
                    Clob value = (Clob) bean.get(col.getName());
                    if (Objects.isNull(value)) {
                        node.set(colName, null);
                    } else {
                        node.set(colName, SqlUtil.clobToStr(value));
                    }
                } else {
                    Object value = bean.get(col.getName());
                    node.set(colName, value);
                }
            }
            resultList.add(node);
            if (resultList.size() > 10000) {
                break;
            }
        }
        return resultList;
    }

    private String formatColumnName(String originName) {
        if (StrUtil.isEmpty(originName)) {
            return "";
        } else if (originName.contains(Constants.SEPARATOR_DOT)) {
            return StrUtil.subAfter(originName, Constants.SEPARATOR_DOT, false);
        } else {
            return originName;
        }
    }


    @Data
    public class ColumnVO {

        private String key;

        private String title;

        private String dataIndex;

        private DataType dataType;

        private Boolean isReadOnly;

    }
}
