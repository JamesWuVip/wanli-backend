package com.wanli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 * 用于配置应用的安全策略
 * 
 * @author wanli-team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 
     * @param http HttpSecurity对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（对于REST API通常不需要）
            .csrf(csrf -> csrf.disable())
            
            // 配置会话管理为无状态（适用于JWT认证）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置授权规则
            .authorizeHttpRequests(authz -> authz
                // 允许健康检查端点无需认证
                .requestMatchers("/api/health", "/health").permitAll()
                // 允许actuator端点无需认证（开发环境）
                .requestMatchers("/api/actuator/**", "/actuator/**").permitAll()
                // 允许认证相关端点无需认证
                .requestMatchers("/api/auth/**").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
}
