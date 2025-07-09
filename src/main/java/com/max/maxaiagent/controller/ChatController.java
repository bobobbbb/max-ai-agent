package com.max.maxaiagent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.max.maxaiagent.common.Result;
import com.max.maxaiagent.service.ChatClientService;
import com.max.maxaiagent.vo.HistoryQuestionVO;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ChatController {

    @Autowired
    private ChatClientService chatClientService;


    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChat(@RequestParam String message, @RequestParam String chatId) {
        String loginIdAsString = StpUtil.getLoginIdAsString();
        return chatClientService.doChat(message, loginIdAsString + ":" + chatId);
    }

    @GetMapping("/getHistory")
    public Result<List<HistoryQuestionVO>> getHistoryChat(String chatId){
        return Result.success(chatClientService.getHistory(chatId));
    }

}
