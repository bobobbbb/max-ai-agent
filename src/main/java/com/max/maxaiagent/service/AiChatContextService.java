package com.max.maxaiagent.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.max.maxaiagent.entity.AiChatContext;
import com.max.maxaiagent.mapper.AiChatContextMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI聊天上下文服务类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatContextService extends ServiceImpl<AiChatContextMapper, AiChatContext> {

    /**
     * 插入AI聊天上下文实体到数据库
     *
     * @param aiChatContext 要插入的AI聊天上下文实体
     * @return 插入成功返回true，失败返回false
     */
    public boolean insertAiChatContext(AiChatContext aiChatContext) {
        log.info("开始插入AI聊天上下文，用户ID: {}, 会话ID: {}", 
                aiChatContext.getUserId(), aiChatContext.getChatId());
        try {
            // 参数验证
            if (aiChatContext.getUserId() == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (aiChatContext.getChatId() == null) {
                throw new IllegalArgumentException("会话ID不能为空");
            }
            if (aiChatContext.getContent() == null || aiChatContext.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("对话内容不能为空");
            }

            // 设置创建时间（如果未设置）
            if (aiChatContext.getCreateTime() == null) {
                aiChatContext.setCreateTime(LocalDateTime.now());
            }
            if (aiChatContext.getUpdateTime() == null) {
                aiChatContext.setUpdateTime(LocalDateTime.now());
            }

            // 插入数据库
            boolean result = save(aiChatContext);
            
            if (result) {
                log.info("AI聊天上下文插入成功，ID: {}", aiChatContext.getId());
            } else {
                log.error("AI聊天上下文插入失败");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("插入AI聊天上下文时发生异常", e);
            throw new RuntimeException("插入AI聊天上下文失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID和会话ID查询聊天上下文列表
     *
     * @param userId 用户ID
     * @param chatId 会话ID
     * @return 聊天上下文列表
     */
    public List<AiChatContext> getChatContextByUserAndChat(Long userId, Long chatId) {
        return list(new LambdaQueryWrapper<AiChatContext>()
                .eq(AiChatContext::getUserId, userId)
                .eq(AiChatContext::getChatId, chatId)
                .orderByAsc(AiChatContext::getCreateTime));
    }

    /**
     * 根据会话ID查询最新的聊天上下文
     *
     * @param chatId 会话ID
     * @param limit 限制数量
     * @return 最新的聊天上下文列表
     */
    public List<AiChatContext> getLatestChatContext(Long chatId, int limit) {
        return list(new LambdaQueryWrapper<AiChatContext>()
                .eq(AiChatContext::getChatId, chatId)
                .orderByDesc(AiChatContext::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 分页查询会话ID对应的消息
     *
     * @param chatId 会话ID
     * @param pageNum 页码，从1开始
     * @param pageSize 每页大小
     * @return 分页的聊天上下文列表
     */
    public List<AiChatContext> getLatestMessagesByChatId(String chatId, Integer pageNum, Integer pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        return list(new LambdaQueryWrapper<AiChatContext>()
                .eq(AiChatContext::getChatId, chatId)
                .orderByDesc(AiChatContext::getCreateTime)
                .last("LIMIT " + pageSize + " OFFSET " + offset));
    }

    /**
     * 根据会话ID统计消息总数
     *
     * @param chatId 会话ID
     * @return 消息总数
     */
    public Long countByChatId(String chatId) {
        return count(new LambdaQueryWrapper<AiChatContext>()
                .eq(AiChatContext::getChatId, chatId));
    }



} 