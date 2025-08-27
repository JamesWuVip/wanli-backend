package com.wanli.exception;

/**
 * 业务异常基类
 * 用于处理业务逻辑相关的异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    public BusinessException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
    }
}