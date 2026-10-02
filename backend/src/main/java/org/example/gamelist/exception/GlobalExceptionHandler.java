package org.example.gamelist.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.gamelist.common.Result;
import org.example.gamelist.common.UserContext;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiServiceException.class)
    public Result<Void> handleAiServiceException(AiServiceException e) {
        log.error("AI服务异常：",e);
        return Result.error(e.getMessage());  // ← 统一格式
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        String username = UserContext.getCurrentUsername();
        log.warn("业务异常：{} | user: {}", e.getMsg(), username == null ? "未登录" : username);
        return Result.error(e.getMsg());
    }

    // 兜底：其他所有未处理异常统一返回友好提示，避免直接暴露 500
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未处理的异常", e);
        return Result.error("服务器开小差了");
    }
}
