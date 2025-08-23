package com.wanli.backend.event;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.wanli.backend.util.LogUtil;

/** 事件发布器 提供统一的事件发布和处理机制 */
@Component
public class EventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;
  private final Executor asyncExecutor;

  public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
    this.asyncExecutor =
        Executors.newFixedThreadPool(
            5,
            r -> {
              Thread thread = new Thread(r, "event-publisher-");
              thread.setDaemon(true);
              return thread;
            });
  }

  /** 同步发布事件 */
  public void publishEvent(BaseEvent event) {
    try {
      event.setEventId(UUID.randomUUID());
      event.setTimestamp(LocalDateTime.now());

      java.util.Map<String, Object> context = new java.util.HashMap<>();
      context.put("eventType", event.getClass().getSimpleName());
      context.put("eventId", event.getEventId());
      LogUtil.logBusiness("EVENT_PUBLISH", context);

      applicationEventPublisher.publishEvent(event);

    } catch (Exception e) {
      LogUtil.logError(
          "EVENT_PUBLISH_ERROR",
          "",
          "PUBLISH_FAILED",
          "事件发布失败: " + event.getClass().getSimpleName(),
          e);
      throw new RuntimeException("事件发布失败", e);
    }
  }

  /** 异步发布事件 */
  public CompletableFuture<Void> publishEventAsync(BaseEvent event) {
    return CompletableFuture.runAsync(() -> publishEvent(event), asyncExecutor);
  }

  /** 发布用户注册事件 */
  public void publishUserRegisteredEvent(UUID userId, String username, String email) {
    UserRegisteredEvent event = new UserRegisteredEvent(userId, username, email);
    publishEvent(event);
  }

  /** 发布用户登录事件 */
  public void publishUserLoginEvent(UUID userId, String username, String ipAddress) {
    UserLoginEvent event = new UserLoginEvent(userId, username, ipAddress);
    publishEvent(event);
  }

  /** 发布课程创建事件 */
  public void publishCourseCreatedEvent(UUID courseId, String title, UUID creatorId) {
    CourseCreatedEvent event = new CourseCreatedEvent(courseId, title, creatorId);
    publishEvent(event);
  }

  /** 发布课程更新事件 */
  public void publishCourseUpdatedEvent(UUID courseId, String title, UUID updaterId) {
    CourseUpdatedEvent event = new CourseUpdatedEvent(courseId, title, updaterId);
    publishEvent(event);
  }

  /** 发布课程删除事件 */
  public void publishCourseDeletedEvent(UUID courseId, String title, UUID deleterId) {
    CourseDeletedEvent event = new CourseDeletedEvent(courseId, title, deleterId);
    publishEvent(event);
  }

  /** 发布课时创建事件 */
  public void publishLessonCreatedEvent(
      UUID lessonId, String title, UUID courseId, UUID creatorId) {
    LessonCreatedEvent event = new LessonCreatedEvent(lessonId, title, courseId, creatorId);
    publishEvent(event);
  }

  /** 发布课时更新事件 */
  public void publishLessonUpdatedEvent(
      UUID lessonId, String title, UUID courseId, UUID updaterId) {
    LessonUpdatedEvent event = new LessonUpdatedEvent(lessonId, title, courseId, updaterId);
    publishEvent(event);
  }

  /** 发布课时删除事件 */
  public void publishLessonDeletedEvent(
      UUID lessonId, String title, UUID courseId, UUID deleterId) {
    LessonDeletedEvent event = new LessonDeletedEvent(lessonId, title, courseId, deleterId);
    publishEvent(event);
  }

  /** 发布系统错误事件 */
  public void publishSystemErrorEvent(String errorType, String errorMessage, Exception exception) {
    SystemErrorEvent event = new SystemErrorEvent(errorType, errorMessage, exception);
    publishEventAsync(event); // 异步发布，避免影响主流程
  }

  /** 发布性能警告事件 */
  public void publishPerformanceWarningEvent(String operation, long executionTime, long threshold) {
    PerformanceWarningEvent event =
        new PerformanceWarningEvent(operation, executionTime, threshold);
    publishEventAsync(event); // 异步发布
  }

  /** 发布安全事件 */
  public void publishSecurityEvent(
      String eventType, UUID userId, String details, String ipAddress) {
    SecurityEvent event = new SecurityEvent(eventType, userId, details, ipAddress);
    publishEvent(event);
  }

  /** 发布缓存清除事件 */
  public void publishCacheClearEvent(String cacheKey, String reason) {
    CacheClearEvent event = new CacheClearEvent("MANUAL", cacheKey, reason, "SYSTEM");
    publishEventAsync(event); // 异步发布
  }

  /** 发布数据同步事件 */
  public void publishDataSyncEvent(String entityType, UUID entityId, String operation) {
    DataSyncEvent event = new DataSyncEvent("AUTO", "LOCAL", "DATABASE", entityType, 1, "PENDING");
    publishEventAsync(event); // 异步发布
  }

  /** 关闭异步执行器 */
  public void shutdown() {
    if (asyncExecutor instanceof java.util.concurrent.ExecutorService) {
      ((java.util.concurrent.ExecutorService) asyncExecutor).shutdown();
    }
  }
}
