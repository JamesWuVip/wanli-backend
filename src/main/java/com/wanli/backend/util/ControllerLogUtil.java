package com.wanli.backend.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Controller层日志工具类 统一处理Controller层的日志记录，消除重复代码 */
public class ControllerLogUtil {

  /**
   * 记录课程操作日志
   *
   * @param operation 操作类型
   * @param description 操作描述
   * @param userId 用户ID
   * @param courseTitle 课程标题
   */
  public static void logCourseOperation(
      String operation, String description, UUID userId, String courseTitle) {
    Map<String, Object> logContext = new HashMap<>();
    logContext.put("userId", userId);
    logContext.put("courseTitle", courseTitle);
    LogUtil.logBusinessOperation(operation, description, logContext);
  }

  /**
   * 记录课程操作日志（包含课程ID）
   *
   * @param operation 操作类型
   * @param description 操作描述
   * @param userId 用户ID
   * @param courseId 课程ID
   * @param courseTitle 课程标题
   */
  public static void logCourseOperation(
      String operation, String description, UUID userId, UUID courseId, String courseTitle) {
    Map<String, Object> logContext = new HashMap<>();
    logContext.put("userId", userId);
    logContext.put("courseId", courseId);
    logContext.put("courseTitle", courseTitle);
    LogUtil.logBusinessOperation(operation, description, logContext);
  }

  /**
   * 记录课时操作日志
   *
   * @param operation 操作类型
   * @param description 操作描述
   * @param userId 用户ID
   * @param lessonTitle 课时标题
   * @param courseId 课程ID
   */
  public static void logLessonOperation(
      String operation, String description, UUID userId, String lessonTitle, UUID courseId) {
    Map<String, Object> logContext = new HashMap<>();
    logContext.put("userId", userId);
    logContext.put("lessonTitle", lessonTitle);
    logContext.put("courseId", courseId);
    LogUtil.logBusinessOperation(operation, description, logContext);
  }

  /**
   * 记录课时操作日志（包含课时ID）
   *
   * @param operation 操作类型
   * @param description 操作描述
   * @param userId 用户ID
   * @param lessonId 课时ID
   * @param lessonTitle 课时标题
   */
  public static void logLessonOperation(
      String operation, String description, UUID userId, UUID lessonId, String lessonTitle) {
    Map<String, Object> logContext = new HashMap<>();
    logContext.put("userId", userId);
    logContext.put("lessonId", lessonId);
    logContext.put("lessonTitle", lessonTitle);
    LogUtil.logBusinessOperation(operation, description, logContext);
  }

  /**
   * 记录用户认证操作日志
   *
   * @param operation 操作类型
   * @param email 用户邮箱
   * @param additionalInfo 额外信息
   */
  public static void logAuthOperation(
      String operation, String email, Map<String, Object> additionalInfo) {
    Map<String, Object> logContext = new HashMap<>();
    logContext.put("email", email);
    if (additionalInfo != null) {
      logContext.putAll(additionalInfo);
    }
    LogUtil.logBusinessOperation(operation, email, logContext);
  }

  /**
   * 记录用户注册操作日志
   *
   * @param email 用户邮箱
   * @param username 用户名
   * @param role 用户角色
   */
  public static void logUserRegistration(String email, String username, String role) {
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put("username", username);
    additionalInfo.put("role", role != null ? role : "student");
    logAuthOperation("USER_REGISTER", email, additionalInfo);
  }

  /**
   * 记录用户登录操作日志
   *
   * @param email 用户邮箱
   * @param loginMethod 登录方式
   */
  public static void logUserLogin(String email, String loginMethod) {
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put("loginMethod", loginMethod);
    logAuthOperation("USER_LOGIN", email, additionalInfo);
  }

  /**
   * 创建简单的日志上下文
   *
   * @param key 键
   * @param value 值
   * @return 日志上下文Map
   */
  public static Map<String, Object> createLogContext(String key, Object value) {
    Map<String, Object> context = new HashMap<>();
    context.put(key, value);
    return context;
  }

  /**
   * 创建课程更新日志上下文
   *
   * @param courseId 课程ID
   * @param request 更新请求对象
   * @return 日志上下文Map
   */
  public static Map<String, Object> createCourseUpdateLogContext(UUID courseId, Object request) {
    Map<String, Object> context = new HashMap<>();
    context.put("courseId", courseId);
    context.put("request", request);
    return context;
  }

  /**
   * 创建课时更新日志上下文
   *
   * @param lessonId 课时ID
   * @param request 更新请求对象
   * @return 日志上下文Map
   */
  public static Map<String, Object> createLessonUpdateLogContext(UUID lessonId, Object request) {
    Map<String, Object> context = new HashMap<>();
    context.put("lessonId", lessonId);
    context.put("request", request);
    return context;
  }

  /**
   * 记录操作错误日志
   *
   * @param operation 操作名称
   * @param exception 异常信息
   * @param context 上下文信息
   */
  public static void logOperationError(
      String operation, Exception exception, Map<String, Object> context) {
    LogUtil.logError(operation, "", "OPERATION_ERROR", exception.getMessage(), exception);
  }

  /**
   * 记录操作错误日志（简化版）
   *
   * @param operation 操作名称
   * @param exception 异常信息
   * @param contextKey 上下文键
   * @param contextValue 上下文值
   */
  public static void logOperationError(
      String operation, Exception exception, String contextKey, Object contextValue) {
    Map<String, Object> context = createLogContext(contextKey, contextValue);
    logOperationError(operation, exception, context);
  }
}
