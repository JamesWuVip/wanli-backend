package com.wanli.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于系统健康状态检查和基础信息获取
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查接口
     * 
     * @return 系统健康状态信息
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "wanli-backend");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取系统信息
     * 
     * @return 系统基础信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "万里项目后端服务");
        response.put("version", "1.0.0");
        response.put("java.version", System.getProperty("java.version"));
        response.put("spring.profiles.active", System.getProperty("spring.profiles.active", "default"));
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

}