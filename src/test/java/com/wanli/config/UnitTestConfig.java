package com.wanli.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

/**
 * 单元测试配置类
 * 专门用于@WebMvcTest等单元测试，排除数据库相关的自动配置
 * 只在unit-test profile下生效
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@TestConfiguration
@Profile("unit-test")
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
public class UnitTestConfig {
    // 单元测试专用配置
    // 排除数据库相关配置，使用H2内存数据库和mock组件
}