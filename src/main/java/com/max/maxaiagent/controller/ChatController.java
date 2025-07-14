package com.max.maxaiagent.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import com.max.maxaiagent.common.Result;
import com.max.maxaiagent.service.ChatClientService;
import com.max.maxaiagent.vo.HistoryQuestionVO;
import com.max.maxaiagent.vo.ChatContextPageVO;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatClientService chatClientService;


    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChat(@RequestParam String message, 
                              @RequestParam String chatId,
                              HttpServletRequest request) {
        String loginIdAsString = StpUtil.getLoginIdAsString();

        // 获取前端发送的Last-Event-ID
        String lastEventId = request.getHeader("Last-Event-ID");

        return chatClientService.doChat(message, loginIdAsString + ":" + chatId,lastEventId);
    }

    @GetMapping("/getHistory")
    public Result<List<HistoryQuestionVO>> getHistoryChat(String chatId){
        return Result.success(chatClientService.getHistory(chatId));
    }

    /**
     * 根据chatId分页查询最新的10条聊天消息
     *
     * @param chatId 会话ID
     * @return 最新的10条聊天消息
     */
    @GetMapping("/getLatestMessages")
    public Result<List<ChatContextPageVO>> getLatestMessagesByChatId(@RequestParam String chatId) {
        List<ChatContextPageVO> messages = chatClientService.getLatestMessagesByChatId(chatId);
        return Result.success(messages);
    }

}
