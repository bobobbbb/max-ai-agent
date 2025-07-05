package com.max.maxaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI聊天问题实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_chat_question")
public class AiChatQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会话ID
     */
    @TableField("chat_id")
    private Long chatId;

    /**
     * 用户问题
     */
    @TableField("question")
    private String question;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除 0=未删除 1=已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
} 