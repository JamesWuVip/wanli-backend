package com.wanli.backend.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

/** 全局异常处理器 统一处理应用程序中的异常并返回标准化的错误响应 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 处理参数验证异常
   *
   * @param ex MethodArgumentNotValidException
   * @param request HttpServletRequest
   * @return 错误响应
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("path", request.getRequestURI());

    // 收集所有字段验证错误
    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

    errorResponse.put("message", "参数验证失败: " + errorMessage);
    errorResponse.put("error", "Validation Failed");

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * 处理绑定异常
   *
   * @param ex BindException
   * @param request HttpServletRequest
   * @return 错误响应
   */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> handleBindException(
      BindException ex, HttpServletRequest request) {

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("path", request.getRequestURI());

    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

    errorResponse.put("message", "参数绑定失败: " + errorMessage);
    errorResponse.put("error", "Bind Exception");

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * 处理参数类型不匹配异常
   *
   * @param ex MethodArgumentTypeMismatchException
   * @param request HttpServletRequest
   * @return 错误响应
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatchException(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("path", request.getRequestURI());
    errorResponse.put(
        "message", "参数类型错误: " + ex.getName() + " 应该是 " + ex.getRequiredType().getSimpleName());
    errorResponse.put("error", "Type Mismatch");

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * 处理非法参数异常
   *
   * @param ex IllegalArgumentException
   * @param request HttpServletRequest
   * @return 错误响应
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("path", request.getRequestURI());
    errorResponse.put("message", "参数错误: " + ex.getMessage());
    errorResponse.put("error", "Illegal Argument");

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * 处理运行时异常
   *
   * @param ex RuntimeException
   * @param request HttpServletRequest
   * @return 错误响应
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(
      RuntimeException ex, HttpServletRequest request) {

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("path", request.getRequestURI());
    errorResponse.put("message", "运行时错误: " + ex.getMessage());
    errorResponse.put("error", "Runtime Exception");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * 处理通用异常
   *
   * @param ex Exception
   * @param request HttpServletRequest
   * @return 错误响应
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(
      Exception ex, HttpServletRequest request) {

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("path", request.getRequestURI());
    errorResponse.put("message", "服务器内部错误: " + ex.getMessage());
    errorResponse.put("error", "Internal Server Error");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
