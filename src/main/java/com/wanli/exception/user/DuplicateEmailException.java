package com.wanli.exception.user;

import com.wanli.exception.BusinessException;

/**
 * 邮箱重复异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class DuplicateEmailException extends BusinessException {
    
    public DuplicateEmailException(String email) {
        super("DUPLICATE_EMAIL", "邮箱已被注册: {0}", email);
    }
}