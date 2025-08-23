package com.wanli.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

/** 课程实体类 对应数据库设计文档中的courses表 */
@Entity
@Table(
    name = "courses",
    indexes = {
      // 创建者ID索引 - 用于按创建者查询课程
      @Index(name = "idx_courses_creator_id", columnList = "creator_id"),
      // 状态索引 - 用于按状态查询课程
      @Index(name = "idx_courses_status", columnList = "status"),
      // 创建时间索引 - 用于按时间排序
      @Index(name = "idx_courses_created_at", columnList = "created_at"),
      // 软删除索引 - 用于过滤已删除记录
      @Index(name = "idx_courses_deleted_at", columnList = "deleted_at"),
      // 复合索引：创建者+状态+删除状态 - 用于高频查询组合
      @Index(
          name = "idx_courses_creator_status_deleted",
          columnList = "creator_id, status, deleted_at"),
      // 复合索引：状态+创建时间+删除状态 - 用于分页查询
      @Index(
          name = "idx_courses_status_created_deleted",
          columnList = "status, created_at, deleted_at"),
      // 标题索引 - 用于模糊查询（部分匹配）
      @Index(name = "idx_courses_title", columnList = "title")
    })
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "UUID")
  private UUID id;

  @Column(name = "creator_id", columnDefinition = "UUID", nullable = false)
  private UUID creatorId;

  @Column(name = "title", length = 255, nullable = false)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "status", length = 50, nullable = false)
  private String status = "DRAFT";

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  // 与Lesson实体的一对多关系
  @OneToMany(
      mappedBy = "course",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      fetch = FetchType.LAZY)
  private List<Lesson> lessons = new ArrayList<>();

  // 默认构造函数
  public Course() {}

  // 构造函数
  public Course(UUID creatorId, String title, String description) {
    this.creatorId = creatorId;
    this.title = title;
    this.description = description;
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(UUID creatorId) {
    this.creatorId = creatorId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public List<Lesson> getLessons() {
    return lessons;
  }

  public void setLessons(List<Lesson> lessons) {
    this.lessons = lessons;
  }

  // 便利方法：添加课时
  public void addLesson(Lesson lesson) {
    lessons.add(lesson);
    lesson.setCourse(this);
  }

  // 便利方法：移除课时
  public void removeLesson(Lesson lesson) {
    lessons.remove(lesson);
    lesson.setCourse(null);
  }

  // 便利方法：检查是否已删除
  public boolean isDeleted() {
    return deletedAt != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Course course = (Course) obj;
    return id != null && id.equals(course.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Course{"
        + "id="
        + id
        + ", creatorId="
        + creatorId
        + ", title='"
        + title
        + "'"
        + ", status='"
        + status
        + "'"
        + ", createdAt="
        + createdAt
        + ", updatedAt="
        + updatedAt
        + ", deletedAt="
        + deletedAt
        + '}';
  }
}
