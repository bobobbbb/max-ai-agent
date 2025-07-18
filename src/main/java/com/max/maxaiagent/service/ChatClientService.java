package com.max.maxaiagent.service;

import cn.dev33.satoken.stp.StpUtil;
import com.max.maxaiagent.dto.ChatContentDTO;
import com.max.maxaiagent.entity.AiChatQuestion;
import com.max.maxaiagent.entity.AiChatContext;
import com.max.maxaiagent.vo.HistoryQuestionVO;
import com.max.maxaiagent.vo.ChatContextPageVO;
import com.max.maxaiagent.vo.PageResult;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class ChatClientService {

    private static final String CHAT_STREAM = "chat-stream:";

    @Value("${lastN}")
    private int lastN;

    @Autowired
    private ChatClient dashScopeChatClient;

    @Autowired
    private AiChatQuestionService aiChatQuestionService;
    
    @Autowired
    private AiChatContextService aiChatContextService;

    @Autowired
    private Advisor aliRagCloudAdvisor;

    private static final String REDIS_KEY_PREFIX = "chatmemory:";
    
    @Autowired
    private RedisTemplate<String, Object> streamRedisTemplate;

    @Autowired
    private RedisTemplate<String, ChatContentDTO> jsonRedisTemplate;


    
    /**
     * 格式化SSE事件
     */
    private String formatSseEvent(MapRecord<String, Object, Object> record) {
        // 获取消息内容
        String message = (String) record.getValue().get("message");
        // 获取记录ID作为事件ID
        String eventId = record.getId().getValue();
        
        // 转换为SSE格式
        return "id: " + eventId + "\n" +
               "data: " + (message != null ? message : "") + "\n\n";
    }
    /**
     * 开始会话并返回消息流
     * 
     * @param message 用户输入的消息
     * @param chatId 会话ID
     * @param lastEventId 最后一条消息的ID,用于断线重连
     * @return 消息流
     */
    public Flux<String> doChat(String message, String chatId, String lastEventId) {
        // 如果lastEventId为空,说明是新会话
        if (lastEventId == null) {
            // 当前毫秒时间戳
            long timestamp = System.currentTimeMillis();
            // 同1ms内可能有多条消息,使用原子序列号区分
            AtomicInteger sequence = new AtomicInteger(0);

            // 调用AI服务获取回复
            Flux<String> content = dashScopeChatClient
                    .prompt()
                    .user(message)
                    // 添加会话记忆advisor
                    .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, lastN))
                    // 添加阿里云RAG服务advisor
                    .advisors(aliRagCloudAdvisor)
                    .stream()
                    .content()
                    .map(text -> {
                        // 生成递增ID
                        String streamId = timestamp + "-" + sequence.getAndIncrement();
                        // 将消息存储到Redis Stream
                        streamRedisTemplate.opsForStream().add(
                                StreamRecords.mapBacked(Map.of("message", text))
                                        .withStreamKey(CHAT_STREAM + chatId)
                                        .withId(RecordId.of(streamId))
                        );
                        // 转换为SSE格式
                        return "id: " + streamId + "\n" +
                                "data: " + text + "\n\n";
                    })
                    .doOnNext(System.out::println);
            return content;
        } else {
            // 断线重连场景,需要读取历史消息
            String streamKey = "chat-stream" + chatId;

            try {
                // 从Redis Stream读取历史消息
                List<MapRecord<String, Object, Object>> historyMessages = streamRedisTemplate
                        .opsForStream()
                        .range(streamKey, Range.closed("-", lastEventId));

                if (historyMessages != null) {
                    // 将历史消息转换为Flux
                    Flux<String> historyMessagesFlux = Flux.fromIterable(historyMessages)
                            .map(this::formatSseEvent)
                            .doOnNext(System.out::println);

                    // 实时订阅新消息
                    Flux<String> liveMessagesFlux = Flux.create(emitter -> {
                        try {
                            String currentOffset = lastEventId;
                            while (!emitter.isCancelled()) {
                                // 阻塞式读取新消息
                                List<MapRecord<String, Object, Object>> records =
                                        streamRedisTemplate.opsForStream()
                                                .read(StreamReadOptions.empty()
                                                                .block(Duration.ofSeconds(30))  // 30秒超时
                                                                .count(10),  // 每次最多读10条
                                                        StreamOffset.create(streamKey, ReadOffset.from(currentOffset)));

                                if (records != null && !records.isEmpty()) {
                                    for (MapRecord<String, Object, Object> record : records) {
                                        String recordId = record.getId().getValue();
                                        // 避免重复发送lastEventId
                                        if (!recordId.equals(lastEventId) && !emitter.isCancelled()) {
                                            emitter.next(formatSseEvent(record));
                                            currentOffset = recordId;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("实时订阅Redis Stream消息失败, streamKey: {}, error: {}", 
                                    streamKey, e.getMessage(), e);
                            if (!emitter.isCancelled()) {
                                emitter.error(e);
                            }
                        } finally {
                            if (!emitter.isCancelled()) {
                                emitter.complete();
                            }
                        }
                    });

                    // 合并历史消息和实时消息
                    return Flux.concat(historyMessagesFlux, liveMessagesFlux);
                } else {
                    log.info("没有找到历史消息, streamKey: {}, lastEventId: {}", streamKey, lastEventId);
                    return Flux.empty();
                }
            } catch (Exception e) {
                log.error("读取Redis Stream历史消息失败, streamKey: {}, lastEventId: {}, error: {}", 
                        streamKey, lastEventId, e.getMessage(), e);
                return Flux.empty();
            }
        }
    }

    public PageResult<HistoryQuestionVO> getHistory(Integer pageNum, Integer pageSize){

        Integer offset = (pageNum-1)*pageSize;

        //查询用户最后问的10条问题
        List<AiChatQuestion> aiChatQuestions = aiChatQuestionService.getChatQuestionByUserId(StpUtil.getLoginIdAsLong(),offset,pageSize);

        Long total = (long) aiChatQuestions.size();

        //组装vo
        List<HistoryQuestionVO> list = aiChatQuestions.stream().map(aiChatQuestion -> {
            HistoryQuestionVO historyQuestionVO = new HistoryQuestionVO();
            historyQuestionVO.setQuestion(aiChatQuestion.getQuestion());
            historyQuestionVO.setUserId(StpUtil.getLoginIdAsLong());
            historyQuestionVO.setChatId(aiChatQuestion.getChatId());
            return historyQuestionVO;
        }).toList();

        return PageResult.of(pageNum,pageSize,total,list);
    }
    
    /**
     * 根据chatId分页查询聊天消息
     *
     * @param chatId 会话ID
     * @param pageNum 页码，从1开始
     * @param pageSize 每页大小
     * @return 分页的聊天消息结果
     */
    public PageResult<ChatContextPageVO> getLatestMessagesByChatId(String chatId, Integer pageNum, Integer pageSize) {

        log.info("根据chatId分页查询消息, chatId: {}, pageNum: {}, pageSize: {}", chatId, pageNum, pageSize);
        
        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        int offset = (pageNum-1)*pageSize;

        String key = REDIS_KEY_PREFIX + chatId;

        List<ChatContextPageVO> records = new ArrayList<>();
        //如果大于20的话，走mysql
        if(offset<=20){

            List<ChatContentDTO> chatContentDTOList = jsonRedisTemplate.opsForList().range(key, -10 - offset, -1 - offset);

            if(chatContentDTOList!=null && !chatContentDTOList.isEmpty()) {
                for (ChatContentDTO chatContentDTO : chatContentDTOList) {
                    ChatContextPageVO vo = new ChatContextPageVO();
                    vo.setChatId(chatId);
                    vo.setUserId(StpUtil.getLoginIdAsLong());
                    vo.setContent(chatContentDTO.getMessages().toString());
                    records.add(vo);
                }
            }

        }else{
            // 分页查询聊天上下文
            List<AiChatContext> aiChatContexts = aiChatContextService.getLatestMessagesByChatId(chatId, pageNum, pageSize);

            records.addAll(aiChatContexts.stream().map(context -> {
                ChatContextPageVO vo = new ChatContextPageVO();
                vo.setUserId(context.getUserId());
                vo.setChatId(context.getChatId());
                vo.setContent(context.getContent());
                return vo;
            }).toList());
        }

        Long total = aiChatContextService.countByChatId(chatId);

        return PageResult.of(pageNum, pageSize, total, records);
    }
}
