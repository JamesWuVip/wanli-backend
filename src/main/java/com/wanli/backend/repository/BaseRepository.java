package com.wanli.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import com.wanli.backend.entity.BaseEntity;

/**
 * 基础Repository接口 提供通用的数据访问方法
 *
 * @param <T> 实体类型，必须继承BaseEntity
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, UUID> {

  /** 根据ID查找未删除的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
  Optional<T> findByIdAndNotDeleted(@Param("id") UUID id);

  /** 查找所有未删除的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
  List<T> findAllNotDeleted();

  /** 分页查找所有未删除的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
  Page<T> findAllNotDeleted(Pageable pageable);

  /** 根据创建者ID查找未删除的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.createdBy = :createdBy AND e.deleted = false")
  List<T> findByCreatedByAndNotDeleted(@Param("createdBy") UUID createdBy);

  /** 根据创建者ID分页查找未删除的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.createdBy = :createdBy AND e.deleted = false")
  Page<T> findByCreatedByAndNotDeleted(@Param("createdBy") UUID createdBy, Pageable pageable);

  /** 根据创建时间范围查找未删除的实体 */
  @Query(
      "SELECT e FROM #{#entityName} e WHERE e.createdAt BETWEEN :startTime AND :endTime AND e.deleted = false")
  List<T> findByCreatedAtBetweenAndNotDeleted(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  /** 根据更新时间范围查找未删除的实体 */
  @Query(
      "SELECT e FROM #{#entityName} e WHERE e.updatedAt BETWEEN :startTime AND :endTime AND e.deleted = false")
  List<T> findByUpdatedAtBetweenAndNotDeleted(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  /** 统计未删除的实体数量 */
  @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false")
  long countNotDeleted();

  /** 根据创建者统计未删除的实体数量 */
  @Query(
      "SELECT COUNT(e) FROM #{#entityName} e WHERE e.createdBy = :createdBy AND e.deleted = false")
  long countByCreatedByAndNotDeleted(@Param("createdBy") UUID createdBy);

  /** 软删除实体 */
  @Modifying
  @Query(
      "UPDATE #{#entityName} e SET e.deleted = true, e.updatedAt = :updatedAt, e.updatedBy = :updatedBy WHERE e.id = :id")
  int softDeleteById(
      @Param("id") UUID id,
      @Param("updatedAt") LocalDateTime updatedAt,
      @Param("updatedBy") UUID updatedBy);

  /** 批量软删除实体 */
  @Modifying
  @Query(
      "UPDATE #{#entityName} e SET e.deleted = true, e.updatedAt = :updatedAt, e.updatedBy = :updatedBy WHERE e.id IN :ids")
  int softDeleteByIds(
      @Param("ids") List<UUID> ids,
      @Param("updatedAt") LocalDateTime updatedAt,
      @Param("updatedBy") UUID updatedBy);

  /** 恢复软删除的实体 */
  @Modifying
  @Query(
      "UPDATE #{#entityName} e SET e.deleted = false, e.updatedAt = :updatedAt, e.updatedBy = :updatedBy WHERE e.id = :id")
  int restoreById(
      @Param("id") UUID id,
      @Param("updatedAt") LocalDateTime updatedAt,
      @Param("updatedBy") UUID updatedBy);

  /** 检查实体是否存在且未删除 */
  @Query(
      "SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
  boolean existsByIdAndNotDeleted(@Param("id") UUID id);

  /** 根据版本号更新实体（乐观锁） */
  @Modifying
  @Query(
      "UPDATE #{#entityName} e SET e.version = e.version + 1, e.updatedAt = :updatedAt, e.updatedBy = :updatedBy WHERE e.id = :id AND e.version = :version")
  int updateVersionById(
      @Param("id") UUID id,
      @Param("version") Long version,
      @Param("updatedAt") LocalDateTime updatedAt,
      @Param("updatedBy") UUID updatedBy);

  /** 查找最近创建的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false ORDER BY e.createdAt DESC")
  List<T> findRecentlyCreated(Pageable pageable);

  /** 查找最近更新的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false ORDER BY e.updatedAt DESC")
  List<T> findRecentlyUpdated(Pageable pageable);

  /** 根据多个创建者ID查找实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.createdBy IN :createdByList AND e.deleted = false")
  List<T> findByCreatedByInAndNotDeleted(@Param("createdByList") List<UUID> createdByList);

  /** 查找指定时间之前创建的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt < :beforeTime AND e.deleted = false")
  List<T> findCreatedBefore(@Param("beforeTime") LocalDateTime beforeTime);

  /** 查找指定时间之后创建的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt > :afterTime AND e.deleted = false")
  List<T> findCreatedAfter(@Param("afterTime") LocalDateTime afterTime);

  /** 查找指定时间之前更新的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt < :beforeTime AND e.deleted = false")
  List<T> findUpdatedBefore(@Param("beforeTime") LocalDateTime beforeTime);

  /** 查找指定时间之后更新的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt > :afterTime AND e.deleted = false")
  List<T> findUpdatedAfter(@Param("afterTime") LocalDateTime afterTime);

  /** 批量更新实体的更新时间和更新者 */
  @Modifying
  @Query(
      "UPDATE #{#entityName} e SET e.updatedAt = :updatedAt, e.updatedBy = :updatedBy WHERE e.id IN :ids")
  int batchUpdateTimestamp(
      @Param("ids") List<UUID> ids,
      @Param("updatedAt") LocalDateTime updatedAt,
      @Param("updatedBy") UUID updatedBy);

  /** 物理删除已软删除的实体（清理数据） */
  @Modifying
  @Query("DELETE FROM #{#entityName} e WHERE e.deleted = true AND e.updatedAt < :beforeTime")
  int physicalDeleteSoftDeleted(@Param("beforeTime") LocalDateTime beforeTime);

  /** 查找所有已删除的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
  List<T> findAllDeleted();

  /** 分页查找所有已删除的实体 */
  @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
  Page<T> findAllDeleted(Pageable pageable);

  /** 统计已删除的实体数量 */
  @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = true")
  long countDeleted();
}
