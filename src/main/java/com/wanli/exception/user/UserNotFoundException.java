package com.wanli.exception.user;

import com.wanli.exception.BusinessException;

/**
 * 用户不存在异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class UserNotFoundException extends BusinessException {
    
    public UserNotFoundException(String username) {
        super("USER_NOT_FOUND", "用户不存在: {0}", username);
    }
    
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "用户不存在: ID={0}", userId);
    }
}