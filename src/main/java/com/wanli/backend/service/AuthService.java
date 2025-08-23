package com.wanli.backend.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanli.backend.entity.User;
import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.repository.UserRepository;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.ConfigUtil;
import com.wanli.backend.util.DatabaseUtil;
import com.wanli.backend.util.JwtUtil;
import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.PerformanceMonitor;
import com.wanli.backend.util.ServiceResponseUtil;
import com.wanli.backend.util.ServiceValidationUtil;

/** 用户认证服务类 处理用户注册、登录等认证相关业务逻辑 应用了缓存、日志记录、配置管理等最佳实践 */
@Service
@Transactional
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final CacheUtil cacheUtil;
  private final ConfigUtil configUtil;

  // 缓存键前缀
  private static final String USER_CACHE_PREFIX = "user:";
  private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts:";

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil,
      CacheUtil cacheUtil,
      ConfigUtil configUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.cacheUtil = cacheUtil;
    this.configUtil = configUtil;
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
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("USER_REGISTER")) {
      // 输入验证
      validateRegistrationInput(username, password, email);

      // 检查用户名和邮箱唯一性
      validateUserUniqueness(username, email);

      // 创建并保存用户
      User user = createUser(username, password, email, role);
      User savedUser = DatabaseUtil.saveSafely(userRepository, user, "User", null);

      // 生成JWT令牌
      String token =
          jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

      // 缓存用户信息
      cacheUser(savedUser);

      // 记录注册日志
      LogUtil.logBusinessOperation(
          "USER_REGISTER", savedUser.getId().toString(), "用户注册成功: " + username);
      LogUtil.logSecurity(
          "USER_REGISTRATION", savedUser.getId().toString(), null, "新用户注册: " + username);

      Map<String, Object> responseData =
          Map.of("token", token, "user", createUserResponse(savedUser));
      return ServiceResponseUtil.success("注册成功", responseData);

    } catch (BusinessException e) {
      LogUtil.logError("USER_REGISTER", "", e.getErrorCode(), e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      LogUtil.logError("USER_REGISTER", "", "REGISTER_ERROR", e.getMessage(), e);
      throw new BusinessException("REGISTER_FAILED", "注册失败，请稍后重试");
    }
  }

  /**
   * 用户登录
   *
   * @param username 用户名
   * @param password 密码
   * @return 登录结果
   */
  public Map<String, Object> login(String username, String password) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("USER_LOGIN")) {
      // 输入验证
      validateLoginInput(username, password);

      // 检查登录尝试次数
      checkLoginAttempts(username);

      // 查找用户（优先从缓存）
      User user = findUserByUsername(username);

      // 验证密码
      if (!passwordEncoder.matches(password, user.getPassword())) {
        recordFailedLoginAttempt(username);
        LogUtil.logSecurity("LOGIN_FAILED", user.getId().toString(), null, "密码错误: " + username);
        throw new BusinessException("LOGIN_FAILED", "用户名或密码错误");
      }

      // 清除失败尝试记录
      clearLoginAttempts(username);

      // 生成JWT令牌
      String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

      // 更新缓存
      cacheUser(user);

      // 记录登录日志
      LogUtil.logBusinessOperation("USER_LOGIN", user.getId().toString(), "用户登录成功: " + username);
      LogUtil.logSecurity("USER_LOGIN", user.getId().toString(), null, "用户登录: " + username);

      Map<String, Object> responseData = Map.of("token", token, "user", createUserResponse(user));
      return ServiceResponseUtil.success("登录成功", responseData);

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      LogUtil.logError("USER_LOGIN", "", "LOGIN_ERROR", e.getMessage(), e);
      throw new BusinessException("LOGIN_FAILED", "登录失败，请稍后重试");
    }
  }

  /**
   * 根据用户ID获取用户信息
   *
   * @param userId 用户ID
   * @return 用户信息
   */
  @Transactional(readOnly = true)
  public Optional<User> getUserById(UUID userId) {
    if (userId == null) {
      throw new IllegalArgumentException("用户ID不能为空");
    }

    // 优先从缓存获取
    String cacheKey = USER_CACHE_PREFIX + userId.toString();
    User cachedUser = cacheUtil.get(cacheKey, User.class);
    if (cachedUser != null) {
      LogUtil.logBusinessOperation("USER_GET_BY_ID", userId.toString(), "从缓存获取用户信息");
      return Optional.of(cachedUser);
    }

    // 从数据库查询
    Optional<User> userOptional =
        DatabaseUtil.findByIdSafely(userRepository, userId, "User", userId.toString());

    // 缓存查询结果
    userOptional.ifPresent(this::cacheUser);

    LogUtil.logBusinessOperation(
        "USER_GET_BY_ID", userId.toString(), userOptional.isPresent() ? "用户信息查询成功" : "用户不存在");

    return userOptional;
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

  // ==================== 私有辅助方法 ====================

  /** 验证注册输入 */
  private void validateRegistrationInput(String username, String password, String email) {
    ServiceValidationUtil.validateNotBlank(username, "用户名不能为空");
    ServiceValidationUtil.validateNotBlank(password, "密码不能为空");
    ServiceValidationUtil.validateNotBlank(email, "邮箱不能为空");
    ServiceValidationUtil.validateEmail(email);

    // 验证密码强度
    int minLength = configUtil.getPasswordMinLength();
    if (password.length() < minLength) {
      throw new BusinessException("WEAK_PASSWORD", "密码长度不能少于" + minLength + "位");
    }
  }

  /** 验证用户唯一性 */
  private void validateUserUniqueness(String username, String email) {
    if (DatabaseUtil.executeQuery(
        "CHECK_USERNAME", "User", null, () -> userRepository.existsByUsername(username))) {
      throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
    }

    if (DatabaseUtil.executeQuery(
        "CHECK_EMAIL", "User", null, () -> userRepository.existsByEmail(email))) {
      throw new BusinessException("EMAIL_EXISTS", "邮箱已被注册");
    }
  }

  /** 创建用户对象 */
  private User createUser(String username, String password, String email, String role) {
    User user = new User();
    user.setFranchiseId(UUID.randomUUID());
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setEmail(email);
    user.setRole(role != null && !role.isEmpty() ? role : "student");
    return user;
  }

  /** 验证登录输入 */
  private void validateLoginInput(String username, String password) {
    ServiceValidationUtil.validateNotBlank(username, "用户名不能为空");
    ServiceValidationUtil.validateNotBlank(password, "密码不能为空");
  }

  /** 检查登录尝试次数 */
  private void checkLoginAttempts(String username) {
    String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;
    Integer attempts = cacheUtil.get(attemptsKey, Integer.class);

    if (attempts != null && attempts >= configUtil.getMaxLoginAttempts()) {
      LogUtil.logSecurity("LOGIN_BLOCKED", null, null, "登录尝试次数过多，账户被锁定: " + username);
      throw new BusinessException("ACCOUNT_LOCKED", "登录尝试次数过多，账户已被锁定");
    }
  }

  /** 根据用户名查找用户 */
  private User findUserByUsername(String username) {
    Optional<User> userOptional =
        DatabaseUtil.executeQuery(
            "FIND_BY_USERNAME", "User", null, () -> userRepository.findByUsername(username));

    if (!userOptional.isPresent()) {
      LogUtil.logSecurity("LOGIN_FAILED", null, null, "用户不存在: " + username);
      throw new BusinessException("LOGIN_FAILED", "用户名或密码错误");
    }

    return userOptional.get();
  }

  /** 记录失败的登录尝试 */
  private void recordFailedLoginAttempt(String username) {
    String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;
    Integer attempts = cacheUtil.get(attemptsKey, Integer.class);
    attempts = (attempts == null) ? 1 : attempts + 1;

    // 缓存失败尝试次数，30分钟过期
    cacheUtil.put(attemptsKey, attempts, 30);

    LogUtil.logSecurity(
        "LOGIN_ATTEMPT_FAILED", null, null, "登录失败尝试: " + username + ", 次数: " + attempts);
  }

  /** 清除登录尝试记录 */
  private void clearLoginAttempts(String username) {
    String attemptsKey = LOGIN_ATTEMPTS_PREFIX + username;
    cacheUtil.remove(attemptsKey);
  }

  /** 缓存用户信息 */
  private void cacheUser(User user) {
    String cacheKey = USER_CACHE_PREFIX + user.getId().toString();
    cacheUtil.put(cacheKey, user, configUtil.getCacheDefaultExpireMinutes());
  }
}
