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

import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class DruidConfig {

    private static final Logger logger = LoggerFactory.getLogger(DruidConfig.class);

    @Bean
    public ServletRegistrationBean statViewServlet() {
        logger.info("init druid dataSource pool statViewServlet...");
        ServletRegistrationBean<StatViewServlet> statViewServlet = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        statViewServlet.setInitParameters(new HashMap<String, String>(4) {{
            put("enabled", "true");
            put("resetEnable", "true");
            put("loginUsername", "admin");
            put("loginPassword", "admin");
        }});
        return statViewServlet;
    }

    @Bean
    public FilterRegistrationBean webStatFilter() {
        logger.info("init druid dataSource pool webStatFilter...");
        FilterRegistrationBean<WebStatFilter> webStatFilter = new FilterRegistrationBean<>(new WebStatFilter());
        webStatFilter.addUrlPatterns("/*");
        webStatFilter.setInitParameters(new HashMap<String, String>(2) {{
            put("enabled", "true");
            put("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        }});
        return webStatFilter;
    }

}
