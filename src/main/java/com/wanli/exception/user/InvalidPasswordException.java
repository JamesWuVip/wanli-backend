package com.wanli.exception.user;

import com.wanli.exception.BusinessException;

/**
 * 密码无效异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class InvalidPasswordException extends BusinessException {
    
    public InvalidPasswordException(String message) {
        super("INVALID_PASSWORD", message);
    }
}