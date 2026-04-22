package com.example.forum.exception;

import com.example.forum.common.Result;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========================
    // 1. 兜底异常（所有未处理错误）
    // ========================
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e, HttpServletRequest request) {

        e.printStackTrace(); // 控制台打印，方便你调试

        return Result.error("服务器内部错误");
    }
    // ========================
    // 2. 参数错误
    // ========================
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public Result<String> handleNullPointer(NullPointerException e) {
        return Result.error("数据异常（空指针）");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntime(RuntimeException e) {
        return Result.error("系统运行异常");
    }
}