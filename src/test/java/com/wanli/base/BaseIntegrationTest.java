package com.wanli.base;

import com.wanli.config.TestSecurityConfig;
import com.wanli.testdata.TestDataManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集成测试基类
 * 提供完整的Spring Boot应用上下文和数据库连接
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Import(TestSecurityConfig.class)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected TestDataManager testDataManager;

    /**
     * 测试前的初始化
     * 子类可以重写此方法进行特定的初始化操作
     */
    @BeforeEach
    protected void setUp() {
        // 基础设置，子类可以重写
        initializeTestData();
    }

    /**
     * 初始化测试数据
     * 子类可以重写此方法来设置特定的测试数据
     */
    protected void initializeTestData() {
        // 默认不做任何操作，子类可以重写
    }

    /**
     * 清理测试数据
     * 在测试完成后调用
     */
    protected void cleanupTestData() {
        testDataManager.cleanupTestData();
    }

    /**
     * 获取测试用的JWT Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token
     */
    protected String getTestJwtToken(final String userId, final String username, final String role) {
        return testDataManager.createTestJwtToken(userId, username, 
            com.wanli.enums.UserRole.valueOf(role.toUpperCase()));
    }

    /**
     * 获取管理员Token
     * 
     * @return 管理员JWT Token
     */
    protected String getAdminToken() {
        return getTestJwtToken("admin-id", "admin", "ADMIN");
    }

    /**
     * 获取教师Token
     * 
     * @return 教师JWT Token
     */
    protected String getTeacherToken() {
        return getTestJwtToken("teacher-id", "teacher", "TEACHER");
    }

    /**
     * 获取学生Token
     * 
     * @return 学生JWT Token
     */
    protected String getStudentToken() {
        return getTestJwtToken("student-id", "student", "STUDENT");
    }
}