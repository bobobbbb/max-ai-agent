package com.max.maxaiagent.config;

import cn.dev33.satoken.stp.StpInterface;
import com.max.maxaiagent.entity.User;
import com.max.maxaiagent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * sa-token权限验证接口实现
 */
@Component
@RequiredArgsConstructor
public class SaTokenConfig implements StpInterface {

    private final UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 根据用户ID查询用户信息
        Long userId = Long.valueOf(loginId.toString());
        User user = userService.getById(userId);
        
        if (user == null) {
            return Collections.emptyList();
        }

        // 这里可以根据用户角色返回不同的权限
        // 目前简单返回基础权限
        return switch (user.getRole()) {
            case "admin" -> List.of("user:read", "user:write", "user:delete", "system:manage");
            case "user" -> List.of("user:read");
            default -> Collections.emptyList();
        };
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 根据用户ID查询用户信息
        Long userId = Long.valueOf(loginId.toString());
        User user = userService.getById(userId);
        
        if (user == null) {
            return Collections.emptyList();
        }

        // 返回用户角色
        return List.of(user.getRole());
    }
} 