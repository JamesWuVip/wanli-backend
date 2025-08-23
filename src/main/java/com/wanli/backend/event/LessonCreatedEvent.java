package com.wanli.backend.event;

import java.util.UUID;

/** 课时创建事件 */
public class LessonCreatedEvent extends BaseEvent {
  private final UUID lessonId;
  private final String title;
  private final UUID courseId;
  private final UUID creatorId;

  public LessonCreatedEvent(UUID lessonId, String title, UUID courseId, UUID creatorId) {
    super(lessonId, "LessonService");
    this.lessonId = lessonId;
    this.title = title;
    this.courseId = courseId;
    this.creatorId = creatorId;
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

  public UUID getCreatorId() {
    return creatorId;
  }
}
