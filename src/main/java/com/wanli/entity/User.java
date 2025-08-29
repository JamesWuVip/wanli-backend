package com.wanli.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 用户实体类 - 优化版本用于JWT认证
 * T-012: 用户认证数据库优化
 * 
 * @author wanli
 * @version 1.6.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    public User() {
        super();
    }
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 255, message = "密码长度必须在6-255个字符之间")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "franchise_id")
    private UUID franchiseId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;
    
    @Column(name = "login_attempts", nullable = false)
    @Builder.Default
    private Integer loginAttempts = 0;
    
    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;
    
    /**
     * 多对一关系：用户所属机构
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institution institution;
    
    /**
     * 检查用户是否被锁定
     * @return 是否被锁定
     */
    public boolean isLocked() {
        return status == UserStatus.LOCKED || 
               (lockedUntil != null && lockedUntil.isAfter(OffsetDateTime.now()));
    }
    
    /**
     * 检查用户是否激活
     * @return 是否激活
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isLocked();
    }
    
    /**
     * 重置登录尝试次数
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }
    
    /**
     * 增加登录尝试次数
     */
    public void incrementLoginAttempts() {
        this.loginAttempts++;
        // 如果尝试次数超过5次，锁定账户30分钟
        if (this.loginAttempts >= 5) {
            this.status = UserStatus.LOCKED;
            this.lockedUntil = OffsetDateTime.now().plusMinutes(30);
        }
    }
    
    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime() {
        this.lastLoginAt = OffsetDateTime.now();
        resetLoginAttempts();
    }
    
    /**
     * 激活用户账户
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        resetLoginAttempts();
    }
    
    /**
     * 检查用户是否已删除
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return getDeletedAt() != null;
    }
    
    /**
     * 软删除用户
     */
    public void softDelete() {
        this.setDeletedAt(java.time.LocalDateTime.now());
        this.status = UserStatus.DELETED;
    }
}