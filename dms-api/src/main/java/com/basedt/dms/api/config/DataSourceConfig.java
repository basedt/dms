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
package com.basedt.dms.api.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.basedt.dms.service.base.datasource.RoutingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.basedt.dms.common.constant.Constants.*;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean(name = DATASOURCE_MASTER)
    @ConfigurationProperties(prefix = "spring.datasource.druid.master")
    public DataSource masterDataSource() {
        logger.info("init master datasource ...");
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = DATASOURCE_LOG)
    @ConfigurationProperties(prefix = "spring.datasource.druid.log")
    public DataSource logDataSource() {
        logger.info("init log datasource ...");
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = DATASOURCE_DYNAMIC)
    public DataSource dynamicDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dsMap = new HashMap<>();
        dsMap.put(DATASOURCE_MASTER, masterDataSource());
        dsMap.put(DATASOURCE_LOG, logDataSource());
        routingDataSource.setTargetDataSources(dsMap);
        return routingDataSource;
    }

}
