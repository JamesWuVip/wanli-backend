package com.wanli.controller;

import com.wanli.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
 * @author wanli
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api")
@Tag(name = "系统监控", description = "系统健康检查和监控接口")
public class HealthController {
    
    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    
    @Value("${spring.application.name:wanli-backend}")
    private String applicationName;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统运行状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", applicationName);
        healthInfo.put("profile", activeProfile);
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("version", "1.0.0");
        
        return ResponseEntity.ok(ApiResponse.success("系统运行正常", healthInfo));
    }
    
    /**
     * 系统信息接口
     */
    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "获取系统基本信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("application", applicationName);
        systemInfo.put("profile", activeProfile);
        systemInfo.put("version", "1.0.0");
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success("获取系统信息成功", systemInfo));
    }
}