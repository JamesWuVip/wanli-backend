package com.wanli.common;

/**
 * 系统常量定义类.
 *
 * <p>包含系统中使用的各种常量定义。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
public final class Constants {

    /** UUID字符串长度常量. */
    public static final int UUID_LENGTH = 36;

    /** 头像URL最大长度常量. */
    public static final int AVATAR_URL_MAX_LENGTH = 500;

    /** 用户名最小长度常量. */
    public static final int USERNAME_MIN_LENGTH = 3;

    /** 用户名最大长度常量. */
    public static final int USERNAME_MAX_LENGTH = 50;

    /** 邮箱最大长度常量. */
    public static final int EMAIL_MAX_LENGTH = 100;

    /** 全名最大长度常量. */
    public static final int FULL_NAME_MAX_LENGTH = 100;

    /** 手机号最大长度常量. */
    public static final int PHONE_MAX_LENGTH = 20;

    /** 默认分页大小. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 最大分页大小. */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 私有构造函数，防止实例化.
     */
    private Constants() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated");
    }
}