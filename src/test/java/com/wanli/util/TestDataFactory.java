package com.wanli.util;

import com.wanli.dto.LoginRequestDto;
import com.wanli.dto.UserCreateDto;
import com.wanli.dto.UserRegistrationDto;
import com.wanli.dto.UserUpdateDto;
import com.wanli.entity.User;
import com.wanli.entity.UserRole;
import com.wanli.entity.UserStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 测试数据工厂类
 * 用于生成各种测试场景所需的测试数据
 */
public class TestDataFactory {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 创建默认测试用户
     */
    public static User createDefaultUser() {
        User user = new User();
        // 不设置ID，让JPA自动生成
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        user.setUsername("testuser_" + uniqueSuffix);
        user.setEmail("test_" + uniqueSuffix + "@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setFullName("Test User");
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }
    
    /**
     * 创建管理员用户
     */
    public static User createAdminUser() {
        User user = new User();
        // 不设置ID，让JPA自动生成
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPasswordHash(passwordEncoder.encode("admin123"));
        user.setFullName("Admin User");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }
    
    /**
     * 创建教师用户
     */
    public static User createTeacherUser() {
        User user = new User();
        // 不设置ID，让JPA自动生成
        user.setUsername("teacher");
        user.setEmail("teacher@example.com");
        user.setPasswordHash(passwordEncoder.encode("teacher123"));
        user.setFullName("Teacher User");
        user.setRole(UserRole.HQ_TEACHER);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }
    
    /**
     * 创建被禁用的用户
     */
    public static User createInactiveUser() {
        User user = createDefaultUser();
        user.setUsername("inactiveuser");
        user.setEmail("inactive@example.com");
        user.setStatus(UserStatus.INACTIVE);
        return user;
    }
    
    /**
     * 创建自定义用户
     */
    public static User createUser(String username, String email, String password, UserRole role, UserStatus status) {
        User user = new User();
        // 不设置ID，让JPA自动生成
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(username + " FullName");
        user.setRole(role);
        user.setStatus(status);
        user.setLoginAttempts(0);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }
    
    /**
     * 创建用户注册DTO
     */
    public static UserRegistrationDto createUserRegistrationDto() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("newuser");
        dto.setEmail("newuser@example.com");
        dto.setPassword("password123");
        dto.setFullName("New User");
        dto.setRole(UserRole.STUDENT);
        return dto;
    }
    
    /**
     * 创建自定义用户注册DTO
     */
    public static UserRegistrationDto createUserRegistrationDto(String username, String email, String password) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setFullName(username + " FullName");
        dto.setRole(UserRole.STUDENT);
        return dto;
    }
    
    /**
     * 创建用户创建DTO
     */
    public static UserCreateDto createUserCreateDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("newuser");
        dto.setEmail("newuser@example.com");
        dto.setPassword("password123");
        dto.setFullName("New User");
        dto.setRole(UserRole.STUDENT);
        return dto;
    }
    
    /**
     * 创建用户更新DTO
     */
    public static UserUpdateDto createUserUpdateDto() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("updated@example.com");
        dto.setFullName("Updated User");
        return dto;
    }
    
    /**
     * 创建登录请求DTO
     */
    public static LoginRequestDto createLoginRequestDto() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsername("testuser");
        dto.setPassword("password123");
        return dto;
    }
    
    /**
     * 创建自定义登录请求DTO
     */
    public static LoginRequestDto createLoginRequestDto(String username, String password) {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }
    
    /**
     * 获取原始密码（未加密）
     */
    public static String getDefaultPassword() {
        return "password123";
    }
    
    /**
     * 获取加密后的密码
     */
    public static String getEncodedPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 生成随机用户名
     */
    public static String generateRandomUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 生成随机邮箱
     */
    public static String generateRandomEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }
}