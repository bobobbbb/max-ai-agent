package com.max.maxaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 密码（哈希后）
     */
    @TableField("password")
    private String password;

    /**
     * 状态 0=禁用 1=正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 角色
     */
    @TableField("role")
    private String role;

    /**
     * 可用额度
     */
    @TableField("credits")
    private Integer credits;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 注册时间
     */
    @TableField(value = "register_time", fill = FieldFill.INSERT)
    private LocalDateTime registerTime;
} 