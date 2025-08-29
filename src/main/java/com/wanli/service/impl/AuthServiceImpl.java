package com.wanli.service.impl;

import com.wanli.dto.request.LoginRequestDto;
import com.wanli.dto.request.RegisterRequestDto;
import com.wanli.dto.response.LoginResponseDto;
import com.wanli.dto.response.UserResponseDto;
import com.wanli.entity.User;
import com.wanli.enums.UserRole;
import com.wanli.exception.AuthenticationException;
import com.wanli.exception.UserAlreadyExistsException;
import com.wanli.repository.UserRepository;
import com.wanli.service.AuthService;
import com.wanli.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException as SpringAuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 认证服务实现类
 * 处理用户注册、登录等认证相关业务逻辑
 *
 * @author wujames
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 用户响应信息
     * @throws UserAlreadyExistsException 用户已存在异常
     */
    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto registerRequest) {
        log.info("开始注册用户: {}", registerRequest.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("用户名已存在: {}", registerRequest.getUsername());
            throw new UserAlreadyExistsException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("邮箱已存在: {}", registerRequest.getEmail());
            throw new UserAlreadyExistsException("邮箱已存在");
        }
        
        // 创建新用户
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .role(UserRole.STUDENT) // 默认角色为学生
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());
        
        return convertToUserResponseDto(savedUser);
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应信息
     * @throws AuthenticationException 认证异常
     */
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("用户尝试登录: {}", loginRequest.getUsername());
        
        try {
            // 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            // 获取用户信息
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                log.error("认证成功但用户不存在: {}", username);
                throw new AuthenticationException("用户不存在");
            }
            
            User user = userOptional.get();
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            
            log.info("用户登录成功: {}", user.getUsername());
            
            return LoginResponseDto.builder()
                    .token(token)
                    .user(convertToUserResponseDto(user))
                    .build();
            
        } catch (SpringAuthenticationException e) {
            log.warn("用户登录失败: {}, 原因: {}", loginRequest.getUsername(), e.getMessage());
            throw new AuthenticationException("用户名或密码错误");
        }
    }

    /**
     * 将User实体转换为UserResponseDto
     *
     * @param user 用户实体
     * @return 用户响应DTO
     */
    private UserResponseDto convertToUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}