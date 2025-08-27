package com.wanli.service.impl;

import com.wanli.dto.UserCreateDto;
import com.wanli.dto.UserRegistrationDto;
import com.wanli.dto.UserUpdateDto;
import com.wanli.entity.User;
import com.wanli.entity.UserStatus;
import com.wanli.exception.user.DuplicateEmailException;
import com.wanli.exception.user.DuplicateUsernameException;
import com.wanli.exception.user.InvalidPasswordException;
import com.wanli.exception.user.UserNotFoundException;
import com.wanli.repository.UserRepository;
import com.wanli.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户服务实现类
 * 
 * @author wanli
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    
    @Override
    public User register(UserRegistrationDto registrationDto) {
        try {
            // 检查用户名是否已存在
            if (existsByUsername(registrationDto.getUsername())) {
                throw new DuplicateUsernameException(registrationDto.getUsername());
            }
            
            // 检查邮箱是否已存在
            if (existsByEmail(registrationDto.getEmail())) {
                throw new DuplicateEmailException(registrationDto.getEmail());
            }
            
            // 创建新用户
            User user = User.builder()
                    .username(registrationDto.getUsername())
                    .passwordHash(passwordEncoder.encode(registrationDto.getPassword()))
                    .email(registrationDto.getEmail())
                    .fullName(registrationDto.getFullName())
                    .role(registrationDto.getRole())
                    .status(UserStatus.ACTIVE)
                    .loginAttempts(0)
                    .build();
            
            User savedUser = userRepository.save(user);
            log.info("User registered successfully: {}", savedUser.getUsername());
            return savedUser;
            
        } catch (DataAccessException e) {
            log.error("Database error during user registration: {}", e.getMessage(), e);
            throw new RuntimeException("用户注册失败", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public User updateUser(UUID userId, UserUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        
        // 检查邮箱是否被其他用户使用
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (existsByEmail(updateDto.getEmail())) {
                throw new DuplicateEmailException(updateDto.getEmail());
            }
            user.setEmail(updateDto.getEmail());
        }
        
        if (updateDto.getFullName() != null) {
            user.setFullName(updateDto.getFullName());
        }
        
        if (updateDto.getRole() != null) {
            user.setRole(updateDto.getRole());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getUsername());
        return updatedUser;
    }
    
    @Override
    public void activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        
        user.activate();
        userRepository.save(user);
        log.info("User activated: {}", user.getUsername());
    }
    
    @Override
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
    }
    
    @Override
    public void lockUser(UUID userId, int lockDurationMinutes) {
        OffsetDateTime lockUntil = OffsetDateTime.now().plusMinutes(lockDurationMinutes);
        userRepository.lockUser(userId, lockUntil);
        log.info("User locked until {}: userId={}", lockUntil, userId);
    }
    
    @Override
    public void unlockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        
        user.resetLoginAttempts();
        user.setLockedUntil(null);
        userRepository.save(user);
        log.info("User unlocked: {}", user.getUsername());
    }
    
    @Override
    public void updateLastLoginTime(UUID userId) {
        userRepository.updateLastLoginTime(userId, OffsetDateTime.now());
    }
    
    @Override
    public void handleLoginFailure(String username) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.incrementLoginAttempts();
            
            if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                user.setLockedUntil(OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                log.warn("User account locked due to too many failed attempts: {}", username);
            }
            
            userRepository.save(user);
        }
    }
    
    @Override
    public void handleLoginSuccess(String username) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.resetLoginAttempts();
            user.updateLastLoginTime();
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
    
    @Override
    public int unlockExpiredUsers() {
        return userRepository.unlockExpiredUsers(OffsetDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    @Override
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId.toString());
        }
        
        userRepository.deleteById(userId);
        log.info("User deleted: userId={}", userId);
    }
    
    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("旧密码不正确");
        }
        
        // 设置新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }
    
    @Override
    public void resetPassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.resetLoginAttempts();
        user.setLockedUntil(null);
        userRepository.save(user);
        log.info("Password reset for user: {}", user.getUsername());
    }
    
    @Override
    public User createUser(UserCreateDto userCreateDto) {
        // 检查用户名是否已存在
        if (existsByUsername(userCreateDto.getUsername())) {
            throw new DuplicateUsernameException(userCreateDto.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (existsByEmail(userCreateDto.getEmail())) {
            throw new DuplicateEmailException(userCreateDto.getEmail());
        }
        
        // 创建User实体
        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setFullName(userCreateDto.getFullName());
        user.setRole(userCreateDto.getRole());
        user.setPasswordHash(passwordEncoder.encode(userCreateDto.getPassword()));
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}