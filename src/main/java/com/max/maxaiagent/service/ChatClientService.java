package com.max.maxaiagent.service;

import com.max.maxaiagent.advisor.MyLoggerAdvisor;
import com.max.maxaiagent.memory.RedisChatMemory;
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
public class ChatClientService {
    private final ChatClient chatClient;
    //系统提示词
    private static final String SYSTEMPROMOTE="你是一位专门辅导面试的“计算机网络面霸型专家”，目标是帮助候选人通过技术面试。你熟知计算机网络的所有高频八股文题，了解面试官心理，知道怎样的答案最容易打动他们。\n" +
            "\n" +
            "对于每一个面试相关问题，请遵循以下格式回答：\n" +
            "\n" +
            "\uD83D\uDFE0 一句话通关总结： 用一句话提炼出核心观点，便于记忆。\n" +
            "\n" +
            "面试高分回答模板： 条理清晰、术语准确、语言简洁，展示候选人技术深度。\n" +
            "\n" +
            "面试加分技巧： 可选部分，点出候选人可以额外说什么来打动面试官，展现主动性或工程实践经验。\n" +
            "\n" +
            "回答风格应简洁、干脆、有逻辑感，突出“专业 + 熟练 + 思路清晰”。";
    private final Advisor aliRagCloudAdvisor;

    //创建一个DashScope的ChatModel
    public ChatClientService(ChatModel dashScopeChatModel, Advisor aliRagCloudAdvisor, RedisChatMemory redisChatMemory) {

        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEMPROMOTE)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(redisChatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
        this.aliRagCloudAdvisor = aliRagCloudAdvisor;
    }
    //开始会话
    public Flux<String> doChat(String message, String chatId) {
        System.out.println("doChat called with message: " + message + ", chatId: " + chatId);
        Flux<String> content = chatClient
                .prompt()
                .user(message)
                //会话记忆advisor
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //阿里云rag云服务advisor
                .advisors(aliRagCloudAdvisor)
                .stream()
                .content();
        // log。info打印内容
        content.subscribe(it -> log.info("doChat emit: " + it));
        return content;
    }

}
