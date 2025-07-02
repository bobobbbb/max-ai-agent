CREATE TABLE `user` (
                        `id` BIGINT NOT NULL COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                        `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
                        `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
                        `password` VARCHAR(255) NOT NULL COMMENT '密码（哈希后）',
                        `status` TINYINT DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
                        `role` VARCHAR(50) DEFAULT 'user' COMMENT '角色',
                        `credits` INT DEFAULT 0 COMMENT '可用额度',
                        `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                        `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    -- 系统字段
                        `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0=未删除 1=已删除',

                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
CREATE TABLE `ai_chat_context` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                   `chat_id` BIGINT NOT NULL COMMENT '会话ID',
                                   `content` TEXT NOT NULL COMMENT '对话上下文内容',

                                   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0=未删除 1=已删除',

                                   PRIMARY KEY (`id`),
                                   KEY `idx_user_chat` (`user_id`, `chat_id`),
                                   KEY `idx_chat_id` (`chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent 上下文表';
