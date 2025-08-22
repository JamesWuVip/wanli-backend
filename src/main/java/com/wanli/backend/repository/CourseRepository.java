package com.wanli.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wanli.backend.entity.Course;

/** 课程数据访问层接口 提供课程相关的数据库操作方法 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

  /**
   * 查找所有未删除的课程
   *
   * @return 课程列表
   */
  @Query("SELECT c FROM Course c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findAllNotDeleted();

  /**
   * 根据ID查找课程（排除已删除的课程）
   *
   * @param id 课程ID
   * @return 课程对象（如果存在）
   */
  @Query("SELECT c FROM Course c WHERE c.id = :id AND c.deletedAt IS NULL")
  Optional<Course> findByIdAndNotDeleted(@Param("id") UUID id);

  /**
   * 根据创建者ID查找课程（排除已删除的课程）
   *
   * @param creatorId 创建者ID
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.creatorId = :creatorId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByCreatorIdAndNotDeleted(@Param("creatorId") UUID creatorId);

  /**
   * 根据状态查找课程（排除已删除的课程）
   *
   * @param status 课程状态
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.status = :status AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByStatusAndNotDeleted(@Param("status") String status);

  /**
   * 根据标题模糊查找课程（排除已删除的课程）
   *
   * @param title 课程标题关键词
   * @return 课程列表
   */
  @Query(
      "SELECT c FROM Course c WHERE c.title LIKE %:title% AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
  List<Course> findByTitleContainingAndNotDeleted(@Param("title") String title);
}
