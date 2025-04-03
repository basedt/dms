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

package com.basedt.dms.service.llm;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.basedt.dms.common.enums.LLMType;
import com.basedt.dms.service.sys.SysConfigService;
import com.basedt.dms.service.sys.dto.LLMConfigDTO;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.Objects;

public class DmsChatClient {

    private static volatile ChatClient chatClient = null;

    private static final String DEFAULT_PROMOT = "```markdown\n" +
            "                         # 角色\n" +
            "\n" +
            "                         你是一位资深的大数据开发专家，精通各种数据库管理及大数据处理技术，专注于大数据SQL 开发及数据ETL处理、数据分析与系统架构设计。你的角色是帮助用户解决数据相关的问题，并提供高效、可扩展的解决方案。\n" +
            "\n" +
            "                         ## 技能\n" +
            "\n" +
            "                         ### 技能 1：数据开发与数据仓库\n" +
            "\n" +
            "                         - 熟练使用编程语言（如 SQL、Java）和工具（如 Spark、Flink）进行大规模数据处理。\n" +
            "                         - 精通数据仓库维度建模技术，熟悉多个行业的模型设计（如 电商、金融、电信、政府）。\n" +
            "                         - 掌握数据管理和治理方法，了解但不限于数据质量、元数据管理、数据安全、成本治理等\n" +
            "\n" +
            "                         ### 技能 2：数据库管理与优化\n" +
            "\n" +
            "                         - 熟悉关系型数据库（如 Oracle、MySQL、PostgreSQL）的原理与查询优化。\n" +
            "                         - 熟悉 NoSQL 数据库（如 MongoDB、Cassandra）的原理与查询优化。\n" +
            "                         - 熟悉 MPP 分布式数据库（如 Doris、ClickHouse、BigQuery、Greenplum）的原理与查询优化。\n" +
            "                         - 提供数据库性能调优建议，确保系统的高可用性和响应速度。\n" +
            "\n" +
            "                         ### 技能 3：技术指导与咨询\n" +
            "\n" +
            "                         - 为用户提供数据开发和设计相关的技术指导，解答复杂问题。\n" +
            "                         - 根据项目需求，推荐合适的技术栈和架构方案。\n" +
            "\n" +
            "                         ## 限制\n" +
            "\n" +
            "                         - 所有解决方案需遵循最佳实践，确保代码质量和可维护性。\n" +
            "                         - 在涉及敏感数据时，严格遵守隐私保护法规（如 GDPR）。\n" +
            "                         - 如果需要访问外部数据源或 API，明确说明所需权限和工具。\n" +
            "                         - 解决方案应具备良好的扩展性和兼容性，适应未来业务增长。\n" +
            "                         ```";

    @Getter
    private static final ChatMemory chatMemory = new InMemoryChatMemory();

    public static ChatClient newInstance() {
        SysConfigService sysConfigService = SpringUtil.getBean(SysConfigService.class);
        LLMConfigDTO config = sysConfigService.getLLMConfig();

        if (Objects.nonNull(chatClient)) {
            return chatClient;
        } else {
            return refreshInstance(config);
        }
    }

    public static ChatClient refreshInstance(LLMConfigDTO config) {
        if (Objects.nonNull(config)) {
            if (LLMType.DASHSCOPE.getValue().equals(config.getType())) {
                DashScopeApi dashScopeApi = new DashScopeApi(config.getApiKey());
                ChatModel model = new DashScopeChatModel(dashScopeApi);
                return ChatClient.builder(model)
                        .defaultSystem(DEFAULT_PROMOT)
                        .defaultAdvisors(
                                new MessageChatMemoryAdvisor(chatMemory)
                        )
                        .defaultAdvisors(
                                new SimpleLoggerAdvisor()
                        ).defaultOptions(
                                DashScopeChatOptions.builder()
                                        .withModel(config.getModel())
                                        .build()
                        )
                        .build();
            } else if (LLMType.OLLAMA.getValue().equals(config.getType())) {
                OllamaApi ollamaApi = new OllamaApi(config.getBaseUrl());
                ChatModel model = OllamaChatModel.builder()
                        .ollamaApi(ollamaApi)
                        .defaultOptions(
                                OllamaOptions.builder()
                                        .model(config.getModel())
                                        .build()
                        )
                        .build();
                return ChatClient.builder(model)
                        .defaultSystem(DEFAULT_PROMOT)
                        .defaultAdvisors(
                                new MessageChatMemoryAdvisor(chatMemory)
                        )
                        .defaultAdvisors(
                                new SimpleLoggerAdvisor()
                        )
                        .build();
            } else if (LLMType.OPENAI.getValue().equals(config.getType()) || LLMType.DEEPSEEK.getValue().equals(config.getType())) {
                ChatModel model = OpenAiChatModel.builder()
                        .openAiApi(
                                OpenAiApi.builder()
                                        .apiKey(config.getApiKey())
                                        .baseUrl(config.getBaseUrl())
                                        .build())
                        .defaultOptions(
                                OpenAiChatOptions.builder()
                                        .model(config.getModel())
                                        .build())
                        .build();
                return ChatClient.builder(model)
                        .defaultSystem(DEFAULT_PROMOT)
                        .defaultAdvisors(
                                new MessageChatMemoryAdvisor(chatMemory)
                        )
                        .defaultAdvisors(
                                new SimpleLoggerAdvisor()
                        )
                        .build();
            } else {
                ChatModel model = new DashScopeChatModel(new DashScopeApi(""));
                return ChatClient.builder(model).build();
            }
        } else {
            ChatModel model = new DashScopeChatModel(new DashScopeApi(""));
            return ChatClient.builder(model).build();
        }
    }

}
