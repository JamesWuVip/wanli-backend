package com.wanli.backend.event;

import java.util.UUID;

/** 课程更新事件 */
public class CourseUpdatedEvent extends BaseEvent {
  private final UUID courseId;
  private final String title;
  private final UUID updaterId;

  public CourseUpdatedEvent(UUID courseId, String title, UUID updaterId) {
    super(courseId, "CourseService");
    this.courseId = courseId;
    this.title = title;
    this.updaterId = updaterId;
  }

  public UUID getCourseId() {
    return courseId;
  }

  public String getTitle() {
    return title;
  }

  public UUID getUpdaterId() {
    return updaterId;
  }
}
