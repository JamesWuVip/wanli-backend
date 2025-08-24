package com.wanli.backend.util;

import java.time.Duration;
import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 缓存工具类 提供统一的缓存管理功能，支持多种缓存策略 包含防缓存穿透、缓存雪崩、缓存击穿等优化机制 */
@Component
public class CacheUtil {

  @Autowired private ConfigUtil configUtil;

  private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
  private final ConcurrentMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();
  private final BitSet bloomFilter = new BitSet(1000000); // 简单布隆过滤器

  // 默认缓存时间（分钟）
  private static final int DEFAULT_EXPIRE_MINUTES = 30;
  // 空值缓存时间（分钟）
  private static final int NULL_CACHE_EXPIRE_MINUTES = 5;
  // 缓存大小限制
  private static final int MAX_CACHE_SIZE = 10000;
  // 空值标识
  private static final Object NULL_VALUE = new Object();

  /** 缓存条目内部类 */
  private static class CacheEntry {
    private final Object value;
    private final long expireTime;
    private volatile long lastAccessTime;
    private final AtomicInteger accessCount;

    public CacheEntry(Object value, long expireTime) {
      this.value = value;
      this.expireTime = expireTime;
      this.lastAccessTime = System.currentTimeMillis();
      this.accessCount = new AtomicInteger(0);
    }

    public boolean isExpired() {
      return System.currentTimeMillis() > expireTime;
    }

    public Object getValue() {
      this.lastAccessTime = System.currentTimeMillis();
      this.accessCount.incrementAndGet();
      return value;
    }

    public long getLastAccessTime() {
      return lastAccessTime;
    }

    public int getAccessCount() {
      return accessCount.get();
    }

    public void updateAccess() {
      this.lastAccessTime = System.currentTimeMillis();
      this.accessCount.incrementAndGet();
    }
  }

  /** 构造函数，启动定期清理任务 */
  public CacheUtil() {
    // 每5分钟清理一次过期缓存
    scheduler.scheduleAtFixedRate(this::cleanExpiredEntries, 5, 5, TimeUnit.MINUTES);
    // 每小时清理一次长时间未访问的缓存
    scheduler.scheduleAtFixedRate(this::cleanUnusedEntries, 1, 1, TimeUnit.HOURS);
  }

  /** 检查缓存是否启用 */
  private boolean isCacheEnabled() {
    return configUtil != null && configUtil.isCacheEnabled();
  }

  /** 存储缓存（使用默认过期时间） */
  public void put(String key, Object value) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "key=" + key + ", operation=put");
      return;
    }
    put(key, value, DEFAULT_EXPIRE_MINUTES);
  }

  /** 存储缓存（指定过期时间，分钟） 包含缓存大小限制 */
  public void put(String key, Object value, int expireMinutes) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "key=" + key + ", operation=put");
      return;
    }

    // 检查缓存大小限制
    if (cache.size() >= MAX_CACHE_SIZE) {
      evictLRU();
    }

    long expireTime = System.currentTimeMillis() + Duration.ofMinutes(expireMinutes).toMillis();
    cache.put(key, new CacheEntry(value, expireTime));
    addToBloomFilter(key);
    LogUtil.logBusinessOperation(
        "CACHE_PUT", "", "key=" + key + ", expire=" + expireMinutes + "min");
  }

  /** 存储缓存（指定过期时间，Duration） */
  public void put(String key, Object value, Duration duration) {
    long expireTime = System.currentTimeMillis() + duration.toMillis();
    cache.put(key, new CacheEntry(value, expireTime));
    LogUtil.logBusinessOperation("CACHE_PUT", "", "key=" + key + ", expire=" + duration.toString());
  }

  /** 获取缓存 支持空值缓存处理 */
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> type) {
    CacheEntry entry = cache.get(key);
    if (entry == null || entry.isExpired()) {
      if (entry != null && entry.isExpired()) {
        cache.remove(key);
        LogUtil.logBusinessOperation("CACHE_EXPIRED", "", "key=" + key);
      }
      return null;
    }

    Object value = entry.getValue();
    // 处理空值缓存
    if (value == NULL_VALUE) {
      LogUtil.logBusinessOperation(
          "CACHE_HIT_NULL", "", "key=" + key + ", accessCount=" + entry.getAccessCount());
      return null;
    }

    LogUtil.logBusinessOperation(
        "CACHE_HIT", "", "key=" + key + ", accessCount=" + entry.getAccessCount());
    return (T) value;
  }

  /** 获取缓存（不指定类型） */
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "key=" + key + ", operation=get");
      return null;
    }

    // 使用布隆过滤器快速判断key是否可能存在
    if (!mightContain(key)) {
      LogUtil.logBusinessOperation("CACHE_MISS_BLOOM", "", "key=" + key);
      return null;
    }

    CacheEntry entry = cache.get(key);
    if (entry == null) {
      LogUtil.logBusinessOperation("CACHE_MISS", "", "key=" + key);
      return null;
    }

    // 检查是否过期
    if (entry.isExpired()) {
      cache.remove(key);
      LogUtil.logBusinessOperation("CACHE_EXPIRED", "", "key=" + key);
      return null;
    }

    Object value = entry.getValue();
    // 处理空值缓存
    if (value == NULL_VALUE) {
      LogUtil.logBusinessOperation(
          "CACHE_HIT_NULL", "", "key=" + key + ", accessCount=" + entry.getAccessCount());
      return null;
    }

    LogUtil.logBusinessOperation(
        "CACHE_HIT", "", "key=" + key + ", accessCount=" + entry.getAccessCount());
    return (T) value;
  }

  /** 获取缓存，如果不存在则使用供应商函数获取并缓存 包含防缓存穿透优化 */
  public <T> T getOrCompute(String key, Class<T> type, Supplier<T> supplier) {
    return getOrCompute(key, type, supplier, DEFAULT_EXPIRE_MINUTES);
  }

  /** 获取缓存，如果不存在则计算并缓存 防止缓存击穿和缓存穿透 */
  public <T> T getOrCompute(String key, Supplier<T> supplier) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation(
          "CACHE_DISABLED",
          "",
          "key=" + key + ", operation=getOrCompute, executing supplier directly");
      return supplier.get();
    }
    return getOrCompute(key, supplier, DEFAULT_EXPIRE_MINUTES);
  }

  /** 获取缓存，如果不存在则使用供应商函数获取并缓存 包含防缓存穿透优化 */
  @SuppressWarnings("unchecked")
  public <T> T getOrCompute(String key, Supplier<T> supplier, int expireMinutes) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation(
          "CACHE_DISABLED",
          "",
          "key=" + key + ", operation=getOrCompute, executing supplier directly");
      return supplier.get();
    }
    return (T) getOrCompute(key, (Class<T>) Object.class, supplier, expireMinutes);
  }

  /** 获取缓存，如果不存在则使用供应商函数获取并缓存（指定类型） 包含防缓存穿透和击穿优化 */
  @SuppressWarnings("unchecked")
  public <T> T getOrCompute(String key, Class<T> type, Supplier<T> supplier, int expireMinutes) {
    // 先检查布隆过滤器，防止缓存穿透
    if (!mightContain(key)) {
      LogUtil.logBusinessOperation("CACHE_BLOOM_FILTER_REJECT", "", "key=" + key);
      return null;
    }

    T cachedValue = get(key, type);
    if (cachedValue != null) {
      return cachedValue;
    }

    // 使用分布式锁防止缓存击穿
    ReentrantLock lock = keyLocks.computeIfAbsent(key, k -> new ReentrantLock());
    lock.lock();
    try {
      // 双重检查
      cachedValue = get(key, type);
      if (cachedValue != null) {
        return cachedValue;
      }

      // 缓存未命中，计算新值
      LogUtil.PerformanceMonitor monitor = LogUtil.startPerformanceMonitor("CACHE_COMPUTE_" + key);
      T newValue = supplier.get();
      monitor.end();

      // 缓存新值（包括null值以防止缓存穿透）
      if (newValue != null) {
        put(key, newValue, expireMinutes);
        addToBloomFilter(key);
      } else {
        // 缓存空值，防止缓存穿透
        putNullValue(key);
      }

      LogUtil.logBusinessOperation(
          "CACHE_MISS",
          "",
          "key=" + key + ", computed=true, value=" + (newValue != null ? "not null" : "null"));
      return newValue;

    } finally {
      lock.unlock();
      // 清理锁映射，避免内存泄漏
      if (!lock.hasQueuedThreads()) {
        keyLocks.remove(key, lock);
      }
    }
  }

  /** 删除缓存 */
  public void remove(String key) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "key=" + key + ", operation=remove");
      return;
    }
    CacheEntry removed = cache.remove(key);
    if (removed != null) {
      LogUtil.logBusinessOperation("CACHE_REMOVE", "", "key=" + key);
    }
  }

  /** 删除缓存（别名方法） */
  public void delete(String key) {
    remove(key);
  }

  /** 清空所有缓存 */
  public void clear() {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "operation=clear");
      return;
    }
    int size = cache.size();
    cache.clear();
    keyLocks.clear();
    LogUtil.logBusinessOperation("CACHE_CLEAR", "", "cleared " + size + " entries");
  }

  /** 检查缓存是否存在且未过期 */
  public boolean exists(String key) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "key=" + key + ", operation=exists");
      return false;
    }

    CacheEntry entry = cache.get(key);
    if (entry == null || entry.isExpired()) {
      if (entry != null && entry.isExpired()) {
        cache.remove(key);
      }
      return false;
    }
    return true;
  }

  /** 获取缓存统计信息 */
  public CacheStats getStats() {
    int totalEntries = cache.size();
    int expiredEntries = 0;
    long totalAccessCount = 0;

    for (CacheEntry entry : cache.values()) {
      if (entry.isExpired()) {
        expiredEntries++;
      }
      totalAccessCount += entry.getAccessCount();
    }

    return new CacheStats(totalEntries, expiredEntries, totalAccessCount);
  }

  /** 清理过期缓存条目 */
  private void cleanExpiredEntries() {
    int cleanedCount = 0;
    // 使用迭代器安全地遍历和删除
    var iterator = cache.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      if (entry.getValue().isExpired()) {
        iterator.remove();
        cleanedCount++;
      }
    }

    if (cleanedCount > 0) {
      LogUtil.logBusinessOperation(
          "CACHE_CLEANUP_EXPIRED", "", "cleaned=" + cleanedCount + " entries");
    }
  }

  /** 清理长时间未访问的缓存条目 */
  private void cleanUnusedEntries() {
    long oneHourAgo = System.currentTimeMillis() - Duration.ofHours(1).toMillis();
    int cleanedCount = 0;

    // 使用迭代器安全地遍历和删除
    var iterator = cache.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      CacheEntry cacheEntry = entry.getValue();
      if (cacheEntry.getLastAccessTime() < oneHourAgo && cacheEntry.getAccessCount() == 0) {
        iterator.remove();
        cleanedCount++;
      }
    }

    if (cleanedCount > 0) {
      LogUtil.logBusinessOperation(
          "CACHE_CLEANUP_UNUSED", "", "cleaned=" + cleanedCount + " entries");
    }
  }

  /** 缓存统计信息类 */
  public static class CacheStats {
    private final int totalEntries;
    private final int expiredEntries;
    private final long totalAccessCount;

    public CacheStats(int totalEntries, int expiredEntries, long totalAccessCount) {
      this.totalEntries = totalEntries;
      this.expiredEntries = expiredEntries;
      this.totalAccessCount = totalAccessCount;
    }

    public int getTotalEntries() {
      return totalEntries;
    }

    public int getExpiredEntries() {
      return expiredEntries;
    }

    public long getTotalAccessCount() {
      return totalAccessCount;
    }

    public double getHitRate() {
      return totalEntries > 0 ? (double) totalAccessCount / totalEntries : 0.0;
    }

    @Override
    public String toString() {
      return String.format(
          "CacheStats{total=%d, expired=%d, access=%d, hitRate=%.2f}",
          totalEntries, expiredEntries, totalAccessCount, getHitRate());
    }
  }

  /** 缓存空值，防止缓存穿透 */
  private void putNullValue(String key) {
    long expireTime =
        System.currentTimeMillis() + Duration.ofMinutes(NULL_CACHE_EXPIRE_MINUTES).toMillis();
    cache.put(key, new CacheEntry(NULL_VALUE, expireTime));
    LogUtil.logBusinessOperation(
        "CACHE_PUT_NULL", "", "key=" + key + ", expire=" + NULL_CACHE_EXPIRE_MINUTES + "min");
  }

  /** 添加键到布隆过滤器 */
  private void addToBloomFilter(String key) {
    int hash1 = key.hashCode() % bloomFilter.size();
    int hash2 = (key.hashCode() * 31) % bloomFilter.size();
    int hash3 = (key.hashCode() * 37) % bloomFilter.size();

    if (hash1 < 0) hash1 += bloomFilter.size();
    if (hash2 < 0) hash2 += bloomFilter.size();
    if (hash3 < 0) hash3 += bloomFilter.size();

    bloomFilter.set(hash1);
    bloomFilter.set(hash2);
    bloomFilter.set(hash3);
  }

  /** 检查键是否可能存在于布隆过滤器中 */
  private boolean mightContain(String key) {
    int hash1 = key.hashCode() % bloomFilter.size();
    int hash2 = (key.hashCode() * 31) % bloomFilter.size();
    int hash3 = (key.hashCode() * 37) % bloomFilter.size();

    if (hash1 < 0) hash1 += bloomFilter.size();
    if (hash2 < 0) hash2 += bloomFilter.size();
    if (hash3 < 0) hash3 += bloomFilter.size();

    return bloomFilter.get(hash1) && bloomFilter.get(hash2) && bloomFilter.get(hash3);
  }

  /** LRU淘汰策略 */
  private void evictLRU() {
    String oldestKey = null;
    long oldestTime = Long.MAX_VALUE;

    for (String key : cache.keySet()) {
      CacheEntry entry = cache.get(key);
      if (entry != null && entry.getLastAccessTime() < oldestTime) {
        oldestTime = entry.getLastAccessTime();
        oldestKey = key;
      }
    }

    if (oldestKey != null) {
      cache.remove(oldestKey);
      LogUtil.logBusinessOperation("CACHE_LRU_EVICT", "", "key=" + oldestKey);
    }
  }

  /** 根据模式删除缓存 */
  public void removeByPattern(String pattern) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation(
          "CACHE_DISABLED", "", "pattern=" + pattern + ", operation=removeByPattern");
      return;
    }

    int removedCount = 0;
    for (String key : cache.keySet()) {
      if (key.matches(pattern.replace("*", ".*"))) {
        cache.remove(key);
        removedCount++;
      }
    }
    LogUtil.logBusinessOperation(
        "CACHE_REMOVE_PATTERN", "", "pattern=" + pattern + ", removed=" + removedCount);
  }

  /** 设置缓存过期时间 */
  public void expire(String key, int expireMinutes) {
    CacheEntry entry = cache.get(key);
    if (entry != null) {
      long newExpireTime =
          System.currentTimeMillis() + Duration.ofMinutes(expireMinutes).toMillis();
      cache.put(key, new CacheEntry(entry.value, newExpireTime));
      LogUtil.logBusinessOperation(
          "CACHE_EXPIRE_SET", "", "key=" + key + ", expire=" + expireMinutes + "min");
    }
  }

  /** 获取缓存剩余过期时间（秒） */
  public long getExpire(String key) {
    CacheEntry entry = cache.get(key);
    if (entry == null) {
      return -2; // 键不存在
    }

    long remainingTime = entry.expireTime - System.currentTimeMillis();
    if (remainingTime <= 0) {
      return -2; // 已过期
    }

    return remainingTime / 1000; // 转换为秒
  }

  /** 批量获取缓存 */
  public java.util.Map<String, Object> multiGet(java.util.List<String> keys) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation(
          "CACHE_DISABLED", "", "keys=" + keys.size() + ", operation=multiGet");
      return new java.util.HashMap<>();
    }

    java.util.Map<String, Object> result = new java.util.HashMap<>();
    for (String key : keys) {
      Object value = get(key);
      if (value != null) {
        result.put(key, value);
      }
    }
    LogUtil.logBusinessOperation(
        "CACHE_MULTI_GET", "", "keys=" + keys.size() + ", hits=" + result.size());
    return result;
  }

  /** 批量获取缓存（指定类型） */
  @SuppressWarnings("unchecked")
  public <T> java.util.Map<String, T> multiGet(java.util.List<String> keys, Class<T> type) {
    java.util.Map<String, T> result = new java.util.HashMap<>();
    for (String key : keys) {
      T value = get(key, type);
      if (value != null) {
        result.put(key, value);
      }
    }
    return result;
  }

  /** 批量设置缓存 */
  public void multiPut(java.util.Map<String, Object> keyValues, int expireMinutes) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation(
          "CACHE_DISABLED", "", "keys=" + keyValues.size() + ", operation=multiPut");
      return;
    }

    for (java.util.Map.Entry<String, Object> entry : keyValues.entrySet()) {
      put(entry.getKey(), entry.getValue(), expireMinutes);
    }
    LogUtil.logBusinessOperation(
        "CACHE_MULTI_PUT", "", "keys=" + keyValues.size() + ", expire=" + expireMinutes + "min");
  }

  /** 仅当键不存在时设置缓存 */
  public boolean setIfAbsent(String key, String value, long expireTimeMs) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "key=" + key + ", operation=setIfAbsent");
      return false;
    }

    // 检查键是否已存在且未过期
    if (exists(key)) {
      return false;
    }

    // 设置缓存
    long expireTime = System.currentTimeMillis() + expireTimeMs;
    cache.put(key, new CacheEntry(value, expireTime));
    addToBloomFilter(key);
    LogUtil.logBusinessOperation(
        "CACHE_SET_IF_ABSENT", "", "key=" + key + ", expire=" + expireTimeMs + "ms");
    return true;
  }

  /** 检查是否有匹配模式的键 */
  public boolean hasKeysMatching(String pattern) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation(
          "CACHE_DISABLED", "", "pattern=" + pattern + ", operation=hasKeysMatching");
      return false;
    }

    String regex = pattern.replace("*", ".*");
    for (String key : cache.keySet()) {
      if (key.matches(regex) && exists(key)) {
        return true;
      }
    }
    return false;
  }

  /** 执行脚本（简化实现，用于分布式锁的原子操作） */
  public Object executeScript(String script, String[] keys, String[] args) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "operation=executeScript");
      return "0";
    }

    // 简化的Lua脚本执行逻辑，主要用于分布式锁的释放操作
    if (keys.length > 0 && args.length > 0) {
      String key = keys[0];
      String expectedValue = args[0];

      // 检查键值是否匹配
      String currentValue = get(key);
      if (expectedValue.equals(currentValue)) {
        remove(key);
        return "1"; // 删除成功
      }
    }
    return "0"; // 删除失败或条件不匹配
  }

  /** 增加计数器 */
  public long increment(String key, long delta) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation("CACHE_DISABLED", "", "key=" + key + ", operation=increment");
      return 0L;
    }

    String currentValue = get(key);
    long newValue = (currentValue != null ? Long.parseLong(currentValue) : 0L) + delta;
    put(key, String.valueOf(newValue));

    LogUtil.logBusinessOperation(
        "CACHE_INCREMENT", "", "key=" + key + ", delta=" + delta + ", newValue=" + newValue);
    return newValue;
  }

  /** 设置缓存并指定过期时间 */
  public void setWithExpire(String key, String value, long expireTimeMs) {
    if (!isCacheEnabled()) {
      LogUtil.logBusinessOperation(
          "CACHE_DISABLED", "", "key=" + key + ", operation=setWithExpire");
      return;
    }

    long expireTime = System.currentTimeMillis() + expireTimeMs;
    cache.put(key, new CacheEntry(value, expireTime));
    addToBloomFilter(key);

    LogUtil.logBusinessOperation(
        "CACHE_SET_WITH_EXPIRE", "", "key=" + key + ", expire=" + expireTimeMs + "ms");
  }

  /** 获取缓存大小 */
  public int size() {
    return cache.size();
  }

  /** 检查缓存是否为空 */
  public boolean isEmpty() {
    return cache.isEmpty();
  }

  /** 关闭缓存工具（清理资源） */
  public void shutdown() {
    scheduler.shutdown();
    keyLocks.clear();
    bloomFilter.clear();
    clear();
  }
}
