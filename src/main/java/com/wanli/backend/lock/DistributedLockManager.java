package com.wanli.backend.lock;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.LogUtil;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/** 分布式锁管理器 提供基于Redis的分布式锁功能，支持可重入锁、读写锁、信号量等 */
@Component
public class DistributedLockManager {

  @Autowired private CacheUtil cacheUtil;

  @Autowired private ApplicationConfigManager configManager;

  private final Map<String, LockInfo> localLocks = new ConcurrentHashMap<>();
  private final AtomicLong lockIdGenerator = new AtomicLong(0);
  private ScheduledExecutorService lockWatchdog;
  private final ReentrantLock managerLock = new ReentrantLock();

  // 锁前缀
  private static final String LOCK_PREFIX = "distributed_lock:";
  private static final String SEMAPHORE_PREFIX = "distributed_semaphore:";
  private static final String READ_LOCK_PREFIX = "distributed_read_lock:";
  private static final String WRITE_LOCK_PREFIX = "distributed_write_lock:";

  /** 锁类型枚举 */
  public enum LockType {
    EXCLUSIVE("exclusive", "排他锁"),
    REENTRANT("reentrant", "可重入锁"),
    READ_WRITE("read_write", "读写锁"),
    SEMAPHORE("semaphore", "信号量"),
    FAIR("fair", "公平锁");

    private final String code;
    private final String description;

    LockType(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }

  /** 锁信息 */
  public static class LockInfo {
    private final String lockId;
    private final String lockKey;
    private final LockType lockType;
    private final String threadId;
    private final long acquireTime;
    private final long expireTime;
    private volatile int reentrantCount;
    private volatile boolean released;
    private ScheduledFuture<?> renewalTask;

    public LockInfo(String lockId, String lockKey, LockType lockType, long expireTime) {
      this.lockId = lockId;
      this.lockKey = lockKey;
      this.lockType = lockType;
      this.threadId = Thread.currentThread().getName();
      this.acquireTime = System.currentTimeMillis();
      this.expireTime = expireTime;
      this.reentrantCount = 1;
      this.released = false;
    }

    // Getters and Setters
    public String getLockId() {
      return lockId;
    }

    public String getLockKey() {
      return lockKey;
    }

    public LockType getLockType() {
      return lockType;
    }

    public String getThreadId() {
      return threadId;
    }

    public long getAcquireTime() {
      return acquireTime;
    }

    public long getExpireTime() {
      return expireTime;
    }

    public int getReentrantCount() {
      return reentrantCount;
    }

    public void setReentrantCount(int reentrantCount) {
      this.reentrantCount = reentrantCount;
    }

    public boolean isReleased() {
      return released;
    }

    public void setReleased(boolean released) {
      this.released = released;
    }

    public ScheduledFuture<?> getRenewalTask() {
      return renewalTask;
    }

    public void setRenewalTask(ScheduledFuture<?> renewalTask) {
      this.renewalTask = renewalTask;
    }

    public long getHoldTime() {
      return System.currentTimeMillis() - acquireTime;
    }

    public long getRemainingTime() {
      return Math.max(0, expireTime - System.currentTimeMillis());
    }

    public boolean isExpired() {
      return System.currentTimeMillis() > expireTime;
    }
  }

  /** 锁配置 */
  public static class LockConfig {
    private long waitTime = 10000; // 等待时间（毫秒）
    private long leaseTime = 30000; // 租约时间（毫秒）
    private long renewalInterval = 10000; // 续约间隔（毫秒）
    private boolean autoRenewal = true; // 自动续约
    private boolean fair = false; // 公平锁
    private int permits = 1; // 信号量许可数

    // Getters and Setters
    public long getWaitTime() {
      return waitTime;
    }

    public void setWaitTime(long waitTime) {
      this.waitTime = waitTime;
    }

    public long getLeaseTime() {
      return leaseTime;
    }

    public void setLeaseTime(long leaseTime) {
      this.leaseTime = leaseTime;
    }

    public long getRenewalInterval() {
      return renewalInterval;
    }

    public void setRenewalInterval(long renewalInterval) {
      this.renewalInterval = renewalInterval;
    }

    public boolean isAutoRenewal() {
      return autoRenewal;
    }

    public void setAutoRenewal(boolean autoRenewal) {
      this.autoRenewal = autoRenewal;
    }

    public boolean isFair() {
      return fair;
    }

    public void setFair(boolean fair) {
      this.fair = fair;
    }

    public int getPermits() {
      return permits;
    }

    public void setPermits(int permits) {
      this.permits = permits;
    }
  }

  /** 锁回调接口 */
  @FunctionalInterface
  public interface LockCallback<T> {
    T execute() throws Exception;
  }

  /** 无返回值锁回调接口 */
  @FunctionalInterface
  public interface VoidLockCallback {
    void execute() throws Exception;
  }

  @PostConstruct
  public void initialize() {
    // 初始化锁看门狗
    lockWatchdog =
        Executors.newScheduledThreadPool(
            2,
            r -> {
              Thread t = new Thread(r, "DistributedLock-Watchdog");
              t.setDaemon(true);
              return t;
            });

    // 启动锁清理任务
    lockWatchdog.scheduleWithFixedDelay(this::cleanupExpiredLocks, 30, 30, TimeUnit.SECONDS);

    LogUtil.logInfo("DISTRIBUTED_LOCK_MANAGER", "", "分布式锁管理器初始化完成");
  }

  /** 获取排他锁 */
  public boolean tryLock(String lockKey) {
    return tryLock(lockKey, new LockConfig());
  }

  /** 获取排他锁（自定义配置） */
  public boolean tryLock(String lockKey, LockConfig config) {
    return tryLock(lockKey, config.getWaitTime(), config.getLeaseTime(), TimeUnit.MILLISECONDS);
  }

  /** 获取排他锁（指定等待和租约时间） */
  public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
    String fullKey = LOCK_PREFIX + lockKey;
    String lockId = generateLockId();
    long waitTimeMs = timeUnit.toMillis(waitTime);
    long leaseTimeMs = timeUnit.toMillis(leaseTime);
    long expireTime = System.currentTimeMillis() + leaseTimeMs;

    try {
      // 尝试获取锁
      boolean acquired = acquireLock(fullKey, lockId, waitTimeMs, leaseTimeMs);

      if (acquired) {
        // 创建锁信息
        LockInfo lockInfo = new LockInfo(lockId, fullKey, LockType.EXCLUSIVE, expireTime);
        localLocks.put(fullKey, lockInfo);

        // 启动自动续约
        startAutoRenewal(lockInfo);

        LogUtil.logInfo(
            "DISTRIBUTED_LOCK_MANAGER",
            "",
            String.format("获取锁成功: %s, lockId: %s, 租约时间: %dms", lockKey, lockId, leaseTimeMs));

        return true;
      }

      return false;

    } catch (Exception e) {
      LogUtil.logError(
          "DISTRIBUTED_LOCK_MANAGER",
          "",
          "LOCK_ACQUIRE_ERROR",
          String.format("获取分布式锁失败: %s", lockKey),
          e);
      return false;
    }
  }

  /** 释放锁 */
  public boolean unlock(String lockKey) {
    String fullKey = LOCK_PREFIX + lockKey;

    managerLock.lock();
    try {
      LockInfo lockInfo = localLocks.get(fullKey);
      if (lockInfo == null) {
        LogUtil.logWarn("DISTRIBUTED_LOCK_MANAGER", "", String.format("尝试释放不存在的锁: %s", lockKey));
        return false;
      }

      // 检查线程所有权
      if (!Thread.currentThread().getName().equals(lockInfo.getThreadId())) {
        LogUtil.logWarn(
            "DISTRIBUTED_LOCK_MANAGER",
            "",
            String.format(
                "尝试释放其他线程的锁: %s, 当前线程: %s, 锁持有线程: %s",
                lockKey, Thread.currentThread().getName(), lockInfo.getThreadId()));
        return false;
      }

      // 处理可重入锁
      if (lockInfo.getReentrantCount() > 1) {
        lockInfo.setReentrantCount(lockInfo.getReentrantCount() - 1);
        LogUtil.logInfo(
            "DISTRIBUTED_LOCK_MANAGER",
            "",
            String.format("可重入锁计数减1: %s, 剩余: %d", lockKey, lockInfo.getReentrantCount()));
        return true;
      }

      // 释放锁
      boolean released = releaseLock(fullKey, lockInfo.getLockId());

      if (released) {
        // 停止自动续约
        stopAutoRenewal(lockInfo);

        // 标记为已释放
        lockInfo.setReleased(true);
        localLocks.remove(fullKey);

        LogUtil.logInfo(
            "DISTRIBUTED_LOCK_MANAGER",
            "",
            String.format("释放锁成功: %s, 持有时间: %dms", lockKey, lockInfo.getHoldTime()));
      }

      return released;

    } finally {
      managerLock.unlock();
    }
  }

  /** 在锁中执行（有返回值） */
  public <T> T executeWithLock(String lockKey, LockCallback<T> callback) {
    return executeWithLock(lockKey, new LockConfig(), callback);
  }

  /** 在锁中执行（有返回值，自定义配置） */
  public <T> T executeWithLock(String lockKey, LockConfig config, LockCallback<T> callback) {
    if (tryLock(lockKey, config)) {
      try {
        return callback.execute();
      } catch (Exception e) {
        throw new RuntimeException("执行锁回调失败: " + lockKey, e);
      } finally {
        unlock(lockKey);
      }
    } else {
      throw new RuntimeException("获取锁失败: " + lockKey);
    }
  }

  /** 在锁中执行（无返回值） */
  public void executeWithLock(String lockKey, VoidLockCallback callback) {
    executeWithLock(lockKey, new LockConfig(), callback);
  }

  /** 在锁中执行（无返回值，自定义配置） */
  public void executeWithLock(String lockKey, LockConfig config, VoidLockCallback callback) {
    executeWithLock(
        lockKey,
        config,
        () -> {
          callback.execute();
          return null;
        });
  }

  /** 获取读锁 */
  public boolean tryReadLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
    String fullKey = READ_LOCK_PREFIX + lockKey;
    String lockId = generateLockId();
    long waitTimeMs = timeUnit.toMillis(waitTime);
    long leaseTimeMs = timeUnit.toMillis(leaseTime);
    long expireTime = System.currentTimeMillis() + leaseTimeMs;

    try {
      // 检查是否有写锁
      String writeKey = WRITE_LOCK_PREFIX + lockKey;
      if (cacheUtil.exists(writeKey)) {
        return false;
      }

      // 获取读锁（可以多个读锁并存）
      String readLockKey = fullKey + ":" + lockId;
      boolean acquired = cacheUtil.setIfAbsent(readLockKey, lockId, leaseTimeMs);

      if (acquired) {
        LockInfo lockInfo = new LockInfo(lockId, readLockKey, LockType.READ_WRITE, expireTime);
        localLocks.put(readLockKey, lockInfo);
        startAutoRenewal(lockInfo);

        LogUtil.logInfo(
            "DISTRIBUTED_LOCK_MANAGER",
            "",
            String.format("获取读锁成功: %s, lockId: %s", lockKey, lockId));

        return true;
      }

      return false;

    } catch (Exception e) {
      LogUtil.logError(
          "DISTRIBUTED_LOCK_MANAGER",
          "",
          "LOCK_ACQUIRE_FAILED",
          String.format("获取读锁失败: %s", lockKey),
          e);
      return false;
    }
  }

  /** 获取写锁 */
  public boolean tryWriteLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
    String fullKey = WRITE_LOCK_PREFIX + lockKey;
    String lockId = generateLockId();
    long waitTimeMs = timeUnit.toMillis(waitTime);
    long leaseTimeMs = timeUnit.toMillis(leaseTime);
    long expireTime = System.currentTimeMillis() + leaseTimeMs;

    try {
      // 检查是否有读锁或写锁
      String readKeyPattern = READ_LOCK_PREFIX + lockKey + ":*";
      if (cacheUtil.hasKeysMatching(readKeyPattern) || cacheUtil.exists(fullKey)) {
        return false;
      }

      // 获取写锁
      boolean acquired = cacheUtil.setIfAbsent(fullKey, lockId, leaseTimeMs);

      if (acquired) {
        LockInfo lockInfo = new LockInfo(lockId, fullKey, LockType.READ_WRITE, expireTime);
        localLocks.put(fullKey, lockInfo);
        startAutoRenewal(lockInfo);

        LogUtil.logInfo(
            "DISTRIBUTED_LOCK_MANAGER",
            "",
            String.format("获取写锁成功: %s, lockId: %s", lockKey, lockId));

        return true;
      }

      return false;

    } catch (Exception e) {
      LogUtil.logError(
          "DISTRIBUTED_LOCK_MANAGER",
          "",
          "LOCK_ACQUIRE_FAILED",
          String.format("获取写锁失败: %s", lockKey),
          e);
      return false;
    }
  }

  /** 获取信号量 */
  public boolean tryAcquire(
      String semaphoreKey, int permits, long waitTime, long leaseTime, TimeUnit timeUnit) {
    String fullKey = SEMAPHORE_PREFIX + semaphoreKey;
    String lockId = generateLockId();
    long waitTimeMs = timeUnit.toMillis(waitTime);
    long leaseTimeMs = timeUnit.toMillis(leaseTime);
    long expireTime = System.currentTimeMillis() + leaseTimeMs;

    try {
      // 尝试获取信号量
      boolean acquired = acquireSemaphore(fullKey, lockId, permits, waitTimeMs, leaseTimeMs);

      if (acquired) {
        LockInfo lockInfo = new LockInfo(lockId, fullKey, LockType.SEMAPHORE, expireTime);
        localLocks.put(fullKey + ":" + lockId, lockInfo);
        startAutoRenewal(lockInfo);

        LogUtil.logInfo(
            "DISTRIBUTED_LOCK_MANAGER",
            "",
            String.format("获取信号量成功: %s, permits: %d, lockId: %s", semaphoreKey, permits, lockId));

        return true;
      }

      return false;

    } catch (Exception e) {
      LogUtil.logError(
          "DISTRIBUTED_LOCK_MANAGER",
          "",
          "SEMAPHORE_ACQUIRE_FAILED",
          String.format("获取信号量失败: %s", semaphoreKey),
          e);
      return false;
    }
  }

  /** 检查锁是否存在 */
  public boolean isLocked(String lockKey) {
    String fullKey = LOCK_PREFIX + lockKey;
    return cacheUtil.exists(fullKey);
  }

  /** 获取锁信息 */
  public LockInfo getLockInfo(String lockKey) {
    String fullKey = LOCK_PREFIX + lockKey;
    return localLocks.get(fullKey);
  }

  /** 强制释放锁（管理员操作） */
  public boolean forceUnlock(String lockKey) {
    String fullKey = LOCK_PREFIX + lockKey;

    try {
      // 删除Redis中的锁
      cacheUtil.delete(fullKey);

      // 清理本地锁信息
      LockInfo lockInfo = localLocks.remove(fullKey);
      if (lockInfo != null) {
        stopAutoRenewal(lockInfo);
        lockInfo.setReleased(true);
      }

      LogUtil.logWarn("DISTRIBUTED_LOCK_MANAGER", "", String.format("强制释放锁: %s", lockKey));

      return true;

    } catch (Exception e) {
      LogUtil.logError(
          "DISTRIBUTED_LOCK_MANAGER",
          "",
          "LOCK_FORCE_UNLOCK_FAILED",
          String.format("强制释放锁失败: %s", lockKey),
          e);
      return false;
    }
  }

  /** 获取锁 */
  private boolean acquireLock(String lockKey, String lockId, long waitTimeMs, long leaseTimeMs) {
    long startTime = System.currentTimeMillis();

    while (System.currentTimeMillis() - startTime < waitTimeMs) {
      // 尝试设置锁
      if (cacheUtil.setIfAbsent(lockKey, lockId, leaseTimeMs)) {
        return true;
      }

      // 检查是否是可重入锁
      String existingLockId = cacheUtil.get(lockKey);
      if (lockId.startsWith(existingLockId.split("-")[0])) {
        // 可重入锁逻辑
        LockInfo lockInfo = localLocks.get(lockKey);
        if (lockInfo != null && lockInfo.getThreadId().equals(Thread.currentThread().getName())) {
          lockInfo.setReentrantCount(lockInfo.getReentrantCount() + 1);
          return true;
        }
      }

      // 等待一段时间后重试
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
      }
    }

    return false;
  }

  /** 释放锁 */
  private boolean releaseLock(String lockKey, String lockId) {
    try {
      // 使用Lua脚本确保原子性
      String script =
          "if redis.call('get', KEYS[1]) == ARGV[1] then "
              + "    return redis.call('del', KEYS[1]) "
              + "else "
              + "    return 0 "
              + "end";

      Object result =
          cacheUtil.executeScript(script, new String[] {lockKey}, new String[] {lockId});
      return "1".equals(String.valueOf(result));

    } catch (Exception e) {
      LogUtil.logError(
          "DISTRIBUTED_LOCK_MANAGER",
          "",
          "LOCK_RELEASE_ERROR",
          String.format("释放分布式锁失败: %s", lockKey),
          e);
      return false;
    }
  }

  /** 获取信号量 */
  private boolean acquireSemaphore(
      String semaphoreKey, String lockId, int permits, long waitTimeMs, long leaseTimeMs) {
    // 简化的信号量实现
    String counterKey = semaphoreKey + ":counter";
    String lockKey = semaphoreKey + ":" + lockId;

    try {
      // 检查当前计数
      String currentCountStr = cacheUtil.get(counterKey);
      int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;

      if (currentCount + permits <= configManager.getCache().getMaxSemaphorePermits()) {
        // 增加计数并设置锁
        cacheUtil.increment(counterKey, permits);
        cacheUtil.setWithExpire(lockKey, lockId, leaseTimeMs);
        return true;
      }

      return false;

    } catch (Exception e) {
      LogUtil.logError(
          "DISTRIBUTED_LOCK_MANAGER",
          "",
          "SEMAPHORE_ACQUIRE_FAILED",
          String.format("获取信号量失败: %s", semaphoreKey),
          e);
      return false;
    }
  }

  /** 启动自动续约 */
  private void startAutoRenewal(LockInfo lockInfo) {
    if (!configManager.getCache().isEnableLockRenewal()) {
      return;
    }

    long renewalInterval = configManager.getCache().getLockRenewalInterval();

    ScheduledFuture<?> renewalTask =
        lockWatchdog.scheduleWithFixedDelay(
            () -> {
              try {
                if (!lockInfo.isReleased() && !lockInfo.isExpired()) {
                  // 续约锁
                  long newExpireTime =
                      System.currentTimeMillis() + configManager.getCache().getLockLeaseTime();
                  cacheUtil.expire(
                      lockInfo.getLockKey(), (int) configManager.getCache().getLockLeaseTime());

                  LogUtil.logInfo(
                      "DISTRIBUTED_LOCK_MANAGER",
                      "",
                      String.format("锁续约成功: %s, 新过期时间: %d", lockInfo.getLockKey(), newExpireTime));
                }
              } catch (Exception e) {
                LogUtil.logError(
                    "DISTRIBUTED_LOCK_MANAGER",
                    "",
                    "LOCK_RENEWAL_FAILED",
                    String.format("锁续约失败: %s", lockInfo.getLockKey()),
                    e);
              }
            },
            renewalInterval,
            renewalInterval,
            TimeUnit.MILLISECONDS);

    lockInfo.setRenewalTask(renewalTask);
  }

  /** 停止自动续约 */
  private void stopAutoRenewal(LockInfo lockInfo) {
    ScheduledFuture<?> renewalTask = lockInfo.getRenewalTask();
    if (renewalTask != null && !renewalTask.isCancelled()) {
      renewalTask.cancel(false);
    }
  }

  /** 清理过期锁 */
  private void cleanupExpiredLocks() {
    try {
      localLocks
          .entrySet()
          .removeIf(
              entry -> {
                LockInfo lockInfo = entry.getValue();
                if (lockInfo.isExpired() || lockInfo.isReleased()) {
                  stopAutoRenewal(lockInfo);
                  return true;
                }
                return false;
              });
    } catch (Exception e) {
      LogUtil.logError("DISTRIBUTED_LOCK_MANAGER", "", "LOCK_CLEANUP_FAILED", "清理过期锁失败", e);
    }
  }

  /** 生成锁ID */
  private String generateLockId() {
    return Thread.currentThread().getName()
        + "-"
        + System.currentTimeMillis()
        + "-"
        + lockIdGenerator.incrementAndGet();
  }

  /** 获取统计信息 */
  public LockStatistics getStatistics() {
    return new LockStatistics(
        localLocks.size(),
        localLocks.values().stream().mapToLong(LockInfo::getHoldTime).average().orElse(0.0),
        lockIdGenerator.get());
  }

  /** 锁统计信息 */
  public static class LockStatistics {
    private final int activeLocks;
    private final double averageHoldTime;
    private final long totalLocks;

    public LockStatistics(int activeLocks, double averageHoldTime, long totalLocks) {
      this.activeLocks = activeLocks;
      this.averageHoldTime = averageHoldTime;
      this.totalLocks = totalLocks;
    }

    public int getActiveLocks() {
      return activeLocks;
    }

    public double getAverageHoldTime() {
      return averageHoldTime;
    }

    public long getTotalLocks() {
      return totalLocks;
    }
  }

  @PreDestroy
  public void shutdown() {
    if (lockWatchdog != null && !lockWatchdog.isShutdown()) {
      lockWatchdog.shutdown();
      try {
        if (!lockWatchdog.awaitTermination(5, TimeUnit.SECONDS)) {
          lockWatchdog.shutdownNow();
        }
      } catch (InterruptedException e) {
        lockWatchdog.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }

    // 释放所有本地锁
    localLocks
        .values()
        .forEach(
            lockInfo -> {
              try {
                releaseLock(lockInfo.getLockKey(), lockInfo.getLockId());
                stopAutoRenewal(lockInfo);
              } catch (Exception e) {
                LogUtil.logError(
                    "DISTRIBUTED_LOCK_MANAGER",
                    "",
                    "LOCK_SHUTDOWN_FAILED",
                    String.format("关闭时释放锁失败: %s", lockInfo.getLockKey()),
                    e);
              }
            });

    localLocks.clear();

    LogUtil.logInfo("DISTRIBUTED_LOCK_MANAGER", "", "分布式锁管理器已关闭");
  }
}
