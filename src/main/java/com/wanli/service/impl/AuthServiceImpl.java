package com.wanli.service.impl;

import com.wanli.dto.*;
import com.wanli.entity.User;
import com.wanli.entity.UserRole;
import com.wanli.entity.UserStatus;
import com.wanli.exception.AuthenticationException;
import com.wanli.exception.UserAlreadyExistsException;
import com.wanli.exception.UserNotFoundException;
import com.wanli.repository.UserRepository;
import com.wanli.security.JwtTokenProvider;
import com.wanli.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * 认证服务实现类
 * 处理用户注册、登录、令牌刷新等认证相关业务逻辑
 * 
 * @author wanli
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationDto registrationDto) {
        log.info("开始用户注册流程，用户名: {}", registrationDto.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            log.warn("用户注册失败，用户名已存在: {}", registrationDto.getUsername());
            throw new UserAlreadyExistsException("用户名已存在: " + registrationDto.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            log.warn("用户注册失败，邮箱已存在: {}", registrationDto.getEmail());
            throw new UserAlreadyExistsException("邮箱已存在: " + registrationDto.getEmail());
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        
        User savedUser = userRepository.save(user);
        log.info("用户注册成功，用户ID: {}, 用户名: {}", savedUser.getId(), savedUser.getUsername());
        
        return convertToUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginDto) {
        log.info("开始用户登录流程，用户名: {}", loginDto.getUsername());
        
        // 查找用户
        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> {
                    log.warn("登录失败，用户不存在: {}", loginDto.getUsername());
                    return new AuthenticationException("用户名或密码错误");
                });
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("登录失败，用户状态异常: {}, 状态: {}", loginDto.getUsername(), user.getStatus());
            throw new AuthenticationException("账户已被禁用或锁定");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            // 增加登录失败次数
            user.incrementLoginAttempts();
            
            // 如果登录失败次数过多，锁定账户
            if (user.getLoginAttempts() >= 5) {
                user.setStatus(UserStatus.LOCKED);
                user.setLockedUntil(OffsetDateTime.now().plusHours(1)); // 锁定1小时
                log.warn("用户登录失败次数过多，账户已锁定: {}", loginDto.getUsername());
            }
            
            userRepository.save(user);
            log.warn("登录失败，密码错误: {}, 失败次数: {}", loginDto.getUsername(), user.getLoginAttempts());
            throw new AuthenticationException("用户名或密码错误");
        }
        
        // 检查账户是否被锁定
        if (user.isLocked()) {
            log.warn("登录失败，账户被锁定: {}, 锁定至: {}", loginDto.getUsername(), user.getLockedUntil());
            throw new AuthenticationException("账户已被锁定，请稍后再试");
        }
        
        // 登录成功，重置登录失败次数
        user.resetLoginAttempts();
        user.updateLastLoginTime();
        userRepository.save(user);
        
        // 生成JWT令牌
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        
        log.info("用户登录成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        
        // 构建登录响应
        LoginResponseDto.UserInfoDto userInfo = LoginResponseDto.UserInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .build();
        
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
                .user(userInfo)
                .build();
    }

    @Override
    public TokenRefreshResponseDto refreshToken(TokenRefreshRequestDto refreshDto) {
        log.info("开始令牌刷新流程");
        
        String refreshToken = refreshDto.getRefreshToken();
        
        // 验证刷新令牌
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("令牌刷新失败，刷新令牌无效");
            throw new AuthenticationException("刷新令牌无效或已过期");
        }
        
        // 从刷新令牌中获取用户名
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("令牌刷新失败，用户不存在: {}", username);
                    return new AuthenticationException("用户不存在");
                });
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("令牌刷新失败，用户状态异常: {}, 状态: {}", username, user.getStatus());
            throw new AuthenticationException("账户已被禁用或锁定");
        }
        
        // 生成新的访问令牌
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        
        log.info("令牌刷新成功，用户: {}", username);
        
        return TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
                .build();
    }

    /**
     * 将User实体转换为UserResponseDto
     */
    private UserResponseDto convertToUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt().atOffset(ZoneOffset.UTC))
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}