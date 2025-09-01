package com.wanli.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * 短信验证码实体类.
 *
 * <p>用于存储短信验证码信息，包括手机号、验证码、类型、使用状态和过期时间等。</p>
 * <p>支持注册、登录、重置密码等多种验证场景。</p>
 *
 * @author JamesWu
 * @since 1.0.0
 */
@Entity
@Table(name = "sms_verification_codes", indexes = {
    @Index(name = "idx_phone_type", columnList = "phone, code_type"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class SmsVerification {

    /**
     * 验证码类型枚举.
     */
    public enum CodeType {
        /** 注册验证. */
        REGISTER,
        /** 登录验证. */
        LOGIN,
        /** 重置密码验证. */
        RESET_PASSWORD
    }

    /** 验证码长度. */
    private static final int CODE_LENGTH = 6;

    /** 手机号长度. */
    private static final int PHONE_LENGTH = 11;

    /**
     * 主键ID.
     */
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 手机号码.
     */
    @NotBlank(message = "手机号不能为空")
    @Size(min = PHONE_LENGTH, max = PHONE_LENGTH, message = "手机号必须为11位数字")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Column(name = "phone", length = 11, nullable = false)
    private String phone;

    /**
     * 验证码.
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = CODE_LENGTH, max = CODE_LENGTH, message = "验证码必须为6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    @Column(name = "code", length = 6, nullable = false)
    private String code;

    /**
     * 验证码类型.
     */
    @NotNull(message = "验证码类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "code_type", nullable = false)
    private CodeType codeType;

    /**
     * 是否已使用.
     */
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    /**
     * 过期时间.
     */
    @NotNull(message = "过期时间不能为空")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 创建时间.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 默认构造函数.
     */
    public SmsVerification() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 带参数的构造函数.
     *
     * @param phone 手机号
     * @param code 验证码
     * @param codeType 验证码类型
     * @param expiresAt 过期时间
     */
    public SmsVerification(final String phone, final String code, 
                          final CodeType codeType, final LocalDateTime expiresAt) {
        this();
        this.phone = phone;
        this.code = code;
        this.codeType = codeType;
        this.expiresAt = expiresAt;
    }

    /**
     * 获取主键ID.
     *
     * @return 主键ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置主键ID.
     *
     * @param id 主键ID
     */
    public void setId(final String id) {
        this.id = id;
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
     * @param phone 手机号码
     */
    public void setPhone(final String phone) {
        this.phone = phone;
    }

    /**
     * 获取验证码.
     *
     * @return 验证码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置验证码.
     *
     * @param code 验证码
     */
    public void setCode(final String code) {
        this.code = code;
    }

    /**
     * 获取验证码类型.
     *
     * @return 验证码类型
     */
    public CodeType getCodeType() {
        return codeType;
    }

    /**
     * 设置验证码类型.
     *
     * @param codeType 验证码类型
     */
    public void setCodeType(final CodeType codeType) {
        this.codeType = codeType;
    }

    /**
     * 获取是否已使用状态.
     *
     * @return 是否已使用
     */
    public Boolean getIsUsed() {
        return isUsed;
    }

    /**
     * 设置是否已使用状态.
     *
     * @param isUsed 是否已使用
     */
    public void setIsUsed(final Boolean isUsed) {
        this.isUsed = isUsed;
    }

    /**
     * 获取过期时间.
     *
     * @return 过期时间
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * 设置过期时间.
     *
     * @param expiresAt 过期时间
     */
    public void setExpiresAt(final LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * 获取创建时间.
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间.
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 检查验证码是否已过期.
     *
     * @return 如果已过期返回true，否则返回false
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 检查验证码是否可用.
     *
     * <p>验证码可用的条件：</p>
     * <ul>
     *   <li>未被使用</li>
     *   <li>未过期</li>
     * </ul>
     *
     * @return 如果可用返回true，否则返回false
     */
    public boolean isAvailable() {
        return !this.isUsed && !isExpired();
    }

    /**
     * 标记验证码为已使用.
     */
    public void markAsUsed() {
        this.isUsed = true;
    }

    /**
     * 返回对象的字符串表示.
     *
     * <p>为了安全考虑，验证码字段显示为[PROTECTED]。</p>
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "SmsVerification{"
                + "id='" + id + "'"
                + ", phone='" + phone + "'"
                + ", code='[PROTECTED]'"
                + ", codeType=" + codeType
                + ", isUsed=" + isUsed
                + ", expiresAt=" + expiresAt
                + ", createdAt=" + createdAt
                + '}';
    }
}