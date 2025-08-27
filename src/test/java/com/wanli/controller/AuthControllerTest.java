package com.wanli.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.dto.LoginRequestDto;
import com.wanli.dto.UserRegistrationDto;
import com.wanli.entity.UserRole;
import com.wanli.service.AuthService;
import com.wanli.util.JwtUtil;
import com.wanli.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器测试
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    @Test
    void testRegisterUser_Success() throws Exception {
        UserRegistrationDto registrationDto = TestDataFactory.createUserRegistrationDto();
        
        when(authService.registerUser(any(UserRegistrationDto.class)))
            .thenReturn(TestDataFactory.createDefaultUser());
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("用户注册成功"))
                .andExpect(jsonPath("$.data.username").exists());
    }
    
    @Test
    void testRegisterUser_DuplicateUsername() throws Exception {
        UserRegistrationDto registrationDto = TestDataFactory.createUserRegistrationDto();
        
        when(authService.registerUser(any(UserRegistrationDto.class)))
            .thenThrow(new RuntimeException("用户名已存在"));
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }
    
    @Test
    void testRegisterUser_InvalidRequest() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        // 不设置必需的字段，使其无效
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLogin_Success() throws Exception {
        LoginRequestDto loginDto = TestDataFactory.createLoginRequestDto();
        String mockToken = "mock.jwt.token";
        
        when(authService.authenticateUser(any(LoginRequestDto.class)))
            .thenReturn(TestDataFactory.createDefaultUser());
        when(jwtUtil.generateAccessToken(any(), any()))
            .thenReturn(mockToken);
        when(jwtUtil.generateRefreshToken(any()))
            .thenReturn("mock.refresh.token");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.accessToken").value(mockToken))
                .andExpect(jsonPath("$.data.refreshToken").value("mock.refresh.token"));
    }
    
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequestDto loginDto = TestDataFactory.createLoginRequestDto();
        
        when(authService.authenticateUser(any(LoginRequestDto.class)))
            .thenThrow(new RuntimeException("用户名或密码错误"));
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }
    
    @Test
    void testLogin_InvalidRequest() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto();
        // 不设置用户名和密码，使其无效
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());
    }
}