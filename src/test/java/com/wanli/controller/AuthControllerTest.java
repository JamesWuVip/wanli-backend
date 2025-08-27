package com.wanli.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.dto.LoginRequestDto;
import com.wanli.dto.LoginResponseDto;
import com.wanli.dto.RegisterRequestDto;
import com.wanli.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegister() throws Exception {
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@example.com");

        when(authService.register(any(RegisterRequestDto.class))).thenReturn("注册成功");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    @Test
    void testLogin() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        LoginResponseDto loginResponse = new LoginResponseDto();
        loginResponse.setToken("jwt-token");
        LoginResponseDto.UserInfoDto userInfo = new LoginResponseDto.UserInfoDto();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        loginResponse.setUserInfo(userInfo);

        when(authService.login(any(LoginRequestDto.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.userInfo.username").value("testuser"));
    }
}