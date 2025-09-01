package com.wanli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求数据传输对象.
 *
 * <p>用于封装用户登录时提交的手机号和密码信息，包含相应的验证规则。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
public final class LoginDTO {

    /** 手机号. */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;

    /** 密码. */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    /**
     * 默认构造函数.
     */
    public LoginDTO() {
    }

    /**
     * 全参构造函数.
     *
     * @param phoneNumber 手机号
     * @param password 密码
     */
    public LoginDTO(final String phoneNumber, final String password) {
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    /**
     * 获取手机号.
     *
     * @return 手机号
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 设置手机号.
     *
     * @param phoneNumber 手机号
     */
    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * 获取密码.
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码.
     *
     * @param password 密码
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * 重写toString方法.
     *
     * <p>为了安全考虑，不输出密码信息。</p>
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "LoginDTO{"
                + "phoneNumber='" + phoneNumber + "'"
                + ", password='[PROTECTED]'"
                + "}";
    }
}