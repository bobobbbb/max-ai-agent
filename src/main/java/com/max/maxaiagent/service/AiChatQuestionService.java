package com.max.maxaiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.max.maxaiagent.entity.AiChatQuestion;
import com.max.maxaiagent.mapper.AiChatQuestionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class AiChatQuestionService extends ServiceImpl<AiChatQuestionMapper, AiChatQuestion> {

    public List<AiChatQuestion> getChatQuestionByChatId(Long chatId) {
        return list(new LambdaQueryWrapper<AiChatQuestion>()
                .eq(AiChatQuestion::getChatId, chatId)
                .orderByDesc(AiChatQuestion::getCreateTime));
    }

    public List<AiChatQuestion> getChatQuestionByUserId(Long userId , Integer offset, Integer pageSize) {
        return list(new LambdaQueryWrapper<AiChatQuestion>()
                .eq(AiChatQuestion::getUserId, userId)
                .orderByDesc(AiChatQuestion::getCreateTime)
                .last("LIMIT"+ pageSize +" OFFSET "+ offset));
    }
    
    public void saveQuestion(AiChatQuestion aiChatQuestion){
        save(aiChatQuestion);
    }
}
