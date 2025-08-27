package com.wanli.exception.user;

import com.wanli.exception.BusinessException;

/**
 * 密码无效异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class InvalidPasswordException extends BusinessException {
    
    public InvalidPasswordException() {
        super("INVALID_PASSWORD", "密码不正确");
    }
    
    public InvalidPasswordException(String reason) {
        super("INVALID_PASSWORD", "密码不符合要求: {0}", reason);
    }
}