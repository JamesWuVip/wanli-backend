package com.wanli.exception;

/**
 * 自定义异常基类
 * 
 * @author wanli
 * @version 1.0.0
 */
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    private final String errorMessage;
    private final Object[] args;
    
    public BaseException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = null;
    }
    
    public BaseException(String errorCode, String errorMessage, Object... args) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = args;
    }
    
    public BaseException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = null;
    }
    
    public BaseException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = args;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Object[] getArgs() {
        return args;
    }
}