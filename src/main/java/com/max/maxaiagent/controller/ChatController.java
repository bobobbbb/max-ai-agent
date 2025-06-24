package com.max.maxaiagent.controller;

import com.max.maxaiagent.service.ChatClientService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class ChatController {
    private ChatClientService chatClientService;
    public Flux<String> doChat(String message, String chatId){
        return chatClientService.doChat(message, chatId);
    }
}
