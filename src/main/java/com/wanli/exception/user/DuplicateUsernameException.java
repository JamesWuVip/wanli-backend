package com.wanli.exception.user;

import com.wanli.exception.BusinessException;

/**
 * 用户名重复异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class DuplicateUsernameException extends BusinessException {
    
    public DuplicateUsernameException(String username) {
        super("DUPLICATE_USERNAME", "用户名已存在: {0}", username);
    }
}