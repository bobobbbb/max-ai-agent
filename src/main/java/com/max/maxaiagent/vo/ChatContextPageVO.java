package com.max.maxaiagent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天上下文分页查询VO
 */
@Data
@Schema(description = "聊天上下文分页查询VO")
public class ChatContextPageVO {

    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "会话ID")
    private String chatId;
    
    @Schema(description = "对话内容")
    private String content;

} 