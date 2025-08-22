package com.wanli.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wanli.backend.service.AuthService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 认证控制器 处理用户注册和登录相关的HTTP请求 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

  // 常量定义
  private static final String SUCCESS_KEY = "success";
  private static final String MESSAGE_KEY = "message";

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /** 用户注册 POST /api/auth/register */
  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
    try {
      Map<String, Object> result =
          authService.register(
              request.getUsername(), request.getPassword(), request.getEmail(), request.getRole());

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
      } else {
        return ResponseEntity.badRequest().body(result);
      }

    } catch (Exception e) {
      return createErrorResponse("注册失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** 用户登录 POST /api/auth/login */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
    try {
      Map<String, Object> result = authService.login(request.getUsername(), request.getPassword());

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        return ResponseEntity.ok(result);
      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
      }

    } catch (Exception e) {
      return createErrorResponse("登录失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** 用户注册请求体 */
  public static class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String role;

    // Getters and Setters
    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }
  }

  /** 用户登录请求体 */
  public static class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    // Getters and Setters
    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  /**
   * 创建错误响应
   *
   * @param message 错误消息
   * @param status HTTP状态码
   * @return 错误响应
   */
  private ResponseEntity<Map<String, Object>> createErrorResponse(
      String message, HttpStatus status) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put(SUCCESS_KEY, false);
    errorResponse.put(MESSAGE_KEY, message);
    return ResponseEntity.status(status).body(errorResponse);
  }
}
