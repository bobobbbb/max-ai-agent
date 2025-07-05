package com.max.maxaiagent.service;

import cn.dev33.satoken.stp.StpUtil;
import com.max.maxaiagent.entity.AiChatQuestion;
import com.max.maxaiagent.vo.HistoryQuestionVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class ChatClientService {
    @Autowired
    private ChatClient dashScopeChatClient;
    @Autowired
    private AiChatQuestionService aiChatQuestionService;

    @Autowired
    private Advisor aliRagCloudAdvisor;
    @Value("${lastN}")
    private int lastN;
    //开始会话
    public Flux<String> doChat(String message, String chatId) {
        System.out.println("doChat called with message: " + message + ", chatId: " + chatId);
        Flux<String> content = dashScopeChatClient
                .prompt()
                .user(message)
                //会话记忆advisor
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, lastN))
                //阿里云rag云服务advisor
                .advisors(aliRagCloudAdvisor)
                .stream()
                .content();
        content.subscribe(it -> log.info("doChat emit: " + it));
        return content;
    }
    public List<HistoryQuestionVO> getHistory(String chatId){
        //查询用户最后问的10条问题
        List<AiChatQuestion> aiChatQuestions = aiChatQuestionService.getChatQuestionByUserId(StpUtil.getLoginIdAsLong());
        //组装vo
        return aiChatQuestions.stream().map(aiChatQuestion -> {
            HistoryQuestionVO historyQuestionVO = new HistoryQuestionVO();
            historyQuestionVO.setQuestion(aiChatQuestion.getQuestion());
            historyQuestionVO.setUserId(StpUtil.getLoginIdAsLong());
            historyQuestionVO.setChatId(aiChatQuestion.getChatId());
            return historyQuestionVO;
        }).toList();
    }
}
