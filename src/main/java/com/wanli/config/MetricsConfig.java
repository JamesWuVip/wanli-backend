package com.wanli.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

/**
 * 监控指标配置类
 * 配置自定义业务指标
 */
@Configuration
@EnableScheduling
public class MetricsConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    private volatile int activeUsers = 0;

    /**
     * 初始化自定义指标
     */
    @PostConstruct
    public void initMetrics() {
        // 注册系统内存使用率指标
        Gauge.builder("system.memory.usage.percent")
                .description("System memory usage percentage")
                .register(meterRegistry, this, MetricsConfig::getMemoryUsagePercent);

        // 注册活跃用户数指标
        Gauge.builder("business.active.users")
                .description("Number of active users")
                .register(meterRegistry, this, MetricsConfig::getActiveUsers);
    }

    /**
     * 获取内存使用百分比
     * @param metricsConfig MetricsConfig实例
     * @return 内存使用百分比
     */
    private static double getMemoryUsagePercent(MetricsConfig metricsConfig) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return (double) usedMemory / maxMemory * 100;
    }

    /**
     * 获取活跃用户数
     * @param metricsConfig MetricsConfig实例
     * @return 活跃用户数
     */
    private static double getActiveUsers(MetricsConfig metricsConfig) {
        return metricsConfig.activeUsers;
    }

    /**
     * 定时更新活跃用户数（示例）
     */
    @Scheduled(fixedRate = 60000) // 每分钟更新一次
    public void updateActiveUsers() {
        // 这里可以实现实际的活跃用户统计逻辑
        // 目前使用模拟数据
        this.activeUsers = (int) (Math.random() * 100);
    }
}
