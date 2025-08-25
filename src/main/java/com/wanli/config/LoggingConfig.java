package com.wanli.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    /**
     * 配置请求日志过滤器
     * 
     * @return CommonsRequestLoggingFilter
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        
        // 记录客户端信息
        loggingFilter.setIncludeClientInfo(true);
        
        // 记录查询字符串
        loggingFilter.setIncludeQueryString(true);
        
        // 记录请求负载（仅用于开发环境）
        loggingFilter.setIncludePayload(false);
        
        // 记录请求头
        loggingFilter.setIncludeHeaders(false);
        
        // 设置最大负载长度
        loggingFilter.setMaxPayloadLength(1000);
        
        // 设置日志前缀
        loggingFilter.setBeforeMessagePrefix("REQUEST: ");
        loggingFilter.setAfterMessagePrefix("RESPONSE: ");
        
        logger.info("Request logging filter configured");
        
        return loggingFilter;
    }

    // HTTP跟踪功能在Spring Boot 2.2+中已被移除
    // 如需HTTP跟踪功能，可以使用自定义实现或第三方库
}