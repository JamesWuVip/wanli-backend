package com.wanli.service.impl;

import com.wanli.config.JwtUtil;
import com.wanli.dto.LoginRequestDto;
import com.wanli.dto.LoginResponseDto;
import com.wanli.dto.UserRegistrationDto;
import com.wanli.dto.UserResponseDto;
import com.wanli.entity.User;
import com.wanli.entity.UserStatus;
import com.wanli.exception.user.InvalidPasswordException;
import com.wanli.exception.user.UserNotFoundException;
import com.wanli.service.AuthService;
import com.wanli.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 认证服务实现类
 * 处理用户注册、登录、登出等认证相关操作
 * 
 * @author wanli
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserResponseDto register(UserRegistrationDto registrationDto) {
        // 实现用户注册逻辑
        return null;
    }
    
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        try {
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // 认证成功，生成JWT Token
            String accessToken = jwtUtil.generateToken(authentication);
            
            // 获取用户信息
            User user = userService.findByUsername(loginRequest.getUsername());
            
            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId(), OffsetDateTime.now());
            
            // 构建用户信息DTO
            LoginResponseDto.UserInfoDto userInfo = LoginResponseDto.UserInfoDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .lastLoginAt(user.getLastLoginAt())
                .build();
            
            // 构建登录响应
            LoginResponseDto response = LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getJwtExpirationInMs() / 1000) // 转换为秒
                .user(userInfo)
                .build();
            
            log.info("User {} logged in successfully", loginRequest.getUsername());
            return response;
            
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            throw new InvalidPasswordException();
        } catch (DisabledException e) {
            log.warn("Disabled user attempted to login: {}", loginRequest.getUsername());
            throw new UserNotFoundException("用户账户已被禁用");
        } catch (LockedException e) {
            log.warn("Locked user attempted to login: {}", loginRequest.getUsername());
            throw new UserNotFoundException("用户账户已被锁定");
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", loginRequest.getUsername());
            throw new InvalidPasswordException();
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", loginRequest.getUsername(), e);
            throw new RuntimeException("登录过程中发生错误", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String username) {
        User user = userService.findByUsername(username);
        return convertToUserResponseDto(user);
    }
    
    @Override
    public void logout(String token) {
        // TODO: 实现Token黑名单机制
        // 可以将Token加入Redis黑名单，在JWT过滤器中检查
        log.info("User logged out, token will be invalidated");
    }
    
    /**
     * 转换User实体为UserResponseDto
     */
    private UserResponseDto convertToUserResponseDto(User user) {
        // 直接使用OffsetDateTime，无需转换
        OffsetDateTime createdAt = user.getCreatedAt();
            
        return UserResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(createdAt)
                .lastLoginAt(user.getLastLoginAt())
                .isActive(user.getStatus() == UserStatus.ACTIVE)
                .build();
    }
}