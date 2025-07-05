package com.max.maxaiagent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.max.maxaiagent.common.Result;
import com.max.maxaiagent.service.ChatClientService;
import com.max.maxaiagent.vo.HistoryQuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ChatController {

    @Autowired
    private ChatClientService chatClientService;

    @PostMapping("/chat")
    public Result<Flux<String>> doChat(String message, String chatId){
        String loginIdAsString = StpUtil.getLoginIdAsString();
        return Result.success(chatClientService.doChat(message, loginIdAsString+":"+chatId));
    }
    @GetMapping("/getHistory")
    public Result<List<HistoryQuestionVO>> getHistoryChat(String chatId){
        return Result.success(chatClientService.getHistory(chatId));
    }

}
