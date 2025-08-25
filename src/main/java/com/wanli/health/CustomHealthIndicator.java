package com.wanli.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义健康检查指标
 * 检查数据库连接和系统资源状态
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try {
            // 检查数据库连接
            boolean dbHealthy = isDatabaseHealthy();
            
            // 检查内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            // 模拟活跃用户数
            int activeUsers = 0; // 这里可以从实际的用户会话管理中获取
            
            Health.Builder healthBuilder = dbHealthy ? Health.up() : Health.down();
            
            // 如果内存使用率超过90%，标记为DOWN
            if (memoryUsagePercent > 90) {
                healthBuilder = Health.down();
            }
            
            return healthBuilder
                    .withDetail("database", dbHealthy ? "UP" : "DOWN")
                    .withDetail("memory_usage", String.format("%.2f%%", memoryUsagePercent))
                    .withDetail("max_memory_mb", maxMemory / 1024 / 1024)
                    .withDetail("used_memory_mb", usedMemory / 1024 / 1024)
                    .withDetail("free_memory_mb", freeMemory / 1024 / 1024)
                    .withDetail("active_users", activeUsers)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    /**
     * 检查数据库连接是否健康
     * @return true if database is healthy, false otherwise
     */
    private boolean isDatabaseHealthy() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (SQLException e) {
            return false;
        }
    }
}
