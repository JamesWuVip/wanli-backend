package com.wanli.entity;

import com.wanli.common.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体类.
 * 
 * <p>用于映射数据库中的users表，包含用户的基本信息、状态和审计字段。
 * 支持JPA审计功能，自动记录创建和更新时间。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>用户基本信息管理（用户名、邮箱、手机号等）</li>
 *   <li>用户状态管理（激活、停用、暂停、删除）</li>
 *   <li>用户角色管理（学生、教师、管理员）</li>
 *   <li>验证状态管理（邮箱验证、手机验证）</li>
 *   <li>审计信息记录（创建时间、更新时间、操作人）</li>
 * </ul>
 * 
 * @author AI Generated
 * @version 1.0
 * @since 2025-01-22
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * 用户唯一标识符.
     * 使用UUID格式，确保全局唯一性.
     */
    @Id
    @Column(name = "id", length = Constants.UUID_LENGTH)
    private String id;

    /**
     * 用户名.
     * 用于用户登录和显示，必须唯一且不能为空.
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = Constants.USERNAME_MIN_LENGTH,
          max = Constants.USERNAME_MAX_LENGTH,
          message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", length = Constants.USERNAME_MAX_LENGTH,
            nullable = false, unique = true)
    private String username;

    /**
     * 用户邮箱地址.
     * 用于邮箱验证、密码重置等功能，可选字段.
     */
    @Email(message = "邮箱格式不正确")
    @Column(name = "email", length = Constants.EMAIL_MAX_LENGTH)
    private String email;

    /**
     * 用户密码哈希值.
     * 存储加密后的密码，不存储明文密码.
     */
    @NotBlank(message = "密码不能为空")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * 用户全名.
     * 用户的真实姓名，可选字段.
     */
    @Size(max = Constants.FULL_NAME_MAX_LENGTH,
          message = "全名长度不能超过100个字符")
    @Column(name = "full_name", length = Constants.FULL_NAME_MAX_LENGTH)
    private String fullName;

    /**
     * 用户手机号.
     * 用于短信验证、登录等功能，必须唯一且不能为空.
     */
    @NotBlank(message = "手机号不能为空")
    @Size(max = Constants.PHONE_MAX_LENGTH, message = "手机号长度不能超过20个字符")
    @Column(name = "phone", length = Constants.PHONE_MAX_LENGTH,
            nullable = false, unique = true)
    private String phone;

    /**
     * 用户头像URL.
     * 存储用户头像的访问地址，可选字段.
     */
    @Column(name = "avatar_url", length = Constants.AVATAR_URL_MAX_LENGTH)
    private String avatarUrl;

    /**
     * 用户角色.
     * 定义用户在系统中的权限级别，默认为学生角色.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private com.wanli.enums.UserRole role = com.wanli.enums.UserRole.STUDENT;

    /**
     * 用户状态.
     * 定义用户账户的当前状态，默认为激活状态.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 邮箱验证状态.
     * 标识用户邮箱是否已通过验证，默认为未验证.
     */
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    /**
     * 手机验证状态.
     * 标识用户手机号是否已通过验证，默认为未验证.
     */
    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    /**
     * 最后登录时间.
     * 记录用户最近一次登录的时间，用于统计和安全监控.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间.
     * 记录用户账户的创建时间，由JPA审计自动设置.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间.
     * 记录用户信息的最后更新时间，由JPA审计自动维护.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建者.
     * 记录创建该用户记录的操作人ID.
     */
    @Column(name = "created_by", length = Constants.UUID_LENGTH)
    private String createdBy;

    /**
     * 更新者.
     * 记录最后更新该用户记录的操作人ID.
     */
    @Column(name = "updated_by", length = Constants.UUID_LENGTH)
    private String updatedBy;

    /**
     * 用户状态枚举.
     * 定义用户账户的各种状态.
     */
    public enum UserStatus {
        /** 激活状态 - 用户可以正常使用系统 */
        ACTIVE,
        /** 非激活状态 - 用户暂时无法使用系统 */
        INACTIVE,
        /** 暂停状态 - 用户因违规等原因被暂停使用 */
        SUSPENDED,
        /** 删除状态 - 用户账户已被删除（软删除） */
        DELETED
    }

    /**
     * 默认构造函数.
     * JPA要求实体类必须有无参构造函数.
     */
    public User() { }

    /**
     * 构造函数 - 创建用户实例.
     * 
     * <p>用于创建新用户时的便捷构造方法，包含必需的基本信息。</p>
     * 
     * @param userId 用户ID，必须是有效的UUID格式
     * @param userName 用户名，必须符合长度和格式要求
     * @param userPhone 手机号，必须符合格式要求
     * @param userPasswordHash 密码哈希值，必须是加密后的密码
     */
    public User(final String userId, final String userName,
                final String userPhone, final String userPasswordHash) {
        this.id = userId;
        this.username = userName;
        this.phone = userPhone;
        this.passwordHash = userPasswordHash;
    }

    /**
     * 获取用户ID.
     *
     * @return 用户ID.
     */
    public String getId() {
        return id;
    }

    /**
     * 设置用户ID.
     *
     * @param userId 用户ID.
     */
    public void setId(final String userId) {
        this.id = userId;
    }

    /**
     * 获取用户名.
     *
     * @return 用户名.
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名.
     *
     * @param userName 用户名.
     */
    public void setUsername(final String userName) {
        this.username = userName;
    }

    /**
     * 获取邮箱.
     *
     * @return 邮箱.
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱.
     *
     * @param userEmail 邮箱.
     */
    public void setEmail(final String userEmail) {
        this.email = userEmail;
    }

    /**
     * 获取密码哈希.
     *
     * @return 密码哈希.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置密码哈希.
     *
     * @param userPasswordHash 密码哈希.
     */
    public void setPasswordHash(final String userPasswordHash) {
        this.passwordHash = userPasswordHash;
    }

    /**
     * 设置密码（兼容方法）.
     *
     * @param password 密码.
     */
    public void setPassword(final String password) {
        this.passwordHash = password;
    }

    /**
     * 获取全名.
     *
     * @return 全名.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * 设置全名.
     *
     * @param userFullName 全名.
     */
    public void setFullName(final String userFullName) {
        this.fullName = userFullName;
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
     * 获取手机号码（兼容方法）.
     *
     * @return 手机号码.
     */
    public String getPhoneNumber() {
        return phone;
    }

    /**
     * 设置手机号码（兼容方法）.
     *
     * @param userPhoneNumber 手机号码.
     */
    public void setPhoneNumber(final String userPhoneNumber) {
        this.phone = userPhoneNumber;
    }

    /**
     * 获取头像URL.
     *
     * @return 头像URL.
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 设置头像URL.
     *
     * @param userAvatarUrl 头像URL.
     */
    public void setAvatarUrl(final String userAvatarUrl) {
        this.avatarUrl = userAvatarUrl;
    }

    /**
     * 获取用户角色.
     *
     * @return 用户角色.
     */
    public com.wanli.enums.UserRole getRole() {
        return role;
    }

    /**
     * 设置用户角色.
     *
     * @param userRole 用户角色.
     */
    public void setRole(final com.wanli.enums.UserRole userRole) {
        this.role = userRole;
    }

    /**
     * 获取用户状态.
     *
     * @return 用户状态.
     */
    public UserStatus getStatus() {
        return status;
    }

    /**
     * 设置用户状态.
     *
     * @param userStatus 用户状态.
     */
    public void setStatus(final UserStatus userStatus) {
        this.status = userStatus;
    }

    /**
     * 获取邮箱验证状态.
     *
     * @return 邮箱验证状态.
     */
    public Boolean getEmailVerified() {
        return emailVerified;
    }

    /**
     * 设置邮箱验证状态.
     *
     * @param userEmailVerified 邮箱验证状态.
     */
    public void setEmailVerified(final Boolean userEmailVerified) {
        this.emailVerified = userEmailVerified;
    }

    /**
     * 获取手机验证状态.
     *
     * @return 手机验证状态.
     */
    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    /**
     * 设置手机验证状态.
     *
     * @param userPhoneVerified 手机验证状态.
     */
    public void setPhoneVerified(final Boolean userPhoneVerified) {
        this.phoneVerified = userPhoneVerified;
    }

    /**
     * 获取最后登录时间.
     *
     * @return 最后登录时间.
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * 设置最后登录时间.
     *
     * @param userLastLoginAt 最后登录时间.
     */
    public void setLastLoginAt(final LocalDateTime userLastLoginAt) {
        this.lastLoginAt = userLastLoginAt;
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
     * @param userCreatedAt 创建时间.
     */
    public void setCreatedAt(final LocalDateTime userCreatedAt) {
        this.createdAt = userCreatedAt;
    }

    /**
     * 获取更新时间.
     *
     * @return 更新时间.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间.
     *
     * @param userUpdatedAt 更新时间.
     */
    public void setUpdatedAt(final LocalDateTime userUpdatedAt) {
        this.updatedAt = userUpdatedAt;
    }

    /**
     * 获取创建者.
     *
     * @return 创建者.
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建者.
     *
     * @param userCreatedBy 创建者.
     */
    public void setCreatedBy(final String userCreatedBy) {
        this.createdBy = userCreatedBy;
    }

    /**
     * 获取更新者.
     *
     * @return 更新者.
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * 设置更新者.
     *
     * @param userUpdatedBy 更新者.
     */
    public void setUpdatedBy(final String userUpdatedBy) {
        this.updatedBy = userUpdatedBy;
    }

    /**
     * 比较两个用户对象是否相等.
     * 基于用户ID进行比较，确保对象唯一性.
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
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    /**
     * 计算用户对象的哈希码.
     * 基于用户ID计算哈希值，与equals方法保持一致.
     *
     * @return 用户对象的哈希码.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 返回用户对象的字符串表示.
     * 包含主要的用户信息字段，用于调试和日志记录.
     *
     * @return 用户对象的字符串表示.
     */
    @Override
    public String toString() {
        return "User{"
                + "id='" + id
                + "', username='" + username
                + "', email='" + email
                + "', fullName='" + fullName
                + "', status=" + status
                + ", createdAt=" + createdAt
                + '}';
    }
}