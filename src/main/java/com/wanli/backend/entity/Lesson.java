package com.wanli.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;

/** 课时实体类 对应数据库设计文档中的lessons表 */
@Entity
@Table(name = "lessons")
public class Lesson {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "UUID")
  private UUID id;

  @JsonProperty("course_id")
  @Column(
      name = "course_id",
      columnDefinition = "UUID",
      nullable = false,
      insertable = false,
      updatable = false)
  private UUID courseId;

  @Column(name = "title", length = 255, nullable = false)
  private String title;

  @JsonProperty("order_index")
  @Column(name = "order_index", nullable = false)
  private Integer orderIndex = 0;

  @JsonProperty("created_at")
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @JsonProperty("updated_at")
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  // 与Course实体的多对一关系
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  // 默认构造函数
  public Lesson() {}

  // 构造函数
  public Lesson(String title, Integer orderIndex) {
    this.title = title;
    this.orderIndex = orderIndex;
  }

  // 构造函数
  public Lesson(Course course, String title, Integer orderIndex) {
    this.course = course;
    this.title = title;
    this.orderIndex = orderIndex;
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getCourseId() {
    return courseId;
  }

  public void setCourseId(UUID courseId) {
    this.courseId = courseId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Integer getOrderIndex() {
    return orderIndex;
  }

  public void setOrderIndex(Integer orderIndex) {
    this.orderIndex = orderIndex;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
    if (course != null) {
      this.courseId = course.getId();
    }
  }

  @Override
  public String toString() {
    return "Lesson{"
        + "id="
        + id
        + ", courseId="
        + courseId
        + ", title='"
        + title
        + '\''
        + ", orderIndex="
        + orderIndex
        + ", createdAt="
        + createdAt
        + '}';
  }
}
