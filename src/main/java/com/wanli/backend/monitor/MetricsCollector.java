package com.wanli.backend.monitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.util.LogUtil;

/** 指标收集器 收集和管理应用性能指标 */
@Component
public class MetricsCollector {

  private final ApplicationConfigManager configManager;

  // 性能指标存储
  private final ConcurrentHashMap<String, OperationMetrics> operationMetrics =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AtomicLong> gauges = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, List<Long>> histograms = new ConcurrentHashMap<>();

  // 系统指标
  private final SystemMetrics systemMetrics = new SystemMetrics();

  public MetricsCollector(ApplicationConfigManager configManager) {
    this.configManager = configManager;
  }

  /** 记录操作执行时间 */
  public void recordOperationTime(String operation, long executionTimeMs) {
    if (!configManager.getMonitoringConfig().isMetricsCollectionEnabled()) {
      return;
    }

    operationMetrics
        .computeIfAbsent(operation, k -> new OperationMetrics())
        .recordExecution(executionTimeMs);

    // 检查是否超过慢操作阈值
    long threshold = configManager.getMonitoringConfig().getSlowOperationThresholdMs();
    if (executionTimeMs > threshold) {
      recordSlowOperation(operation, executionTimeMs, threshold);
    }

    // 记录到直方图
    histograms
        .computeIfAbsent("operation_time_" + operation, k -> new ArrayList<>())
        .add(executionTimeMs);
  }

  /** 增加计数器 */
  public void incrementCounter(String counterName) {
    incrementCounter(counterName, 1);
  }

  /** 增加计数器指定值 */
  public void incrementCounter(String counterName, long value) {
    if (!configManager.getMonitoringConfig().isMetricsCollectionEnabled()) {
      return;
    }

    counters.computeIfAbsent(counterName, k -> new LongAdder()).add(value);
  }

  /** 设置仪表值 */
  public void setGauge(String gaugeName, long value) {
    if (!configManager.getMonitoringConfig().isMetricsCollectionEnabled()) {
      return;
    }

    gauges.computeIfAbsent(gaugeName, k -> new AtomicLong()).set(value);
  }

  /** 记录仪表值（别名方法） */
  public void recordGauge(String gaugeName, double value) {
    setGauge(gaugeName, (long) value);
  }

  /** 记录业务事件 */
  public void recordBusinessEvent(String eventType, String details) {
    incrementCounter("business_event_" + eventType);
    Map<String, Object> context = new HashMap<>();
    context.put("eventType", eventType);
    context.put("details", details);
    LogUtil.logBusiness("BUSINESS_METRIC", context);
  }

  /** 记录API调用 */
  public void recordApiCall(String endpoint, String method, int statusCode, long responseTimeMs) {
    String metricName =
        String.format("api_%s_%s", method.toLowerCase(), endpoint.replaceAll("/", "_"));

    recordOperationTime(metricName, responseTimeMs);
    incrementCounter(metricName + "_total");
    incrementCounter(String.format("%s_status_%d", metricName, statusCode));

    if (statusCode >= 400) {
      incrementCounter(metricName + "_errors");
    }
  }

  /** 记录数据库操作 */
  public void recordDatabaseOperation(String operation, String table, long executionTimeMs) {
    String metricName = String.format("db_%s_%s", operation.toLowerCase(), table);

    recordOperationTime(metricName, executionTimeMs);
    incrementCounter(metricName + "_total");

    // 检查慢查询
    long threshold = configManager.getDatabaseConfig().getSlowQueryThresholdMs();
    if (executionTimeMs > threshold) {
      incrementCounter("db_slow_queries");
      LogUtil.logError(
          "SLOW_QUERY",
          "",
          "SLOW_QUERY",
          String.format("慢查询: %s.%s 执行时间 %dms", table, operation, executionTimeMs),
          null);
    }
  }

  /** 记录缓存操作 */
  public void recordCacheOperation(String operation, boolean hit) {
    incrementCounter("cache_" + operation.toLowerCase() + "_total");
    if (hit) {
      incrementCounter("cache_" + operation.toLowerCase() + "_hits");
    } else {
      incrementCounter("cache_" + operation.toLowerCase() + "_misses");
    }
  }

  /** 记录用户活动 */
  public void recordUserActivity(String activity, java.util.UUID userId) {
    incrementCounter("user_activity_" + activity);
    setGauge("last_user_activity_time", System.currentTimeMillis());
  }

  /** 记录系统资源使用情况 */
  public void recordSystemMetrics() {
    Runtime runtime = Runtime.getRuntime();

    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long usedMemory = totalMemory - freeMemory;
    long maxMemory = runtime.maxMemory();

    setGauge("jvm_memory_total", totalMemory);
    setGauge("jvm_memory_used", usedMemory);
    setGauge("jvm_memory_free", freeMemory);
    setGauge("jvm_memory_max", maxMemory);
    setGauge("jvm_memory_usage_percent", (usedMemory * 100) / maxMemory);

    setGauge("jvm_processors", runtime.availableProcessors());

    systemMetrics.updateMetrics();
  }

  /** 获取操作指标 */
  public OperationMetrics getOperationMetrics(String operation) {
    return operationMetrics.get(operation);
  }

  /** 获取计数器值 */
  public long getCounterValue(String counterName) {
    LongAdder counter = counters.get(counterName);
    return counter != null ? counter.sum() : 0;
  }

  /** 获取仪表值 */
  public long getGaugeValue(String gaugeName) {
    AtomicLong gauge = gauges.get(gaugeName);
    return gauge != null ? gauge.get() : 0;
  }

  /** 获取所有指标摘要 */
  public MetricsSummary getMetricsSummary() {
    MetricsSummary summary = new MetricsSummary();

    // 操作指标
    summary.operationMetrics =
        operationMetrics.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getSummary()));

    // 计数器
    summary.counters =
        counters.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().sum()));

    // 仪表
    summary.gauges =
        gauges.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));

    summary.systemMetrics = systemMetrics.getSummary();
    summary.timestamp = LocalDateTime.now();

    return summary;
  }

  /** 重置所有指标 */
  public void resetMetrics() {
    operationMetrics.clear();
    counters.clear();
    gauges.clear();
    histograms.clear();
    systemMetrics.reset();

    Map<String, Object> context = new HashMap<>();
    context.put("message", "所有指标已重置");
    LogUtil.logBusiness("METRICS_RESET", context);
  }

  /** 记录慢操作 */
  private void recordSlowOperation(String operation, long executionTime, long threshold) {
    incrementCounter("slow_operations_total");
    incrementCounter("slow_operation_" + operation);

    LogUtil.logError(
        "SLOW_OPERATION",
        "",
        "SLOW_OPERATION",
        String.format("慢操作: %s 执行时间 %dms 超过阈值 %dms", operation, executionTime, threshold),
        null);
  }

  /** 定期清理历史数据 */
  @Scheduled(fixedRate = 3600000) // 每小时执行一次
  public void cleanupHistoricalData() {
    // 清理直方图中的旧数据，只保留最近1000条记录
    histograms.forEach(
        (key, values) -> {
          if (values.size() > 1000) {
            synchronized (values) {
              if (values.size() > 1000) {
                values.subList(0, values.size() - 1000).clear();
              }
            }
          }
        });

    Map<String, Object> context = new HashMap<>();
    context.put("message", "指标历史数据清理完成");
    LogUtil.logBusiness("METRICS_CLEANUP", context);
  }

  /** 定期记录系统指标 */
  @Scheduled(fixedRate = 60000) // 每分钟执行一次
  public void collectSystemMetrics() {
    recordSystemMetrics();
  }

  /** 操作指标类 */
  public static class OperationMetrics {
    private final LongAdder totalExecutions = new LongAdder();
    private final LongAdder totalTime = new LongAdder();
    private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxTime = new AtomicLong(0);
    private final LongAdder errorCount = new LongAdder();

    public void recordExecution(long executionTime) {
      totalExecutions.increment();
      totalTime.add(executionTime);

      // 更新最小值
      minTime.updateAndGet(current -> Math.min(current, executionTime));

      // 更新最大值
      maxTime.updateAndGet(current -> Math.max(current, executionTime));
    }

    public void recordError() {
      errorCount.increment();
    }

    public OperationSummary getSummary() {
      long executions = totalExecutions.sum();
      long total = totalTime.sum();

      OperationSummary summary = new OperationSummary();
      summary.totalExecutions = executions;
      summary.totalTime = total;
      summary.averageTime = executions > 0 ? total / executions : 0;
      summary.minTime = minTime.get() == Long.MAX_VALUE ? 0 : minTime.get();
      summary.maxTime = maxTime.get();
      summary.errorCount = errorCount.sum();
      summary.errorRate = executions > 0 ? (double) summary.errorCount / executions : 0.0;

      return summary;
    }
  }

  /** 系统指标类 */
  public static class SystemMetrics {
    private long startTime = System.currentTimeMillis();
    private final AtomicLong lastGcTime = new AtomicLong(0);
    private final AtomicLong gcCount = new AtomicLong(0);

    public void updateMetrics() {
      // 更新GC信息
      java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()
          .forEach(
              gcBean -> {
                gcCount.addAndGet(gcBean.getCollectionCount());
                lastGcTime.set(gcBean.getCollectionTime());
              });
    }

    public SystemSummary getSummary() {
      SystemSummary summary = new SystemSummary();
      summary.uptime = System.currentTimeMillis() - startTime;
      summary.gcCount = gcCount.get();
      summary.gcTime = lastGcTime.get();
      return summary;
    }

    public void reset() {
      startTime = System.currentTimeMillis();
      lastGcTime.set(0);
      gcCount.set(0);
    }
  }

  // 摘要类
  public static class MetricsSummary {
    public Map<String, OperationSummary> operationMetrics;
    public Map<String, Long> counters;
    public Map<String, Long> gauges;
    public SystemSummary systemMetrics;
    public LocalDateTime timestamp;
  }

  public static class OperationSummary {
    public long totalExecutions;
    public long totalTime;
    public long averageTime;
    public long minTime;
    public long maxTime;
    public long errorCount;
    public double errorRate;
  }

  public static class SystemSummary {
    public long uptime;
    public long gcCount;
    public long gcTime;
  }
}
