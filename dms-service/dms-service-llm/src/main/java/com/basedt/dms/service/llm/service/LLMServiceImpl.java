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

package com.basedt.dms.service.llm.service;

import cn.hutool.json.JSONUtil;
import com.basedt.dms.service.llm.DmsChatClient;
import com.basedt.dms.service.llm.LLMService;
import com.basedt.dms.service.llm.dto.ChatMsgDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


@Service
public class LLMServiceImpl implements LLMService {

    private static final Integer CHAT_MEMORY_RETRIEVE_SIZE = 100;

    @Override
    public String simpleChat(List<String> message, String cid) {
        ChatClient client = DmsChatClient.newInstance();
        Prompt prompt = new Prompt(getUserMessages(message));
        String result = client.prompt(prompt)
                .advisors(spec ->
                        spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, cid)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE)
                )
                .call()
                .content();
        ChatMsgDTO chatMsg = new ChatMsgDTO(result);
        return JSONUtil.toJsonStr(chatMsg);
    }

    @Override
    public Flux<String> streamChat(List<String> message, String cid) {
        ChatClient client = DmsChatClient.newInstance();
        Prompt prompt = new Prompt(getUserMessages(message));
        return client.prompt(prompt)
                .advisors(spec ->
                        spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, cid)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE)
                )
                .stream()
                .content()
                .map(msg -> {
                    ChatMsgDTO chatMsg = new ChatMsgDTO(msg);
                    return JSONUtil.toJsonStr(chatMsg);
                });
    }

    private List<Message> getUserMessages(List<String> message) {
        List<Message> messagList = new ArrayList<>();
        for (String msg : message) {
            Message user = new UserMessage(msg);
            messagList.add(user);
        }
        return messagList;
    }

}
