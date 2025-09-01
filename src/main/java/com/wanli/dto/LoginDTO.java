package com.wanli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 登录请求DTO.
 *
 * <p>用于接收用户登录请求的手机号和密码信息，包含数据验证规则。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
public class LoginDTO {

    /** 手机号长度. */
    private static final int PHONE_LENGTH = 11;

    /** 密码最小长度. */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /** 密码最大长度. */
    private static final int MAX_PASSWORD_LENGTH = 100;

    /** 手机号. */
    @NotBlank(message = "手机号不能为空")
    @Size(min = PHONE_LENGTH, max = PHONE_LENGTH,
          message = "手机号必须为11位数字")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码. */
    @NotBlank(message = "密码不能为空")
    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH,
          message = "密码长度必须在6-100个字符之间")
    private String password;

    /**
     * 默认构造函数.
     */
    public LoginDTO() {
    }

    /**
     * 带参数的构造函数.
     *
     * @param userPhone 手机号.
     * @param userPassword 密码.
     */
    public LoginDTO(final String userPhone, final String userPassword) {
        this.phone = userPhone;
        this.password = userPassword;
    }

    /**
     * 获取手机号.
     *
     * @return 手机号.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号.
     *
     * @param userPhone 手机号.
     */
    public void setPhone(final String userPhone) {
        this.phone = userPhone;
    }

    /**
     * 获取密码.
     *
     * @return 密码.
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码.
     *
     * @param userPassword 密码.
     */
    public void setPassword(final String userPassword) {
        this.password = userPassword;
    }

    /**
     * 返回对象的字符串表示.
     *
     * <p>为了安全考虑，密码字段显示为[PROTECTED]。</p>.
     *
     * @return 对象的字符串表示.
     */
    @Override
    public String toString() {
        return "LoginDTO{"
                + "phone='" + phone
                + "', password='[PROTECTED]'"
                + '}';
    }
}