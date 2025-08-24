package com.wanli.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.service.AuthService;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthService authService;

  private Map<String, Object> successResponse;
  private Map<String, Object> userData;
  private String testToken;

  @BeforeEach
  void setUp() {
    testToken = "test.jwt.token";

    userData = new HashMap<>();
    userData.put("id", UUID.randomUUID());
    userData.put("username", "testuser");
    userData.put("email", "test@example.com");
    userData.put("role", "STUDENT");

    Map<String, Object> responseData = new HashMap<>();
    responseData.put("token", testToken);
    responseData.put("user", userData);

    successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("message", "操作成功");
    successResponse.put("data", responseData);
  }

  @Test
  void register_Success() throws Exception {
    // Given
    AuthController.RegisterRequest request = new AuthController.RegisterRequest();
    request.setUsername("testuser");
    request.setPassword("password123");
    request.setEmail("test@example.com");
    request.setRole("STUDENT");

    successResponse.put("message", "注册成功");
    when(authService.register(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(successResponse);

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("注册成功"))
        .andExpect(jsonPath("$.data.token").value(testToken))
        .andExpect(jsonPath("$.data.user.username").value("testuser"))
        .andExpect(jsonPath("$.data.user.email").value("test@example.com"));

    verify(authService).register("test@example.com", "password123", "testuser", "STUDENT");
  }

  @Test
  void register_InvalidUsername_BadRequest() throws Exception {
    // Given
    AuthController.RegisterRequest request = new AuthController.RegisterRequest();
    request.setUsername(""); // 空用户名
    request.setPassword("password123");
    request.setEmail("test@example.com");
    request.setRole("STUDENT");

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void register_InvalidPassword_BadRequest() throws Exception {
    // Given
    AuthController.RegisterRequest request = new AuthController.RegisterRequest();
    request.setUsername("testuser");
    request.setPassword("123"); // 密码太短
    request.setEmail("test@example.com");
    request.setRole("STUDENT");

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void register_InvalidEmail_BadRequest() throws Exception {
    // Given
    AuthController.RegisterRequest request = new AuthController.RegisterRequest();
    request.setUsername("testuser");
    request.setPassword("password123");
    request.setEmail("invalid-email"); // 无效邮箱格式
    request.setRole("STUDENT");

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void register_ServiceException_InternalServerError() throws Exception {
    // Given
    AuthController.RegisterRequest request = new AuthController.RegisterRequest();
    request.setUsername("testuser");
    request.setPassword("password123");
    request.setEmail("test@example.com");
    request.setRole("STUDENT");

    when(authService.register(anyString(), anyString(), anyString(), anyString()))
        .thenThrow(new BusinessException("REGISTER_FAILED", "注册失败"));

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());

    verify(authService).register("test@example.com", "password123", "testuser", "STUDENT");
  }

  @Test
  void login_Success() throws Exception {
    // Given
    AuthController.LoginRequest request = new AuthController.LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    successResponse.put("message", "登录成功");
    when(authService.login(anyString(), anyString())).thenReturn(successResponse);

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("登录成功"))
        .andExpect(jsonPath("$.data.token").value(testToken))
        .andExpect(jsonPath("$.data.user.email").value("test@example.com"));

    verify(authService).login("test@example.com", "password123");
  }

  @Test
  void login_InvalidEmail_BadRequest() throws Exception {
    // Given
    AuthController.LoginRequest request = new AuthController.LoginRequest();
    request.setEmail("invalid-email"); // 无效邮箱格式
    request.setPassword("password123");

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).login(anyString(), anyString());
  }

  @Test
  void login_EmptyPassword_BadRequest() throws Exception {
    // Given
    AuthController.LoginRequest request = new AuthController.LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword(""); // 空密码

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).login(anyString(), anyString());
  }

  @Test
  void login_ServiceException_InternalServerError() throws Exception {
    // Given
    AuthController.LoginRequest request = new AuthController.LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    when(authService.login(anyString(), anyString()))
        .thenThrow(new BusinessException("LOGIN_FAILED", "登录失败"));

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());

    verify(authService).login("test@example.com", "password123");
  }

  @Test
  void register_MissingRequestBody_BadRequest() throws Exception {
    // When & Then
    mockMvc
        .perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void login_MissingRequestBody_BadRequest() throws Exception {
    // When & Then
    mockMvc
        .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(authService, never()).login(anyString(), anyString());
  }

  @Test
  void register_LongUsername_BadRequest() throws Exception {
    // Given
    AuthController.RegisterRequest request = new AuthController.RegisterRequest();
    request.setUsername("a".repeat(51)); // 超过50个字符
    request.setPassword("password123");
    request.setEmail("test@example.com");
    request.setRole("STUDENT");

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void register_LongPassword_BadRequest() throws Exception {
    // Given
    AuthController.RegisterRequest request = new AuthController.RegisterRequest();
    request.setUsername("testuser");
    request.setPassword("a".repeat(101)); // 超过100个字符
    request.setEmail("test@example.com");
    request.setRole("STUDENT");

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(anyString(), anyString(), anyString(), anyString());
  }
}
