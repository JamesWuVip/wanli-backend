package com.wanli.testdata;

import com.wanli.entity.User;
import com.wanli.enums.UserRole;
import com.wanli.enums.UserStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 测试数据管理器
 * 提供统一的测试数据创建和管理功能
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@Component
public class TestDataManager {

    /**
     * 创建测试用户
     * 
     * @param username 用户名
     * @param email 邮箱
     * @param role 用户角色
     * @return 测试用户对象
     */
    public User createTestUser(final String username, final String email, final UserRole role) {
        final User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNlBX2qJ.q.kJEjO6"); // encoded "password123"
        user.setFullName("Test " + username);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        return user;
    }

    /**
     * 创建默认管理员用户
     * 
     * @return 管理员用户
     */
    public User createDefaultAdmin() {
        return createTestUser("admin", "admin@test.com", UserRole.ADMIN);
    }

    /**
     * 创建默认教师用户
     * 
     * @return 教师用户
     */
    public User createDefaultTeacher() {
        return createTestUser("teacher", "teacher@test.com", UserRole.TEACHER);
    }

    /**
     * 创建默认学生用户
     * 
     * @return 学生用户
     */
    public User createDefaultStudent() {
        return createTestUser("student", "student@test.com", UserRole.STUDENT);
    }

    /**
     * 创建批量测试用户
     * 
     * @param count 用户数量
     * @param role 用户角色
     * @return 用户列表
     */
    public List<User> createBatchUsers(final int count, final UserRole role) {
        final List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            final String username = role.name().toLowerCase() + i;
            final String email = username + "@test.com";
            users.add(createTestUser(username, email, role));
        }
        return users;
    }

    /**
     * 创建测试用的JWT Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token
     */
    public String createTestJwtToken(final String userId, final String username, final UserRole role) {
        // 这里返回一个测试用的JWT Token
        // 在实际测试中，应该使用JwtUtil来生成真实的token
        return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    }

    /**
     * 清理测试数据
     * 用于测试后的数据清理
     */
    public void cleanupTestData() {
        // 实现测试数据清理逻辑
        // 可以根据需要清理特定的测试数据
    }

    /**
     * 验证测试数据的完整性
     * 
     * @param user 用户对象
     * @return 是否有效
     */
    public boolean isValidTestUser(final User user) {
        return user != null 
            && user.getId() != null 
            && user.getUsername() != null 
            && user.getEmail() != null 
            && user.getRole() != null 
            && user.getStatus() != null;
    }
}