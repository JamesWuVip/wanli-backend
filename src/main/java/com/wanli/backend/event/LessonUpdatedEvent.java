package com.wanli.backend.event;

import java.util.UUID;

/** 课时更新事件 */
public class LessonUpdatedEvent extends BaseEvent {
  private final UUID lessonId;
  private final String title;
  private final UUID courseId;
  private final UUID updaterId;

  public LessonUpdatedEvent(UUID lessonId, String title, UUID courseId, UUID updaterId) {
    super(lessonId, "LessonService");
    this.lessonId = lessonId;
    this.title = title;
    this.courseId = courseId;
    this.updaterId = updaterId;
  }

  public UUID getLessonId() {
    return lessonId;
  }

  public String getTitle() {
    return title;
  }

  public UUID getCourseId() {
    return courseId;
  }

  public UUID getUpdaterId() {
    return updaterId;
  }
}
