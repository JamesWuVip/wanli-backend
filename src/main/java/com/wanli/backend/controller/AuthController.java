package com.wanli.backend.controller;

import com.wanli.backend.dto.LoginRequest;
import com.wanli.backend.dto.RegisterRequest;
import com.wanli.backend.service.AuthService;
import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.PerformanceMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            LogUtil.logBusinessOperation("USER_REGISTER", "username=" + request.getUsername() + ", email=" + request.getEmail());
            
            Map<String, Object> response = authService.register(request.getUsername(), request.getEmail(), request.getPassword(), request.getRole());
            
            PerformanceMonitor.recordApiCall("/api/auth/register", System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PerformanceMonitor.recordApiCall("/api/auth/register", System.currentTimeMillis() - startTime);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            LogUtil.logBusinessOperation("USER_LOGIN", "username=" + request.getUsername());
            
            Map<String, Object> response = authService.login(request.getUsername(), request.getPassword());
            
            PerformanceMonitor.recordApiCall("/api/auth/login", System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PerformanceMonitor.recordApiCall("/api/auth/login", System.currentTimeMillis() - startTime);
            throw e;
        }
    }
}
