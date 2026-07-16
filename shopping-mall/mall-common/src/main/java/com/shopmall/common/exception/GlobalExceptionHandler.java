package com.shopmall.common.exception;

import com.shopmall.common.result.Result;
import com.shopmall.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获Controller层抛出的异常，转换为标准的Result格式返回给前端
 * 这样前端收到的响应格式始终一致，不会出现乱七八糟的错误页面
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 捕获自定义业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 捕获参数校验异常（@Valid 校验失败） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("参数校验异常：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /** 捕获参数绑定异常 */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        log.warn("参数绑定异常：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /** 兜底：捕获所有未处理的异常 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}