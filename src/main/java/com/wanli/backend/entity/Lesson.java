package com.wanli.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanli.backend.enums.LessonStatus;

import jakarta.persistence.*;

/** 课时实体类 对应数据库设计文档中的lessons表 */
@Entity
@Table(
    name = "lessons",
    indexes = {
      // 课程ID索引 - 用于按课程查询课时
      @Index(name = "idx_lessons_course_id", columnList = "course_id"),
      // 创建者ID索引 - 用于按创建者查询课时
      @Index(name = "idx_lessons_creator_id", columnList = "creator_id"),
      // 排序索引 - 用于课时排序
      @Index(name = "idx_lessons_order_index", columnList = "order_index"),
      // 创建时间索引 - 用于按时间排序
      @Index(name = "idx_lessons_created_at", columnList = "created_at"),
      // 软删除索引 - 用于过滤已删除记录
      @Index(name = "idx_lessons_deleted_at", columnList = "deleted_at"),
      // 复合索引：课程+排序+删除状态 - 用于获取课程的课时列表
      @Index(
          name = "idx_lessons_course_order_deleted",
          columnList = "course_id, order_index, deleted_at"),
      // 复合索引：课程+创建时间+删除状态 - 用于分页查询
      @Index(
          name = "idx_lessons_course_created_deleted",
          columnList = "course_id, created_at, deleted_at"),
      // 复合索引：创建者+创建时间+删除状态 - 用于创建者的课时查询
      @Index(
          name = "idx_lessons_creator_created_deleted",
          columnList = "creator_id, created_at, deleted_at"),
      // 标题索引 - 用于模糊查询（部分匹配）
      @Index(name = "idx_lessons_title", columnList = "title"),
      // 状态索引 - 用于按状态查询课时
      @Index(name = "idx_lessons_status", columnList = "status"),
      // 时长索引 - 用于按时长排序和筛选
      @Index(name = "idx_lessons_duration", columnList = "duration"),
      // 复合索引：课程+状态+删除状态 - 用于获取特定状态的课程课时
      @Index(
          name = "idx_lessons_course_status_deleted",
          columnList = "course_id, status, deleted_at"),
      // 复合索引：状态+创建时间+删除状态 - 用于按状态和时间查询
      @Index(
          name = "idx_lessons_status_created_deleted",
          columnList = "status, created_at, deleted_at")
    })
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

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @JsonProperty("video_url")
  @Column(name = "video_url", length = 500)
  private String videoUrl;

  @Column(name = "duration", nullable = true)
  private Integer duration; // 课时时长（秒）

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private LessonStatus status = LessonStatus.DRAFT; // 课时状态

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

  // 与User实体的多对一关系（创建者）
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @JsonProperty("creator_id")
  @Column(
      name = "creator_id",
      columnDefinition = "UUID",
      nullable = false,
      insertable = false,
      updatable = false)
  private UUID creatorId;

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

  // 完整构造函数
  public Lesson(
      Course course,
      User creator,
      String title,
      String content,
      String videoUrl,
      Integer orderIndex) {
    this.course = course;
    this.creator = creator;
    this.title = title;
    this.content = content;
    this.videoUrl = videoUrl;
    this.orderIndex = orderIndex;
  }

  // 扩展构造函数
  public Lesson(
      Course course,
      User creator,
      String title,
      String description,
      String content,
      String videoUrl,
      Integer duration,
      LessonStatus status,
      Integer orderIndex) {
    this.course = course;
    this.creator = creator;
    this.title = title;
    this.description = description;
    this.content = content;
    this.videoUrl = videoUrl;
    this.duration = duration;
    this.status = status;
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

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public void setVideoUrl(String videoUrl) {
    this.videoUrl = videoUrl;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  public LessonStatus getStatus() {
    return status;
  }

  public void setStatus(LessonStatus status) {
    this.status = status;
  }

  // 便利方法：设置状态（通过字符串）
  public void setStatusByCode(String statusCode) {
    this.status = LessonStatus.fromCode(statusCode);
  }

  // 便利方法：获取状态代码
  public String getStatusCode() {
    return status != null ? status.getCode() : null;
  }

  // 便利方法：检查是否可以发布
  public boolean canPublish() {
    return status != null && status.canPublish();
  }

  // 便利方法：检查是否可以归档
  public boolean canArchive() {
    return status != null && status.canArchive();
  }

  // 便利方法：检查是否可以编辑
  public boolean canEdit() {
    return status != null && status.canEdit();
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

  public User getCreator() {
    return creator;
  }

  public void setCreator(User creator) {
    this.creator = creator;
    if (creator != null) {
      this.creatorId = creator.getId();
    }
  }

  public UUID getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(UUID creatorId) {
    this.creatorId = creatorId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Lesson lesson = (Lesson) obj;
    return id != null && id.equals(lesson.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
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
        + "'"
        + ", description='"
        + (description != null
            ? description.substring(0, Math.min(description.length(), 50)) + "..."
            : "null")
        + "'"
        + ", videoUrl='"
        + videoUrl
        + "'"
        + ", duration="
        + duration
        + ", status="
        + status
        + ", orderIndex="
        + orderIndex
        + ", createdAt="
        + createdAt
        + "}";
  }
}
