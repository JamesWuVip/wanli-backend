package com.wanli.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author wanli-team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查接口
     * 
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from Backend!");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 欢迎接口
     * 
     * @return 欢迎信息
     */
    @GetMapping("/welcome")
    public ResponseEntity<Map<String, String>> welcome() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "欢迎使用万里后端系统！");
        response.put("description", "这是一个基于Spring Boot的Java后端项目");
        
        return ResponseEntity.ok(response);
    }
}