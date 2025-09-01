package com.wanli.enums;

/**
 * 用户状态枚举.
 * 
 * <p>定义用户在系统中的状态类型。</p>
 * 
 * @author AI Generated
 * @version 1.0
 */
public enum UserStatus {
    
    /**
     * 活跃状态.
     * 用户可以正常使用系统的所有功能。
     */
    ACTIVE,
    
    /**
     * 非活跃状态.
     * 用户暂时不活跃，但账户仍然有效。
     */
    INACTIVE,
    
    /**
     * 暂停状态.
     * 用户账户被暂停，无法使用系统功能。
     */
    SUSPENDED,
    
    /**
     * 已删除状态.
     * 用户账户已被标记为删除，但数据仍保留。
     */
    DELETED
}