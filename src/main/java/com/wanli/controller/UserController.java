package com.wanli.controller;

import com.wanli.common.ApiResponse;
import com.wanli.dto.UserCreateDto;
import com.wanli.dto.UserUpdateDto;
import com.wanli.entity.User;
import com.wanli.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * 用户控制器
 * 
 * @author wanli
 * @version 1.0.0
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    
    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        User createdUser = userService.createUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("用户创建成功", createdUser));
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(Authentication authentication) {
        log.info("获取当前用户信息请求: {}", authentication.getName());
        User user = userService.getUserByUsername(authentication.getName());
        return ApiResponse.success(user);
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable UUID id) {
        log.info("获取用户请求: {}", id);
        User user = userService.getUserById(id);
        return ApiResponse.success(user);
    }
    
    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    public ApiResponse<User> getUserByUsername(@PathVariable String username) {
        log.info("根据用户名获取用户请求: {}", username);
        User user = userService.getUserByUsername(username);
        return ApiResponse.success(user);
    }
    
    /**
     * 获取所有用户或按角色查询用户
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #role == 'STUDENT') or (hasRole('TEACHER') and #role == 'TEACHER')")
    public ApiResponse<List<User>> getAllUsers(@RequestParam(required = false) String role) {
        log.info("获取用户请求，角色过滤: {}", role);
        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userService.getUsersByRole(role);
        } else {
            users = userService.getAllUsers();
        }
        return ApiResponse.success(users);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> updateUser(@PathVariable UUID id, @RequestBody UserUpdateDto updateDto) {
        log.info("更新用户请求: {}", id);
        User updatedUser = userService.updateUser(id, updateDto);
        return ApiResponse.success("用户更新成功", updatedUser);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable UUID id) {
        log.info("删除用户请求: {}", id);
        userService.deleteUser(id);
        return ApiResponse.success("用户删除成功");
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check/username/{username}")
    public ApiResponse<Boolean> checkUsername(@PathVariable String username) {
        log.info("检查用户名请求: {}", username);
        boolean exists = userService.existsByUsername(username);
        return ApiResponse.success(exists);
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check/email/{email}")
    public ApiResponse<Boolean> checkEmail(@PathVariable String email) {
        log.info("检查邮箱请求: {}", email);
        boolean exists = userService.existsByEmail(email);
        return ApiResponse.success(exists);
    }
    
}