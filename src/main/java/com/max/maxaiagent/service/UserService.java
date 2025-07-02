package com.max.maxaiagent.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.max.maxaiagent.dto.RegisterDTO;
import com.max.maxaiagent.dto.UserInfoVO;
import com.max.maxaiagent.entity.User;
import com.max.maxaiagent.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 用户注册
     */
    public UserInfoVO register(RegisterDTO registerDTO) {
        // 验证两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StrUtil.isNotBlank(registerDTO.getEmail()) && existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (StrUtil.isNotBlank(registerDTO.getPhone()) && existsByPhone(registerDTO.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        // 加密密码
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt()));
        user.setStatus(1); // 默认启用
        user.setRole("user"); // 默认普通用户
        user.setCredits(0); // 默认0额度
        user.setRegisterTime(LocalDateTime.now());

        // 保存用户
        save(user);

        // 转换为DTO返回
        return BeanUtil.copyProperties(user, UserInfoVO.class);
    }

    /**
     * 根据用户名查询用户
     */
    public User findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)) > 0;
    }

    /**
     * 检查手机号是否存在
     */
    public boolean existsByPhone(String phone) {
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)) > 0;
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
    }
} 