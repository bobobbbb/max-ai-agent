package com.max.maxaiagent.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.max.maxaiagent.entity.AiChatQuestion;
import com.max.maxaiagent.entity.AiChatContext;
import com.max.maxaiagent.vo.HistoryQuestionVO;
import com.max.maxaiagent.vo.ChatContextPageVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatResponse;
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
    private AiChatContextService aiChatContextService;

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
                .content()
                .doOnNext(System.out::println);
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
    
    /**
     * 根据chatId分页查询最新的10条聊天消息
     *
     * @param chatId 会话ID
     * @return 最新的10条聊天消息列表
     */
    public List<ChatContextPageVO> getLatestMessagesByChatId(String chatId) {
        log.info("根据chatId查询最新10条消息, chatId: {}", chatId);
        
        // 查询最新的10条聊天上下文
        List<AiChatContext> aiChatContexts = aiChatContextService.getLatestMessagesByChatId(chatId);
        
        // 转换为VO对象并返回
        return aiChatContexts.stream().map(context -> {
            ChatContextPageVO vo = new ChatContextPageVO();
            vo.setId(context.getId());
            vo.setUserId(context.getUserId());
            vo.setChatId(context.getChatId());
            vo.setContent(context.getContent());
            vo.setCreateTime(context.getCreateTime());
            vo.setUpdateTime(context.getUpdateTime());
            return vo;
        }).toList();
    }
}
