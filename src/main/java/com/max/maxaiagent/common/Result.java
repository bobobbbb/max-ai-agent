package com.max.maxaiagent.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一响应结果
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> {

    @Schema(description = "响应码")
    private Integer code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "是否成功")
    private Boolean success;

    public Result() {}

    public Result(Integer code, String message, T data, Boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    public static <T> Result<T> success(String 登出成功) {
        return new Result<>(200, "操作成功", null, true);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, true);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, true);
    }

    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null, false);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null, false);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null, false);
    }
} 