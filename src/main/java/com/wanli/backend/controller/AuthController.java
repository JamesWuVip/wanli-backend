package com.wanli.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wanli.backend.service.AuthService;
import com.wanli.backend.util.ControllerLogUtil;
import com.wanli.backend.util.ControllerResponseUtil;
import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.ServiceValidationUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

  private static final String SUCCESS_KEY = "success";
  private static final String MESSAGE_KEY = "message";

  @Autowired private AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
    LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("AuthController.register");

    try {
      // 输入验证
      ServiceValidationUtil.validateNotBlank(request.getEmail(), "邮箱");
      ServiceValidationUtil.validateNotBlank(request.getPassword(), "密码");
      ServiceValidationUtil.validateNotBlank(request.getUsername(), "用户名");

      // 调用服务层进行注册
      Map<String, Object> result =
          authService.register(
              request.getEmail(), request.getPassword(), request.getUsername(), request.getRole());

      // 记录用户注册日志
      ControllerLogUtil.logUserRegistration(
          request.getEmail(), request.getUsername(), request.getRole());

      return ControllerResponseUtil.fromServiceResult(result);
    } catch (IllegalArgumentException e) {
      return ControllerResponseUtil.createBadRequestResponse(e.getMessage());
    } catch (Exception e) {
      ControllerLogUtil.logOperationError("用户注册失败", e, "email", request.getEmail());
      return ControllerResponseUtil.createInternalServerErrorResponse("注册失败，请稍后重试");
    } finally {
      monitor.end();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
    LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("AuthController.login");

    try {
      // 输入验证
      ServiceValidationUtil.validateNotBlank(request.getEmail(), "邮箱");
      ServiceValidationUtil.validateNotBlank(request.getPassword(), "密码");

      // 调用服务层进行登录
      Map<String, Object> result = authService.login(request.getEmail(), request.getPassword());

      // 记录用户登录日志
      ControllerLogUtil.logUserLogin(request.getEmail(), "password");

      return ControllerResponseUtil.fromServiceResult(result);
    } catch (IllegalArgumentException e) {
      return ControllerResponseUtil.createBadRequestResponse(e.getMessage());
    } catch (Exception e) {
      ControllerLogUtil.logOperationError("用户登录失败", e, "email", request.getEmail());
      return ControllerResponseUtil.createInternalServerErrorResponse("登录失败，请稍后重试");
    } finally {
      monitor.end();
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
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;

    // Getters and Setters
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }
}
