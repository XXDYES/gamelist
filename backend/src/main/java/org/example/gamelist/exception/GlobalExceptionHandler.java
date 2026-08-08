package org.example.gamelist.exception;

import org.example.gamelist.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiServiceException.class)
    public Result<Void> handleAiServiceException(AiServiceException e) {
        return Result.error(e.getMessage());  // ← 统一格式
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getMsg());
    }
}