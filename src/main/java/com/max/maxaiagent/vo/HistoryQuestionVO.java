package com.max.maxaiagent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 历史聊天VO
 */
@Data
@Schema(description = "历史聊天VO")
public class HistoryQuestionVO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID
     */
    private Long chatId;

    /**
     * 用户问题
     */
    private String question;

}
