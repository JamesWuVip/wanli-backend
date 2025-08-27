package com.wanli.base;

import com.wanli.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * 测试基类
 * 提供通用的测试配置和工具方法
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public abstract class TestBase {
    
    protected TestDataFactory testDataFactory;
    
    @BeforeEach
    void setUp() {
        testDataFactory = new TestDataFactory();
        setupTestData();
    }
    
    /**
     * 子类可以重写此方法来设置特定的测试数据
     */
    protected void setupTestData() {
        // 默认实现为空，子类可以重写
    }
    
    /**
     * 清理测试数据
     */
    protected void cleanupTestData() {
        // 默认实现为空，子类可以重写
    }
}