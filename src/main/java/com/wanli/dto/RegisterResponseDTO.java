package com.wanli.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 用户注册响应DTO.
 *
 * <p>用于返回用户注册成功后的响应数据，包含用户基本信息和注册状态。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponseDTO {

    /** 用户ID. */
    private String userId;

    /** 用户名. */
    private String username;

    /** 手机号码. */
    private String phone;

    /** 全名. */
    private String fullName;

    /** 手机号验证状态. */
    private boolean phoneVerified;

    /** 注册时间戳. */
    private long registeredAt;

    /** 手机号验证提示信息. */
    private String verificationMessage;

    /**
     * 默认构造函数.
     */
    public RegisterResponseDTO() {
    }

    /**
     * 带参数的构造函数.
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param phone 手机号码
     * @param fullName 全名
     */
    public RegisterResponseDTO(final String userId, final String username,
                              final String phone, final String fullName) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.fullName = fullName;
        this.phoneVerified = false;
        this.registeredAt = System.currentTimeMillis();
    }

    /**
     * 获取用户ID.
     *
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置用户ID.
     *
     * @param id 用户ID
     */
    public void setUserId(final String id) {
        this.userId = id;
    }

    /**
     * 获取用户名.
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名.
     *
     * @param userName 用户名
     */
    public void setUsername(final String userName) {
        this.username = userName;
    }

    /**
     * 获取手机号码.
     *
     * @return 手机号码
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号码.
     *
     * @param userPhone 手机号码
     */
    public void setPhone(final String userPhone) {
        this.phone = userPhone;
    }

    /**
     * 获取全名.
     *
     * @return 全名
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * 设置全名.
     *
     * @param userFullName 全名
     */
    public void setFullName(final String userFullName) {
        this.fullName = userFullName;
    }

    /**
     * 获取手机号验证状态.
     *
     * @return 手机号验证状态
     */
    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    /**
     * 设置手机号验证状态.
     *
     * @param verified 手机号验证状态
     */
    public void setPhoneVerified(final boolean verified) {
        this.phoneVerified = verified;
    }

    /**
     * 获取注册时间戳.
     *
     * @return 注册时间戳
     */
    public long getRegisteredAt() {
        return registeredAt;
    }

    /**
     * 设置注册时间戳.
     *
     * @param timestamp 注册时间戳
     */
    public void setRegisteredAt(final long timestamp) {
        this.registeredAt = timestamp;
    }

    /**
     * 获取手机号验证提示信息.
     *
     * @return 手机号验证提示信息
     */
    public String getVerificationMessage() {
        return verificationMessage;
    }

    /**
     * 设置手机号验证提示信息.
     *
     * @param message 手机号验证提示信息
     */
    public void setVerificationMessage(final String message) {
        this.verificationMessage = message;
    }

    /**
     * 设置响应消息.
     *
     * @param message 响应消息
     */
    public void setMessage(final String message) {
        this.verificationMessage = message;
    }

    /**
     * 获取响应消息.
     *
     * @return 响应消息
     */
    public String getMessage() {
        return verificationMessage;
    }

    /**
     * 设置短信发送状态.
     *
     * @param smsSent 短信发送状态
     */
    public void setSmsSent(final boolean smsSent) {
        // 这里可以根据需要添加一个专门的字段，目前使用phoneVerified字段
        this.phoneVerified = smsSent;
    }

    /**
     * 获取短信发送状态.
     *
     * @return 短信发送状态
     */
    public boolean isSmsSent() {
        return phoneVerified;
    }

    /**
     * 返回对象的字符串表示.
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "RegisterResponseDTO{"
                + "userId='" + userId + "'"
                + ", username='" + username + "'"
                + ", phone='" + phone + "'"
                + ", fullName='" + fullName + "'"
                + ", phoneVerified=" + phoneVerified
                + ", registeredAt=" + registeredAt
                + ", verificationMessage='" + verificationMessage + "'"
                + '}';
    }
}