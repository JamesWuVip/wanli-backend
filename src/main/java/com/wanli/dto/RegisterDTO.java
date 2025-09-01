package com.wanli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求DTO.
 *
 * <p>用于接收用户注册请求的数据，包含用户名、手机号、密码等信息，
 * 并提供完整的数据验证规则。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
public class RegisterDTO {

    /** 用户名最小长度. */
    private static final int MIN_USERNAME_LENGTH = 3;

    /** 用户名最大长度. */
    private static final int MAX_USERNAME_LENGTH = 50;

    /** 密码最小长度. */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /** 密码最大长度. */
    private static final int MAX_PASSWORD_LENGTH = 100;

    /** 全名最大长度. */
    private static final int MAX_FULL_NAME_LENGTH = 100;

    /** 手机号长度. */
    private static final int PHONE_LENGTH = 11;

    /** 用户名. */
    @NotBlank(message = "用户名不能为空")
    @Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH,
          message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
             message = "用户名只能包含字母、数字和下划线")
    private String username;

    /** 手机号. */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(min = PHONE_LENGTH, max = PHONE_LENGTH, message = "手机号必须为11位数字")
    private String phone;

    /** 密码. */
    @NotBlank(message = "密码不能为空")
    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH,
          message = "密码长度必须在6-100个字符之间")
    private String password;

    /** 确认密码. */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /** 全名（可选）. */
    @Size(max = MAX_FULL_NAME_LENGTH, message = "全名长度不能超过100个字符")
    private String fullName;

    /**
     * 默认构造函数.
     */
    public RegisterDTO() {
    }

    /**
     * 带参数的构造函数.
     *
     * @param userName 用户名
     * @param userPhone 手机号
     * @param userPassword 密码
     * @param confirmUserPassword 确认密码
     */
    public RegisterDTO(final String userName, final String userPhone,
                      final String userPassword, final String confirmUserPassword) {
        this.username = userName;
        this.phone = userPhone;
        this.password = userPassword;
        this.confirmPassword = confirmUserPassword;
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
     * 获取手机号.
     *
     * @return 手机号
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号.
     *
     * @param userPhone 手机号
     */
    public void setPhone(final String userPhone) {
        this.phone = userPhone;
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
     * @param userPassword 密码
     */
    public void setPassword(final String userPassword) {
        this.password = userPassword;
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
     * @param confirmUserPassword 确认密码
     */
    public void setConfirmPassword(final String confirmUserPassword) {
        this.confirmPassword = confirmUserPassword;
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
     * 验证密码和确认密码是否一致.
     *
     * @return 如果密码一致返回true，否则返回false
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * 返回对象的字符串表示.
     *
     * <p>为了安全考虑，密码字段显示为[PROTECTED]。</p>
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "RegisterDTO{"
                + "username='" + username + "'"
                + ", phone='" + phone + "'"
                + ", password='[PROTECTED]'"
                + ", confirmPassword='[PROTECTED]'"
                + ", fullName='" + fullName + "'"
                + '}';
    }
}