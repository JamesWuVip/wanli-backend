package com.wanli.backend.event;

import java.util.UUID;

public class LessonDeletedEvent extends BaseEvent {
  private UUID lessonId;
  private String lessonTitle;
  private UUID courseId;
  private UUID deletedBy;

  public LessonDeletedEvent(UUID lessonId, String lessonTitle, UUID courseId, UUID deletedBy) {
    super();
    this.lessonId = lessonId;
    this.lessonTitle = lessonTitle;
    this.courseId = courseId;
    this.deletedBy = deletedBy;
  }

  public UUID getLessonId() {
    return lessonId;
  }

  public String getLessonTitle() {
    return lessonTitle;
  }

  public UUID getCourseId() {
    return courseId;
  }

  public UUID getDeletedBy() {
    return deletedBy;
  }

  // 别名方法，用于兼容EventListener中的调用
  public UUID getDeleterId() {
    return deletedBy;
  }

  public String getTitle() {
    return lessonTitle;
  }
}
