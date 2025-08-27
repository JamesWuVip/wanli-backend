package com.wanli.service;

import com.wanli.dto.LoginRequestDto;
import com.wanli.dto.LoginResponseDto;
import com.wanli.dto.UserRegistrationDto;
import com.wanli.dto.UserResponseDto;

/**
 * 认证服务接口
 * 
 * @author wanli
 * @version 1.0.0
 */
public interface AuthService {
    
    /**
     * 用户注册
     */
    UserResponseDto register(UserRegistrationDto registrationDto);
    
    /**
     * 用户登录
     */
    LoginResponseDto login(LoginRequestDto loginRequest);
    
    /**
     * 获取当前用户信息
     */
    UserResponseDto getCurrentUser(String username);
    
    /**
     * 用户登出
     */
    void logout(String token);
}