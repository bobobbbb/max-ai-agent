package com.max.maxaiagent.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.max.maxaiagent.dto.ChatContentDTO;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xtwang
 * @des ChatContentDTO Redis序列化器
 * @date 2025/2/11 下午3:00
 */
public class ChatContentDTORedisSerializer implements RedisSerializer<ChatContentDTO> {

    private final ObjectMapper objectMapper;

    public ChatContentDTORedisSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(ChatContentDTO chatContentDTO) {
        if (chatContentDTO == null) {
            return new byte[0];
        }
        try {
            return objectMapper.writeValueAsBytes(chatContentDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("无法序列化ChatContentDTO", e);
        }
    }

    @Override
    public ChatContentDTO deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(bytes);
            
            // 创建ChatContentDTO对象
            ChatContentDTO chatContentDTO = new ChatContentDTO();
            
            // 设置基本字段
            if (rootNode.has("chatId") && !rootNode.get("chatId").isNull()) {
                chatContentDTO.setChatId(rootNode.get("chatId").asLong());
            }
            if (rootNode.has("messageId") && !rootNode.get("messageId").isNull()) {
                chatContentDTO.setMessageId(rootNode.get("messageId").asLong());
            }
            if (rootNode.has("status") && !rootNode.get("status").isNull()) {
                chatContentDTO.setStatus(rootNode.get("status").asText());
            }
            
            // 处理messages字段
            if (rootNode.has("messages") && !rootNode.get("messages").isNull()) {
                JsonNode messagesNode = rootNode.get("messages");
                List<Message> messages = new ArrayList<>();
                
                if (messagesNode.isArray()) {
                    for (JsonNode messageNode : messagesNode) {
                        Message message = deserializeMessage(messageNode);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                }
                chatContentDTO.setMessages(messages);
            }
            
            return chatContentDTO;
        } catch (Exception e) {
            throw new RuntimeException("无法反序列化ChatContentDTO", e);
        }
    }

    /**
     * 反序列化单个Message对象
     */
    private Message deserializeMessage(JsonNode messageNode) {
        try {
            // 尝试获取messageType字段
            String messageType = null;
            if (messageNode.has("messageType")) {
                messageType = messageNode.get("messageType").asText();
            }
            
            // 获取文本内容
            String text = "";
            if (messageNode.has("text")) {
                text = messageNode.get("text").asText();
            } else if (messageNode.has("content")) {
                text = messageNode.get("content").asText();
            }
            
            // 根据类型创建对应的Message对象
            if ("USER".equalsIgnoreCase(messageType)) {
                return new UserMessage(text);
            } else if ("ASSISTANT".equalsIgnoreCase(messageType)) {
                return new AssistantMessage(text);
            } else {
                // 如果没有明确的类型信息，尝试根据其他字段判断
                // 或者默认创建UserMessage
                return new UserMessage(text);
            }
        } catch (Exception e) {
            // 如果解析失败，返回null
            return null;
        }
    }
} 