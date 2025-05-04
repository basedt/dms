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
package com.basedt.dms.common.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;

public class DateTimeUtil {

    public static final String NORMAL_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String NORMAL_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String UTC_DATETIME_MS_WITH_ZONE_OFFSET_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String UTC_DATETIME_MS_WITH_OFFSET_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static final String NORMAL_DATE_PATTERN = "yyyy-MM-dd";

    public static final String CHINESE_DATE_PATTERN = "yyyy年MM月dd日";

    public static final String CHINESE_DATETIME_PATTERN = "yyyy年MM月dd日HH时mm分ss秒";


    public static Date toDate(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public static Date toDate(LocalDate localDate) {
        if (Objects.isNull(localDate)) {
            return null;
        }
        return toDate(localDate.atStartOfDay());
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (Objects.isNull(timestamp)) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(int seconds) {
        Instant instant = Instant.ofEpochSecond(seconds);
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(String dateStr, String... patterns) {
        if (Objects.isNull(dateStr)) {
            return null;
        }
        LocalDateTime localDateTime = null;
        for (String pattern : patterns) {
            localDateTime = toLocalDateTime(dateStr, pattern);
            if (Objects.isNull(localDateTime)) {
                continue;
            } else {
                break;
            }
        }
        return localDateTime;
    }

    private static LocalDateTime toLocalDateTime(String dateStr, String pattern) {
        LocalDateTime localDateTime = null;
        try {
            localDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            //nothing to do
        }
        return localDateTime;
    }

    public static LocalDate toLocalDate(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate toLocalDate(String dateStr, String pattern) {
        if (Objects.isNull(dateStr)) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static String toChar(LocalDateTime date, String pattern) {
        if (Objects.isNull(date)) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String toChar(long timestamp, String pattern) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return toChar(localDateTime, pattern);
    }

    public static Long getTimeInterval(LocalDateTime sd, LocalDateTime ed) {
        long sdTime = sd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long edTime = ed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return sdTime - edTime;
    }

    public static Long toTimeStamp(String dateStr) throws ParseException {
        if (StrUtil.isBlank(dateStr)) {
            return null;
        }
        DateTime dateTime = DateUtil.parse(dateStr);
        return dateTime.getTime();
    }

}
