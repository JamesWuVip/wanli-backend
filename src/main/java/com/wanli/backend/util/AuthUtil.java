package com.wanli.backend.util;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 认证工具类 统一处理JWT令牌验证和用户认证相关操作 */
@Component
public class AuthUtil {

  @Autowired private JwtUtil jwtUtil;

  private static final String BEARER_PREFIX = "Bearer ";

  /**
   * 从Authorization头中提取JWT令牌
   *
   * @param authHeader Authorization头
   * @return JWT令牌，如果格式不正确返回null
   */
  public static String extractTokenFromHeader(String authHeader) {
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return null;
    }
    return authHeader.substring(BEARER_PREFIX.length());
  }

  /**
   * 验证JWT令牌并获取用户ID
   *
   * @param authHeader Authorization头
   * @return 用户ID
   * @throws IllegalArgumentException 当令牌无效时
   */
  public UUID validateTokenAndGetUserId(String authHeader) {
    String token = extractTokenFromHeader(authHeader);
    if (token == null) {
      throw new IllegalArgumentException("认证失败：无效的token格式");
    }

    try {
      return jwtUtil.getUserIdFromToken(token);
    } catch (Exception e) {
      throw new IllegalArgumentException("认证失败：无效的token");
    }
  }

  /**
   * 验证JWT令牌并获取用户ID（不抛出异常版本）
   *
   * @param authHeader Authorization头
   * @return 用户ID，验证失败返回null
   */
  public UUID validateTokenAndGetUserIdSafely(String authHeader) {
    try {
      return validateTokenAndGetUserId(authHeader);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 检查Authorization头是否有效
   *
   * @param authHeader Authorization头
   * @return 是否有效
   */
  public static boolean isValidAuthHeader(String authHeader) {
    return authHeader != null
        && authHeader.startsWith(BEARER_PREFIX)
        && authHeader.length() > BEARER_PREFIX.length();
  }

  /**
   * 验证令牌格式
   *
   * @param token JWT令牌
   * @return 是否格式正确
   */
  public static boolean isValidTokenFormat(String token) {
    return token != null && !token.trim().isEmpty();
  }

  /**
   * 创建Bearer格式的Authorization头
   *
   * @param token JWT令牌
   * @return Authorization头值
   */
  public static String createBearerHeader(String token) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token不能为空");
    }
    return BEARER_PREFIX + token;
  }

  /**
   * 验证用户权限
   *
   * @param userId 用户ID
   * @param requiredRole 需要的角色
   * @return 是否有权限
   */
  public static boolean hasPermission(UUID userId, String requiredRole) {
    if (userId == null || requiredRole == null) {
      return false;
    }

    try {
      // 这里应该调用用户服务来检查权限
      // 暂时返回true，实际实现需要根据业务逻辑
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 验证用户是否为管理员
   *
   * @param userId 用户ID
   * @return 是否为管理员
   */
  public static boolean isAdmin(UUID userId) {
    return hasPermission(userId, "ADMIN");
  }

  /**
   * 验证用户是否为教师
   *
   * @param userId 用户ID
   * @return 是否为教师
   */
  public static boolean isTeacher(UUID userId) {
    return hasPermission(userId, "TEACHER");
  }
}
