package com.wanli.backend.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanli.backend.entity.User;
import com.wanli.backend.repository.UserRepository;
import com.wanli.backend.util.JwtUtil;

/** 用户认证服务类 处理用户注册、登录等认证相关业务逻辑 */
@Service
@Transactional
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(
      UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  /**
   * 用户注册
   *
   * @param username 用户名
   * @param password 密码
   * @param email 邮箱
   * @param role 用户角色
   * @return 注册结果
   */
  public Map<String, Object> register(String username, String password, String email, String role) {
    Map<String, Object> result = new HashMap<>();

    // 检查用户名是否已存在
    if (userRepository.existsByUsername(username)) {
      result.put("success", false);
      result.put("message", "用户名已存在");
      return result;
    }

    // 检查邮箱是否已存在
    if (userRepository.existsByEmail(email)) {
      result.put("success", false);
      result.put("message", "邮箱已被注册");
      return result;
    }

    try {
      // 创建新用户
      User user = new User();
      user.setFranchiseId(UUID.randomUUID()); // 临时设置，实际应根据业务逻辑确定
      user.setUsername(username);
      user.setPassword(passwordEncoder.encode(password));
      user.setEmail(email);
      user.setRole(role != null && !role.isEmpty() ? role : "student"); // 使用传入的角色，默认为学员

      User savedUser = userRepository.save(user);

      // 生成JWT令牌
      String token =
          jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

      result.put("success", true);
      result.put("message", "注册成功");
      result.put("token", token);
      result.put("user", createUserResponse(savedUser));

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "注册失败：" + e.getMessage());
    }

    return result;
  }

  /**
   * 用户登录
   *
   * @param username 用户名
   * @param password 密码
   * @return 登录结果
   */
  public Map<String, Object> login(String username, String password) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 查找用户
      Optional<User> userOptional = userRepository.findByUsername(username);

      if (!userOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "用户名或密码错误");
        return result;
      }

      User user = userOptional.get();

      // 验证密码
      if (!passwordEncoder.matches(password, user.getPassword())) {
        result.put("success", false);
        result.put("message", "用户名或密码错误");
        return result;
      }

      // 生成JWT令牌
      String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

      result.put("success", true);
      result.put("message", "登录成功");
      result.put("token", token);
      result.put("user", createUserResponse(user));

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "登录失败：" + e.getMessage());
    }

    return result;
  }

  /**
   * 根据用户ID获取用户信息
   *
   * @param userId 用户ID
   * @return 用户信息
   */
  @Transactional(readOnly = true)
  public Optional<User> getUserById(UUID userId) {
    return userRepository.findByIdAndNotDeleted(userId);
  }

  /**
   * 创建用户响应对象（不包含敏感信息）
   *
   * @param user 用户实体
   * @return 用户响应对象
   */
  private Map<String, Object> createUserResponse(User user) {
    Map<String, Object> userResponse = new HashMap<>();
    userResponse.put("id", user.getId());
    userResponse.put("username", user.getUsername());
    userResponse.put("email", user.getEmail());
    userResponse.put("role", user.getRole());
    userResponse.put("created_at", user.getCreatedAt());
    userResponse.put("updated_at", user.getUpdatedAt());
    return userResponse;
  }
}
