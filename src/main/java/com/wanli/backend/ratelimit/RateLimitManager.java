package com.wanli.backend.ratelimit;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.LogUtil;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/** API限流管理器 提供多种限流策略：令牌桶、滑动窗口、固定窗口、漏桶等 */
@Component
public class RateLimitManager {

  @Autowired private CacheUtil cacheUtil;

  @Autowired private ApplicationConfigManager configManager;

  private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
  private final AtomicLong requestCounter = new AtomicLong(0);
  private ScheduledExecutorService cleanupExecutor;

  // 限流前缀
  private static final String RATE_LIMIT_PREFIX = "rate_limit:";
  private static final String TOKEN_BUCKET_PREFIX = "token_bucket:";
  private static final String SLIDING_WINDOW_PREFIX = "sliding_window:";
  private static final String FIXED_WINDOW_PREFIX = "fixed_window:";
  private static final String LEAKY_BUCKET_PREFIX = "leaky_bucket:";

  /** 限流算法类型 */
  public enum LimitType {
    TOKEN_BUCKET("token_bucket", "令牌桶"),
    SLIDING_WINDOW("sliding_window", "滑动窗口"),
    FIXED_WINDOW("fixed_window", "固定窗口"),
    LEAKY_BUCKET("leaky_bucket", "漏桶"),
    CONCURRENT("concurrent", "并发数限制");

    private final String code;
    private final String description;

    LimitType(String code, String description) {
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

  /** 限流级别 */
  public enum LimitLevel {
    GLOBAL("global", "全局限流"),
    USER("user", "用户限流"),
    IP("ip", "IP限流"),
    API("api", "接口限流"),
    CUSTOM("custom", "自定义限流");

    private final String code;
    private final String description;

    LimitLevel(String code, String description) {
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

  /** 限流配置 */
  public static class RateLimitConfig {
    private LimitType limitType = LimitType.TOKEN_BUCKET;
    private LimitLevel limitLevel = LimitLevel.API;
    private long capacity = 100; // 容量
    private long refillRate = 10; // 补充速率（每秒）
    private long windowSize = 60; // 窗口大小（秒）
    private long maxConcurrent = 10; // 最大并发数
    private boolean enableBlacklist = false; // 启用黑名单
    private boolean enableWhitelist = false; // 启用白名单
    private long blockDuration = 300; // 阻塞时长（秒）

    // Getters and Setters
    public LimitType getLimitType() {
      return limitType;
    }

    public void setLimitType(LimitType limitType) {
      this.limitType = limitType;
    }

    public LimitLevel getLimitLevel() {
      return limitLevel;
    }

    public void setLimitLevel(LimitLevel limitLevel) {
      this.limitLevel = limitLevel;
    }

    public long getCapacity() {
      return capacity;
    }

    public void setCapacity(long capacity) {
      this.capacity = capacity;
    }

    public long getRefillRate() {
      return refillRate;
    }

    public void setRefillRate(long refillRate) {
      this.refillRate = refillRate;
    }

    public long getWindowSize() {
      return windowSize;
    }

    public void setWindowSize(long windowSize) {
      this.windowSize = windowSize;
    }

    public long getMaxConcurrent() {
      return maxConcurrent;
    }

    public void setMaxConcurrent(long maxConcurrent) {
      this.maxConcurrent = maxConcurrent;
    }

    public boolean isEnableBlacklist() {
      return enableBlacklist;
    }

    public void setEnableBlacklist(boolean enableBlacklist) {
      this.enableBlacklist = enableBlacklist;
    }

    public boolean isEnableWhitelist() {
      return enableWhitelist;
    }

    public void setEnableWhitelist(boolean enableWhitelist) {
      this.enableWhitelist = enableWhitelist;
    }

    public long getBlockDuration() {
      return blockDuration;
    }

    public void setBlockDuration(long blockDuration) {
      this.blockDuration = blockDuration;
    }
  }

  /** 限流结果 */
  public static class RateLimitResult {
    private final boolean allowed;
    private final long remainingTokens;
    private final long resetTime;
    private final String reason;
    private final long waitTime;

    public RateLimitResult(
        boolean allowed, long remainingTokens, long resetTime, String reason, long waitTime) {
      this.allowed = allowed;
      this.remainingTokens = remainingTokens;
      this.resetTime = resetTime;
      this.reason = reason;
      this.waitTime = waitTime;
    }

    public boolean isAllowed() {
      return allowed;
    }

    public long getRemainingTokens() {
      return remainingTokens;
    }

    public long getResetTime() {
      return resetTime;
    }

    public String getReason() {
      return reason;
    }

    public long getWaitTime() {
      return waitTime;
    }
  }

  /** 限流器接口 */
  public interface RateLimiter {
    RateLimitResult tryAcquire(String key, int permits);

    void reset(String key);

    RateLimitStatistics getStatistics();
  }

  /** 令牌桶限流器 */
  public class TokenBucketLimiter implements RateLimiter {
    private final RateLimitConfig config;

    public TokenBucketLimiter(RateLimitConfig config) {
      this.config = config;
    }

    @Override
    public RateLimitResult tryAcquire(String key, int permits) {
      String bucketKey = TOKEN_BUCKET_PREFIX + key;
      String tokensKey = bucketKey + ":tokens";
      String lastRefillKey = bucketKey + ":last_refill";

      try {
        long currentTime = System.currentTimeMillis();

        // 获取当前令牌数和上次补充时间
        String tokensStr = cacheUtil.get(tokensKey);
        String lastRefillStr = cacheUtil.get(lastRefillKey);

        long currentTokens = tokensStr != null ? Long.parseLong(tokensStr) : config.getCapacity();
        long lastRefillTime = lastRefillStr != null ? Long.parseLong(lastRefillStr) : currentTime;

        // 计算需要补充的令牌数
        long timePassed = currentTime - lastRefillTime;
        long tokensToAdd = (timePassed / 1000) * config.getRefillRate();

        // 更新令牌数（不超过容量）
        currentTokens = Math.min(config.getCapacity(), currentTokens + tokensToAdd);

        if (currentTokens >= permits) {
          // 有足够令牌，扣除并允许请求
          currentTokens -= permits;

          // 更新缓存
          cacheUtil.setWithExpire(
              tokensKey, String.valueOf(currentTokens), config.getWindowSize() * 2);
          cacheUtil.setWithExpire(
              lastRefillKey, String.valueOf(currentTime), config.getWindowSize() * 2);

          return new RateLimitResult(
              true,
              currentTokens,
              currentTime + (config.getCapacity() - currentTokens) * 1000 / config.getRefillRate(),
              "令牌桶允许",
              0);
        } else {
          // 令牌不足，拒绝请求
          long waitTime = (permits - currentTokens) * 1000 / config.getRefillRate();
          return new RateLimitResult(
              false, currentTokens, currentTime + waitTime, "令牌桶限流", waitTime);
        }

      } catch (Exception e) {
        LogUtil.logError(
            "RATE_LIMIT_MANAGER", "", "TOKEN_BUCKET_ERROR", String.format("令牌桶限流检查失败: %s", key), e);
        return new RateLimitResult(true, 0, 0, "限流检查异常，默认允许", 0);
      }
    }

    @Override
    public void reset(String key) {
      String bucketKey = TOKEN_BUCKET_PREFIX + key;
      cacheUtil.delete(bucketKey + ":tokens");
      cacheUtil.delete(bucketKey + ":last_refill");
    }

    @Override
    public RateLimitStatistics getStatistics() {
      return new RateLimitStatistics("TokenBucket", 0, 0, 0);
    }
  }

  /** 滑动窗口限流器 */
  public class SlidingWindowLimiter implements RateLimiter {
    private final RateLimitConfig config;

    public SlidingWindowLimiter(RateLimitConfig config) {
      this.config = config;
    }

    @Override
    public RateLimitResult tryAcquire(String key, int permits) {
      String windowKey = SLIDING_WINDOW_PREFIX + key;
      long currentTime = System.currentTimeMillis();
      long windowStart = currentTime - config.getWindowSize() * 1000;

      try {
        // 使用Lua脚本实现原子操作
        String script =
            "local key = KEYS[1]\n"
                + "local window_start = ARGV[1]\n"
                + "local current_time = ARGV[2]\n"
                + "local capacity = tonumber(ARGV[3])\n"
                + "local permits = tonumber(ARGV[4])\n"
                + "\n"
                + "-- 清理过期记录\n"
                + "redis.call('ZREMRANGEBYSCORE', key, 0, window_start)\n"
                + "\n"
                + "-- 获取当前窗口内的请求数\n"
                + "local current_count = redis.call('ZCARD', key)\n"
                + "\n"
                + "if current_count + permits <= capacity then\n"
                + "    -- 添加当前请求\n"
                + "    for i = 1, permits do\n"
                + "        redis.call('ZADD', key, current_time, current_time .. '-' .. i)\n"
                + "    end\n"
                + "    -- 设置过期时间\n"
                + "    redis.call('EXPIRE', key, "
                + (config.getWindowSize() * 2)
                + ")\n"
                + "    return {1, capacity - current_count - permits, 0}\n"
                + "else\n"
                + "    return {0, capacity - current_count, 1000}\n"
                + "end";

        Object result =
            cacheUtil.executeScript(
                script,
                new String[] {windowKey},
                new String[] {
                  String.valueOf(windowStart),
                  String.valueOf(currentTime),
                  String.valueOf(config.getCapacity()),
                  String.valueOf(permits)
                });

        if (result instanceof java.util.List) {
          @SuppressWarnings("unchecked")
          java.util.List<Object> resultList = (java.util.List<Object>) result;
          boolean allowed = "1".equals(String.valueOf(resultList.get(0)));
          long remaining = Long.parseLong(String.valueOf(resultList.get(1)));
          long waitTime = Long.parseLong(String.valueOf(resultList.get(2)));

          return new RateLimitResult(
              allowed,
              remaining,
              currentTime + config.getWindowSize() * 1000,
              allowed ? "滑动窗口允许" : "滑动窗口限流",
              waitTime);
        }

        return new RateLimitResult(true, 0, 0, "限流检查异常，默认允许", 0);

      } catch (Exception e) {
        LogUtil.logError(
            "RATE_LIMIT_MANAGER",
            "",
            "SLIDING_WINDOW_ERROR",
            String.format("滑动窗口限流检查失败: %s", key),
            e);
        return new RateLimitResult(true, 0, 0, "限流检查异常，默认允许", 0);
      }
    }

    @Override
    public void reset(String key) {
      String windowKey = SLIDING_WINDOW_PREFIX + key;
      cacheUtil.delete(windowKey);
    }

    @Override
    public RateLimitStatistics getStatistics() {
      return new RateLimitStatistics("SlidingWindow", 0, 0, 0);
    }
  }

  /** 固定窗口限流器 */
  public class FixedWindowLimiter implements RateLimiter {
    private final RateLimitConfig config;

    public FixedWindowLimiter(RateLimitConfig config) {
      this.config = config;
    }

    @Override
    public RateLimitResult tryAcquire(String key, int permits) {
      long currentTime = System.currentTimeMillis();
      long windowStart =
          (currentTime / (config.getWindowSize() * 1000)) * (config.getWindowSize() * 1000);
      String windowKey = FIXED_WINDOW_PREFIX + key + ":" + windowStart;

      try {
        // 获取当前窗口的计数
        String countStr = cacheUtil.get(windowKey);
        long currentCount = countStr != null ? Long.parseLong(countStr) : 0;

        if (currentCount + permits <= config.getCapacity()) {
          // 增加计数
          long newCount = cacheUtil.increment(windowKey, permits);

          // 设置过期时间
          long ttl = windowStart + config.getWindowSize() * 1000 - currentTime + 1000;
          cacheUtil.expire(windowKey, (int) (ttl / 1000));

          return new RateLimitResult(
              true,
              config.getCapacity() - newCount,
              windowStart + config.getWindowSize() * 1000,
              "固定窗口允许",
              0);
        } else {
          // 超出限制
          long resetTime = windowStart + config.getWindowSize() * 1000;
          long waitTime = resetTime - currentTime;

          return new RateLimitResult(
              false, config.getCapacity() - currentCount, resetTime, "固定窗口限流", waitTime);
        }

      } catch (Exception e) {
        LogUtil.logError(
            "RATE_LIMIT_MANAGER",
            "",
            "FIXED_WINDOW_ERROR",
            String.format("固定窗口限流检查失败: %s", key),
            e);
        return new RateLimitResult(true, 0, 0, "限流检查异常，默认允许", 0);
      }
    }

    @Override
    public void reset(String key) {
      // 固定窗口会自动过期，这里可以强制清理
      String pattern = FIXED_WINDOW_PREFIX + key + ":*";
      // 由于CacheUtil没有deleteByPattern方法，这里简化处理
      // cacheUtil.delete(pattern);
    }

    @Override
    public RateLimitStatistics getStatistics() {
      return new RateLimitStatistics("FixedWindow", 0, 0, 0);
    }
  }

  /** 漏桶限流器 */
  public class LeakyBucketLimiter implements RateLimiter {
    private final RateLimitConfig config;

    public LeakyBucketLimiter(RateLimitConfig config) {
      this.config = config;
    }

    @Override
    public RateLimitResult tryAcquire(String key, int permits) {
      String bucketKey = LEAKY_BUCKET_PREFIX + key;
      String volumeKey = bucketKey + ":volume";
      String lastLeakKey = bucketKey + ":last_leak";

      try {
        long currentTime = System.currentTimeMillis();

        // 获取当前水量和上次漏水时间
        String volumeStr = cacheUtil.get(volumeKey);
        String lastLeakStr = cacheUtil.get(lastLeakKey);

        long currentVolume = volumeStr != null ? Long.parseLong(volumeStr) : 0;
        long lastLeakTime = lastLeakStr != null ? Long.parseLong(lastLeakStr) : currentTime;

        // 计算漏出的水量
        long timePassed = currentTime - lastLeakTime;
        long volumeToLeak = (timePassed / 1000) * config.getRefillRate();

        // 更新水量（不小于0）
        currentVolume = Math.max(0, currentVolume - volumeToLeak);

        if (currentVolume + permits <= config.getCapacity()) {
          // 桶未满，加入请求
          currentVolume += permits;

          // 更新缓存
          cacheUtil.setWithExpire(
              volumeKey, String.valueOf(currentVolume), config.getWindowSize() * 2);
          cacheUtil.setWithExpire(
              lastLeakKey, String.valueOf(currentTime), config.getWindowSize() * 2);

          // 计算处理时间（按漏桶速率）
          long processTime = currentVolume * 1000 / config.getRefillRate();

          return new RateLimitResult(
              true,
              config.getCapacity() - currentVolume,
              currentTime + processTime,
              "漏桶允许",
              processTime);
        } else {
          // 桶满，拒绝请求
          return new RateLimitResult(
              false,
              config.getCapacity() - currentVolume,
              currentTime + (currentVolume * 1000 / config.getRefillRate()),
              "漏桶限流",
              (currentVolume * 1000 / config.getRefillRate()));
        }

      } catch (Exception e) {
        LogUtil.logError(
            "RATE_LIMIT_MANAGER", "", "LEAKY_BUCKET_ERROR", String.format("漏桶限流检查失败: %s", key), e);
        return new RateLimitResult(true, 0, 0, "限流检查异常，默认允许", 0);
      }
    }

    @Override
    public void reset(String key) {
      String bucketKey = LEAKY_BUCKET_PREFIX + key;
      cacheUtil.delete(bucketKey + ":volume");
      cacheUtil.delete(bucketKey + ":last_leak");
    }

    @Override
    public RateLimitStatistics getStatistics() {
      return new RateLimitStatistics("LeakyBucket", 0, 0, 0);
    }
  }

  /** 限流统计信息 */
  public static class RateLimitStatistics {
    private final String limiterType;
    private final long totalRequests;
    private final long allowedRequests;
    private final long blockedRequests;

    public RateLimitStatistics(
        String limiterType, long totalRequests, long allowedRequests, long blockedRequests) {
      this.limiterType = limiterType;
      this.totalRequests = totalRequests;
      this.allowedRequests = allowedRequests;
      this.blockedRequests = blockedRequests;
    }

    public String getLimiterType() {
      return limiterType;
    }

    public long getTotalRequests() {
      return totalRequests;
    }

    public long getAllowedRequests() {
      return allowedRequests;
    }

    public long getBlockedRequests() {
      return blockedRequests;
    }

    public double getBlockRate() {
      return totalRequests > 0 ? (double) blockedRequests / totalRequests : 0.0;
    }
  }

  @PostConstruct
  public void initialize() {
    // 初始化清理任务
    cleanupExecutor =
        Executors.newScheduledThreadPool(
            1,
            r -> {
              Thread t = new Thread(r, "RateLimit-Cleanup");
              t.setDaemon(true);
              return t;
            });

    // 启动清理任务
    cleanupExecutor.scheduleWithFixedDelay(this::cleanupExpiredLimiters, 60, 60, TimeUnit.SECONDS);

    LogUtil.logInfo("RATE_LIMIT_MANAGER", "", "API限流管理器初始化完成");
  }

  /** 检查限流 */
  public RateLimitResult checkLimit(String key, RateLimitConfig config) {
    return checkLimit(key, 1, config);
  }

  /** 检查限流（指定许可数） */
  public RateLimitResult checkLimit(String key, int permits, RateLimitConfig config) {
    // 检查白名单
    if (config.isEnableWhitelist() && isInWhitelist(key)) {
      return new RateLimitResult(true, Long.MAX_VALUE, 0, "白名单允许", 0);
    }

    // 检查黑名单
    if (config.isEnableBlacklist() && isInBlacklist(key)) {
      return new RateLimitResult(
          false,
          0,
          System.currentTimeMillis() + config.getBlockDuration() * 1000,
          "黑名单阻塞",
          config.getBlockDuration() * 1000);
    }

    // 获取或创建限流器
    String limiterKey = config.getLimitType().getCode() + ":" + key;
    RateLimiter limiter = rateLimiters.computeIfAbsent(limiterKey, k -> createLimiter(config));

    // 执行限流检查
    RateLimitResult result = limiter.tryAcquire(key, permits);

    // 记录请求
    requestCounter.incrementAndGet();

    // 记录日志
    if (!result.isAllowed()) {
      LogUtil.logWarn(
          "RATE_LIMIT_MANAGER",
          "",
          String.format(
              "限流触发: key=%s, type=%s, reason=%s, waitTime=%dms",
              key,
              config.getLimitType().getDescription(),
              result.getReason(),
              result.getWaitTime()));
    }

    return result;
  }

  /** 重置限流 */
  public void resetLimit(String key, LimitType limitType) {
    String limiterKey = limitType.getCode() + ":" + key;
    RateLimiter limiter = rateLimiters.get(limiterKey);
    if (limiter != null) {
      limiter.reset(key);
      LogUtil.logInfo(
          "RATE_LIMIT_MANAGER",
          "",
          String.format("重置限流: key=%s, type=%s", key, limitType.getDescription()));
    }
  }

  /** 添加到黑名单 */
  public void addToBlacklist(String key, long duration, TimeUnit timeUnit) {
    String blacklistKey = "blacklist:" + key;
    cacheUtil.setWithExpire(blacklistKey, "blocked", timeUnit.toSeconds(duration));

    LogUtil.logWarn(
        "RATE_LIMIT_MANAGER",
        "",
        String.format(
            "添加到黑名单: key=%s, duration=%d%s", key, duration, timeUnit.name().toLowerCase()));
  }

  /** 从黑名单移除 */
  public void removeFromBlacklist(String key) {
    String blacklistKey = "blacklist:" + key;
    cacheUtil.delete(blacklistKey);

    LogUtil.logInfo("RATE_LIMIT_MANAGER", "", String.format("从黑名单移除: key=%s", key));
  }

  /** 添加到白名单 */
  public void addToWhitelist(String key) {
    String whitelistKey = "whitelist:" + key;
    cacheUtil.setWithExpire(whitelistKey, "allowed", 86400); // 24小时过期

    LogUtil.logInfo("RATE_LIMIT_MANAGER", "", String.format("添加到白名单: key=%s", key));
  }

  /** 从白名单移除 */
  public void removeFromWhitelist(String key) {
    String whitelistKey = "whitelist:" + key;
    cacheUtil.delete(whitelistKey);

    LogUtil.logInfo("RATE_LIMIT_MANAGER", "", String.format("从白名单移除: key=%s", key));
  }

  /** 检查是否在黑名单 */
  private boolean isInBlacklist(String key) {
    String blacklistKey = "blacklist:" + key;
    return cacheUtil.exists(blacklistKey);
  }

  /** 检查是否在白名单 */
  private boolean isInWhitelist(String key) {
    String whitelistKey = "whitelist:" + key;
    return cacheUtil.exists(whitelistKey);
  }

  /** 创建限流器 */
  private RateLimiter createLimiter(RateLimitConfig config) {
    switch (config.getLimitType()) {
      case TOKEN_BUCKET:
        return new TokenBucketLimiter(config);
      case SLIDING_WINDOW:
        return new SlidingWindowLimiter(config);
      case FIXED_WINDOW:
        return new FixedWindowLimiter(config);
      case LEAKY_BUCKET:
        return new LeakyBucketLimiter(config);
      default:
        return new TokenBucketLimiter(config);
    }
  }

  /** 清理过期的限流器 */
  private void cleanupExpiredLimiters() {
    try {
      // 这里可以实现更复杂的清理逻辑
      // 目前简单地清理一些不活跃的限流器
      if (rateLimiters.size() > 1000) {
        rateLimiters.clear();
        LogUtil.logInfo("RATE_LIMIT_MANAGER", "", "清理限流器缓存");
      }
    } catch (Exception e) {
      LogUtil.logError("RATE_LIMIT_MANAGER", "", "CLEANUP_ERROR", "清理限流器失败", e);
    }
  }

  /** 获取统计信息 */
  public GlobalRateLimitStatistics getGlobalStatistics() {
    return new GlobalRateLimitStatistics(
        rateLimiters.size(),
        requestCounter.get(),
        rateLimiters.entrySet().stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getStatistics())));
  }

  /** 全局限流统计信息 */
  public static class GlobalRateLimitStatistics {
    private final int activeLimiters;
    private final long totalRequests;
    private final Map<String, RateLimitStatistics> limiterStatistics;

    public GlobalRateLimitStatistics(
        int activeLimiters,
        long totalRequests,
        Map<String, RateLimitStatistics> limiterStatistics) {
      this.activeLimiters = activeLimiters;
      this.totalRequests = totalRequests;
      this.limiterStatistics = limiterStatistics;
    }

    public int getActiveLimiters() {
      return activeLimiters;
    }

    public long getTotalRequests() {
      return totalRequests;
    }

    public Map<String, RateLimitStatistics> getLimiterStatistics() {
      return limiterStatistics;
    }
  }

  /** 构建限流键 */
  public static String buildLimitKey(LimitLevel level, String... parts) {
    StringBuilder keyBuilder = new StringBuilder(level.getCode());
    for (String part : parts) {
      if (part != null && !part.isEmpty()) {
        keyBuilder.append(":").append(part);
      }
    }
    return keyBuilder.toString();
  }

  /** 创建默认配置 */
  public static RateLimitConfig createDefaultConfig(LimitType limitType) {
    RateLimitConfig config = new RateLimitConfig();
    config.setLimitType(limitType);

    switch (limitType) {
      case TOKEN_BUCKET:
        config.setCapacity(100);
        config.setRefillRate(10);
        break;
      case SLIDING_WINDOW:
        config.setCapacity(100);
        config.setWindowSize(60);
        break;
      case FIXED_WINDOW:
        config.setCapacity(100);
        config.setWindowSize(60);
        break;
      case LEAKY_BUCKET:
        config.setCapacity(100);
        config.setRefillRate(10);
        break;
      case CONCURRENT:
        config.setMaxConcurrent(10);
        break;
    }

    return config;
  }

  @PreDestroy
  public void shutdown() {
    if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
      cleanupExecutor.shutdown();
      try {
        if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
          cleanupExecutor.shutdownNow();
        }
      } catch (InterruptedException e) {
        cleanupExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }

    rateLimiters.clear();

    LogUtil.logInfo("RATE_LIMIT_MANAGER", "", "API限流管理器已关闭");
  }
}
