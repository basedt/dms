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

package com.basedt.dms.api.controller.workspace;

import cn.hutool.json.JSONUtil;
import com.basedt.dms.api.vo.ChatParamVO;
import com.basedt.dms.service.llm.LLMService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping(path = "/api/workspace/ai")
@Tag(name = "AI")
public class LLMChatController {

    private final LLMService llmService;

    public LLMChatController(LLMService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/simple/chat")
    public String simpleChat(@RequestBody ChatParamVO chatParam) {
      return  llmService.simpleChat(chatParam.getMessages(), chatParam.getCid());
    }

    @PostMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestParam(value = "query") String query, HttpServletResponse response) {
        ChatParamVO chatMsg = JSONUtil.toBean(query, ChatParamVO.class);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setHeader("Cache-Control", "no-cache");
        response.setBufferSize(0);
        Flux<String> result = llmService.streamChat(chatMsg.getMessages(), chatMsg.getCid());
        return result.map(msg -> ServerSentEvent.<String>builder()
                        .event("stream-chat")
                        .data(msg)
                        .build())
//                .doOnNext(event -> log.info("Sending event: {}", event))
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.<String>builder()
                                .data(e.getMessage())
                                .event("error")
                                .build()
                ));

    }

}
