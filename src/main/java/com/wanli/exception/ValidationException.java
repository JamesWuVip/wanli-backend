package com.wanli.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证异常类
 * 用于处理参数验证失败异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class ValidationException extends BaseException {
    
    private final List<FieldError> fieldErrors;
    
    public ValidationException(String errorMessage) {
        super("VALIDATION_ERROR", errorMessage);
        this.fieldErrors = new ArrayList<>();
    }
    
    public ValidationException(List<FieldError> fieldErrors) {
        super("VALIDATION_ERROR", "参数验证失败");
        this.fieldErrors = fieldErrors;
    }
    
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
    
    /**
     * 字段错误信息
     */
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
        
        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }
        
        public String getField() {
            return field;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
    }
}