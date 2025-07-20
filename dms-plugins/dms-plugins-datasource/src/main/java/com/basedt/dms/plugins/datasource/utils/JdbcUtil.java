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
package com.basedt.dms.plugins.datasource.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcUtil {

    private final static Logger log = LoggerFactory.getLogger(JdbcUtil.class);

    public final static ConcurrentHashMap<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<>();

    public static Connection getConnection(String jdbcUrl, String driverClassName, String userName, String password, Properties attrs) throws ClassNotFoundException, SQLException {
        Class.forName(driverClassName);
        Properties props = new Properties();
        props.put("user", userName);
        props.put("password", password);
        if (attrs != null) {
            props.putAll(attrs);
        }
        return DriverManager.getConnection(jdbcUrl, props);
    }

    public static Connection getConnectionSilently(String jdbcUrl, String driverClassName, String userName, String password, Properties attrs) {
        Connection conn = null;
        try {
            conn = getConnection(jdbcUrl, driverClassName, userName, password, attrs);
        } catch (ClassNotFoundException | SQLException e) {
            log.error("get database connection error : {}", e.getMessage());
        }
        return conn;
    }

    public static Connection getConnectionSilently(String jdbcUrl, String driverClassName, String userName, String password, Map<String, String> attrs) {
        Properties props = new Properties();
        props.putAll(attrs);
        return getConnectionSilently(jdbcUrl, driverClassName, userName, password, props);
    }

    public static Connection getConnectionSilently(String jdbcUrl, String driverClassName, String userName, String password) {
        Properties props = new Properties();
        return getConnectionSilently(jdbcUrl, driverClassName, userName, password, props);
    }

    public static DataSource getDataSource(String datasourceName, String jdbcUrl, String driverClassName, String userName, String password, Map<String, String> attrs) {
        Properties props = new Properties();
        props.putAll(attrs);
        return getDataSource(datasourceName, jdbcUrl, driverClassName, userName, password, props);
    }

    public static DataSource getDataSource(String datasourceName, String jdbcUrl, String driverClassName, String userName, String password, Properties attrs) {
        if (Objects.nonNull(datasourceName) && JdbcUtil.DATA_SOURCE_MAP.containsKey(datasourceName)) {
            return JdbcUtil.DATA_SOURCE_MAP.get(datasourceName);
        }
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(jdbcUrl);
        dataSource.setMinIdle(5);
        dataSource.setMaxActive(20);
        dataSource.setRemoveAbandoned(true);
        dataSource.setRemoveAbandonedTimeout(5);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(30000);
        if (attrs != null) {
            try {
                DruidDataSourceFactory.config(dataSource, attrs);
            } catch (SQLException e) {
                log.error("get dataSource error : {}", e.getMessage());
            }
        }
        JdbcUtil.DATA_SOURCE_MAP.putIfAbsent(datasourceName, dataSource);
        return dataSource;
    }

    public static int execute(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    public static void close(Connection conn, PreparedStatement pstm, ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        close(conn, pstm);
    }

    public static void close(Connection conn, Statement stm, ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        close(conn, stm);
    }

    public static void close(Connection conn, PreparedStatement pstm) throws SQLException {
        if (pstm != null) {
            pstm.close();
        }
        close(conn);

    }

    public static void close(Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    public static void close(Connection conn, Statement stm) throws SQLException {
        if (stm != null) {
            stm.close();
        }
        close(conn);
    }

    public static void close(Connection conn, ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        close(conn);
    }

    public static void closeSilently(Connection conn, PreparedStatement pstm, ResultSet rs) {
        try {
            close(conn, pstm, rs);
        } catch (SQLException e) {
            log.error("jdbc connection close error : {}", e.getMessage());
        }
    }

    public static void closeSilently(Connection conn, PreparedStatement pstm) {
        try {
            close(conn, pstm);
        } catch (SQLException e) {
            log.error("jdbc connection close error : {}", e.getMessage());
        }
    }

    public static void closeSilently(Connection conn) {
        try {
            close(conn);
        } catch (SQLException e) {
            log.error("jdbc connection close error : {}", e.getMessage());
        }

    }

}
