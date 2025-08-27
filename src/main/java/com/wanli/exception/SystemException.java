package com.wanli.exception;

/**
 * 系统异常基类
 * 用于处理系统运行时异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class SystemException extends BaseException {
    
    public SystemException(String errorCode, String errorMessage, Object... args) {
        super(errorCode, errorMessage, args);
    }
    
    public SystemException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorCode, errorMessage, cause, args);
    }
}