package com.wanli.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 测试专用安全配置
 * 禁用所有安全检查以便进行异常处理测试
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    /**
     * 配置测试用安全过滤器链
     * 允许所有请求通过，不进行任何安全检查
     * 
     * @param http HTTP安全配置
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }
}