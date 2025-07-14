package com.max.maxaiagent.memory;
 
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.max.maxaiagent.dto.ChatContentDTO;
import com.max.maxaiagent.entity.AiChatContext;
import com.max.maxaiagent.entity.AiChatQuestion;
import com.max.maxaiagent.service.AiChatContextService;
import com.max.maxaiagent.service.AiChatQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xtwang
 * @des RedisChatMemory
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MyChatMemory implements ChatMemory {

    private final AiChatContextService aiChatContextService;

    private final AiChatQuestionService  aiChatQuestionService;
    private static final String REDIS_KEY_PREFIX = "chatmemory:";
    private final RedisTemplate<String, ChatContentDTO> jsonRedisTemplate;
 
    @Override
    public void add(String conversationId, List<Message> messages) {
        //组装key
        String key = REDIS_KEY_PREFIX + conversationId;
        //利用雪花算法生成messageId
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        String messageId = snowflake.nextIdStr();
        //分割出userId和chatId
        String userId=conversationId.split(":")[0];
        String chatId=conversationId.split(":")[1];
        //组装redisjson格式信息
        ChatContentDTO chatContentDTO = new ChatContentDTO();
        chatContentDTO.setChatId(Long.valueOf(chatId));
        chatContentDTO.setMessageId(Long.valueOf(messageId));
        chatContentDTO.setMessages(messages);
        //todo 后续使用枚举类
        chatContentDTO.setStatus("unfinish");
        // 存储到 Redis
        jsonRedisTemplate.opsForList().rightPush(key, chatContentDTO);
        //创建AiChatContext实体并赋值
        AiChatContext aiChatContext = new AiChatContext();
        aiChatContext.setUserId(Long.valueOf(userId));
        aiChatContext.setChatId(chatId);
        aiChatContext.setContent(messages.toString());
        //插入数据库
        //todo 后续换成rabbitmq
        aiChatContextService.insertAiChatContext(aiChatContext);
        //检查如果是新会话，记录用户问题到数据库
        if(aiChatQuestionService.getChatQuestionByChatId(Long.valueOf(chatId)).isEmpty()){
            AiChatQuestion aiChatQuestion = new AiChatQuestion();
            aiChatQuestion.setUserId(Long.valueOf(userId));
            aiChatQuestion.setChatId(Long.valueOf(chatId));
            aiChatQuestion.setQuestion(messages.toString());
            aiChatQuestionService.saveQuestion(aiChatQuestion);
        }
    }
    @Override
    public List<Message> get(String conversationId, int lastN) {
        //组装redis的key
        String key = REDIS_KEY_PREFIX + conversationId;
        // 从 Redis 获取最新的 lastN 条消息
        List<ChatContentDTO> chatContentDTOList = jsonRedisTemplate.opsForList().range(key, -lastN, -1);
        //用于返回message
        List<Message> serializedMessages = new ArrayList<>();
        //遍历chatContentDTOList，将每个chatContentDTO的messages添加到serializedMessages中
        if(chatContentDTOList.size()>0 && !chatContentDTOList.isEmpty()){
            for(ChatContentDTO chatContentDTO:chatContentDTOList){
                serializedMessages.addAll(chatContentDTO.getMessages());
            }
        }
        return serializedMessages;
    }
 
    @Override
    public void clear(String conversationId) {
        jsonRedisTemplate.delete(REDIS_KEY_PREFIX + conversationId);
    }
 
}