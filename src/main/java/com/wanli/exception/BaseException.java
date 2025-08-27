package com.wanli.exception;

/**
 * 基础异常类
 * 所有自定义异常的基类
 * 
 * @author wanli
 * @version 1.0.0
 */
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    private final String errorMessage;
    private final Object[] args;
    
    public BaseException(String errorCode, String errorMessage, Object... args) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = args;
    }
    
    public BaseException(String errorCode, String errorMessage, Throwable cause, Object... args) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.args = args;
    }
    
    /**
     * 获取错误码
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误消息
     * @return 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 获取消息参数
     * @return 消息参数数组
     */
    public Object[] getArgs() {
        return args;
    }
}