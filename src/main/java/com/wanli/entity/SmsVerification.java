package com.wanli.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 短信验证码实体类.
 * 
 * <p>用于存储短信验证码信息，支持多种验证场景，包括注册、登录、
 * 密码重置等。提供验证码的生成、验证、过期管理等功能。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>验证码信息存储（手机号、验证码、类型等）</li>
 *   <li>验证码状态管理（未使用、已使用、已过期）</li>
 *   <li>多场景支持（注册、登录、密码重置、手机验证）</li>
 *   <li>过期时间管理和自动清理</li>
 *   <li>使用次数和频率控制</li>
 * </ul>
 * 
 * @author AI Generated
 * @version 1.0
 * @since 2025-01-22
 */
@Entity
@Table(name = "sms_verifications")
@EntityListeners(AuditingEntityListener.class)
public class SmsVerification {

    /**
     * 验证码记录唯一标识符.
     * 使用UUID格式，确保全局唯一性.
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 手机号码.
     * 接收验证码的手机号，必须符合格式要求.
     */
    @NotBlank(message = "手机号不能为空")
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    /**
     * 验证码.
     * 发送给用户的数字验证码，通常为4-6位数字.
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度必须在4-8位之间")
    @Column(name = "code", length = 8, nullable = false)
    private String code;

    /**
     * 验证码类型.
     * 标识验证码的使用场景，如注册、登录、密码重置等.
     */
    @NotNull(message = "验证码类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VerificationType type;

    /**
     * 使用状态.
     * 标识验证码是否已被使用，防止重复使用.
     */
    @Column(name = "used", nullable = false)
    private Boolean used = false;

    /**
     * 过期时间.
     * 验证码的有效期截止时间，超过此时间验证码失效.
     */
    @NotNull(message = "过期时间不能为空")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 创建时间.
     * 验证码的生成时间，由JPA审计自动设置.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 使用时间.
     * 验证码被使用的时间，验证成功时设置.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * IP地址.
     * 请求验证码时的客户端IP地址，用于安全监控.
     */
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * 用户代理.
     * 请求验证码时的客户端User-Agent信息.
     */
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 验证码类型枚举.
     * 定义验证码的各种使用场景.
     */
    public enum VerificationType {
        /** 用户注册验证 */
        REGISTRATION,
        /** 用户登录验证 */
        LOGIN,
        /** 密码重置验证 */
        PASSWORD_RESET,
        /** 手机号验证 */
        PHONE_VERIFICATION,
        /** 账户安全验证 */
        SECURITY_VERIFICATION,
        /** 绑定手机号验证 */
        BIND_PHONE,
        /** 解绑手机号验证 */
        UNBIND_PHONE
    }

    /**
     * 默认构造函数.
     * JPA要求实体类必须有无参构造函数.
     */
    public SmsVerification() { }

    /**
     * 构造函数 - 创建短信验证码实例.
     * 
     * <p>用于创建新的验证码记录，包含必需的基本信息。</p>
     * 
     * @param verificationId 验证码记录ID，必须是有效的UUID格式
     * @param phoneNum 手机号码，必须符合格式要求
     * @param verificationCode 验证码，通常为4-6位数字
     * @param verificationType 验证码类型，标识使用场景
     * @param expireTime 过期时间，验证码的有效期截止时间
     */
    public SmsVerification(final String verificationId, final String phoneNum,
                          final String verificationCode, final VerificationType verificationType,
                          final LocalDateTime expireTime) {
        this.id = verificationId;
        this.phoneNumber = phoneNum;
        this.code = verificationCode;
        this.type = verificationType;
        this.expiresAt = expireTime;
    }

    /**
     * 检查验证码是否有效.
     * 
     * <p>验证码有效的条件：</p>
     * <ul>
     *   <li>未被使用（used = false）</li>
     *   <li>未过期（当前时间 < expiresAt）</li>
     * </ul>
     * 
     * @return 如果验证码有效返回true，否则返回false
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * 检查验证码是否已过期.
     * 
     * @return 如果验证码已过期返回true，否则返回false
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 标记验证码为已使用.
     * 
     * <p>验证码使用后应调用此方法，防止重复使用。
     * 同时记录使用时间用于审计。</p>
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 获取验证码ID.
     *
     * @return 验证码ID.
     */
    public String getId() {
        return id;
    }

    /**
     * 设置验证码ID.
     *
     * @param verificationId 验证码ID.
     */
    public void setId(final String verificationId) {
        this.id = verificationId;
    }

    /**
     * 获取手机号码.
     *
     * @return 手机号码.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 设置手机号码.
     *
     * @param phoneNum 手机号码.
     */
    public void setPhoneNumber(final String phoneNum) {
        this.phoneNumber = phoneNum;
    }

    /**
     * 获取验证码.
     *
     * @return 验证码.
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置验证码.
     *
     * @param verificationCode 验证码.
     */
    public void setCode(final String verificationCode) {
        this.code = verificationCode;
    }

    /**
     * 获取验证码类型.
     *
     * @return 验证码类型.
     */
    public VerificationType getType() {
        return type;
    }

    /**
     * 设置验证码类型.
     *
     * @param verificationType 验证码类型.
     */
    public void setType(final VerificationType verificationType) {
        this.type = verificationType;
    }

    /**
     * 获取使用状态.
     *
     * @return 使用状态.
     */
    public Boolean getUsed() {
        return used;
    }

    /**
     * 设置使用状态.
     *
     * @param usedStatus 使用状态.
     */
    public void setUsed(final Boolean usedStatus) {
        this.used = usedStatus;
    }

    /**
     * 获取过期时间.
     *
     * @return 过期时间.
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * 设置过期时间.
     *
     * @param expireTime 过期时间.
     */
    public void setExpiresAt(final LocalDateTime expireTime) {
        this.expiresAt = expireTime;
    }

    /**
     * 获取创建时间.
     *
     * @return 创建时间.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间.
     *
     * @param createTime 创建时间.
     */
    public void setCreatedAt(final LocalDateTime createTime) {
        this.createdAt = createTime;
    }

    /**
     * 获取使用时间.
     *
     * @return 使用时间.
     */
    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    /**
     * 设置使用时间.
     *
     * @param useTime 使用时间.
     */
    public void setUsedAt(final LocalDateTime useTime) {
        this.usedAt = useTime;
    }

    /**
     * 获取IP地址.
     *
     * @return IP地址.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * 设置IP地址.
     *
     * @param clientIpAddress IP地址.
     */
    public void setIpAddress(final String clientIpAddress) {
        this.ipAddress = clientIpAddress;
    }

    /**
     * 获取用户代理.
     *
     * @return 用户代理.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 设置用户代理.
     *
     * @param clientUserAgent 用户代理.
     */
    public void setUserAgent(final String clientUserAgent) {
        this.userAgent = clientUserAgent;
    }

    /**
     * 比较两个短信验证码对象是否相等.
     * 基于验证码ID进行比较，确保对象唯一性.
     *
     * @param o 要比较的对象.
     * @return 如果对象相等返回true，否则返回false.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SmsVerification that = (SmsVerification) o;
        return Objects.equals(id, that.id);
    }

    /**
     * 计算短信验证码对象的哈希码.
     * 基于验证码ID计算哈希值，与equals方法保持一致.
     *
     * @return 短信验证码对象的哈希码.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 返回短信验证码对象的字符串表示.
     * 包含主要的验证码信息字段，用于调试和日志记录.
     * 注意：不包含敏感的验证码内容.
     *
     * @return 短信验证码对象的字符串表示.
     */
    @Override
    public String toString() {
        return "SmsVerification{"
                + "id='" + id
                + "', phoneNumber='" + phoneNumber
                + "', type=" + type
                + ", used=" + used
                + ", expiresAt=" + expiresAt
                + ", createdAt=" + createdAt
                + '}';
    }
}