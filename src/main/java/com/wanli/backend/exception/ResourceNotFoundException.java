package com.wanli.backend.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

/** 资源未找到异常 当请求的资源不存在时抛出此异常 */
public class ResourceNotFoundException extends BusinessException {

  private final String resourceType;
  private final Object resourceId;

  /**
   * 构造函数
   *
   * @param message 异常消息
   */
  public ResourceNotFoundException(String message) {
    super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    this.resourceType = null;
    this.resourceId = null;
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param resourceType 资源类型
   * @param resourceId 资源ID
   */
  public ResourceNotFoundException(String message, String resourceType, Object resourceId) {
    super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  /**
   * 获取资源类型
   *
   * @return 资源类型
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * 获取资源ID
   *
   * @return 资源ID
   */
  public Object getResourceId() {
    return resourceId;
  }

  /**
   * 创建用户未找到异常
   *
   * @param userId 用户ID
   * @return ResourceNotFoundException实例
   */
  public static ResourceNotFoundException forUser(UUID userId) {
    String message = String.format("用户不存在: %s", userId);
    return new ResourceNotFoundException(message, "user", userId);
  }

  /**
   * 创建用户未找到异常（通过邮箱）
   *
   * @param email 邮箱
   * @return ResourceNotFoundException实例
   */
  public static ResourceNotFoundException forUserByEmail(String email) {
    String message = String.format("用户不存在: %s", email);
    return new ResourceNotFoundException(message, "user", email);
  }

  /**
   * 创建课程未找到异常
   *
   * @param courseId 课程ID
   * @return ResourceNotFoundException实例
   */
  public static ResourceNotFoundException forCourse(UUID courseId) {
    String message = String.format("课程不存在: %s", courseId);
    return new ResourceNotFoundException(message, "course", courseId);
  }

  /**
   * 创建课时未找到异常
   *
   * @param lessonId 课时ID
   * @return ResourceNotFoundException实例
   */
  public static ResourceNotFoundException forLesson(UUID lessonId) {
    String message = String.format("课时不存在: %s", lessonId);
    return new ResourceNotFoundException(message, "lesson", lessonId);
  }

  /**
   * 创建通用资源未找到异常
   *
   * @param resourceType 资源类型
   * @param resourceId 资源ID
   * @return ResourceNotFoundException实例
   */
  public static ResourceNotFoundException forResource(String resourceType, Object resourceId) {
    String message = String.format("%s不存在: %s", resourceType, resourceId);
    return new ResourceNotFoundException(message, resourceType, resourceId);
  }

  /**
   * 创建课程下课时未找到异常
   *
   * @param courseId 课程ID
   * @param lessonId 课时ID
   * @return ResourceNotFoundException实例
   */
  public static ResourceNotFoundException forLessonInCourse(UUID courseId, UUID lessonId) {
    String message = String.format("课程 %s 中不存在课时: %s", courseId, lessonId);
    return new ResourceNotFoundException(message, "lesson", lessonId);
  }

  /**
   * 创建用户课程关系未找到异常
   *
   * @param userId 用户ID
   * @param courseId 课程ID
   * @return ResourceNotFoundException实例
   */
  public static ResourceNotFoundException forUserCourseRelation(UUID userId, UUID courseId) {
    String message = String.format("用户 %s 与课程 %s 之间不存在关联关系", userId, courseId);
    return new ResourceNotFoundException(
        message, "user_course_relation", String.format("%s-%s", userId, courseId));
  }
}
