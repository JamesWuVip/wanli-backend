package com.wanli.backend.exception;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.ResponseUtil;

import jakarta.validation.ConstraintViolationException;

/** 全局异常处理器 统一处理应用中的各种异常，提供一致的错误响应格式 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 处理业务异常 */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, Object>> handleBusinessException(
      BusinessException ex, WebRequest request) {

    LogUtil.logError(
        "BUSINESS_EXCEPTION", getCurrentUserId(request), ex.getErrorCode(), ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseUtil.error(ex.getErrorCode(), ex.getMessage()));
  }

  /** 处理资源未找到异常 */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
      ResourceNotFoundException ex, WebRequest request) {

    LogUtil.logError(
        "RESOURCE_NOT_FOUND", getCurrentUserId(request), ex.getErrorCode(), ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ResponseUtil.error(ex.getErrorCode(), ex.getMessage()));
  }

  /** 处理权限拒绝异常 */
  @ExceptionHandler(PermissionDeniedException.class)
  public ResponseEntity<Map<String, Object>> handlePermissionDeniedException(
      PermissionDeniedException ex, WebRequest request) {

    LogUtil.logSecurity(
        "PERMISSION_DENIED",
        getCurrentUserId(request),
        getClientIpAddress(request),
        ex.getMessage());

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ResponseUtil.error(ex.getErrorCode(), ex.getMessage()));
  }

  /** 处理参数验证异常 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException ex, WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    LogUtil.logError(
        "VALIDATION_ERROR",
        getCurrentUserId(request),
        "INVALID_INPUT",
        "参数验证失败: " + errors.toString(),
        ex);

    Map<String, Object> response = ResponseUtil.error("INVALID_INPUT", "参数验证失败");
    response.put("errors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /** 处理绑定异常 */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> handleBindException(
      BindException ex, WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    LogUtil.logError(
        "BIND_ERROR",
        getCurrentUserId(request),
        "INVALID_INPUT",
        "数据绑定失败: " + errors.toString(),
        ex);

    Map<String, Object> response = ResponseUtil.error("INVALID_INPUT", "数据绑定失败");
    response.put("errors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /** 处理非法参数异常 */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {

    LogUtil.logError(
        "ILLEGAL_ARGUMENT", getCurrentUserId(request), "INVALID_ARGUMENT", ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseUtil.error("INVALID_ARGUMENT", "参数错误: " + ex.getMessage()));
  }

  /** 处理空指针异常 */
  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<Map<String, Object>> handleNullPointerException(
      NullPointerException ex, WebRequest request) {

    LogUtil.logError(
        "NULL_POINTER",
        getCurrentUserId(request),
        "INTERNAL_ERROR",
        "空指针异常: " + ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseUtil.error("INTERNAL_ERROR", "系统内部错误，请联系管理员"));
  }

  /** 处理数据库访问异常 */
  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<Map<String, Object>> handleDataAccessException(
      DataAccessException ex, WebRequest request) {

    LogUtil.logError(
        "DATABASE_ACCESS_ERROR",
        getCurrentUserId(request),
        "DATABASE_ERROR",
        "数据库访问异常: " + ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseUtil.error("DATABASE_ERROR", "数据库操作失败，请稍后重试"));
  }

  /** 处理数据完整性违反异常 */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex, WebRequest request) {

    LogUtil.logError(
        "DATA_INTEGRITY_VIOLATION",
        getCurrentUserId(request),
        "DATA_CONSTRAINT_ERROR",
        "数据完整性违反: " + ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ResponseUtil.error("DATA_CONSTRAINT_ERROR", "数据约束冲突，请检查输入数据"));
  }

  /** 处理重复键异常 */
  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateKeyException(
      DuplicateKeyException ex, WebRequest request) {

    LogUtil.logError(
        "DUPLICATE_KEY",
        getCurrentUserId(request),
        "DUPLICATE_RESOURCE",
        "重复键异常: " + ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ResponseUtil.error("DUPLICATE_RESOURCE", "资源已存在，请检查输入数据"));
  }

  /** 处理SQL异常 */
  @ExceptionHandler(SQLException.class)
  public ResponseEntity<Map<String, Object>> handleSQLException(
      SQLException ex, WebRequest request) {

    LogUtil.logError(
        "SQL_EXCEPTION",
        getCurrentUserId(request),
        "DATABASE_ERROR",
        "SQL异常: " + ex.getMessage() + ", SQLState: " + ex.getSQLState(),
        ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseUtil.error("DATABASE_ERROR", "数据库操作异常，请联系管理员"));
  }

  /** 处理认证异常 */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(
      AuthenticationException ex, WebRequest request) {

    LogUtil.logSecurity(
        "AUTHENTICATION_FAILED",
        getCurrentUserId(request),
        getClientIpAddress(request),
        "认证失败: " + ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ResponseUtil.error("AUTHENTICATION_FAILED", "认证失败，请重新登录"));
  }

  /** 处理凭据错误异常 */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
      BadCredentialsException ex, WebRequest request) {

    LogUtil.logSecurity(
        "BAD_CREDENTIALS",
        getCurrentUserId(request),
        getClientIpAddress(request),
        "凭据错误: " + ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ResponseUtil.error("BAD_CREDENTIALS", "用户名或密码错误"));
  }

  /** 处理访问拒绝异常 */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
      AccessDeniedException ex, WebRequest request) {

    LogUtil.logSecurity(
        "ACCESS_DENIED",
        getCurrentUserId(request),
        getClientIpAddress(request),
        "访问拒绝: " + ex.getMessage());

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ResponseUtil.error("ACCESS_DENIED", "访问被拒绝，权限不足"));
  }

  /** 处理约束违反异常 */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation -> {
              String fieldName = violation.getPropertyPath().toString();
              String errorMessage = violation.getMessage();
              errors.put(fieldName, errorMessage);
            });

    LogUtil.logError(
        "CONSTRAINT_VIOLATION",
        getCurrentUserId(request),
        "VALIDATION_ERROR",
        "约束违反: " + errors.toString(),
        ex);

    Map<String, Object> response = ResponseUtil.error("VALIDATION_ERROR", "数据验证失败");
    response.put("errors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /** 处理HTTP消息不可读异常 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, WebRequest request) {

    LogUtil.logError(
        "MESSAGE_NOT_READABLE",
        getCurrentUserId(request),
        "INVALID_REQUEST_BODY",
        "请求体不可读: " + ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseUtil.error("INVALID_REQUEST_BODY", "请求体格式错误，请检查JSON格式"));
  }

  /** 处理缺少请求参数异常 */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex, WebRequest request) {

    LogUtil.logError(
        "MISSING_PARAMETER",
        getCurrentUserId(request),
        "MISSING_REQUIRED_PARAMETER",
        "缺少必需参数: " + ex.getParameterName(),
        ex);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ResponseUtil.error("MISSING_REQUIRED_PARAMETER", "缺少必需参数: " + ex.getParameterName()));
  }

  /** 处理方法参数类型不匹配异常 */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex, WebRequest request) {

    LogUtil.logError(
        "ARGUMENT_TYPE_MISMATCH",
        getCurrentUserId(request),
        "INVALID_PARAMETER_TYPE",
        "参数类型不匹配: " + ex.getName() + ", 期望类型: " + ex.getRequiredType(),
        ex);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ResponseUtil.error(
                "INVALID_PARAMETER_TYPE",
                "参数类型错误: " + ex.getName() + " 应为 " + ex.getRequiredType().getSimpleName()));
  }

  /** 处理HTTP请求方法不支持异常 */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, WebRequest request) {

    LogUtil.logError(
        "METHOD_NOT_SUPPORTED",
        getCurrentUserId(request),
        "METHOD_NOT_ALLOWED",
        "不支持的HTTP方法: " + ex.getMethod(),
        ex);

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(
            ResponseUtil.error(
                "METHOD_NOT_ALLOWED",
                "不支持的HTTP方法: "
                    + ex.getMethod()
                    + ", 支持的方法: "
                    + String.join(", ", ex.getSupportedMethods())));
  }

  /** 处理HTTP媒体类型不支持异常 */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleHttpMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException ex, WebRequest request) {

    LogUtil.logError(
        "MEDIA_TYPE_NOT_SUPPORTED",
        getCurrentUserId(request),
        "UNSUPPORTED_MEDIA_TYPE",
        "不支持的媒体类型: " + ex.getContentType(),
        ex);

    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(ResponseUtil.error("UNSUPPORTED_MEDIA_TYPE", "不支持的媒体类型: " + ex.getContentType()));
  }

  /** 处理文件上传大小超限异常 */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex, WebRequest request) {

    LogUtil.logError(
        "UPLOAD_SIZE_EXCEEDED",
        getCurrentUserId(request),
        "FILE_TOO_LARGE",
        "文件上传大小超限: " + ex.getMaxUploadSize(),
        ex);

    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(
            ResponseUtil.error(
                "FILE_TOO_LARGE", "文件大小超过限制，最大允许: " + ex.getMaxUploadSize() + " 字节"));
  }

  /** 处理404异常 */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
      NoHandlerFoundException ex, WebRequest request) {

    LogUtil.logError(
        "NO_HANDLER_FOUND",
        getCurrentUserId(request),
        "ENDPOINT_NOT_FOUND",
        "未找到处理器: " + ex.getRequestURL(),
        ex);

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ResponseUtil.error("ENDPOINT_NOT_FOUND", "请求的端点不存在: " + ex.getRequestURL()));
  }

  /** 处理IO异常 */
  @ExceptionHandler(IOException.class)
  public ResponseEntity<Map<String, Object>> handleIOException(IOException ex, WebRequest request) {

    LogUtil.logError(
        "IO_EXCEPTION", getCurrentUserId(request), "IO_ERROR", "IO异常: " + ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseUtil.error("IO_ERROR", "文件操作失败，请稍后重试"));
  }

  /** 处理连接异常 */
  @ExceptionHandler(ConnectException.class)
  public ResponseEntity<Map<String, Object>> handleConnectException(
      ConnectException ex, WebRequest request) {

    LogUtil.logError(
        "CONNECTION_FAILED",
        getCurrentUserId(request),
        "CONNECTION_ERROR",
        "连接失败: " + ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(ResponseUtil.error("CONNECTION_ERROR", "服务连接失败，请稍后重试"));
  }

  /** 处理超时异常 */
  @ExceptionHandler({TimeoutException.class, SocketTimeoutException.class})
  public ResponseEntity<Map<String, Object>> handleTimeoutException(
      Exception ex, WebRequest request) {

    LogUtil.logError(
        "TIMEOUT_EXCEPTION",
        getCurrentUserId(request),
        "REQUEST_TIMEOUT",
        "请求超时: " + ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
        .body(ResponseUtil.error("REQUEST_TIMEOUT", "请求超时，请稍后重试"));
  }

  /** 处理其他未捕获的异常 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(
      Exception ex, WebRequest request) {

    LogUtil.logError(
        "UNHANDLED_EXCEPTION",
        getCurrentUserId(request),
        "INTERNAL_ERROR",
        "未处理的异常: " + ex.getClass().getSimpleName() + ": " + ex.getMessage(),
        ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseUtil.error("INTERNAL_ERROR", "系统内部错误，请稍后重试"));
  }

  /** 获取当前用户ID */
  private String getCurrentUserId(WebRequest request) {
    // 从请求中获取用户ID，这里需要根据实际的认证机制实现
    String userId = request.getHeader("X-User-Id");
    return userId != null ? userId : "anonymous";
  }

  /** 获取客户端IP地址 */
  private String getClientIpAddress(WebRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return "unknown";
  }
}
