package com.wanli.backend.cache;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.monitor.MetricsCollector;
import com.wanli.backend.util.LogUtil;

/** 缓存性能优化器 分析缓存使用模式并提供优化建议 */
@Component
@ConditionalOnBean(RedisTemplate.class)
public class CacheOptimizer {

  private final ApplicationConfigManager configManager;
  private final MetricsCollector metricsCollector;
  private final RedisTemplate<String, Object> redisTemplate;

  // 缓存访问统计
  private final ConcurrentHashMap<String, CacheAccessStats> accessStats = new ConcurrentHashMap<>();

  // 缓存模式分析
  private final ConcurrentHashMap<String, CachePattern> cachePatterns = new ConcurrentHashMap<>();

  // 热点数据追踪
  private final ConcurrentHashMap<String, HotKeyStats> hotKeyStats = new ConcurrentHashMap<>();

  // 优化建议缓存
  private volatile CacheOptimizationReport lastOptimizationReport;

  public CacheOptimizer(
      ApplicationConfigManager configManager,
      MetricsCollector metricsCollector,
      RedisTemplate<String, Object> redisTemplate) {
    this.configManager = configManager;
    this.metricsCollector = metricsCollector;
    this.redisTemplate = redisTemplate;
  }

  /** 记录缓存访问 */
  public void recordCacheAccess(
      String key, CacheOperation operation, boolean hit, long responseTime) {
    if (!configManager.getCacheConfig().isOptimizationEnabled()) {
      return;
    }

    // 更新访问统计
    CacheAccessStats stats = accessStats.computeIfAbsent(key, k -> new CacheAccessStats(k));
    stats.recordAccess(operation, hit, responseTime);

    // 更新热点统计
    updateHotKeyStats(key, operation);

    // 分析访问模式
    analyzeAccessPattern(key, operation, hit);

    // 记录指标
    metricsCollector.recordCacheOperation(operation.name(), hit);
    metricsCollector.recordOperationTime("cache_" + operation.name().toLowerCase(), responseTime);
  }

  /** 更新热点Key统计 */
  private void updateHotKeyStats(String key, CacheOperation operation) {
    HotKeyStats stats = hotKeyStats.computeIfAbsent(key, k -> new HotKeyStats(k));
    stats.incrementAccess(operation);

    // 检查是否成为热点Key
    if (stats.isHotKey()) {
      metricsCollector.incrementCounter("cache_hot_key_detected");
      Map<String, Object> context = new HashMap<>();
      context.put("key", key);
      context.put("accessCount", stats.getTotalAccess());
      LogUtil.logBusiness("CACHE_HOT_KEY", context);
    }
  }

  /** 分析访问模式 */
  private void analyzeAccessPattern(String key, CacheOperation operation, boolean hit) {
    CachePattern pattern = cachePatterns.computeIfAbsent(key, k -> new CachePattern(k));
    pattern.recordAccess(operation, hit);

    // 分析模式类型
    PatternType patternType = pattern.analyzePattern();
    if (patternType != PatternType.UNKNOWN) {
      metricsCollector.incrementCounter("cache_pattern_" + patternType.name().toLowerCase());
    }
  }

  /** 执行缓存优化分析 */
  public CacheOptimizationReport performOptimizationAnalysis() {
    CacheOptimizationReport report = new CacheOptimizationReport();
    report.timestamp = LocalDateTime.now();
    report.suggestions = new ArrayList<>();
    report.hotKeys = new ArrayList<>();
    report.inefficientKeys = new ArrayList<>();
    report.patternAnalysis = new HashMap<>();

    try {
      // 分析热点Key
      analyzeHotKeys(report);

      // 分析低效Key
      analyzeInefficientKeys(report);

      // 分析访问模式
      analyzeAccessPatterns(report);

      // 分析内存使用
      analyzeMemoryUsage(report);

      // 生成优化建议
      generateOptimizationSuggestions(report);

      // 缓存报告
      lastOptimizationReport = report;

      Map<String, Object> context = new HashMap<>();
      context.put("suggestionCount", report.suggestions.size());
      LogUtil.logBusiness("CACHE_OPTIMIZATION_ANALYSIS", context);

    } catch (Exception e) {
      LogUtil.logError("CACHE_OPTIMIZATION_ERROR", "", "CACHE_002", "缓存优化分析失败", e);
    }

    return report;
  }

  /** 分析热点Key */
  private void analyzeHotKeys(CacheOptimizationReport report) {
    List<HotKeyStats> hotKeys =
        hotKeyStats.values().stream()
            .filter(HotKeyStats::isHotKey)
            .sorted((a, b) -> Long.compare(b.getTotalAccess(), a.getTotalAccess()))
            .limit(20) // 取前20个热点Key
            .collect(Collectors.toList());

    for (HotKeyStats hotKey : hotKeys) {
      HotKeyInfo info = new HotKeyInfo();
      info.key = hotKey.getKey();
      info.accessCount = hotKey.getTotalAccess();
      info.readCount = hotKey.getReadCount();
      info.writeCount = hotKey.getWriteCount();
      info.lastAccessTime = hotKey.getLastAccessTime();

      report.hotKeys.add(info);
    }
  }

  /** 分析低效Key */
  private void analyzeInefficientKeys(CacheOptimizationReport report) {
    for (CacheAccessStats stats : accessStats.values()) {
      if (stats.isInefficient()) {
        InefficientKeyInfo info = new InefficientKeyInfo();
        info.key = stats.getKey();
        info.hitRate = stats.getHitRate();
        info.averageResponseTime = stats.getAverageResponseTime();
        info.totalAccess = stats.getTotalAccess();
        info.reason = determineInefficiencyReason(stats);

        report.inefficientKeys.add(info);
      }
    }
  }

  /** 分析访问模式 */
  private void analyzeAccessPatterns(CacheOptimizationReport report) {
    Map<PatternType, Long> patternCounts = new HashMap<>();

    for (CachePattern pattern : cachePatterns.values()) {
      PatternType type = pattern.analyzePattern();
      patternCounts.merge(type, 1L, Long::sum);
    }

    report.patternAnalysis = patternCounts;
  }

  /** 分析内存使用 */
  private void analyzeMemoryUsage(CacheOptimizationReport report) {
    try {
      // 获取Redis内存信息
      String memoryInfo =
          redisTemplate.getConnectionFactory().getConnection().info("memory").toString();

      // 解析内存使用情况
      report.memoryUsage = parseMemoryInfo(memoryInfo);

    } catch (Exception e) {
      LogUtil.logWarn("CACHE_MEMORY_ANALYSIS", "", "缓存内存分析失败: " + e.getMessage());
    }
  }

  /** 生成优化建议 */
  private void generateOptimizationSuggestions(CacheOptimizationReport report) {
    // 热点Key优化建议
    if (!report.hotKeys.isEmpty()) {
      report.suggestions.add(
          new OptimizationSuggestion(
              SuggestionType.HOT_KEY_OPTIMIZATION,
              "检测到 " + report.hotKeys.size() + " 个热点Key，建议考虑数据分片或读写分离",
              SuggestionPriority.HIGH));
    }

    // 低效Key优化建议
    if (!report.inefficientKeys.isEmpty()) {
      long lowHitRateKeys =
          report.inefficientKeys.stream().filter(key -> key.hitRate < 0.5).count();

      if (lowHitRateKeys > 0) {
        report.suggestions.add(
            new OptimizationSuggestion(
                SuggestionType.HIT_RATE_OPTIMIZATION,
                "发现 " + lowHitRateKeys + " 个低命中率Key，建议检查缓存策略",
                SuggestionPriority.MEDIUM));
      }
    }

    // 访问模式优化建议
    Long writeHeavyPatterns = report.patternAnalysis.get(PatternType.WRITE_HEAVY);
    if (writeHeavyPatterns != null && writeHeavyPatterns > 10) {
      report.suggestions.add(
          new OptimizationSuggestion(
              SuggestionType.PATTERN_OPTIMIZATION,
              "检测到 " + writeHeavyPatterns + " 个写密集型模式，建议优化写入策略",
              SuggestionPriority.MEDIUM));
    }

    // 内存使用优化建议
    if (report.memoryUsage != null && report.memoryUsage.usagePercent > 80) {
      report.suggestions.add(
          new OptimizationSuggestion(
              SuggestionType.MEMORY_OPTIMIZATION,
              "缓存内存使用率过高 ("
                  + String.format("%.1f", report.memoryUsage.usagePercent)
                  + "%)，建议清理过期数据或增加内存",
              SuggestionPriority.HIGH));
    }
  }

  /** 确定低效原因 */
  private String determineInefficiencyReason(CacheAccessStats stats) {
    if (stats.getHitRate() < 0.3) {
      return "命中率过低";
    }
    if (stats.getAverageResponseTime() > 100) {
      return "响应时间过长";
    }
    if (stats.getWriteRatio() > 0.8) {
      return "写操作过多";
    }
    return "未知原因";
  }

  /** 解析内存信息 */
  private MemoryUsageInfo parseMemoryInfo(String memoryInfo) {
    MemoryUsageInfo info = new MemoryUsageInfo();

    // 简单解析，实际实现需要更复杂的解析逻辑
    String[] lines = memoryInfo.split("\n");
    for (String line : lines) {
      if (line.startsWith("used_memory:")) {
        info.usedMemory = Long.parseLong(line.split(":")[1].trim());
      } else if (line.startsWith("maxmemory:")) {
        info.maxMemory = Long.parseLong(line.split(":")[1].trim());
      }
    }

    if (info.maxMemory > 0) {
      info.usagePercent = (double) info.usedMemory / info.maxMemory * 100;
    }

    return info;
  }

  /** 应用优化建议 */
  public void applyOptimizations(List<String> suggestionIds) {
    if (lastOptimizationReport == null) {
      LogUtil.logWarn("CACHE_OPTIMIZATION_APPLY", "", "没有可用的优化报告");
      return;
    }

    for (String suggestionId : suggestionIds) {
      try {
        applySingleOptimization(suggestionId);
        metricsCollector.incrementCounter("cache_optimizations_applied");

      } catch (Exception e) {
        LogUtil.logError(
            "CACHE_OPTIMIZATION_APPLY_ERROR", "", "CACHE_001", "应用优化建议失败: " + suggestionId, e);
      }
    }
  }

  /** 应用单个优化建议 */
  private void applySingleOptimization(String suggestionId) {
    // 这里实现具体的优化逻辑
    // 例如：清理过期Key、调整TTL、优化数据结构等
    Map<String, Object> context = new HashMap<>();
    context.put("suggestionId", suggestionId);
    LogUtil.logBusiness("CACHE_OPTIMIZATION_APPLIED", context);
  }

  /** 获取最新的优化报告 */
  public CacheOptimizationReport getLatestOptimizationReport() {
    return lastOptimizationReport;
  }

  /** 重置统计数据 */
  public void resetStatistics() {
    accessStats.clear();
    cachePatterns.clear();
    hotKeyStats.clear();
    lastOptimizationReport = null;

    Map<String, Object> context = new HashMap<>();
    LogUtil.logBusiness("CACHE_STATS_RESET", context);
  }

  /** 定期执行优化分析 */
  @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
  public void scheduledOptimizationAnalysis() {
    if (!configManager.getCacheConfig().isOptimizationEnabled()) {
      return;
    }

    try {
      CacheOptimizationReport report = performOptimizationAnalysis();

      // 如果有高优先级建议，记录警告日志
      long highPrioritySuggestions =
          report.suggestions.stream().filter(s -> s.priority == SuggestionPriority.HIGH).count();

      if (highPrioritySuggestions > 0) {
        LogUtil.logWarn(
            "CACHE_OPTIMIZATION_ALERT", "", "发现 " + highPrioritySuggestions + " 个高优先级缓存优化建议");
      }

    } catch (Exception e) {
      LogUtil.logError("SCHEDULED_CACHE_OPTIMIZATION", "", "CACHE_002", "定期缓存优化分析失败", e);
    }
  }

  // 枚举定义
  public enum CacheOperation {
    GET,
    SET,
    DELETE,
    EXISTS,
    EXPIRE
  }

  public enum PatternType {
    READ_HEAVY, // 读密集型
    WRITE_HEAVY, // 写密集型
    BALANCED, // 平衡型
    CACHE_ASIDE, // 缓存旁路
    WRITE_THROUGH, // 写透
    UNKNOWN // 未知
  }

  public enum SuggestionType {
    HOT_KEY_OPTIMIZATION,
    HIT_RATE_OPTIMIZATION,
    PATTERN_OPTIMIZATION,
    MEMORY_OPTIMIZATION,
    TTL_OPTIMIZATION
  }

  public enum SuggestionPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  // 数据类定义
  public static class CacheAccessStats {
    private final String key;
    private final LongAdder totalAccess = new LongAdder();
    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder writes = new LongAdder();
    private final LongAdder totalResponseTime = new LongAdder();
    private volatile LocalDateTime lastAccess;

    public CacheAccessStats(String key) {
      this.key = key;
    }

    public void recordAccess(CacheOperation operation, boolean hit, long responseTime) {
      totalAccess.increment();
      totalResponseTime.add(responseTime);
      lastAccess = LocalDateTime.now();

      if (operation == CacheOperation.GET) {
        if (hit) {
          hits.increment();
        } else {
          misses.increment();
        }
      } else if (operation == CacheOperation.SET) {
        writes.increment();
      }
    }

    public String getKey() {
      return key;
    }

    public long getTotalAccess() {
      return totalAccess.sum();
    }

    public double getHitRate() {
      long totalReads = hits.sum() + misses.sum();
      return totalReads > 0 ? (double) hits.sum() / totalReads : 0.0;
    }

    public double getAverageResponseTime() {
      long total = totalAccess.sum();
      return total > 0 ? (double) totalResponseTime.sum() / total : 0.0;
    }

    public double getWriteRatio() {
      long total = totalAccess.sum();
      return total > 0 ? (double) writes.sum() / total : 0.0;
    }

    public boolean isInefficient() {
      return getHitRate() < 0.5 || getAverageResponseTime() > 100 || getWriteRatio() > 0.8;
    }
  }

  public static class CachePattern {
    private final String key;
    private final LongAdder reads = new LongAdder();
    private final LongAdder writes = new LongAdder();
    private final List<LocalDateTime> accessTimes = new ArrayList<>();

    public CachePattern(String key) {
      this.key = key;
    }

    public void recordAccess(CacheOperation operation, boolean hit) {
      synchronized (accessTimes) {
        accessTimes.add(LocalDateTime.now());
        if (accessTimes.size() > 100) {
          accessTimes.remove(0);
        }
      }

      if (operation == CacheOperation.GET) {
        reads.increment();
      } else if (operation == CacheOperation.SET) {
        writes.increment();
      }
    }

    public PatternType analyzePattern() {
      long readCount = reads.sum();
      long writeCount = writes.sum();
      long total = readCount + writeCount;

      if (total == 0) return PatternType.UNKNOWN;

      double readRatio = (double) readCount / total;

      if (readRatio > 0.8) {
        return PatternType.READ_HEAVY;
      } else if (readRatio < 0.2) {
        return PatternType.WRITE_HEAVY;
      } else {
        return PatternType.BALANCED;
      }
    }
  }

  public static class HotKeyStats {
    private final String key;
    private final LongAdder totalAccess = new LongAdder();
    private final LongAdder readCount = new LongAdder();
    private final LongAdder writeCount = new LongAdder();
    private volatile LocalDateTime lastAccessTime;

    public HotKeyStats(String key) {
      this.key = key;
    }

    public void incrementAccess(CacheOperation operation) {
      totalAccess.increment();
      lastAccessTime = LocalDateTime.now();

      if (operation == CacheOperation.GET) {
        readCount.increment();
      } else if (operation == CacheOperation.SET) {
        writeCount.increment();
      }
    }

    public boolean isHotKey() {
      return totalAccess.sum() > 1000; // 访问次数超过1000认为是热点Key
    }

    public String getKey() {
      return key;
    }

    public long getTotalAccess() {
      return totalAccess.sum();
    }

    public long getReadCount() {
      return readCount.sum();
    }

    public long getWriteCount() {
      return writeCount.sum();
    }

    public LocalDateTime getLastAccessTime() {
      return lastAccessTime;
    }
  }

  // 报告相关类
  public static class CacheOptimizationReport {
    public LocalDateTime timestamp;
    public List<OptimizationSuggestion> suggestions;
    public List<HotKeyInfo> hotKeys;
    public List<InefficientKeyInfo> inefficientKeys;
    public Map<PatternType, Long> patternAnalysis;
    public MemoryUsageInfo memoryUsage;
  }

  public static class OptimizationSuggestion {
    public SuggestionType type;
    public String description;
    public SuggestionPriority priority;
    public String id;

    public OptimizationSuggestion(
        SuggestionType type, String description, SuggestionPriority priority) {
      this.type = type;
      this.description = description;
      this.priority = priority;
      this.id = type.name() + "_" + System.currentTimeMillis();
    }
  }

  public static class HotKeyInfo {
    public String key;
    public long accessCount;
    public long readCount;
    public long writeCount;
    public LocalDateTime lastAccessTime;
  }

  public static class InefficientKeyInfo {
    public String key;
    public double hitRate;
    public double averageResponseTime;
    public long totalAccess;
    public String reason;
  }

  public static class MemoryUsageInfo {
    public long usedMemory;
    public long maxMemory;
    public double usagePercent;
  }
}
