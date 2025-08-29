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
        User user = userService.register(registrationDto);
        return convertToUserResponseDto(user);
    }
    
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        // 查找用户
        User user = userService.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException(loginRequest.getUsername()));
        
        // 检查用户状态
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new DisabledException("用户账户已被停用");
        }
        
        // 检查账户是否被锁定
        if (user.isLocked()) {
            userService.handleLoginFailure(loginRequest.getUsername());
            throw new LockedException("用户账户已被锁定，请稍后再试");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            // 处理登录失败
            userService.handleLoginFailure(loginRequest.getUsername());
            throw new BadCredentialsException("用户名或密码错误");
        }
        
        try {
            // 执行Spring Security认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            // 生成JWT Token
            String token = jwtUtil.generateToken(authentication);
            
            // 处理登录成功
            userService.handleLoginSuccess(loginRequest.getUsername());
            
            // 构建响应
            LoginResponseDto.UserInfoDto userInfo = LoginResponseDto.UserInfoDto.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();
            
            return LoginResponseDto.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .user(userInfo)
                    .build();
            
        } catch (AuthenticationException e) {
            // 处理登录失败
            userService.handleLoginFailure(loginRequest.getUsername());
            log.warn("Login failed for user: {}, reason: {}", loginRequest.getUsername(), e.getMessage());
            throw new InvalidPasswordException("用户名或密码错误");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        
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
        // 将LocalDateTime转换为OffsetDateTime
        OffsetDateTime createdAt = user.getCreatedAt() != null ? 
            user.getCreatedAt().atOffset(OffsetDateTime.now().getOffset()) : null;
            
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