package com.wanli.backend.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.LogUtil;

/** 缓存管理器 提供统一的缓存管理功能，包括缓存策略、过期时间管理、缓存预热等 */
@Component
public class CacheManager {

  private final CacheUtil cacheUtil;

  // 缓存过期时间配置（分钟）
  private static final int SHORT_CACHE_EXPIRE = 5; // 短期缓存：5分钟
  private static final int MEDIUM_CACHE_EXPIRE = 30; // 中期缓存：30分钟
  private static final int LONG_CACHE_EXPIRE = 120; // 长期缓存：2小时
  private static final int PERSISTENT_CACHE_EXPIRE = 1440; // 持久缓存：24小时

  public CacheManager(CacheUtil cacheUtil) {
    this.cacheUtil = cacheUtil;
  }

  /** 缓存策略枚举 */
  public enum CacheStrategy {
    SHORT(SHORT_CACHE_EXPIRE), // 频繁变化的数据
    MEDIUM(MEDIUM_CACHE_EXPIRE), // 一般业务数据
    LONG(LONG_CACHE_EXPIRE), // 相对稳定的数据
    PERSISTENT(PERSISTENT_CACHE_EXPIRE); // 基础配置数据

    private final int expireMinutes;

    CacheStrategy(int expireMinutes) {
      this.expireMinutes = expireMinutes;
    }

    public int getExpireMinutes() {
      return expireMinutes;
    }
  }

  /**
   * 获取缓存数据，如果不存在则加载并缓存 包含防缓存穿透、击穿优化
   *
   * @param cacheKey 缓存键
   * @param dataLoader 数据加载器
   * @param strategy 缓存策略
   * @param <T> 数据类型
   * @return 缓存数据
   */
  @SuppressWarnings("unchecked")
  public <T> T getOrLoad(String cacheKey, Supplier<T> dataLoader, CacheStrategy strategy) {
    try {
      // 使用CacheUtil的优化方法，包含防穿透和击穿机制
      return cacheUtil.getOrCompute(cacheKey, dataLoader, strategy.getExpireMinutes());

    } catch (Exception e) {
      LogUtil.logError("CACHE_ERROR", "", "CACHE_OPERATION_FAILED", "缓存操作失败: " + cacheKey, e);
      // 缓存操作失败时，直接从数据源加载
      return dataLoader.get();
    }
  }

  /**
   * 获取缓存数据，如果不存在则加载并缓存（指定类型） 包含防缓存穿透、击穿优化
   *
   * @param cacheKey 缓存键
   * @param dataLoader 数据加载器
   * @param strategy 缓存策略
   * @param type 数据类型
   * @param <T> 数据类型
   * @return 缓存数据
   */
  @SuppressWarnings("unchecked")
  public <T> T getOrLoad(
      String cacheKey, Supplier<T> dataLoader, CacheStrategy strategy, Class<T> type) {
    try {
      // 使用CacheUtil的优化方法，包含防穿透和击穿机制
      return cacheUtil.getOrCompute(cacheKey, type, dataLoader, strategy.getExpireMinutes());

    } catch (Exception e) {
      LogUtil.logError("CACHE_ERROR", "", "CACHE_OPERATION_FAILED", "缓存操作失败: " + cacheKey, e);
      // 缓存操作失败时，直接从数据源加载
      return dataLoader.get();
    }
  }

  /**
   * 存储数据到缓存
   *
   * @param cacheKey 缓存键
   * @param data 数据
   * @param strategy 缓存策略
   * @param <T> 数据类型
   */
  public <T> void put(String cacheKey, T data, CacheStrategy strategy) {
    if (data != null) {
      cacheUtil.put(cacheKey, data, strategy.getExpireMinutes());
    }
  }

  /**
   * 存储数据到缓存（使用默认中期策略）
   *
   * @param cacheKey 缓存键
   * @param data 数据
   * @param <T> 数据类型
   */
  public <T> void put(String cacheKey, T data) {
    put(cacheKey, data, CacheStrategy.MEDIUM);
  }

  /**
   * 获取缓存数据
   *
   * @param cacheKey 缓存键
   * @param <T> 数据类型
   * @return 缓存数据，如果不存在返回null
   */
  public <T> T get(String cacheKey) {
    return cacheUtil.get(cacheKey);
  }

  /**
   * 获取缓存数据（指定类型）
   *
   * @param cacheKey 缓存键
   * @param type 数据类型
   * @param <T> 数据类型
   * @return 缓存数据，如果不存在返回null
   */
  public <T> T get(String cacheKey, Class<T> type) {
    return cacheUtil.get(cacheKey, type);
  }

  /**
   * 删除缓存
   *
   * @param cacheKey 缓存键
   */
  public void evict(String cacheKey) {
    cacheUtil.remove(cacheKey);
    java.util.Map<String, Object> context = new java.util.HashMap<>();
    context.put("cacheKey", cacheKey);
    LogUtil.logBusiness("CACHE_EVICT", context);
  }

  /**
   * 批量删除缓存
   *
   * @param cacheKeys 缓存键数组
   */
  public void evictAll(String... cacheKeys) {
    for (String cacheKey : cacheKeys) {
      evict(cacheKey);
    }
  }

  /**
   * 清除匹配模式的缓存
   *
   * @param pattern 缓存键模式
   */
  public void evictByPattern(String pattern) {
    try {
      cacheUtil.removeByPattern(pattern);
      java.util.Map<String, Object> context = new java.util.HashMap<>();
      context.put("pattern", pattern);
      LogUtil.logBusiness("CACHE_EVICT_PATTERN", context);
    } catch (Exception e) {
      LogUtil.logError("CACHE_ERROR", "", "CACHE_EVICT_FAILED", "模式缓存清除失败: " + pattern, e);
    }
  }

  /**
   * 检查缓存是否存在
   *
   * @param cacheKey 缓存键
   * @return 是否存在
   */
  public boolean exists(String cacheKey) {
    return cacheUtil.exists(cacheKey);
  }

  /**
   * 设置缓存过期时间
   *
   * @param cacheKey 缓存键
   * @param timeout 过期时间
   * @param timeUnit 时间单位
   */
  public void expire(String cacheKey, long timeout, TimeUnit timeUnit) {
    int minutes = (int) timeUnit.toMinutes(timeout);
    cacheUtil.expire(cacheKey, minutes);
  }

  /**
   * 获取缓存剩余过期时间（秒）
   *
   * @param cacheKey 缓存键
   * @return 剩余过期时间，-1表示永不过期，-2表示键不存在
   */
  public long getExpire(String cacheKey) {
    return cacheUtil.getExpire(cacheKey);
  }

  /**
   * 缓存预热 在应用启动时预加载常用数据
   *
   * @param cacheKey 缓存键
   * @param dataLoader 数据加载器
   * @param strategy 缓存策略
   * @param <T> 数据类型
   */
  public <T> void warmUp(String cacheKey, Supplier<T> dataLoader, CacheStrategy strategy) {
    try {
      java.util.Map<String, Object> context = new java.util.HashMap<>();
      context.put("cacheKey", cacheKey);
      LogUtil.logBusiness("CACHE_WARMUP_START", context);
      T data = dataLoader.get();
      if (data != null) {
        put(cacheKey, data, strategy);
        LogUtil.logBusiness("CACHE_WARMUP_COMPLETE", context);
      } else {
        LogUtil.logWarn("CACHE_WARMUP", "CACHE_WARMUP_EMPTY_DATA", "缓存预热数据为空: " + cacheKey);
      }
    } catch (Exception e) {
      LogUtil.logError("CACHE_WARMUP", "", "CACHE_WARMUP_FAILED", "缓存预热失败: " + cacheKey, e);
    }
  }

  /**
   * 批量获取缓存
   *
   * @param cacheKeys 缓存键列表
   * @param type 数据类型
   * @param <T> 数据类型
   * @return 缓存数据映射
   */
  public <T> java.util.Map<String, T> multiGet(java.util.List<String> cacheKeys, Class<T> type) {
    return cacheUtil.multiGet(cacheKeys, type);
  }

  /**
   * 批量设置缓存
   *
   * @param keyValues 键值对映射
   * @param strategy 缓存策略
   */
  public void multiPut(java.util.Map<String, Object> keyValues, CacheStrategy strategy) {
    cacheUtil.multiPut(keyValues, strategy.getExpireMinutes());
  }

  /**
   * 获取缓存大小
   *
   * @return 缓存大小
   */
  public int size() {
    return cacheUtil.size();
  }

  /**
   * 检查缓存是否为空
   *
   * @return 是否为空
   */
  public boolean isEmpty() {
    return cacheUtil.isEmpty();
  }

  /**
   * 获取缓存统计信息
   *
   * @return 缓存统计信息
   */
  public CacheStats getStats() {
    CacheUtil.CacheStats utilStats = cacheUtil.getStats();
    CacheStats stats = new CacheStats();
    stats.setHitCount(utilStats.getTotalAccessCount());
    stats.setMissCount(utilStats.getTotalEntries() - utilStats.getTotalAccessCount());
    stats.setEvictionCount(utilStats.getExpiredEntries());
    return stats;
  }

  /** 缓存统计信息类 */
  public static class CacheStats {
    private long hitCount = 0;
    private long missCount = 0;
    private long evictionCount = 0;

    // Getters and setters
    public long getHitCount() {
      return hitCount;
    }

    public void setHitCount(long hitCount) {
      this.hitCount = hitCount;
    }

    public long getMissCount() {
      return missCount;
    }

    public void setMissCount(long missCount) {
      this.missCount = missCount;
    }

    public long getEvictionCount() {
      return evictionCount;
    }

    public void setEvictionCount(long evictionCount) {
      this.evictionCount = evictionCount;
    }

    public double getHitRate() {
      long totalRequests = hitCount + missCount;
      return totalRequests == 0 ? 0.0 : (double) hitCount / totalRequests;
    }
  }
}
