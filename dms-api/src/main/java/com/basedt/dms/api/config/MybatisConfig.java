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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.basedt.dms.service.security.utils.SecurityUtil;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;

import static com.basedt.dms.api.config.MybatisConfig.BASE_PACKAGE_MAPPER;
import static com.basedt.dms.api.config.MybatisConfig.SQL_SESSION_FACTORY;
import static com.basedt.dms.common.constant.Constants.DATASOURCE_DYNAMIC;
import static com.basedt.dms.common.constant.Constants.TRANSACTION_MANAGER_MASTER;


@Configuration
@MapperScan(basePackages = BASE_PACKAGE_MAPPER, sqlSessionFactoryRef = SQL_SESSION_FACTORY)
public class MybatisConfig {

    public static final String SQL_SESSION_FACTORY = "sqlSessionFactory";

    public static final String BASE_PACKAGE_MAPPER = "com.basedt.dms.dao.mapper";


    @Primary
    @Bean(value = TRANSACTION_MANAGER_MASTER)
    public DataSourceTransactionManager transactionManager(@Qualifier(value = DATASOURCE_DYNAMIC) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public MybatisConfiguration mybatisConfiguration() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(Log4j2Impl.class);
        return configuration;
    }

    @Bean
    public GlobalConfig globalConfig(@Autowired MetaHandler metaHandler) {
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setMetaObjectHandler(metaHandler);
        return globalConfig;
    }

    @Primary
    @Bean(SQL_SESSION_FACTORY)
    public SqlSessionFactory sqlSessionFactory(@Qualifier(value = DATASOURCE_DYNAMIC) DataSource dataSource,
                                               MybatisConfiguration configuration,
                                               GlobalConfig globalConfig) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();

        MybatisPlusProperties properties = new MybatisPlusProperties();
        properties.setMapperLocations(new String[]{"classpath*:com.basedt.dms.dao.mapper/**/*.xml"});
        sqlSessionFactoryBean.setMapperLocations(properties.resolveMapperLocations());

        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.basedt.dms.dao.entity");
        sqlSessionFactoryBean.setPlugins(paginationInterceptor());
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    @Component
    public static class MetaHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            String userName = getUserNameOrDefault();
            this.strictInsertFill(metaObject, "creator", () -> userName, String.class);
            this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
            this.strictInsertFill(metaObject, "editor", () -> userName, String.class);
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            String userName = getUserNameOrDefault();
            this.strictUpdateFill(metaObject, "editor", () -> userName, String.class);
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        }

        private String getUserNameOrDefault() {
            String userName = SecurityUtil.getCurrentUserName();
            return StrUtil.isEmpty(userName) ? "sys" : userName;
        }

    }
}