package com.max.maxaiagent;

import com.max.maxaiagent.service.ChatClientService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 取消注释即可在 SpringBoot 项目启动时执行
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;
    @Resource
    private ChatClientService chatClientService;

    @Override
    public void run(String... args) throws Exception {
        chatClientService.doChat("你好", "1");
    }
}
