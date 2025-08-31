package com.wanli.controller;

import com.wanli.dto.ApiResponse;
import com.wanli.dto.LoginDTO;
import com.wanli.dto.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * 登录控制器
 * 提供用户登录相关的API接口
 * 
 * @author wanli-team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class LoginController {
    
    /**
     * 用户登录接口
     * 
     * @param loginDTO 登录请求数据
     * @return 登录响应结果
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        
        // 简单的模拟登录逻辑（实际项目中应该验证用户名密码）
        // 这里只是为了测试CI/CD流程
        if ("admin".equals(loginDTO.getUsername()) && "123456".equals(loginDTO.getPassword())) {
            // 模拟登录成功
            LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .userId(UUID.randomUUID().toString())
                .username(loginDTO.getUsername())
                .token("mock-token-" + System.currentTimeMillis())
                .loginTime(System.currentTimeMillis())
                .build();
            
            ApiResponse<LoginResponseDTO> response = ApiResponse.success(responseDTO);
            return ResponseEntity.ok(response);
        } else {
            // 模拟登录失败
            ApiResponse<LoginResponseDTO> response = ApiResponse.error(2001, "用户名或密码错误");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取登录状态接口
     * 
     * @return 登录状态
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> getLoginStatus() {
        ApiResponse<String> response = ApiResponse.success("登录API服务正常运行");
        return ResponseEntity.ok(response);
    }
}