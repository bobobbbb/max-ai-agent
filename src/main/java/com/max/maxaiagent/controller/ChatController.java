package com.max.maxaiagent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.max.maxaiagent.service.ChatClientService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
@RestController
@RequestMapping("/client")
public class ChatController {

    @Autowired
    private ChatClientService chatClientService;

    @PostMapping("/chat")
    public Flux<String> doChat(String message, String chatId){
        String loginIdAsString = StpUtil.getLoginIdAsString();
        return chatClientService.doChat(message, loginIdAsString+":"+chatId);
    }
}
