package com.max.maxaiagent.controller;
import cn.dev33.satoken.stp.StpUtil;
import com.max.maxaiagent.common.Result;
import com.max.maxaiagent.dto.LoginDTO;
import com.max.maxaiagent.vo.LoginVO;
import com.max.maxaiagent.dto.RegisterDTO;
import com.max.maxaiagent.vo.UserInfoVO;
import com.max.maxaiagent.entity.User;
import com.max.maxaiagent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户管理", description = "用户注册、登录等接口")
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册接口")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            UserInfoVO userInfo = userService.register(registerDTO);
            log.info("用户注册成功: {}", userInfo.getUsername());
            return Result.success("注册成功", userInfo);
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            // 查找用户
            User user = userService.findByUsername(loginDTO.getUsername());
            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                return Result.error("账号已被禁用");
            }

            // 验证密码
            if (!userService.verifyPassword(loginDTO.getPassword(), user.getPassword())) {
                return Result.error("用户名或密码错误");
            }

            // 执行登录
            StpUtil.login(user.getId());

            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId());

            // 构造响应
            LoginVO response = new LoginVO();
            response.setToken(StpUtil.getTokenValue());
            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhone(user.getPhone());
            userInfo.setStatus(user.getStatus());
            userInfo.setRole(user.getRole());
            userInfo.setCredits(user.getCredits());
            userInfo.setLastLoginTime(user.getLastLoginTime());
            userInfo.setRegisterTime(user.getRegisterTime());
            response.setUserInfo(userInfo);
            log.info("用户登录成功: {}", user.getUsername());
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return Result.error("登录失败");
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public Result<Void> logout() {
        try {
            StpUtil.logout();
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
            return Result.error("登出失败");
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已被使用")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(!exists);
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已被注册")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Result.success(!exists);
    }

    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已被注册")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        return Result.success(!exists);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    public Result<UserInfoVO> getUserInfo() {
        try {
            // 检查是否登录
            StpUtil.checkLogin();
            
            // 获取当前用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 查询用户信息
            var user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 转换为DTO
            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhone(user.getPhone());
            userInfo.setStatus(user.getStatus());
            userInfo.setRole(user.getRole());
            userInfo.setCredits(user.getCredits());
            userInfo.setLastLoginTime(user.getLastLoginTime());
            userInfo.setRegisterTime(user.getRegisterTime());
            
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.error("获取用户信息失败");
        }
    }
} 