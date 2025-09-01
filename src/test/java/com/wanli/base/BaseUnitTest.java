package com.wanli.base;

import com.wanli.config.UnitTestConfig;
import com.wanli.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * 单元测试基类
 * 提供轻量级的测试环境，使用Mock对象
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = UnitTestConfig.class)
@ActiveProfiles("unit-test")
@TestPropertySource(locations = "classpath:application-unit-test.yml")
@Import(UnitTestConfig.class)
public abstract class BaseUnitTest {

    /**
     * 测试数据工厂
     */
    protected final TestDataFactory testDataFactory = new TestDataFactory();

    /**
     * 测试前的初始化
     * 子类可以重写此方法进行特定的初始化操作
     */
    @BeforeEach
    protected void setUp() {
        // 基础设置，子类可以重写
        initializeMocks();
    }

    /**
     * 初始化Mock对象
     * 子类可以重写此方法来设置特定的Mock行为
     */
    protected void initializeMocks() {
        // 默认不做任何操作，子类可以重写
    }

    /**
     * 验证Mock对象的交互
     * 子类可以重写此方法来验证特定的Mock交互
     */
    protected void verifyMockInteractions() {
        // 默认不做任何操作，子类可以重写
    }

    /**
     * 重置所有Mock对象
     * 在需要清理Mock状态时调用
     */
    protected void resetMocks() {
        // 子类可以重写此方法来重置特定的Mock对象
    }
}