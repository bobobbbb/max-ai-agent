package com.max.maxaiagent.service;

import com.max.maxaiagent.advisor.MyLoggerAdvisor;
import com.max.maxaiagent.memory.RedisChatMemory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
@AllArgsConstructor
public class ChatClientService {
    private final ChatClient dashScopeChatClient;
    private final Advisor aliRagCloudAdvisor;
    //开始会话
    public Flux<String> doChat(String message, String chatId) {
        System.out.println("doChat called with message: " + message + ", chatId: " + chatId);
        Flux<String> content = dashScopeChatClient
                .prompt()
                .user(message)
                //会话记忆advisor
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //阿里云rag云服务advisor
                .advisors(aliRagCloudAdvisor)
                .stream()
                .content();
        content.subscribe(it -> log.info("doChat emit: " + it));
        return content;
    }

}
