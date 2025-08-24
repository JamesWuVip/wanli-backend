package com.wanli.backend.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** 数据库工具类 提供数据库操作的性能监控和优化功能 */
public class DatabaseUtil {

  /** 执行数据库查询操作并监控性能 */
  public static <T> T executeQuery(
      String operation, String tableName, String userId, Supplier<T> querySupplier) {
    long startTime = System.currentTimeMillis();
    LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_QUERY_" + operation);

    try {
      T result = querySupplier.get();
      monitor.end();

      // 记录数据库操作日志
      long duration = System.currentTimeMillis() - startTime;
      LogUtil.logDatabaseOperation(operation, tableName, userId, duration);

      return result;
    } catch (Exception e) {
      monitor.end();
      LogUtil.logError("DB_QUERY_" + operation, userId, "DB_ERROR", e.getMessage(), e);
      throw e;
    }
  }

  /** 执行数据库更新操作并监控性能 */
  public static <T> T executeUpdate(
      String operation, String tableName, String userId, Supplier<T> updateSupplier) {
    long startTime = System.currentTimeMillis();
    LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_UPDATE_" + operation);

    try {
      T result = updateSupplier.get();
      monitor.end();

      // 记录数据库操作日志
      long duration = System.currentTimeMillis() - startTime;
      LogUtil.logDatabaseOperation(operation, tableName, userId, duration);

      return result;
    } catch (Exception e) {
      monitor.end();
      LogUtil.logError("DB_UPDATE_" + operation, userId, "DB_ERROR", e.getMessage(), e);
      throw e;
    }
  }

  /** 安全地根据ID查找实体 */
  public static <T, ID> Optional<T> findByIdSafely(
      JpaRepository<T, ID> repository, ID id, String entityName, String userId) {
    return executeQuery("FIND_BY_ID", entityName, userId, () -> repository.findById(id));
  }

  /** 安全地执行查询操作 */
  public static <T> T findByIdSafely(Supplier<T> querySupplier, String operationName) {
    return executeQuery(operationName, "CUSTOM_QUERY", null, querySupplier);
  }

  /** 安全地保存实体 */
  public static <T> T saveSafely(
      JpaRepository<T, ?> repository, T entity, String entityName, String userId) {
    return executeUpdate("SAVE", entityName, userId, () -> repository.save(entity));
  }

  /** 安全地保存并刷新实体 */
  public static <T> T saveAndFlushSafely(
      JpaRepository<T, ?> repository, T entity, String entityName, String userId) {
    return executeUpdate(
        "SAVE_AND_FLUSH", entityName, userId, () -> repository.saveAndFlush(entity));
  }

  /** 安全地删除实体 */
  public static <T, ID> void deleteByIdSafely(
      JpaRepository<T, ID> repository, ID id, String entityName, String userId) {
    executeUpdate(
        "DELETE_BY_ID",
        entityName,
        userId,
        () -> {
          repository.deleteById(id);
          return null;
        });
  }

  /** 安全地查找所有实体 */
  public static <T> List<T> findAllSafely(
      JpaRepository<T, ?> repository, String entityName, String userId) {
    return executeQuery("FIND_ALL", entityName, userId, repository::findAll);
  }

  /** 安全地分页查询实体 */
  public static <T> Page<T> findAllSafely(
      JpaRepository<T, ?> repository, Pageable pageable, String entityName, String userId) {
    return executeQuery(
        "FIND_ALL_PAGEABLE", entityName, userId, () -> repository.findAll(pageable));
  }

  /** 安全地统计实体数量 */
  public static <T> long countSafely(
      JpaRepository<T, ?> repository, String entityName, String userId) {
    return executeQuery("COUNT", entityName, userId, repository::count);
  }

  /** 安全地检查实体是否存在 */
  public static <T, ID> boolean existsByIdSafely(
      JpaRepository<T, ID> repository, ID id, String entityName, String userId) {
    return executeQuery("EXISTS_BY_ID", entityName, userId, () -> repository.existsById(id));
  }

  /** 批量保存实体 */
  public static <T> List<T> saveAllSafely(
      JpaRepository<T, ?> repository, Iterable<T> entities, String entityName, String userId) {
    return executeUpdate("SAVE_ALL", entityName, userId, () -> repository.saveAll(entities));
  }

  /** 批量删除实体 */
  public static <T> void deleteAllSafely(
      JpaRepository<T, ?> repository, Iterable<T> entities, String entityName, String userId) {
    executeUpdate(
        "DELETE_ALL",
        entityName,
        userId,
        () -> {
          repository.deleteAll(entities);
          return null;
        });
  }

  /** 事务性操作包装器 */
  public static <T> T executeInTransaction(
      String operation, String userId, Supplier<T> transactionSupplier) {
    LogUtil.PerformanceMonitor monitor =
        LogUtil.startPerformanceMonitor("TRANSACTION_" + operation);

    try {
      T result = transactionSupplier.get();
      monitor.end();

      LogUtil.logBusinessOperation("TRANSACTION_" + operation, userId, "执行成功");
      return result;
    } catch (Exception e) {
      monitor.end();
      LogUtil.logError("TRANSACTION_" + operation, userId, "TRANSACTION_ERROR", e.getMessage(), e);
      throw e;
    }
  }

  // 实例方法版本，用于测试
  public boolean isHealthy(JpaRepository<?, ?> repository) {
    try {
      LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_HEALTH_CHECK");
      long count = repository.count();
      monitor.end();
      return true;
    } catch (Exception e) {
      LogUtil.logError("DB_HEALTH_CHECK", "", "DB_HEALTH_ERROR", e.getMessage(), e);
      return false;
    }
  }

  public <T, ID> Optional<T> findByIdSafely(JpaRepository<T, ID> repository, ID id) {
    try {
      if (id == null) {
        return Optional.empty();
      }
      return repository.findById(id);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public <T> Optional<T> saveSafely(JpaRepository<T, ?> repository, T entity) {
    try {
      if (entity == null) {
        return Optional.empty();
      }
      LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_SAVE");
      T saved = repository.save(entity);
      monitor.end();
      return Optional.of(saved);
    } catch (Exception e) {
      LogUtil.logError("DB_SAVE", "", "DB_SAVE_ERROR", e.getMessage(), e);
      return Optional.empty();
    }
  }

  public <T> Optional<T> saveAndFlushSafely(JpaRepository<T, ?> repository, T entity) {
    try {
      if (entity == null) {
        return Optional.empty();
      }
      LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_SAVE_FLUSH");
      T saved = repository.saveAndFlush(entity);
      monitor.end();
      return Optional.of(saved);
    } catch (Exception e) {
      LogUtil.logError("DB_SAVE_FLUSH", "", "DB_SAVE_FLUSH_ERROR", e.getMessage(), e);
      return Optional.empty();
    }
  }

  public <T, ID> boolean deleteByIdSafely(JpaRepository<T, ID> repository, ID id) {
    try {
      if (id == null) {
        return false;
      }
      LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_DELETE");
      repository.deleteById(id);
      monitor.end();
      return true;
    } catch (Exception e) {
      LogUtil.logError("DB_DELETE", "", "DB_DELETE_ERROR", e.getMessage(), e);
      return false;
    }
  }

  public <T> List<T> findAllSafely(JpaRepository<T, ?> repository) {
    try {
      return repository.findAll();
    } catch (Exception e) {
      return List.of();
    }
  }

  public <T> long countSafely(JpaRepository<T, ?> repository) {
    try {
      return repository.count();
    } catch (Exception e) {
      return 0L;
    }
  }

  public <T, ID> boolean existsByIdSafely(JpaRepository<T, ID> repository, ID id) {
    try {
      if (id == null) {
        return false;
      }
      LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_EXISTS");
      boolean exists = repository.existsById(id);
      monitor.end();
      return exists;
    } catch (Exception e) {
      LogUtil.logError("DB_EXISTS", "", "DB_EXISTS_ERROR", e.getMessage(), e);
      return false;
    }
  }

  public <T> List<T> saveAllSafely(JpaRepository<T, ?> repository, Iterable<T> entities) {
    try {
      if (entities == null) {
        return new ArrayList<>();
      }
      LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_SAVE_ALL");
      List<T> saved = repository.saveAll(entities);
      monitor.end();
      return saved;
    } catch (Exception e) {
      LogUtil.logError("DB_SAVE_ALL", "", "DB_SAVE_ALL_ERROR", e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  public <T> boolean deleteAllSafely(JpaRepository<T, ?> repository, Iterable<T> entities) {
    try {
      if (entities == null) {
        return false;
      }
      LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("DB_DELETE_ALL");
      repository.deleteAll(entities);
      monitor.end();
      return true;
    } catch (Exception e) {
      LogUtil.logError("DB_DELETE_ALL", "", "DB_DELETE_ALL_ERROR", e.getMessage(), e);
      return false;
    }
  }

  public DatabaseStats getStats() {
    return new DatabaseStats(0L, 0L, 0.0, 0.0);
  }

  /** 获取数据库性能统计 */
  public static class DatabaseStats {
    private final long totalQueries;
    private final long totalUpdates;
    private final double averageQueryTime;
    private final double averageUpdateTime;

    public DatabaseStats(
        long totalQueries, long totalUpdates, double averageQueryTime, double averageUpdateTime) {
      this.totalQueries = totalQueries;
      this.totalUpdates = totalUpdates;
      this.averageQueryTime = averageQueryTime;
      this.averageUpdateTime = averageUpdateTime;
    }

    public long getTotalQueries() {
      return totalQueries;
    }

    public long getQueryCount() {
      return totalQueries;
    }

    public long getTotalUpdates() {
      return totalUpdates;
    }

    public long getUpdateCount() {
      return totalUpdates;
    }

    public double getAverageQueryTime() {
      return averageQueryTime;
    }

    public double getAverageUpdateTime() {
      return averageUpdateTime;
    }

    @Override
    public String toString() {
      return String.format(
          "DatabaseStats{queries=%d, updates=%d, avgQueryTime=%.2fms, avgUpdateTime=%.2fms}",
          totalQueries, totalUpdates, averageQueryTime, averageUpdateTime);
    }
  }
}
