package com.wanli.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 监控指标服务
 * 提供业务指标记录和统计功能
 * 
 * @author wanli-backend
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class MetricsService {

    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Timer apiRequestTimer;

    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.loginSuccessCounter = Counter.builder("user.login.success")
                .description("Number of successful user logins")
                .register(meterRegistry);
        
        this.loginFailureCounter = Counter.builder("user.login.failure")
                .description("Number of failed user login attempts")
                .register(meterRegistry);
        
        this.apiRequestTimer = Timer.builder("api.request.duration")
                .description("API request processing time")
                .register(meterRegistry);
    }

    /**
     * 记录用户登录成功
     */
    public void recordLoginSuccess() {
        loginSuccessCounter.increment();
    }

    /**
     * 记录用户登录失败
     */
    public void recordLoginFailure() {
        loginFailureCounter.increment();
    }

    /**
     * 记录API请求时间
     * 
     * @param duration 请求持续时间（毫秒）
     */
    public void recordApiRequestTime(long duration) {
        apiRequestTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 开始API请求计时
     * 
     * @return Timer.Sample
     */
    public Timer.Sample startApiRequestTimer() {
        return Timer.start();
    }

    /**
     * 停止API请求计时并记录
     * 
     * @param sample Timer.Sample
     */
    public void stopApiRequestTimer(Timer.Sample sample) {
        sample.stop(apiRequestTimer);
    }
}