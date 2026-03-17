package com.wanpu.myshortlink.common.exceptions;

import com.wanpu.myshortlink.common.result.Result;
import com.wanpu.myshortlink.common.result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ServiceException.class)
  public Result<Void> handleServiceException(ServiceException e) {
    log.warn("[业务异常] 错误码{},错误信息{}",e.getCode(),e.getMessage());
    return Result.fail(e.getCode(),e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .findFirst()
        .orElse("参数校验失败");
    log.warn("[参数校验失败] {}", message);
    return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
  }

  /** 拦截 @RequestParam 参数校验异常 */
  @ExceptionHandler(ConstraintViolationException.class)
  public Result<Void> handleConstraintException(ConstraintViolationException ex) {
    log.warn("[参数校验失败] {}", ex.getMessage());
    return Result.fail(ResultCode.PARAM_ERROR.getCode(), ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public Result<Void> handleException(Exception ex) {
    log.error("[系统异常] ", ex);
    return Result.fail(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMessage());
  }

}
