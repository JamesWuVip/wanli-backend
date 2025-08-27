package com.wanli.exception.system;

import com.wanli.exception.SystemException;

/**
 * 数据库异常
 * 
 * @author wanli
 * @version 1.0.0
 */
public class DatabaseException extends SystemException {
    
    public DatabaseException(String operation, Throwable cause) {
        super("DATABASE_ERROR", "数据库操作失败: {0}", cause, operation);
    }
}