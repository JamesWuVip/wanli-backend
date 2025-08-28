package com.wanli.util;

import com.wanli.entity.User;
import com.wanli.entity.UserRole;
import com.wanli.entity.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 测试数据工厂类
 * 用于创建测试所需的各种实体对象
 */
public class TestDataFactory {

    /**
     * 创建默认用户
     */
    public static User createDefaultUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hashedPassword");
        user.setFullName("Test User");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        return user;
    }

    /**
     * 创建管理员用户
     */
    public static User createAdminUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPasswordHash("$2a$10$hashedAdminPassword");
        user.setFullName("Admin User");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        return user;
    }

    /**
     * 创建教师用户
     */
    public static User createTeacherUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("teacher");
        user.setEmail("teacher@example.com");
        user.setPasswordHash("$2a$10$hashedTeacherPassword");
        user.setFullName("Teacher User");
        user.setRole(UserRole.HQ_TEACHER);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        return user;
    }

    /**
     * 创建被锁定的用户
     */
    public static User createLockedUser() {
        User user = createDefaultUser();
        user.setUsername("lockeduser");
        user.setEmail("locked@example.com");
        user.setLoginAttempts(5);
        user.setLockedUntil(OffsetDateTime.now().plusHours(1));
        return user;
    }

    /**
     * 创建非激活用户
     */
    public static User createInactiveUser() {
        User user = createDefaultUser();
        user.setUsername("inactiveuser");
        user.setEmail("inactive@example.com");
        user.setStatus(UserStatus.INACTIVE);
        return user;
    }

    /**
     * 创建具有指定用户名的用户
     */
    public static User createUserWithUsername(String username) {
        User user = createDefaultUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }

    /**
     * 创建具有指定邮箱的用户
     */
    public static User createUserWithEmail(String email) {
        User user = createDefaultUser();
        user.setEmail(email);
        user.setUsername(email.split("@")[0]);
        return user;
    }

    /**
     * 创建具有指定角色的用户
     */
    public static User createUserWithRole(UserRole role) {
        User user = createDefaultUser();
        user.setRole(role);
        return user;
    }

    /**
     * 创建具有指定状态的用户
     */
    public static User createUserWithStatus(UserStatus status) {
        User user = createDefaultUser();
        user.setStatus(status);
        return user;
    }
}