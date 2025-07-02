package com.max.maxaiagent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录响应DTO
 */
@Data
@Schema(description = "用户登录响应")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "用户信息")
    private UserInfoVO userInfo;
} 