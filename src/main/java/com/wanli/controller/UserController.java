package com.wanli.controller;

import com.wanli.common.ApiResponse;
import com.wanli.dto.UserCreateDto;
import com.wanli.dto.UserUpdateDto;
import com.wanli.entity.User;
import com.wanli.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 用户管理控制器
 * 
 * @author wanli
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    /**
     * 创建用户（管理员功能）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        User user = userService.createUser(userCreateDto);
        return ResponseEntity.ok(ApiResponse.success("用户创建成功", user));
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户详细信息")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", user));
    }
    
    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名获取用户详细信息")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", user));
    }
    
    /**
     * 获取所有用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有用户", description = "获取系统中所有用户列表")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
    }
    
    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页获取用户", description = "分页获取用户列表")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersPage(Pageable pageable) {
        Page<User> users = userService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateDto updateDto) {
        User user = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", user));
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户", description = "删除指定用户")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
    }
    
    /**
     * 激活用户
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "激活用户", description = "激活指定用户账户")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户激活成功"));
    }
    
    /**
     * 停用用户
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "停用用户", description = "停用指定用户账户")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户停用成功"));
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username/{username}")
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("检查完成", exists));
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email/{email}")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已存在")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("检查完成", exists));
    }
}