package com.wanli.exception.user;

import com.wanli.exception.BusinessException;

/**
 * 权限不足异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class InsufficientPermissionException extends BusinessException {
    
    public InsufficientPermissionException(String operation) {
        super("INSUFFICIENT_PERMISSION", "权限不足，无法执行操作: {0}", operation);
    }
}