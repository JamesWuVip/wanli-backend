package com.wanli.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 监控指标服务
 * 提供业务指标记录功能
 */
@Service
public class MetricsService {

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;
    private Timer apiRequestTimer;

    /**
     * 初始化监控指标
     */
    @PostConstruct
    public void initMetrics() {
        loginSuccessCounter = Counter.builder("auth.login.success")
                .description("Number of successful logins")
                .register(meterRegistry);

        loginFailureCounter = Counter.builder("auth.login.failure")
                .description("Number of failed logins")
                .register(meterRegistry);

        apiRequestTimer = Timer.builder("api.request.duration")
                .description("API request duration")
                .register(meterRegistry);
    }

    /**
     * 记录登录成功
     */
    public void recordLoginSuccess() {
        loginSuccessCounter.increment();
    }

    /**
     * 记录登录失败
     */
    public void recordLoginFailure() {
        loginFailureCounter.increment();
    }

    /**
     * 记录API请求时间
     * @param duration 请求持续时间（毫秒）
     */
    public void recordApiRequestTime(long duration) {
        apiRequestTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 开始计时API请求
     * @return Timer.Sample
     */
    public Timer.Sample startApiRequestTimer() {
        return Timer.start(meterRegistry);
    }
}
