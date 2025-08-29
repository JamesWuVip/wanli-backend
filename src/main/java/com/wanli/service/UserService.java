package com.wanli.service;

import com.wanli.dto.UserCreateDto;
import com.wanli.dto.UserRegistrationDto;
import com.wanli.dto.UserUpdateDto;
import com.wanli.entity.User;
import com.wanli.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户服务接口
 * 
 * @author wanli
 * @version 1.0.0
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    User register(UserRegistrationDto registrationDto);
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据ID查找用户
     */
    Optional<User> findById(UUID id);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 更新用户信息
     */
    User updateUser(UUID userId, UserUpdateDto updateDto);
    
    /**
     * 激活用户账户
     */
    void activateUser(UUID userId);
    
    /**
     * 停用用户账户
     */
    void deactivateUser(UUID userId);
    
    /**
     * 锁定用户账户
     */
    void lockUser(UUID userId, int lockDurationMinutes);
    
    /**
     * 解锁用户账户
     */
    void unlockUser(UUID userId);
    
    /**
     * 更新用户最后登录时间
     */
    void updateLastLoginTime(UUID userId);
    
    /**
     * 处理登录失败
     */
    void handleLoginFailure(String username);
    
    /**
     * 处理登录成功
     */
    void handleLoginSuccess(String username);
    
    /**
     * 批量解锁过期的用户
     */
    int unlockExpiredUsers();
    
    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * 分页查询用户
     */
    Page<User> findAll(Pageable pageable);
    
    /**
     * 删除用户
     */
    void deleteUser(UUID userId);
    
    /**
     * 修改用户密码
     */
    void changePassword(UUID userId, String oldPassword, String newPassword);
    
    /**
     * 重置用户密码
     */
    void resetPassword(UUID userId, String newPassword);
    
    /**
     * 创建用户
     */
    User createUser(UserCreateDto userCreateDto);
    
    /**
     * 根据ID获取用户
     */
    User getUserById(UUID id);
    
    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);
    
    /**
     * 获取所有用户
     */
    List<User> getAllUsers();
    
    /**
     * 根据角色获取用户列表
     */
    List<User> getUsersByRole(String role);
    
}