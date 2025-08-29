package com.wanli.entity;

/**
 * 用户角色枚举
 * 
 * @author wanli
 * @version 1.0.0
 */
public enum UserRole {
    HQ_TEACHER("总部教师", "ROLE_HQ_TEACHER"),
    BRANCH_TEACHER("分校教师", "ROLE_BRANCH_TEACHER"),
    STUDENT("学生", "ROLE_STUDENT"),
    ADMIN("管理员", "ROLE_ADMIN");
    
    private final String displayName;
    private final String authority;
    
    UserRole(String displayName, String authority) {
        this.displayName = displayName;
        this.authority = authority;
    }
    
    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取权限标识
     * @return 权限标识
     */
    public String getAuthority() {
        return authority;
    }
}