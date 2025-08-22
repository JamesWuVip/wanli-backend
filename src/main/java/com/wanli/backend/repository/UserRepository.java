package com.wanli.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wanli.backend.entity.User;

/** 用户数据访问层接口 提供用户相关的数据库操作方法 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  /**
   * 根据用户名查找用户（排除已删除的用户）
   *
   * @param username 用户名
   * @return 用户对象（如果存在）
   */
  @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
  Optional<User> findByUsername(@Param("username") String username);

  /**
   * 根据邮箱查找用户（排除已删除的用户）
   *
   * @param email 邮箱
   * @return 用户对象（如果存在）
   */
  @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
  Optional<User> findByEmail(@Param("email") String email);

  /**
   * 检查用户名是否已存在（排除已删除的用户）
   *
   * @param username 用户名
   * @return 是否存在
   */
  @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
  boolean existsByUsername(@Param("username") String username);

  /**
   * 检查邮箱是否已存在（排除已删除的用户）
   *
   * @param email 邮箱
   * @return 是否存在
   */
  @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
  boolean existsByEmail(@Param("email") String email);

  /**
   * 根据ID查找用户（排除已删除的用户）
   *
   * @param id 用户ID
   * @return 用户对象（如果存在）
   */
  @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
  Optional<User> findByIdAndNotDeleted(@Param("id") UUID id);

  /**
   * 检查用户名或邮箱是否已存在（排除已删除的用户）
   *
   * @param username 用户名
   * @param email 邮箱
   * @return 是否存在
   */
  @Query(
      "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE (u.username = :username OR u.email = :email) AND u.deletedAt IS NULL")
  boolean existsByUsernameOrEmailAndDeletedAtIsNull(
      @Param("username") String username, @Param("email") String email);

  /**
   * 根据ID查找未删除的用户
   *
   * @param id 用户ID
   * @return 用户信息
   */
  Optional<User> findByIdAndDeletedAtIsNull(UUID id);
}
