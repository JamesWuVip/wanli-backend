package com.wanli.backend.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.exception.ResourceNotFoundException;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.DatabaseUtil;
import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.PerformanceMonitor;
import com.wanli.backend.util.ValidationUtil;

/** 服务层基类 提供通用的服务层功能和最佳实践 包括缓存管理、性能监控、异常处理、日志记录等 */
public abstract class BaseService {

  protected final CacheUtil cacheUtil;

  protected BaseService(CacheUtil cacheUtil) {
    this.cacheUtil = cacheUtil;
  }

  /**
   * 执行带性能监控的操作
   *
   * @param operationName 操作名称
   * @param operation 操作逻辑
   * @param <T> 返回类型
   * @return 操作结果
   */
  protected <T> T executeWithMonitoring(String operationName, Supplier<T> operation) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.start(operationName)) {
      return operation.get();
    }
  }

  /**
   * 执行带事务和性能监控的操作
   *
   * @param operationName 操作名称
   * @param operation 操作逻辑
   * @param <T> 返回类型
   * @return 操作结果
   */
  @Transactional(rollbackFor = Exception.class)
  protected <T> T executeTransactionalWithMonitoring(String operationName, Supplier<T> operation) {
    return executeWithMonitoring(operationName, operation);
  }

  /**
   * 安全地根据ID查找实体
   *
   * @param entitySupplier 实体查询供应商
   * @param entityType 实体类型名称
   * @param entityId 实体ID
   * @param <T> 实体类型
   * @return 实体对象
   * @throws ResourceNotFoundException 当实体不存在时
   */
  protected <T> T findEntityById(
      Supplier<Optional<T>> entitySupplier, String entityType, UUID entityId) {
    return entitySupplier
        .get()
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    entityType.toUpperCase() + "_NOT_FOUND", entityType, entityId.toString()));
  }

  /**
   * 验证UUID格式
   *
   * @param id ID字符串
   * @param fieldName 字段名称
   * @throws BusinessException 当ID格式无效时
   */
  protected void validateUUID(String id, String fieldName) {
    if (!ValidationUtil.isValidUUID(id)) {
      throw new BusinessException(
          "INVALID_" + fieldName.toUpperCase() + "_ID", "无效的" + fieldName + "ID格式");
    }
  }

  /**
   * 验证必填字段
   *
   * @param value 字段值
   * @param fieldName 字段名称
   * @throws BusinessException 当字段为空时
   */
  protected void validateRequired(String value, String fieldName) {
    if (ValidationUtil.isBlank(value)) {
      throw new BusinessException("EMPTY_" + fieldName.toUpperCase(), fieldName + "不能为空");
    }
  }

  /**
   * 从缓存获取数据，如果不存在则从数据库加载并缓存
   *
   * @param cacheKey 缓存键
   * @param dataLoader 数据加载器
   * @param expireMinutes 过期时间（分钟）
   * @param <T> 数据类型
   * @return 数据对象
   */
  protected <T> T getFromCacheOrLoad(String cacheKey, Supplier<T> dataLoader, int expireMinutes) {
    T cachedData = cacheUtil.get(cacheKey);
    if (cachedData != null) {
      return cachedData;
    }

    T data = dataLoader.get();
    if (data != null) {
      cacheUtil.put(cacheKey, data, expireMinutes);
    }

    return data;
  }

  /**
   * 从缓存获取数据，如果不存在则从数据库加载并缓存（使用默认过期时间）
   *
   * @param cacheKey 缓存键
   * @param dataLoader 数据加载器
   * @param <T> 数据类型
   * @return 数据对象
   */
  protected <T> T getFromCacheOrLoad(String cacheKey, Supplier<T> dataLoader) {
    return getFromCacheOrLoad(cacheKey, dataLoader, 60); // 默认60分钟
  }

  /**
   * 清除缓存
   *
   * @param cacheKey 缓存键
   */
  protected void clearCache(String cacheKey) {
    cacheUtil.remove(cacheKey);
  }

  /**
   * 清除多个缓存
   *
   * @param cacheKeys 缓存键数组
   */
  protected void clearCaches(String... cacheKeys) {
    for (String cacheKey : cacheKeys) {
      clearCache(cacheKey);
    }
  }

  /**
   * 记录业务操作日志
   *
   * @param operation 操作名称
   * @param userId 用户ID
   * @param details 操作详情
   */
  protected void logBusinessOperation(String operation, String userId, Object details) {
    if (details instanceof String) {
      LogUtil.logBusinessOperation(operation, userId, (String) details);
    } else if (details instanceof Map) {
      LogUtil.logBusinessOperation(operation, userId, (Map<String, Object>) details);
    } else {
      LogUtil.logBusinessOperation(operation, userId, details != null ? details.toString() : null);
    }
  }

  /**
   * 记录安全相关日志
   *
   * @param event 安全事件
   * @param userId 用户ID
   * @param ipAddress IP地址
   * @param details 事件详情
   */
  protected void logSecurityEvent(String event, String userId, String ipAddress, String details) {
    LogUtil.logSecurity(event, userId, ipAddress, details);
  }

  /**
   * 记录错误日志
   *
   * @param operation 操作名称
   * @param userId 用户ID
   * @param errorCode 错误码
   * @param message 错误消息
   * @param exception 异常对象
   */
  protected void logError(
      String operation, String userId, String errorCode, String message, Exception exception) {
    LogUtil.logError(operation, userId, errorCode, message, exception);
  }

  /**
   * 安全地保存实体
   *
   * @param repository 仓库对象
   * @param entity 实体对象
   * @param operationName 操作名称
   * @param <T> 实体类型
   * @param <R> 仓库类型
   * @return 保存后的实体
   */
  protected <T, R extends JpaRepository<T, ?>> T saveSafely(
      R repository, T entity, String operationName) {
    return DatabaseUtil.saveSafely(repository, entity, operationName, "system");
  }

  /**
   * 构建缓存键
   *
   * @param prefix 前缀
   * @param suffix 后缀
   * @return 缓存键
   */
  protected String buildCacheKey(String prefix, String suffix) {
    return prefix + suffix;
  }

  /**
   * 构建缓存键
   *
   * @param prefix 前缀
   * @param id UUID类型的ID
   * @return 缓存键
   */
  protected String buildCacheKey(String prefix, UUID id) {
    return prefix + id.toString();
  }
}
