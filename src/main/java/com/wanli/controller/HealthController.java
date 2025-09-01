package com.wanli.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器.
     *
 * <p>提供应用程序健康状态检查功能，包括数据库、Redis和磁盘空间检查。</p>.
     *
 * @author JamesWu.
 * @since 1.0.0.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 数据库连接超时时间（秒）.
     */
    private static final int DB_CONNECTION_TIMEOUT_SECONDS = 5;

    /**
     * 磁盘使用率健康阈值（百分比）.
     */
    private static final double DISK_USAGE_HEALTHY_THRESHOLD = 90.0;

    /**
     * 百分比计算常量.
     */
    private static final double PERCENTAGE_MULTIPLIER = 100.0;

    /**
     * HTTP服务不可用状态码.
     */
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;

    /**
     * 数据源.
     */
    @Autowired
    private DataSource dataSource;

    /**
     * Redis模板.
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 基础健康检查.
     *
     * @return 健康检查结果.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        boolean allHealthy = true;

        // 检查数据库连接
        Map<String, Object> dbHealth = checkDatabase();
        components.put("database", dbHealth);
        if (!"UP".equals(dbHealth.get("status"))) {
            allHealthy = false;
        }

        // 检查Redis连接
        Map<String, Object> redisHealth = checkRedis();
        components.put("redis", redisHealth);
        if (!"UP".equals(redisHealth.get("status"))) {
            allHealthy = false;
        }

        // 检查磁盘空间
        Map<String, Object> diskHealth = checkDiskSpace();
        components.put("diskSpace", diskHealth);
        if (!"UP".equals(diskHealth.get("status"))) {
            allHealthy = false;
        }

        response.put("status", allHealthy ? "UP" : "DOWN");
        response.put("timestamp", LocalDateTime.now());
        response.put("components", components);

        return ResponseEntity.ok(response);
    }

    /**
     * 详细健康检查.
     *
     * @return 详细的系统信息和健康状态.
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();

        // 系统信息
        Map<String, Object> system = new HashMap<>();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("availableProcessors",
                Runtime.getRuntime().availableProcessors());
        system.put("maxMemory", Runtime.getRuntime().maxMemory());
        system.put("totalMemory", Runtime.getRuntime().totalMemory());
        system.put("freeMemory", Runtime.getRuntime().freeMemory());

        response.put("system", system);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * 检查数据库连接.
     *
     * @return 数据库连接状态.
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            Connection connection = dataSource.getConnection();
            boolean isValid = connection.isValid(DB_CONNECTION_TIMEOUT_SECONDS);
            connection.close();

            result.put("status", isValid ? "UP" : "DOWN");
            result.put("database", "MySQL");
            if (isValid) {
                result.put("details", "Database connection is healthy");
            } else {
                result.put("details", "Database connection validation failed");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("details", "Failed to connect to database");
        }
        return result;
    }

    /**
     * 检查Redis连接.
     *
     * @return Redis连接状态.
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory()
                    .getConnection();
            String pong = connection.ping();
            connection.close();

            boolean isHealthy = "PONG".equals(pong);
            result.put("status", isHealthy ? "UP" : "DOWN");
            result.put("redis", "Redis");
            if (isHealthy) {
                result.put("details", "Redis connection is healthy");
            } else {
                result.put("details", "Redis ping failed");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("details", "Failed to connect to Redis");
        }
        return result;
    }

    /**
     * 检查磁盘空间.
     *
     * @return 磁盘空间使用情况.
     */
    private Map<String, Object> checkDiskSpace() {
        Map<String, Object> result = new HashMap<>();
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercentage = (double) usedSpace / totalSpace
                    * PERCENTAGE_MULTIPLIER;

            boolean isHealthy = usagePercentage < DISK_USAGE_HEALTHY_THRESHOLD;

            result.put("status", isHealthy ? "UP" : "DOWN");
            result.put("total", totalSpace);
            result.put("free", freeSpace);
            result.put("used", usedSpace);
            result.put("usagePercentage", usagePercentage);
            result.put("threshold", DISK_USAGE_HEALTHY_THRESHOLD);

            if (isHealthy) {
                result.put("details", "Disk space usage is within healthy limits");
            } else {
                result.put("details", "Disk space usage exceeds healthy threshold");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("details", "Failed to check disk space");
        }
        return result;
    }

    /**
     * 简单的存活检查.
     *
     * @return 简单的存活状态.
     */
    @GetMapping("/health/liveness")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Application is alive");
        return ResponseEntity.ok(response);
    }

    /**
     * 就绪检查.
     *
     * @return 应用程序就绪状态.
     */
    @GetMapping("/health/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        boolean isReady = true;

        // 检查关键依赖
        Map<String, Object> dbHealth = checkDatabase();
        if (!"UP".equals(dbHealth.get("status"))) {
            isReady = false;
        }

        Map<String, Object> redisHealth = checkRedis();
        if (!"UP".equals(redisHealth.get("status"))) {
            isReady = false;
        }

        response.put("status", isReady ? "UP" : "DOWN");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", isReady ? "Application is ready" : "Application is not ready");

        return isReady ? ResponseEntity.ok(response) :
                ResponseEntity.status(HTTP_SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Actuator兼容的健康检查端点.
     *
     * @return Actuator格式的健康检查结果.
     */
    @GetMapping("/health/actuator")
    public ResponseEntity<Map<String, Object>> actuatorHealth() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        boolean allHealthy = true;

        // 数据库检查
        Map<String, Object> dbHealth = checkDatabase();
        components.put("db", dbHealth);
        if (!"UP".equals(dbHealth.get("status"))) {
            allHealthy = false;
        }

        // Redis检查
        Map<String, Object> redisHealth = checkRedis();
        components.put("redis", redisHealth);
        if (!"UP".equals(redisHealth.get("status"))) {
            allHealthy = false;
        }

        // 磁盘空间检查
        Map<String, Object> diskHealth = checkDiskSpace();
        components.put("diskSpace", diskHealth);
        if (!"UP".equals(diskHealth.get("status"))) {
            allHealthy = false;
        }

        response.put("status", allHealthy ? "UP" : "DOWN");
        response.put("components", components);

        return ResponseEntity.ok(response);
    }
}