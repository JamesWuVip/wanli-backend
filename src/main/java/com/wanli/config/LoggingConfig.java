package com.wanli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * 日志配置类
 * 配置请求日志和HTTP跟踪
 * 
 * @author wanli-backend
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class LoggingConfig {

    /**
     * 配置请求日志过滤器
     * 记录HTTP请求的详细信息
     * 
     * @return CommonsRequestLoggingFilter
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }

    // 注意: HttpTraceRepository 在 Spring Boot 2.2+ 中已被移除
    // 如需HTTP跟踪功能，请考虑使用 Spring Boot Actuator 的其他端点
    // 或实现自定义的HTTP跟踪解决方案
}