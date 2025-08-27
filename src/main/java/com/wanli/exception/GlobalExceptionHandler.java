package com.wanli.exception;

import com.wanli.common.ApiResponse;
import com.wanli.exception.user.*;
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

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author wanli
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理用户不存在异常
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException e) {
        logger.warn("User not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getErrorCode(), formatMessage(e.getErrorMessage(), e.getArgs())));
    }
    
    /**
     * 处理用户名重复异常
     */
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUsernameException(DuplicateUsernameException e) {
        logger.warn("Duplicate username: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getErrorCode(), formatMessage(e.getErrorMessage(), e.getArgs())));
    }
    
    /**
     * 处理邮箱重复异常
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmailException(DuplicateEmailException e) {
        logger.warn("Duplicate email: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getErrorCode(), formatMessage(e.getErrorMessage(), e.getArgs())));
    }
    
    /**
     * 处理密码无效异常
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPasswordException(InvalidPasswordException e) {
        logger.warn("Invalid password: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getErrorCode(), formatMessage(e.getErrorMessage(), e.getArgs())));
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        logger.warn("Business exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getErrorCode(), formatMessage(e.getErrorMessage(), e.getArgs())));
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<List<ValidationException.FieldError>>> handleValidationException(ValidationException e) {
        logger.warn("Validation exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getErrorCode(), e.getErrorMessage(), e.getFieldErrors()));
    }
    
    /**
     * 处理方法参数校验失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ValidationException.FieldError>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.warn("Method argument not valid: {}", e.getMessage());
        
        List<ValidationException.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationException.FieldError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()))
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "参数校验失败", fieldErrors));
    }
    
    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<ValidationException.FieldError>>> handleConstraintViolationException(ConstraintViolationException e) {
        logger.warn("Constraint violation: {}", e.getMessage());
        
        List<ValidationException.FieldError> fieldErrors = e.getConstraintViolations().stream()
                .map(violation -> new ValidationException.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()))
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "参数校验失败", fieldErrors));
    }
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        logger.warn("Authentication exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTHENTICATION_ERROR", "认证失败: " + e.getMessage()));
    }
    
    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "访问被拒绝: " + e.getMessage()));
    }
    
    /**
     * 处理数据访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException e) {
        logger.error("Data access exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DATABASE_ERROR", "数据库访问异常"));
    }
    
    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.error("Data integrity violation: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DATA_INTEGRITY_ERROR", "数据完整性约束违反"));
    }
    
    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("HTTP method not supported: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("METHOD_NOT_ALLOWED", "不支持的HTTP方法: " + e.getMethod()));
    }
    
    /**
     * 处理HTTP媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        logger.warn("HTTP media type not supported: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("MEDIA_TYPE_NOT_SUPPORTED", "不支持的媒体类型: " + e.getContentType()));
    }
    
    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(SystemException e) {
        logger.error("System exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getErrorCode(), formatMessage(e.getErrorMessage(), e.getArgs())));
    }
    
    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        logger.error("Unexpected exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "系统内部错误"));
    }
    
    /**
     * 格式化消息
     */
    private String formatMessage(String message, Object[] args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return MessageFormat.format(message, args);
    }
}