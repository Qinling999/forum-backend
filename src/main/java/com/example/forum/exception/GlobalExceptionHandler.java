package com.example.forum.exception;

import com.example.forum.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========================
    // 1. 参数错误（前端问题）
    // ========================
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error("参数错误：" + e.getMessage());
    }

    // ========================
    // 2. 空指针异常
    // ========================
    @ExceptionHandler(NullPointerException.class)
    public Result<String> handleNullPointer(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.error("系统异常（空指针）");
    }

    // ========================
    // 3. 运行时异常（业务异常）
    // ========================
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntime(RuntimeException e) {
        log.error("运行时异常", e);
        return Result.error(e.getMessage() != null ? e.getMessage() : "系统运行异常");
    }

    // ========================
    // 4. 兜底异常（最后一道防线）
    // ========================
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e, HttpServletRequest request) {

        log.error("系统异常，请求路径：{}", request.getRequestURI(), e);

        return Result.error("服务器内部错误");
    }
}