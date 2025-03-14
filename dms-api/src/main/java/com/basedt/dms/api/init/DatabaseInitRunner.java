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
package com.basedt.dms.api.init;

import com.basedt.dms.plugins.datasource.utils.JdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Order(1)
@Slf4j
@Component
public class DatabaseInitRunner implements ApplicationRunner {

    @Value("${spring.datasource.druid.master.url}")
    private String jdbc_url;

    @Value("${spring.datasource.druid.master.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.druid.master.username}")
    private String userName;

    @Value("${spring.datasource.druid.master.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isNeedInit()) {
            log.info("Initialize database script");
            List<InputStream> inList = new ArrayList<>();
            inList.add(ClassUtils.getDefaultClassLoader().getResourceAsStream("db/init/dms.sql"));
            inList.add(ClassUtils.getDefaultClassLoader().getResourceAsStream("db/init/dms_quartz.sql"));
            Connection conn = null;
            try {
                conn = JdbcUtil.getConnection(this.jdbc_url, this.driverClassName, this.userName, this.password, null);
                for (InputStream in : inList) {
                    ScriptRunner scriptRunner = new ScriptRunner(conn);
                    scriptRunner.setSendFullScript(true);
                    scriptRunner.setLogWriter(null);
                    scriptRunner.runScript(new InputStreamReader(in));
                }
            } catch (Exception e) {
                log.info("sql script init error : {}", e.getMessage());
            } finally {
                JdbcUtil.close(conn);
            }
        }
    }

    private boolean isNeedInit() {
        Connection conn = null;
        PreparedStatement pstm = null;
        try {
            conn = JdbcUtil.getConnection(this.jdbc_url, this.driverClassName, this.userName, this.password, null);
            String sql = "select 1 from sys_config";
            pstm = conn.prepareStatement(sql);
            pstm.executeQuery();
            return false;
        } catch (Exception e) {
            log.info(e.getMessage());
            return true;
        } finally {
            JdbcUtil.closeSilently(conn, pstm);
        }
    }
}
