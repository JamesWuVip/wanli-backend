package com.wanli.backend.event;

import java.util.UUID;

/** 课程创建事件 */
public class CourseCreatedEvent extends BaseEvent {
  private final UUID courseId;
  private final String title;
  private final UUID creatorId;

  public CourseCreatedEvent(UUID courseId, String title, UUID creatorId) {
    super(courseId, "CourseService");
    this.courseId = courseId;
    this.title = title;
    this.creatorId = creatorId;
  }

  public UUID getCourseId() {
    return courseId;
  }

  public String getTitle() {
    return title;
  }

  public UUID getCreatorId() {
    return creatorId;
  }
}
