package com.max.maxaiagent.dto;

import lombok.Data;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

@Data
public class ChatContentDTO {
    private Long chatId;

    private Long messageId;

    private List<Message> messages;

    private String status;
}
