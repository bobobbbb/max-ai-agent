package com.max.maxaiagent.service;

import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class ChatClientService {
    @Autowired
    private ChatClient dashScopeChatClient;
    @Autowired
    private Advisor aliRagCloudAdvisor;
    @Value("${lastN}")
    private int lastN;
    //开始会话
    public Flux<String> doChat(String message, String chatId) {
        System.out.println("doChat called with message: " + message + ", chatId: " + chatId);
        Flux<String> content = dashScopeChatClient
                .prompt()
                .user(message)
                //会话记忆advisor
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, lastN))
                //阿里云rag云服务advisor
                .advisors(aliRagCloudAdvisor)
                .stream()
                .content();
        content.subscribe(it -> log.info("doChat emit: " + it));
        return content;
    }

}
