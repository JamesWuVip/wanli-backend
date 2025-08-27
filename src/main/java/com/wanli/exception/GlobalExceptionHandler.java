package com.wanli.exception;

import com.wanli.common.ApiResponse;
import com.wanli.common.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常
 * 
 * @author wanli
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理用户不存在异常
     */
    @ExceptionHandler(com.wanli.exception.user.UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(com.wanli.exception.user.UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode(),
            e.getErrorMessage()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 处理重复用户名异常
     */
    @ExceptionHandler(com.wanli.exception.user.DuplicateUsernameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUsernameException(com.wanli.exception.user.DuplicateUsernameException e) {
        log.warn("Duplicate username: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode(),
            e.getErrorMessage()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理重复邮箱异常
     */
    @ExceptionHandler(com.wanli.exception.user.DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmailException(com.wanli.exception.user.DuplicateEmailException e) {
        log.warn("Duplicate email: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode(),
            e.getErrorMessage()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理密码无效异常
     */
    @ExceptionHandler(com.wanli.exception.user.InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPasswordException(com.wanli.exception.user.InvalidPasswordException e) {
        log.warn("Invalid password: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode(),
            e.getErrorMessage()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage(), e);
        
        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode(),
            e.getErrorMessage()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(ValidationException e) {
        log.warn("Validation exception occurred: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getMessage())
        );
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            e.getErrorCode(),
            e.getErrorMessage(),
            errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理Bean Validation异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e) {
        log.warn("Method argument validation failed: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            ErrorCode.VALIDATION_ERROR.getCode(),
            "参数验证失败",
            errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException e) {
        log.warn("Constraint violation occurred: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(fieldName, message);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            ErrorCode.VALIDATION_ERROR.getCode(),
            "参数验证失败",
            errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.UNAUTHORIZED.getCode(),
            "认证失败"
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * 处理授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.FORBIDDEN.getCode(),
            "访问被拒绝"
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * 处理数据库异常
     */
    @ExceptionHandler({DataAccessException.class})
    public ResponseEntity<ApiResponse<Void>> handleDatabaseException(Exception e) {
        log.error("Database exception occurred: {}", e.getMessage(), e);
        
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.DATABASE_ERROR.getCode(),
            "数据库操作失败"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage(), e);
        
        String message = "数据操作失败";
        if (e.getCause() instanceof ConstraintViolationException) {
            message = "数据重复或违反约束条件";
        }
        
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.DATA_INTEGRITY_VIOLATION.getCode(),
            message
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.METHOD_NOT_ALLOWED.getCode(),
            "请求方法不支持: " + e.getMethod()
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    /**
     * 处理HTTP媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("Media type not supported: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_PARAMETER.getCode(),
            "不支持的媒体类型: " + e.getContentType()
        );
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }
    
    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(SystemException e) {
        log.error("System exception occurred: {}", e.getMessage(), e);
        
        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode(),
            "系统内部错误"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.SYSTEM_ERROR.getCode(),
            "系统内部错误"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 格式化错误消息
     */
    private String formatMessage(String message, Object[] args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return MessageFormat.format(message, args);
    }
}