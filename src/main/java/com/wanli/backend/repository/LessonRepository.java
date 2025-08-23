package com.wanli.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wanli.backend.entity.Lesson;

/** 课时数据访问层接口 提供课时相关的数据库操作方法 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

  /**
   * 根据课程ID查找所有课时（排除已删除的课时）- 无限制，谨慎使用 使用JOIN FETCH避免N+1查询问题 仅用于管理员或特殊场景
   *
   * @param courseId 课程ID
   * @return 课时列表，按order_index排序
   */
  @Query(
      "SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.courseId = :courseId AND l.deletedAt IS NULL ORDER BY l.orderIndex ASC")
  List<Lesson> findByCourseIdAndNotDeletedUnlimited(@Param("courseId") UUID courseId);

  /**
   * 根据课程ID查找所有课时（排除已删除的课时）- 分页限制 使用JOIN FETCH避免N+1查询问题 防止内存溢出 使用索引：idx_lessons_course_order_deleted
   *
   * @param courseId 课程ID
   * @param pageable 分页参数
   * @return 课时列表，按order_index排序
   */
  @Query(
      "SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.courseId = :courseId AND l.deletedAt IS NULL ORDER BY l.orderIndex ASC")
  List<Lesson> findByCourseIdAndNotDeletedList(@Param("courseId") UUID courseId, Pageable pageable);

  /**
   * 根据课程ID查找所有课时（排除已删除的课时）- 不预加载Course 用于只需要课时基本信息的场景
   *
   * @param courseId 课程ID
   * @return 课时列表，按order_index排序
   */
  @Query(
      "SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.deletedAt IS NULL ORDER BY l.orderIndex ASC")
  List<Lesson> findByCourseIdAndNotDeletedWithoutCourse(@Param("courseId") UUID courseId);

  /**
   * 根据课程ID查找所有课时（排除已删除的课时）- 按orderIndex排序
   *
   * @param courseId 课程ID
   * @return 课时列表，按order_index排序
   */
  @Query(
      "SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.deletedAt IS NULL ORDER BY l.orderIndex ASC")
  List<Lesson> findByCourseIdAndNotDeletedOrderByOrderIndex(@Param("courseId") UUID courseId);

  /**
   * 根据ID查找课时（排除已删除的课时） 使用JOIN FETCH预加载Course关联
   *
   * @param id 课时ID
   * @return 课时对象（如果存在）
   */
  @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.id = :id AND l.deletedAt IS NULL")
  Optional<Lesson> findByIdAndNotDeleted(@Param("id") UUID id);

  /**
   * 根据ID查找课时（排除已删除的课时）- 不预加载Course 用于只需要课时基本信息的场景
   *
   * @param id 课时ID
   * @return 课时对象（如果存在）
   */
  @Query("SELECT l FROM Lesson l WHERE l.id = :id AND l.deletedAt IS NULL")
  Optional<Lesson> findByIdAndNotDeletedWithoutCourse(@Param("id") UUID id);

  /**
   * 根据课程ID和课时标题查找课时（排除已删除的课时） 使用JOIN FETCH预加载Course关联
   *
   * @param courseId 课程ID
   * @param title 课时标题
   * @return 课时对象（如果存在）
   */
  @Query(
      "SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.courseId = :courseId AND l.title = :title AND l.deletedAt IS NULL")
  Optional<Lesson> findByCourseIdAndTitleAndNotDeleted(
      @Param("courseId") UUID courseId, @Param("title") String title);

  /**
   * 获取指定课程中的最大order_index值
   *
   * @param courseId 课程ID
   * @return 最大order_index值，如果没有课时则返回0
   */
  @Query(
      "SELECT COALESCE(MAX(l.orderIndex), 0) FROM Lesson l WHERE l.courseId = :courseId AND l.deletedAt IS NULL")
  Integer findMaxOrderIndexByCourseId(@Param("courseId") UUID courseId);

  /**
   * 检查指定课程和order_index的课时是否存在（排除已删除的课时）
   *
   * @param courseId 课程ID
   * @param orderIndex 排序索引
   * @return 是否存在
   */
  @Query(
      "SELECT COUNT(l) > 0 FROM Lesson l WHERE l.courseId = :courseId AND l.orderIndex = :orderIndex AND l.deletedAt IS NULL")
  boolean existsByCourseIdAndOrderIndexAndNotDeleted(
      @Param("courseId") UUID courseId, @Param("orderIndex") Integer orderIndex);

  // ========== 分页查询方法 ==========

  /**
   * 分页查询指定课程的课时（排除已删除的课时） 使用JOIN FETCH预加载Course关联
   *
   * @param courseId 课程ID
   * @param pageable 分页参数
   * @return 分页的课时列表
   */
  @Query(
      "SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.courseId = :courseId AND l.deletedAt IS NULL")
  Page<Lesson> findByCourseIdAndNotDeleted(@Param("courseId") UUID courseId, Pageable pageable);

  /**
   * 分页查询指定课程的课时（排除已删除的课时）- 不预加载Course 用于只需要课时基本信息的场景
   *
   * @param courseId 课程ID
   * @param pageable 分页参数
   * @return 分页的课时列表
   */
  @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.deletedAt IS NULL")
  Page<Lesson> findByCourseIdAndNotDeletedWithoutCourse(
      @Param("courseId") UUID courseId, Pageable pageable);

  /**
   * 分页查询所有课时（排除已删除的课时） 使用JOIN FETCH预加载Course关联
   *
   * @param pageable 分页参数
   * @return 分页的课时列表
   */
  @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.deletedAt IS NULL")
  Page<Lesson> findAllNotDeleted(Pageable pageable);

  /**
   * 查询所有未删除的课时（分页限制） 防止内存溢出
   *
   * @param pageable 分页参数
   * @return 课时列表
   */
  @Query("SELECT l FROM Lesson l WHERE l.deletedAt IS NULL ORDER BY l.createdAt DESC")
  List<Lesson> findAllNotDeletedList(Pageable pageable);

  /**
   * 查询所有未删除的课时（无限制，谨慎使用） 仅用于管理员或特殊场景
   *
   * @return 课时列表
   */
  @Query("SELECT l FROM Lesson l WHERE l.deletedAt IS NULL ORDER BY l.createdAt DESC")
  List<Lesson> findAllNotDeletedUnlimited();

  // ========== 批量操作方法 ==========

  /**
   * 批量查询多个课程的课时（限制最大返回数量和输入参数数量） 防止内存溢出和SQL性能问题 使用索引：idx_lessons_course_order_deleted
   *
   * @param courseIds 课程ID列表（建议不超过50个）
   * @return 课时列表（最多2000条）
   */
  @Query(
      "SELECT l FROM Lesson l WHERE l.courseId IN :courseIds AND l.deletedAt IS NULL ORDER BY l.courseId, l.orderIndex ASC")
  List<Lesson> findByCourseIdsAndNotDeleted(
      @Param("courseIds") List<UUID> courseIds, Pageable pageable);

  /**
   * 批量查询多个课程的课时（无限制，谨慎使用） 仅用于管理员或特殊场景
   *
   * @param courseIds 课程ID列表
   * @return 课时列表
   */
  @Query(
      "SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.courseId IN :courseIds AND l.deletedAt IS NULL ORDER BY l.courseId, l.orderIndex ASC")
  List<Lesson> findByCourseIdsAndNotDeletedUnlimited(@Param("courseIds") List<UUID> courseIds);

  /**
   * 批量查询多个课时ID（限制最大返回数量和输入参数数量） 防止内存溢出和SQL性能问题
   *
   * @param lessonIds 课时ID列表（建议不超过200个）
   * @return 课时列表（最多1000条）
   */
  @Query(
      "SELECT l FROM Lesson l WHERE l.id IN :lessonIds AND l.deletedAt IS NULL ORDER BY l.createdAt DESC")
  List<Lesson> findByIdsAndNotDeleted(@Param("lessonIds") List<UUID> lessonIds, Pageable pageable);

  /**
   * 批量查询多个课时ID（无限制，谨慎使用） 仅用于管理员或特殊场景
   *
   * @param lessonIds 课时ID列表
   * @return 课时列表
   */
  @Query(
      "SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.id IN :lessonIds AND l.deletedAt IS NULL")
  List<Lesson> findByIdsAndNotDeletedUnlimited(@Param("lessonIds") List<UUID> lessonIds);

  // ========== 统计查询方法 ==========

  /**
   * 统计指定课程的课时数量（排除已删除的课时）
   *
   * @param courseId 课程ID
   * @return 课时数量
   */
  @Query("SELECT COUNT(l) FROM Lesson l WHERE l.courseId = :courseId AND l.deletedAt IS NULL")
  long countByCourseIdAndNotDeleted(@Param("courseId") UUID courseId);

  /**
   * 统计多个课程的课时数量（排除已删除的课时）
   *
   * @param courseIds 课程ID列表
   * @return 课时数量
   */
  @Query("SELECT COUNT(l) FROM Lesson l WHERE l.courseId IN :courseIds AND l.deletedAt IS NULL")
  long countByCourseIdsAndNotDeleted(@Param("courseIds") List<UUID> courseIds);

  /**
   * 根据标题模糊查询课时（分页限制） 防止内存溢出 使用索引：idx_lessons_title（前缀匹配更高效）
   *
   * @param title 标题关键词
   * @param pageable 分页参数
   * @return 课时列表
   */
  @Query(
      "SELECT l FROM Lesson l WHERE l.title LIKE CONCAT(:title, '%') AND l.deletedAt IS NULL ORDER BY l.createdAt DESC")
  List<Lesson> findByTitleContainingAndNotDeleted(@Param("title") String title, Pageable pageable);

  /**
   * 根据标题模糊查询课时（无限制，谨慎使用） 仅用于管理员或特殊场景
   *
   * @param title 标题关键词
   * @return 课时列表
   */
  @Query(
      "SELECT l FROM Lesson l WHERE l.title LIKE %:title% AND l.deletedAt IS NULL ORDER BY l.createdAt DESC")
  List<Lesson> findByTitleContainingAndNotDeletedUnlimited(@Param("title") String title);
}
