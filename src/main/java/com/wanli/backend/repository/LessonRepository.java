package com.wanli.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wanli.backend.entity.Lesson;

/** 课时数据访问层接口 提供课时相关的数据库操作方法 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

  /**
   * 根据课程ID查找所有课时（排除已删除的课时）
   *
   * @param courseId 课程ID
   * @return 课时列表，按order_index排序
   */
  @Query(
      "SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.deletedAt IS NULL ORDER BY l.orderIndex ASC")
  List<Lesson> findByCourseIdAndNotDeleted(@Param("courseId") UUID courseId);

  /**
   * 根据ID查找课时（排除已删除的课时）
   *
   * @param id 课时ID
   * @return 课时对象（如果存在）
   */
  @Query("SELECT l FROM Lesson l WHERE l.id = :id AND l.deletedAt IS NULL")
  Optional<Lesson> findByIdAndNotDeleted(@Param("id") UUID id);

  /**
   * 根据课程ID和课时标题查找课时（排除已删除的课时）
   *
   * @param courseId 课程ID
   * @param title 课时标题
   * @return 课时对象（如果存在）
   */
  @Query(
      "SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.title = :title AND l.deletedAt IS NULL")
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
}
