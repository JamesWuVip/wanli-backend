package com.wanli.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 监控指标服务类
 * 提供业务指标记录功能
 * 
 * @author wanli-backend
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class MetricsService {

    @Autowired
    private Counter userLoginCounter;

    @Autowired
    private Counter userLoginFailureCounter;

    @Autowired
    private Timer apiRequestTimer;

    /**
     * 记录用户登录成功
     */
    public void recordUserLogin() {
        userLoginCounter.increment();
    }

    /**
     * 记录用户登录失败
     */
    public void recordUserLoginFailure() {
        userLoginFailureCounter.increment();
    }

    /**
     * 记录API请求时间
     * 
     * @param runnable 要执行的代码块
     */
    public void recordApiRequest(Runnable runnable) {
        Timer.Sample sample = Timer.start();
        try {
            runnable.run();
        } finally {
            sample.stop(apiRequestTimer);
        }
    }

    /**
     * 获取API请求计时器
     * 
     * @return Timer
     */
    public Timer getApiRequestTimer() {
        return apiRequestTimer;
    }
}