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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 用户实体类
 * 
 * @author wanli
 * @version 1.0.0
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
    
    // 手动添加builder方法以解决Lombok注解处理器问题
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    public static class UserBuilder {
        private String username;
        private String passwordHash;
        private String email;
        private String fullName;
        private UserRole role;
        private UserStatus status = UserStatus.ACTIVE;
        private OffsetDateTime lastLoginAt;
        private Integer loginAttempts = 0;
        private OffsetDateTime lockedUntil;
        
        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }
        
        public UserBuilder role(UserRole role) {
            this.role = role;
            return this;
        }
        
        public UserBuilder status(UserStatus status) {
            this.status = status;
            return this;
        }
        
        public UserBuilder lastLoginAt(OffsetDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }
        
        public UserBuilder loginAttempts(Integer loginAttempts) {
            this.loginAttempts = loginAttempts;
            return this;
        }
        
        public UserBuilder lockedUntil(OffsetDateTime lockedUntil) {
            this.lockedUntil = lockedUntil;
            return this;
        }
        
        public User build() {
            User user = new User();
            user.username = this.username;
            user.passwordHash = this.passwordHash;
            user.email = this.email;
            user.fullName = this.fullName;
            user.role = this.role;
            user.status = this.status;
            user.lastLoginAt = this.lastLoginAt;
            user.loginAttempts = this.loginAttempts;
            user.lockedUntil = this.lockedUntil;
            return user;
        }
    }
    
    // 手动添加getter和setter方法以解决Lombok注解处理器问题
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(OffsetDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Integer getLoginAttempts() {
        return loginAttempts;
    }
    
    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }
    
    public OffsetDateTime getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(OffsetDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}