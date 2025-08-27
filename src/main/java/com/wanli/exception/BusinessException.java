package com.wanli.exception;

/**
 * 业务逻辑异常基类
 * 
 * @author wanli
 * @version 1.0.0
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
    
    public BusinessException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
    
    public BusinessException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
    }
}