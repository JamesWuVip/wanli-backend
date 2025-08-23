package com.wanli.backend.event;

import java.util.UUID;

public class CourseDeletedEvent extends BaseEvent {
  private UUID courseId;
  private String courseName;
  private UUID deletedBy;

  public CourseDeletedEvent(UUID courseId, String courseName, UUID deletedBy) {
    super();
    this.courseId = courseId;
    this.courseName = courseName;
    this.deletedBy = deletedBy;
  }

  public UUID getCourseId() {
    return courseId;
  }

  public String getCourseName() {
    return courseName;
  }

  public UUID getDeletedBy() {
    return deletedBy;
  }

  // 别名方法，用于兼容EventListener中的调用
  public UUID getDeleterId() {
    return deletedBy;
  }

  public String getTitle() {
    return courseName;
  }
}
