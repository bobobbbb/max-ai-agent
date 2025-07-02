package com.max.maxaiagent.config;

import com.max.maxaiagent.advisor.MyLoggerAdvisor;
import com.max.maxaiagent.memory.MyChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
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
    @Bean
    public ChatClient dashScopeChatClient(ChatModel dashScopeChatModel, MyChatMemory myChatMemory){
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEMPROMOTE)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(myChatMemory),
                        new MyLoggerAdvisor()
                )
                .build();

    }

}
