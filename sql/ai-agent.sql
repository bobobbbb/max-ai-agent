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
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
