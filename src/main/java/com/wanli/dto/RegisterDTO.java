package com.wanli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求数据传输对象.
 *
 * <p>用于封装用户注册时提交的所有必要信息，包含相应的验证规则。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
public final class RegisterDTO {

    /** 用户名. */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /** 手机号. */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;

    /** 密码. */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    /** 确认密码. */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /** 全名. */
    @NotBlank(message = "全名不能为空")
    @Size(min = 2, max = 50, message = "全名长度必须在2-50位之间")
    private String fullName;

    /**
     * 默认构造函数.
     */
    public RegisterDTO() {
    }

    /**
     * 全参构造函数.
     *
     * @param username 用户名
     * @param phoneNumber 手机号
     * @param password 密码
     * @param confirmPassword 确认密码
     * @param fullName 全名
     */
    public RegisterDTO(final String username,
                      final String phoneNumber,
                      final String password,
                      final String confirmPassword,
                      final String fullName) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.fullName = fullName;
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
     * @param username 用户名
     */
    public void setUsername(final String username) {
        this.username = username;
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
     * 获取确认密码.
     *
     * @return 确认密码
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * 设置确认密码.
     *
     * @param confirmPassword 确认密码
     */
    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
     * @param fullName 全名
     */
    public void setFullName(final String fullName) {
        this.fullName = fullName;
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
        return "RegisterDTO{"
                + "username='" + username + "'"
                + ", phoneNumber='" + phoneNumber + "'"
                + ", password='[PROTECTED]'"
                + ", confirmPassword='[PROTECTED]'"
                + ", fullName='" + fullName + "'"
                + "}";
    }
}