package com.wanli.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     * 
     * @param ex 方法参数验证异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("code", 6001);
        response.put("message", "参数验证失败");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());
        
        log.warn("参数验证失败: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理绑定异常
     * 
     * @param ex 绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("code", 6002);
        response.put("message", "数据绑定失败");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());
        
        log.warn("数据绑定失败: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理非法参数异常
     * 
     * @param ex 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 6003);
        response.put("message", "参数错误: " + ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        
        log.warn("参数错误: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理运行时异常
     * 
     * @param ex 运行时异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 1001);
        response.put("message", "系统运行异常");
        response.put("timestamp", LocalDateTime.now());
        
        log.error("系统运行异常: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理通用异常
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 1000);
        response.put("message", "系统内部错误");
        response.put("timestamp", LocalDateTime.now());
        
        log.error("系统内部错误: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}