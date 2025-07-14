package com.max.maxaiagent.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.max.maxaiagent.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import java.io.IOException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理sa-token未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录: {}", e.getMessage());
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供Token";
            case NotLoginException.INVALID_TOKEN -> "Token无效";
            case NotLoginException.TOKEN_TIMEOUT -> "Token已过期";
            case NotLoginException.BE_REPLACED -> "Token已被顶下线";
            case NotLoginException.KICK_OUT -> "Token已被踢下线";
            default -> "用户未登录";
        };
        return Result.error(401, message);
    }

    /**
     * 处理sa-token权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足");
    }

    /**
     * 处理sa-token角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色权限不足: {}", e.getMessage());
        return Result.error(403, "角色权限不足");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        StringBuilder message = new StringBuilder();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            message.append(error.getDefaultMessage()).append("; ");
        }
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message.toString());
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        StringBuilder message = new StringBuilder();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            message.append(error.getDefaultMessage()).append("; ");
        }
        log.warn("参数绑定失败: {}", message);
        return Result.error(400, message.toString());
    }

    /**
     * 处理SSE客户端断开连接异常
     * 这是正常现象，客户端刷新或关闭页面时会触发
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        // 不返回任何内容，因为连接已断开
        // 只记录调试级别日志，避免污染错误日志
        log.debug("SSE客户端连接已断开: {}", e.getMessage());
    }

    /**
     * 处理IO异常（如网络中断、Broken pipe等）
     * 在SSE流式传输中，客户端断开连接时经常出现
     */
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e) {
        // 检查是否是连接中断相关的异常
        String message = e.getMessage();
        if (message != null && (
            message.contains("Broken pipe") ||
            message.contains("Connection reset") ||
            message.contains("远程主机强迫关闭了一个现有的连接"))) {
            log.debug("网络连接已断开: {}", message);
        } else {
            log.warn("IO异常: ", e);
        }
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统异常");
    }
} 