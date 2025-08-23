package com.wanli.backend.event;

// import org.springframework.context.event.EventListener; // 避免与类名冲突
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.LogUtil;

/** 事件监听器 处理各种业务事件 */
@Component
public class EventListener {

  private final CacheUtil cacheUtil;

  public EventListener(CacheUtil cacheUtil) {
    this.cacheUtil = cacheUtil;
  }

  /** 处理用户注册事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleUserRegisteredEvent(UserRegisteredEvent event) {
    try {
      LogUtil.logInfo(
          "USER_REGISTERED",
          event.getUserId().toString(),
          String.format("用户注册成功: %s (%s)", event.getUsername(), event.getEmail()));

      // 发送欢迎邮件（如果启用）
      sendWelcomeEmail(event.getEmail(), event.getUsername());

      // 初始化用户相关缓存
      initializeUserCache(event.getUserId());

      // 记录用户统计信息
      updateUserStatistics("register");

    } catch (Exception e) {
      LogUtil.logError(
          "USER_REGISTERED_HANDLER_ERROR",
          event.getUserId().toString(),
          "USER_REGISTERED_ERROR",
          "处理用户注册事件失败",
          e);
    }
  }

  /** 处理用户登录事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleUserLoginEvent(UserLoginEvent event) {
    try {
      LogUtil.logInfo(
          "USER_LOGIN",
          event.getUserId().toString(),
          String.format("用户登录: %s, IP: %s", event.getUsername(), event.getIpAddress()));

      // 更新用户最后登录时间
      updateLastLoginTime(event.getUserId());

      // 记录登录统计
      updateUserStatistics("login");

      // 检查异常登录
      checkSuspiciousLogin(event.getUserId(), event.getIpAddress());

    } catch (Exception e) {
      LogUtil.logError(
          "USER_LOGIN_HANDLER_ERROR",
          event.getUserId().toString(),
          "USER_LOGIN_ERROR",
          "处理用户登录事件失败",
          e);
    }
  }

  /** 处理课程创建事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleCourseCreatedEvent(CourseCreatedEvent event) {
    try {
      LogUtil.logInfo(
          "COURSE_CREATED",
          event.getCreatorId().toString(),
          String.format("课程创建: %s (ID: %s)", event.getTitle(), event.getCourseId()));

      // 清除相关缓存
      clearCourseRelatedCache(event.getCreatorId());

      // 更新课程统计
      updateCourseStatistics("create");

      // 发送通知（如果需要）
      notifyCourseCreation(event.getCourseId(), event.getTitle(), event.getCreatorId());

    } catch (Exception e) {
      LogUtil.logError(
          "COURSE_CREATED_HANDLER_ERROR",
          event.getCreatorId().toString(),
          "COURSE_CREATED_ERROR",
          "处理课程创建事件失败",
          e);
    }
  }

  /** 处理课程更新事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleCourseUpdatedEvent(CourseUpdatedEvent event) {
    try {
      LogUtil.logInfo(
          "COURSE_UPDATED",
          event.getUpdaterId().toString(),
          String.format("课程更新: %s (ID: %s)", event.getTitle(), event.getCourseId()));

      // 清除课程缓存
      clearCourseCache(event.getCourseId());

      // 更新搜索索引
      updateSearchIndex("course", event.getCourseId());

    } catch (Exception e) {
      LogUtil.logError(
          "COURSE_UPDATED_HANDLER_ERROR",
          event.getUpdaterId().toString(),
          "COURSE_UPDATED_ERROR",
          "处理课程更新事件失败",
          e);
    }
  }

  /** 处理课程删除事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleCourseDeletedEvent(CourseDeletedEvent event) {
    try {
      LogUtil.logInfo(
          "COURSE_DELETED",
          event.getDeleterId().toString(),
          String.format("课程删除: %s (ID: %s)", event.getTitle(), event.getCourseId()));

      // 清除所有相关缓存
      clearAllCourseRelatedCache(event.getCourseId());

      // 更新统计信息
      updateCourseStatistics("delete");

      // 清理相关数据
      cleanupCourseRelatedData(event.getCourseId());

    } catch (Exception e) {
      LogUtil.logError(
          "COURSE_DELETED_HANDLER_ERROR",
          event.getDeleterId().toString(),
          "COURSE_DELETED_ERROR",
          "处理课程删除事件失败",
          e);
    }
  }

  /** 处理课时创建事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleLessonCreatedEvent(LessonCreatedEvent event) {
    try {
      LogUtil.logInfo(
          "LESSON_CREATED",
          event.getCreatorId().toString(),
          String.format(
              "课时创建: %s (ID: %s, 课程ID: %s)",
              event.getTitle(), event.getLessonId(), event.getCourseId()));

      // 清除课程相关缓存
      clearCourseCache(event.getCourseId());

      // 更新课程统计
      updateLessonStatistics("create");

    } catch (Exception e) {
      LogUtil.logError(
          "LESSON_CREATED_HANDLER_ERROR",
          event.getCreatorId().toString(),
          "LESSON_CREATED_ERROR",
          "处理课时创建事件失败",
          e);
    }
  }

  /** 处理课时更新事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleLessonUpdatedEvent(LessonUpdatedEvent event) {
    try {
      LogUtil.logInfo(
          "LESSON_UPDATED",
          event.getUpdaterId().toString(),
          String.format("课时更新: %s (ID: %s)", event.getTitle(), event.getLessonId()));

      // 清除课时和课程缓存
      clearLessonCache(event.getLessonId());
      clearCourseCache(event.getCourseId());

    } catch (Exception e) {
      LogUtil.logError(
          "LESSON_UPDATED_HANDLER_ERROR",
          event.getUpdaterId().toString(),
          "LESSON_UPDATED_ERROR",
          "处理课时更新事件失败",
          e);
    }
  }

  /** 处理课时删除事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleLessonDeletedEvent(LessonDeletedEvent event) {
    try {
      LogUtil.logInfo(
          "LESSON_DELETED",
          event.getDeleterId().toString(),
          String.format("课时删除: %s (ID: %s)", event.getTitle(), event.getLessonId()));

      // 清除所有相关缓存
      clearLessonCache(event.getLessonId());
      clearCourseCache(event.getCourseId());

      // 更新统计信息
      updateLessonStatistics("delete");

    } catch (Exception e) {
      LogUtil.logError(
          "LESSON_DELETED_HANDLER_ERROR",
          event.getDeleterId().toString(),
          "LESSON_DELETED_ERROR",
          "处理课时删除事件失败",
          e);
    }
  }

  /** 处理系统错误事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handleSystemErrorEvent(SystemErrorEvent event) {
    try {
      LogUtil.logError(
          "SYSTEM_ERROR",
          "",
          "SYSTEM_ERROR_CODE",
          String.format("系统错误: %s - %s", event.getErrorType(), event.getErrorMessage()),
          event.getException());

      // 发送错误报告（如果配置了）
      sendErrorReport(event);

      // 更新错误统计
      updateErrorStatistics(event.getErrorType());

    } catch (Exception e) {
      LogUtil.logError(
          "SYSTEM_ERROR_HANDLER_ERROR", "", "SYSTEM_ERROR_HANDLER_ERROR_CODE", "处理系统错误事件失败", e);
    }
  }

  /** 处理性能警告事件 */
  @org.springframework.context.event.EventListener
  @Async
  public void handlePerformanceWarningEvent(PerformanceWarningEvent event) {
    try {
      LogUtil.logWarn(
          "PERFORMANCE_WARNING",
          "",
          String.format(
              "性能警告: %s 执行时间 %dms 超过阈值 %dms",
              event.getOperation(), event.getExecutionTime(), event.getThreshold()));

      // 记录性能统计
      updatePerformanceStatistics(event.getOperation(), event.getExecutionTime());

    } catch (Exception e) {
      LogUtil.logError(
          "PERFORMANCE_WARNING_HANDLER_ERROR",
          "",
          "PERFORMANCE_WARNING_ERROR_CODE",
          "处理性能警告事件失败",
          e);
    }
  }

  /** 处理安全事件 */
  @org.springframework.context.event.EventListener
  public void handleSecurityEvent(SecurityEvent event) {
    try {
      LogUtil.logSecurity(
          event.getEventType(),
          event.getUserId().toString(),
          event.getIpAddress(),
          String.format("安全事件: %s - %s", event.getEventType(), event.getDetails()));

      // 更新安全统计
      updateSecurityStatistics(event.getEventType());

      // 如果是严重安全事件，立即处理
      if (isCriticalSecurityEvent(event.getEventType())) {
        handleCriticalSecurityEvent(event);
      }

    } catch (Exception e) {
      LogUtil.logError(
          "SECURITY_EVENT_HANDLER_ERROR",
          event.getUserId().toString(),
          "SECURITY_EVENT_ERROR",
          "处理安全事件失败",
          e);
    }
  }

  // 私有辅助方法
  private void sendWelcomeEmail(String email, String username) {
    // 实现欢迎邮件发送逻辑
    LogUtil.logInfo("WELCOME_EMAIL", "", "发送欢迎邮件: " + email);
  }

  private void initializeUserCache(java.util.UUID userId) {
    // 初始化用户相关缓存
    String cacheKey = "user:" + userId;
    cacheUtil.remove(cacheKey); // 清除可能存在的旧缓存
  }

  private void updateUserStatistics(String action) {
    // 更新用户统计信息
    String statsKey = "stats:user:" + action;
    // 更新统计信息 - 使用简单的计数器实现
    Object currentValue = cacheUtil.get(statsKey, Integer.class);
    int newValue = (currentValue != null ? (Integer) currentValue : 0) + 1;
    cacheUtil.put(statsKey, newValue, 60); // 缓存1小时
  }

  private void updateLastLoginTime(java.util.UUID userId) {
    // 更新用户最后登录时间
    String cacheKey = "user:last_login:" + userId;
    cacheUtil.put(cacheKey, System.currentTimeMillis(), 86400); // 24小时过期
  }

  private void checkSuspiciousLogin(java.util.UUID userId, String ipAddress) {
    // 检查异常登录行为
    String cacheKey = "user:login_ips:" + userId;
    // 实现异常登录检测逻辑
  }

  private void clearCourseRelatedCache(java.util.UUID userId) {
    cacheUtil.removeByPattern("course:user:" + userId + ":*");
    cacheUtil.removeByPattern("courses:*");
  }

  private void clearCourseCache(java.util.UUID courseId) {
    cacheUtil.remove("course:" + courseId);
    cacheUtil.removeByPattern("lessons:course:" + courseId + ":*");
  }

  private void clearAllCourseRelatedCache(java.util.UUID courseId) {
    clearCourseCache(courseId);
    cacheUtil.removeByPattern("course:*:" + courseId + ":*");
  }

  private void clearLessonCache(java.util.UUID lessonId) {
    cacheUtil.remove("lesson:" + lessonId);
  }

  private void updateCourseStatistics(String action) {
    String statsKey = "stats:course:" + action;
    // 更新统计信息 - 使用简单的计数器实现
    Object currentValue = cacheUtil.get(statsKey, Integer.class);
    int newValue = (currentValue != null ? (Integer) currentValue : 0) + 1;
    cacheUtil.put(statsKey, newValue, 60); // 缓存1小时
  }

  private void updateLessonStatistics(String action) {
    String statsKey = "stats:lesson:" + action;
    // 更新统计信息 - 使用简单的计数器实现
    Object currentValue = cacheUtil.get(statsKey, Integer.class);
    int newValue = (currentValue != null ? (Integer) currentValue : 0) + 1;
    cacheUtil.put(statsKey, newValue, 60); // 缓存1小时
  }

  private void updateErrorStatistics(String errorType) {
    String statsKey = "stats:error:" + errorType;
    // 更新统计信息 - 使用简单的计数器实现
    Object currentValue = cacheUtil.get(statsKey, Integer.class);
    int newValue = (currentValue != null ? (Integer) currentValue : 0) + 1;
    cacheUtil.put(statsKey, newValue, 60); // 缓存1小时
  }

  private void updatePerformanceStatistics(String operation, long executionTime) {
    String statsKey = "stats:performance:" + operation;
    // 更新统计信息 - 使用简单的计数器实现
    Object currentValue = cacheUtil.get(statsKey, Integer.class);
    int newValue = (currentValue != null ? (Integer) currentValue : 0) + 1;
    cacheUtil.put(statsKey, newValue, 60); // 缓存1小时
  }

  private void updateSecurityStatistics(String eventType) {
    String statsKey = "stats:security:" + eventType;
    // 更新统计信息 - 使用简单的计数器实现
    Object currentValue = cacheUtil.get(statsKey, Integer.class);
    int newValue = (currentValue != null ? (Integer) currentValue : 0) + 1;
    cacheUtil.put(statsKey, newValue, 60); // 缓存1小时
  }

  private void notifyCourseCreation(
      java.util.UUID courseId, String title, java.util.UUID creatorId) {
    // 实现课程创建通知逻辑
  }

  private void updateSearchIndex(String entityType, java.util.UUID entityId) {
    // 实现搜索索引更新逻辑
  }

  private void cleanupCourseRelatedData(java.util.UUID courseId) {
    // 实现课程相关数据清理逻辑
  }

  private void sendErrorReport(SystemErrorEvent event) {
    // 实现错误报告发送逻辑
  }

  private boolean isCriticalSecurityEvent(String eventType) {
    return "BRUTE_FORCE_ATTACK".equals(eventType)
        || "UNAUTHORIZED_ACCESS".equals(eventType)
        || "SUSPICIOUS_ACTIVITY".equals(eventType);
  }

  private void handleCriticalSecurityEvent(SecurityEvent event) {
    // 处理严重安全事件
    LogUtil.logSecurity(
        "CRITICAL_SECURITY_EVENT",
        event.getUserId().toString(),
        event.getIpAddress(),
        "处理严重安全事件: " + event.getEventType());
  }
}
