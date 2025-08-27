package com.wanli.common;

/**
 * 错误码枚举
 * 定义系统中所有的错误码和对应的错误消息
 * 
 * @author wanli
 * @version 1.0.0
 */
public enum ErrorCode {
    
    // 通用错误码 (1000-1999)
    SUCCESS("0000", "操作成功"),
    SYSTEM_ERROR("1000", "系统内部错误"),
    INVALID_PARAMETER("1001", "参数无效"),
    VALIDATION_ERROR("1002", "参数验证失败"),
    UNAUTHORIZED("1003", "未授权访问"),
    FORBIDDEN("1004", "访问被禁止"),
    NOT_FOUND("1005", "资源不存在"),
    METHOD_NOT_ALLOWED("1006", "请求方法不允许"),
    REQUEST_TIMEOUT("1007", "请求超时"),
    TOO_MANY_REQUESTS("1008", "请求过于频繁"),
    
    // 用户相关错误码 (2000-2999)
    USER_NOT_FOUND("2000", "用户不存在"),
    DUPLICATE_USERNAME("2001", "用户名已存在"),
    DUPLICATE_EMAIL("2002", "邮箱已被注册"),
    INVALID_PASSWORD("2003", "密码不正确"),
    USER_DISABLED("2004", "用户已被禁用"),
    USER_LOCKED("2005", "用户已被锁定"),
    PASSWORD_EXPIRED("2006", "密码已过期"),
    INSUFFICIENT_PERMISSION("2007", "权限不足"),
    
    // 认证相关错误码 (3000-3999)
    INVALID_TOKEN("3000", "Token无效"),
    AUTH_TOKEN_INVALID("3000", "认证Token无效"),
    TOKEN_EXPIRED("3001", "Token已过期"),
    TOKEN_NOT_FOUND("3002", "Token不存在"),
    INVALID_CREDENTIALS("3003", "用户名或密码错误"),
    LOGIN_FAILED("3004", "登录失败"),
    LOGOUT_FAILED("3005", "登出失败"),
    
    // 课程相关错误码 (4000-4999)
    COURSE_NOT_FOUND("4000", "课程不存在"),
    COURSE_ACCESS_DENIED("4001", "无权访问课程"),
    COURSE_ALREADY_EXISTS("4002", "课程已存在"),
    COURSE_NOT_PUBLISHED("4003", "课程未发布"),
    COURSE_FULL("4004", "课程已满员"),
    
    // 课时相关错误码 (5000-5999)
    LESSON_NOT_FOUND("5000", "课时不存在"),
    LESSON_ACCESS_DENIED("5001", "无权访问课时"),
    LESSON_NOT_AVAILABLE("5002", "课时不可用"),
    
    // 数据库相关错误码 (6000-6999)
    DATABASE_ERROR("6000", "数据库操作失败"),
    DATA_INTEGRITY_VIOLATION("6001", "数据完整性约束违反"),
    DUPLICATE_KEY("6002", "数据重复"),
    
    // 外部服务错误码 (7000-7999)
    EXTERNAL_SERVICE_ERROR("7000", "外部服务调用失败"),
    SERVICE_UNAVAILABLE("7001", "服务不可用"),
    
    // 文件相关错误码 (8000-8999)
    FILE_NOT_FOUND("8000", "文件不存在"),
    FILE_UPLOAD_FAILED("8001", "文件上传失败"),
    FILE_SIZE_EXCEEDED("8002", "文件大小超出限制"),
    INVALID_FILE_TYPE("8003", "文件类型不支持");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}