package com.wanli.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Micrometer监控指标配置类
 * 配置自定义监控指标和业务指标
 * 
 * @author wanli-backend
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class MetricsConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private DataSource dataSource;

    // 活跃用户数计数器
    private final AtomicInteger activeUsers = new AtomicInteger(0);

    /**
     * 用户登录计数器
     */
    @Bean
    public Counter userLoginCounter() {
        return Counter.builder("user.login.count")
                .description("Number of user logins")
                .tag("type", "success")
                .register(meterRegistry);
    }

    /**
     * 用户登录失败计数器
     */
    @Bean
    public Counter userLoginFailureCounter() {
        return Counter.builder("user.login.failure.count")
                .description("Number of failed user logins")
                .tag("type", "failure")
                .register(meterRegistry);
    }

    /**
     * API请求计时器
     */
    @Bean
    public Timer apiRequestTimer() {
        return Timer.builder("api.request.duration")
                .description("API request duration")
                .register(meterRegistry);
    }

    /**
     * 数据库连接池监控
     */
    @Bean
    public Gauge databaseConnectionGauge() {
        return Gauge.builder("database.connection.active", this, MetricsConfig::getDatabaseConnections)
                .description("Active database connections")
                .register(meterRegistry);
    }

    /**
     * 活跃用户数监控
     */
    @Bean
    public Gauge activeUsersGauge() {
        return Gauge.builder("users.active.count", activeUsers, AtomicInteger::get)
                .description("Number of active users")
                .register(meterRegistry);
    }

    /**
     * 系统内存使用监控
     */
    @Bean
    public Gauge memoryUsageGauge() {
        return Gauge.builder("system.memory.usage", this, MetricsConfig::getMemoryUsage)
                .description("System memory usage percentage")
                .register(meterRegistry);
    }

    /**
     * 自定义健康检查指标
     */
    @Bean
    public Gauge healthCheckGauge() {
        return Gauge.builder("health.check.status", this, MetricsConfig::getHealthStatus)
                .description("Application health check status (1=UP, 0=DOWN)")
                .register(meterRegistry);
    }

    /**
     * 获取数据库连接数
     */
    private double getDatabaseConnections() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(1) ? 1.0 : 0.0;
        } catch (SQLException e) {
            return 0.0;
        }
    }

    /**
     * 获取内存使用率
     */
    private double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return (double) usedMemory / maxMemory * 100;
    }

    /**
     * 获取健康检查状态
     */
    private double getHealthStatus() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5) ? 1.0 : 0.0;
        } catch (SQLException e) {
            return 0.0;
        }
    }

    /**
     * 增加活跃用户数
     */
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    /**
     * 减少活跃用户数
     */
    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }
}