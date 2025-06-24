package com.max.maxaiagent.controller;

import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class ChatController {
    public Flux<String> doChat(String message, String chatId){
        return Flux.just("Hello World");
    }
}
