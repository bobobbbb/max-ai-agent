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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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

    @Value("${redis_list_max_size}")
    private int maxSize;

    private final AiChatContextService aiChatContextService;

    private final AiChatQuestionService  aiChatQuestionService;

    private static final String REDIS_KEY_PREFIX = "chatmemory:";

    private static final String REDIS_STREAM_KEY_PREFIX = "chat-stream:";

    private final RedisTemplate<String, ChatContentDTO> jsonRedisTemplate;

    private static final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    @Autowired
    private RedisTemplate<String, Object> streamRedisTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public void add(String conversationId, List<Message> messages) {
        //组装key
        String key = REDIS_KEY_PREFIX + conversationId;
        //利用雪花算法生成messageId
        String messageId = snowflake.nextIdStr();
        //分割出userId和chatId
        String userId=conversationId.split(":")[0];
        String chatId=conversationId.split(":")[1];
        
        // 使用编程式事务管理数据库和Redis操作，保证强一致性
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    log.info("开始事务操作，会话ID: {}, 用户ID: {}", chatId, userId);
                    
                    //创建AiChatContext实体并赋值
                    AiChatContext aiChatContext = new AiChatContext();
                    aiChatContext.setUserId(Long.valueOf(userId));
                    aiChatContext.setChatId(chatId);
                    aiChatContext.setMessageId(messageId);
                    aiChatContext.setContent(messages.toString());
                    
                    // 插入聊天上下文到数据库
                    boolean insertResult = aiChatContextService.insertAiChatContext(aiChatContext);
                    if (!insertResult) {
                        log.error("插入聊天上下文失败，会话ID: {}", chatId);
                        throw new RuntimeException("插入聊天上下文失败");
                    }
                    
                    //检查如果是新会话，记录用户问题到数据库
                    if(aiChatQuestionService.getChatQuestionByChatId(Long.valueOf(chatId)).isEmpty()){
                        AiChatQuestion aiChatQuestion = new AiChatQuestion();
                        aiChatQuestion.setUserId(Long.valueOf(userId));
                        aiChatQuestion.setChatId(Long.valueOf(chatId));
                        aiChatQuestion.setQuestion(messages.toString());
                        
                        // 保存问题到数据库
                        aiChatQuestionService.saveQuestion(aiChatQuestion);
                        log.info("新会话问题保存成功，会话ID: {}", chatId);
                    }
                    
                    log.info("数据库操作成功，开始Redis操作，会话ID: {}", chatId);
                    
                    // 在同一个事务中执行Redis操作，保证强一致性
                    try {
                        //组装redisjson格式信息
                        ChatContentDTO chatContentDTO = new ChatContentDTO();
                        chatContentDTO.setChatId(Long.valueOf(chatId));
                        chatContentDTO.setMessageId(Long.valueOf(messageId));
                        chatContentDTO.setMessages(messages);
                        //todo 后续使用枚举类
                        chatContentDTO.setStatus("unfinish");
                        
                        // 使用Redis事务保证原子性：rightPush + trim + delete 要么全部成功，要么全部失败
                        executeRedisAtomicOperations(key, chatContentDTO, chatId);
                        
                        log.info("Redis操作成功，会话ID: {}", chatId);
                        
                    } catch (Exception redisException) {
                        log.error("Redis操作失败，会话ID: {}, 错误: {}", chatId, redisException.getMessage(), redisException);
                        // Redis操作失败时，回滚整个事务以保证数据一致性
                        // 因为查询策略依赖Redis和MySQL的强一致性
                        throw new RuntimeException("Redis操作失败，为保证数据一致性，回滚事务: " + redisException.getMessage(), redisException);
                    }
                    
                    log.info("事务操作全部成功，会话ID: {}", chatId);
                    
                } catch (Exception e) {
                    log.error("事务操作失败，会话ID: {}, 错误: {}", chatId, e.getMessage(), e);
                    // 标记事务回滚
                    status.setRollbackOnly();
                    throw new RuntimeException("事务操作失败: " + e.getMessage(), e);
                }
            }
        });
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

    /**
     * 执行Redis原子性操作，保证rightPush + trim + delete的原子性
     * 使用Lua脚本确保操作的原子性，避免部分成功部分失败的问题
     *
     * @param key Redis键
     * @param chatContentDTO 聊天内容DTO
     * @param chatId 会话ID
     */
    private void executeRedisAtomicOperations(String key, ChatContentDTO chatContentDTO, String chatId) {
        // 使用Lua脚本保证原子性，避免rightPush成功但trim失败的问题
        String luaScript = """
            -- 参数说明: KEYS[1]=listKey, KEYS[2]=streamKey, ARGV[1]=chatData, ARGV[2]=maxSize
            local listKey = KEYS[1]
            local streamKey = KEYS[2]
            local chatData = ARGV[1]
            local maxSize = tonumber(ARGV[2])
            
            -- 1. rightPush操作
            local pushResult = redis.call('RPUSH', listKey, chatData)
            
            -- 2. trim操作，保留最新的maxSize条数据
            local trimResult = redis.call('LTRIM', listKey, -maxSize, -1)
            
            -- 3. 删除流中的会话
            local delResult = redis.call('DEL', streamKey)
            
            -- 返回操作结果
            return {pushResult, trimResult, delResult}
        """;
        
        try {
            // 序列化聊天内容
            String serializedData = JSONUtil.toJsonStr(chatContentDTO);
            String streamKey = REDIS_STREAM_KEY_PREFIX + chatId;
            
            // 使用更简洁的方式执行Lua脚本
            Object result = jsonRedisTemplate.execute((RedisCallback<Object>) (connection) -> {
                return connection.scriptingCommands().eval(
                    luaScript.getBytes(),
                    ReturnType.MULTI,
                    2,
                    key.getBytes(),
                    streamKey.getBytes(),
                    serializedData.getBytes(),
                    String.valueOf(maxSize).getBytes()
                );
            });
            
            log.info("Redis Lua脚本执行成功，会话ID: {}, 结果: {}", chatId, result);
            
        } catch (Exception e) {
            log.error("Redis Lua脚本执行失败，会话ID: {}, 错误: {}", chatId, e.getMessage(), e);
            throw new RuntimeException("Redis原子操作失败: " + e.getMessage(), e);
        }
    }
 
}