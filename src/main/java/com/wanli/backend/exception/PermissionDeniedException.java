package com.wanli.backend.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

/** 权限拒绝异常 当用户没有足够权限执行某个操作时抛出此异常 */
public class PermissionDeniedException extends BusinessException {

  private final UUID userId;
  private final String action;
  private final String resource;

  /**
   * 构造函数
   *
   * @param message 异常消息
   */
  public PermissionDeniedException(String message) {
    super(message, "PERMISSION_DENIED", HttpStatus.FORBIDDEN);
    this.userId = null;
    this.action = null;
    this.resource = null;
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param userId 用户ID
   * @param action 尝试执行的操作
   */
  public PermissionDeniedException(String message, UUID userId, String action) {
    super(message, "PERMISSION_DENIED", HttpStatus.FORBIDDEN);
    this.userId = userId;
    this.action = action;
    this.resource = null;
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param userId 用户ID
   * @param action 尝试执行的操作
   * @param resource 相关资源
   */
  public PermissionDeniedException(String message, UUID userId, String action, String resource) {
    super(message, "PERMISSION_DENIED", HttpStatus.FORBIDDEN);
    this.userId = userId;
    this.action = action;
    this.resource = resource;
  }

  /**
   * 获取用户ID
   *
   * @return 用户ID
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   * 获取尝试执行的操作
   *
   * @return 操作名称
   */
  public String getAction() {
    return action;
  }

  /**
   * 获取相关资源
   *
   * @return 资源名称
   */
  public String getResource() {
    return resource;
  }

  /**
   * 创建课程操作权限异常
   *
   * @param userId 用户ID
   * @param action 操作类型
   * @return PermissionDeniedException实例
   */
  public static PermissionDeniedException forCourseOperation(UUID userId, String action) {
    String message = String.format("用户 %s 没有权限执行课程操作: %s", userId, action);
    return new PermissionDeniedException(message, userId, action, "course");
  }

  /**
   * 创建课时操作权限异常
   *
   * @param userId 用户ID
   * @param action 操作类型
   * @return PermissionDeniedException实例
   */
  public static PermissionDeniedException forLessonOperation(UUID userId, String action) {
    String message = String.format("用户 %s 没有权限执行课时操作: %s", userId, action);
    return new PermissionDeniedException(message, userId, action, "lesson");
  }

  /**
   * 创建角色不足异常
   *
   * @param userId 用户ID
   * @param requiredRole 所需角色
   * @param currentRole 当前角色
   * @return PermissionDeniedException实例
   */
  public static PermissionDeniedException forInsufficientRole(
      UUID userId, String requiredRole, String currentRole) {
    String message =
        String.format("用户 %s 角色权限不足，需要 %s 角色，当前角色: %s", userId, requiredRole, currentRole);
    return new PermissionDeniedException(message, userId, "role_check");
  }

  /**
   * 创建资源访问权限异常
   *
   * @param userId 用户ID
   * @param resourceType 资源类型
   * @param resourceId 资源ID
   * @return PermissionDeniedException实例
   */
  public static PermissionDeniedException forResourceAccess(
      UUID userId, String resourceType, String resourceId) {
    String message = String.format("用户 %s 没有权限访问 %s: %s", userId, resourceType, resourceId);
    return new PermissionDeniedException(message, userId, "access", resourceType);
  }
}
