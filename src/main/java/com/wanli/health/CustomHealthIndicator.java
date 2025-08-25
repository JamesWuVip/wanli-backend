package com.wanli.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义健康检查指标
 * 提供数据库连接和应用状态检查
 * 
 * @author wanli-backend
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component("custom")
public class CustomHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            // 检查数据库连接
            if (isDatabaseHealthy()) {
                builder.up()
                    .withDetail("database", "Available")
                    .withDetail("connection_pool", "Active");
            } else {
                builder.down()
                    .withDetail("database", "Unavailable")
                    .withDetail("connection_pool", "Inactive");
            }
            
            // 检查内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;
            
            java.util.Map<String, String> memoryDetails = new java.util.HashMap<>();
            memoryDetails.put("max", maxMemory / 1024 / 1024 + "MB");
            memoryDetails.put("total", totalMemory / 1024 / 1024 + "MB");
            memoryDetails.put("used", usedMemory / 1024 / 1024 + "MB");
            memoryDetails.put("free", freeMemory / 1024 / 1024 + "MB");
            memoryDetails.put("usage_percentage", String.format("%.2f%%", memoryUsagePercentage));
            
            builder.withDetail("memory", memoryDetails);
            
            // 如果内存使用率超过90%，标记为DOWN
            if (memoryUsagePercentage > 90) {
                builder.down().withDetail("memory_status", "High memory usage detected");
            }
            
        } catch (Exception e) {
            builder.down()
                .withDetail("error", e.getMessage())
                .withException(e);
        }
        
        return builder.build();
    }

    /**
     * 检查数据库是否健康
     * 
     * @return boolean
     */
    private boolean isDatabaseHealthy() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (SQLException e) {
            return false;
        }
    }
}