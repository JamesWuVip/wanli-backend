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

import com.wanli.backend.entity.Course;

/** 课程数据访问层接口 提供课程相关的数据库操作方法 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

  /**
   * 查找所有未删除的课程（限制最大返回数量） 防止内存溢出，建议使用分页查询
   *
   * @return 课程列表（最多1000条）
   */
  @Query(value = "SELECT c FROM Course c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findAllNotDeletedList(Pageable pageable);

  /**
   * 查找所有未删除的课程（无限制，谨慎使用） 仅用于管理员或特殊场景
   *
   * @return 课程列表
   */
  @Query("SELECT c FROM Course c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findAllNotDeletedUnlimited();

  /**
   * 根据ID查找课程（排除已删除的课程）
   *
   * @param id 课程ID
   * @return 课程对象（如果存在）
   */
  @Query("SELECT c FROM Course c WHERE c.id = :id AND c.deletedAt IS NULL")
  Optional<Course> findByIdAndNotDeleted(@Param("id") UUID id);

  /**
   * 根据创建者ID查询课程（分页限制） 防止内存溢出 使用索引：idx_courses_creator_status_deleted
   *
   * @param creatorId 创建者ID
   * @param pageable 分页参数
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.creatorId = :creatorId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByCreatorIdAndNotDeletedList(
      @Param("creatorId") UUID creatorId, Pageable pageable);

  /**
   * 根据创建者ID查找课程（排除已删除的课程，无限制） 仅用于管理员或特殊场景
   *
   * @param creatorId 创建者ID
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.creatorId = :creatorId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByCreatorIdAndNotDeletedUnlimited(@Param("creatorId") UUID creatorId);

  /**
   * 根据状态查询课程（分页限制） 防止内存溢出 使用索引：idx_courses_status_created_deleted
   *
   * @param status 课程状态
   * @param pageable 分页参数
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.status = :status AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByStatusAndNotDeletedList(@Param("status") String status, Pageable pageable);

  /**
   * 根据状态查找课程（排除已删除的课程，无限制） 仅用于管理员或特殊场景
   *
   * @param status 课程状态
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.status = :status AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByStatusAndNotDeletedUnlimited(@Param("status") String status);

  /**
   * 根据标题模糊查询课程（分页限制） 防止内存溢出 使用索引：idx_courses_title（前缀匹配更高效）
   *
   * @param title 标题关键词
   * @param pageable 分页参数
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.title LIKE CONCAT(:title, '%') AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByTitleContainingAndNotDeletedList(
      @Param("title") String title, Pageable pageable);

  /**
   * 根据标题模糊查找课程（排除已删除的课程，无限制） 仅用于管理员或特殊场景
   *
   * @param title 课程标题关键词
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.title LIKE %:title% AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByTitleContainingAndNotDeletedUnlimited(@Param("title") String title);

  // ========== 分页查询方法 ==========

  /**
   * 分页查询所有未删除的课程
   *
   * @param pageable 分页参数
   * @return 分页的课程列表
   */
  @Query("SELECT c FROM Course c WHERE c.deletedAt IS NULL")
  Page<Course> findAllNotDeleted(Pageable pageable);

  /**
   * 分页查询指定创建者的课程（排除已删除的课程）
   *
   * @param creatorId 创建者ID
   * @param pageable 分页参数
   * @return 分页的课程列表
   */
  @Query("SELECT c FROM Course c WHERE c.creatorId = :creatorId AND c.deletedAt IS NULL")
  Page<Course> findByCreatorIdAndNotDeleted(@Param("creatorId") UUID creatorId, Pageable pageable);

  /**
   * 分页查询指定状态的课程（排除已删除的课程）
   *
   * @param status 课程状态
   * @param pageable 分页参数
   * @return 分页的课程列表
   */
  @Query("SELECT c FROM Course c WHERE c.status = :status AND c.deletedAt IS NULL")
  Page<Course> findByStatusAndNotDeleted(@Param("status") String status, Pageable pageable);

  /**
   * 分页查询标题包含关键词的课程（排除已删除的课程）
   *
   * @param title 课程标题关键词
   * @param pageable 分页参数
   * @return 分页的课程列表
   */
  @Query("SELECT c FROM Course c WHERE c.title LIKE %:title% AND c.deletedAt IS NULL")
  Page<Course> findByTitleContainingAndNotDeleted(@Param("title") String title, Pageable pageable);

  // ========== 批量操作方法 ==========

  /**
   * 批量查询多个课程ID（限制最大返回数量和输入参数数量） 防止内存溢出和SQL性能问题
   *
   * @param courseIds 课程ID列表（建议不超过100个）
   * @return 课程列表（最多500条）
   */
  @Query(
      "SELECT c FROM Course c WHERE c.id IN :courseIds AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByIdsAndNotDeleted(@Param("courseIds") List<UUID> courseIds, Pageable pageable);

  /**
   * 批量查询多个课程ID（无限制，谨慎使用） 仅用于管理员或特殊场景
   *
   * @param courseIds 课程ID列表
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.id IN :courseIds AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByIdsAndNotDeletedUnlimited(@Param("courseIds") List<UUID> courseIds);

  /**
   * 批量查询多个创建者的课程（限制最大返回数量和输入参数数量） 防止内存溢出和SQL性能问题
   *
   * @param creatorIds 创建者ID列表（建议不超过50个）
   * @return 课程列表（最多1000条）
   */
  @Query(
      "SELECT c FROM Course c WHERE c.creatorId IN :creatorIds AND c.deletedAt IS NULL ORDER BY c.creatorId, c.createdAt DESC")
  List<Course> findByCreatorIdsAndNotDeleted(
      @Param("creatorIds") List<UUID> creatorIds, Pageable pageable);

  /**
   * 批量查询多个创建者的课程（无限制，谨慎使用） 仅用于管理员或特殊场景
   *
   * @param creatorIds 创建者ID列表
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.creatorId IN :creatorIds AND c.deletedAt IS NULL ORDER BY c.creatorId, c.createdAt DESC")
  List<Course> findByCreatorIdsAndNotDeletedUnlimited(@Param("creatorIds") List<UUID> creatorIds);

  // ========== 统计查询方法 ==========

  /**
   * 统计指定创建者的课程数量（排除已删除的课程）
   *
   * @param creatorId 创建者ID
   * @return 课程数量
   */
  @Query("SELECT COUNT(c) FROM Course c WHERE c.creatorId = :creatorId AND c.deletedAt IS NULL")
  long countByCreatorIdAndNotDeleted(@Param("creatorId") UUID creatorId);

  /**
   * 统计指定状态的课程数量（排除已删除的课程）
   *
   * @param status 课程状态
   * @return 课程数量
   */
  @Query("SELECT COUNT(c) FROM Course c WHERE c.status = :status AND c.deletedAt IS NULL")
  long countByStatusAndNotDeleted(@Param("status") String status);

  /**
   * 统计所有未删除的课程数量
   *
   * @return 课程数量
   */
  @Query("SELECT COUNT(c) FROM Course c WHERE c.deletedAt IS NULL")
  long countAllNotDeleted();

  // ========== 高级查询方法 ==========

  /**
   * 查询最近创建的课程（限制数量）
   *
   * @param limit 限制数量
   * @return 课程列表
   */
  @Query(
      value = "SELECT c FROM Course c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC",
      nativeQuery = false)
  List<Course> findRecentCourses(Pageable pageable);

  /**
   * 根据多个条件查询课程（支持分页）
   *
   * @param creatorId 创建者ID（可选）
   * @param status 状态（可选）
   * @param titleKeyword 标题关键词（可选）
   * @param pageable 分页参数
   * @return 分页的课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE "
          + "(:creatorId IS NULL OR c.creatorId = :creatorId) AND "
          + "(:status IS NULL OR c.status = :status) AND "
          + "(:titleKeyword IS NULL OR c.title LIKE %:titleKeyword%) AND "
          + "c.deletedAt IS NULL")
  Page<Course> findByMultipleConditions(
      @Param("creatorId") UUID creatorId,
      @Param("status") String status,
      @Param("titleKeyword") String titleKeyword,
      Pageable pageable);
}
