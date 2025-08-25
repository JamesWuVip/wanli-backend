package com.wanli.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

/**
 * Micrometer监控指标配置
 * 配置自定义监控指标和度量
 * 
 * @author wanli-backend
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class MetricsConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 配置系统内存使用率指标
     * 
     * @return Gauge
     */
    @Bean
    public Gauge memoryUsageGauge() {
        return Gauge.builder("system.memory.usage")
                .description("System memory usage percentage")
                .tag("type", "heap")
                .register(meterRegistry, this, obj -> {
                    Runtime runtime = Runtime.getRuntime();
                    long maxMemory = runtime.maxMemory();
                    long totalMemory = runtime.totalMemory();
                    long freeMemory = runtime.freeMemory();
                    long usedMemory = totalMemory - freeMemory;
                    return (double) usedMemory / maxMemory * 100;
                });
    }

    /**
     * 配置活跃用户数指标
     * 
     * @return Gauge
     */
    @Bean
    public Gauge activeUsersGauge() {
        return Gauge.builder("application.users.active")
                .description("Number of active users")
                .tag("status", "online")
                .register(meterRegistry, this, obj -> {
                    // TODO: 实现实际的活跃用户统计逻辑
                    return 0.0;
                });
    }
}