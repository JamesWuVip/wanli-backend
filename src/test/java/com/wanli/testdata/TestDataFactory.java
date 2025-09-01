package com.wanli.testdata;

import com.wanli.entity.User;
import com.wanli.enums.UserRole;
import com.wanli.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 测试数据工厂类
 * 使用Builder模式创建测试数据
 * 
 * @author JamesWu
 * @since 1.0.0
 */
public final class TestDataFactory {

    public TestDataFactory() {
        // 工具类构造函数
    }

    /**
     * 创建用户构建器
     * 
     * @return 用户构建器
     */
    public static UserBuilder userBuilder() {
        return new UserBuilder();
    }

    /**
     * 用户构建器
     */
    public static class UserBuilder {
        private String id = UUID.randomUUID().toString();
        private String username = "testuser";
        private String phone = "13800138000";
        private String password = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNlBX2qJ.q.kJEjO6";
        private String fullName = "Test User";
        private UserRole role = UserRole.STUDENT;
        private User.UserStatus status = User.UserStatus.ACTIVE;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private String createdBy = "system";
        private String updatedBy = "system";

        /**
         * 设置用户ID
         * 
         * @param id 用户ID
         * @return 构建器
         */
        public UserBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * 设置用户名
         * 
         * @param username 用户名
         * @return 构建器
         */
        public UserBuilder username(final String username) {
            this.username = username;
            return this;
        }

        /**
         * 设置手机号
         * 
         * @param phone 手机号
         * @return 构建器
         */
        public UserBuilder phone(final String phone) {
            this.phone = phone;
            return this;
        }

        /**
         * 设置密码
         * 
         * @param password 密码
         * @return 构建器
         */
        public UserBuilder password(final String password) {
            this.password = password;
            return this;
        }

        /**
         * 设置全名
         * 
         * @param fullName 全名
         * @return 构建器
         */
        public UserBuilder fullName(final String fullName) {
            this.fullName = fullName;
            return this;
        }

        /**
         * 设置用户角色
         * 
         * @param role 用户角色
         * @return 构建器
         */
        public UserBuilder role(final UserRole role) {
            this.role = role;
            return this;
        }

        /**
         * 设置用户状态
         * 
         * @param status 用户状态
         * @return 构建器
         */
        public UserBuilder status(final User.UserStatus status) {
            this.status = status;
            return this;
        }

        /**
         * 设置创建时间
         * 
         * @param createdAt 创建时间
         * @return 构建器
         */
        public UserBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * 设置更新时间
         * 
         * @param updatedAt 更新时间
         * @return 构建器
         */
        public UserBuilder updatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * 设置创建者
         * 
         * @param createdBy 创建者
         * @return 构建器
         */
        public UserBuilder createdBy(final String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * 设置更新者
         * 
         * @param updatedBy 更新者
         * @return 构建器
         */
        public UserBuilder updatedBy(final String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        /**
         * 创建管理员用户
         * 
         * @return 构建器
         */
        public UserBuilder asAdmin() {
            this.role = UserRole.ADMIN;
            this.username = "admin";
            this.phone = "13800138000";
            this.fullName = "Test Admin";
            return this;
        }

        /**
         * 创建教师用户
         * 
         * @return 构建器
         */
        public UserBuilder asTeacher() {
            this.role = UserRole.TEACHER;
            this.username = "teacher";
            this.phone = "13800138001";
            this.fullName = "Test Teacher";
            return this;
        }

        /**
         * 创建学生用户
         * 
         * @return 构建器
         */
        public UserBuilder asStudent() {
            this.role = UserRole.STUDENT;
            this.username = "student";
            this.phone = "13800138002";
            this.fullName = "Test Student";
            return this;
        }

        /**
         * 设置为非激活状态
         * 
         * @return 构建器
         */
        public UserBuilder inactive() {
            this.status = User.UserStatus.INACTIVE;
            return this;
        }

        /**
         * 构建用户对象
         * 
         * @return 用户对象
         */
        public User build() {
            final User user = new User();
            user.setId(this.id);
            user.setUsername(this.username);
            user.setPhone(this.phone);
            user.setPassword(this.password);
            user.setFullName(this.fullName);
            user.setRole(this.role);
            user.setStatus(this.status);
            user.setCreatedAt(this.createdAt);
            user.setUpdatedAt(this.updatedAt);
            user.setCreatedBy(this.createdBy);
            user.setUpdatedBy(this.updatedBy);
            return user;
        }
    }
}