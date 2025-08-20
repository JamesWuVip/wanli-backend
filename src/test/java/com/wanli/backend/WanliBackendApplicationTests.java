package com.wanli.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 万里后端应用测试类
 * 
 * @author wanli-team
 * @version 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
class WanliBackendApplicationTests {

    /**
     * 测试应用上下文加载
     */
    @Test
    void contextLoads() {
        // 测试Spring Boot应用上下文是否能正常加载
    }

}