package com.max.maxaiagent.memory;
 
import cn.dev33.satoken.stp.StpUtil;
import com.max.maxaiagent.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
/**
 * @author xtwang
 * @des RedisChatMemory
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatMemory implements ChatMemory {
    private static final String REDIS_KEY_PREFIX = "chatmemory:";
    private final RedisTemplate<String, Message> redisTemplate;
 
    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = REDIS_KEY_PREFIX + conversationId;
        // 存储到 Redis
        redisTemplate.opsForList().rightPushAll(key, messages);
    }
 
    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = REDIS_KEY_PREFIX + conversationId;
        // 从 Redis 获取最新的 lastN 条消息
        List<Message> serializedMessages = redisTemplate.opsForList().range(key, -lastN, -1);
        if (serializedMessages != null) {
            return serializedMessages;
        }
        return List.of();
    }
 
    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(REDIS_KEY_PREFIX + conversationId);
    }
 
}