package com.wanli.entity;

/**
 * 用户状态枚举
 * 
 * @author wanli
 * @version 1.0.0
 */
public enum UserStatus {
    ACTIVE("激活"),
    INACTIVE("未激活"),
    LOCKED("锁定"),
    DELETED("已删除");
    
    private final String displayName;
    
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
}