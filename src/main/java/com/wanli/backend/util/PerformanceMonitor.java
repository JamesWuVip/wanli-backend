package com.wanli.backend.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/** 性能监控工具类 提供方法执行时间监控、内存使用监控、线程监控等功能 */
@Component
public class PerformanceMonitor {

  private static final Map<String, PerformanceMetrics> methodMetrics = new ConcurrentHashMap<>();
  private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
  private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

  /** 性能指标数据类 */
  public static class PerformanceMetrics {
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private volatile long maxExecutionTime = 0;
    private volatile long minExecutionTime = Long.MAX_VALUE;

    public void recordExecution(long executionTime) {
      totalExecutions.incrementAndGet();
      totalExecutionTime.addAndGet(executionTime);

      synchronized (this) {
        if (executionTime > maxExecutionTime) {
          maxExecutionTime = executionTime;
        }
        if (executionTime < minExecutionTime) {
          minExecutionTime = executionTime;
        }
      }
    }

    public long getTotalExecutions() {
      return totalExecutions.get();
    }

    public long getAverageExecutionTime() {
      long executions = totalExecutions.get();
      return executions > 0 ? totalExecutionTime.get() / executions : 0;
    }

    public long getMaxExecutionTime() {
      return maxExecutionTime;
    }

    public long getMinExecutionTime() {
      return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
    }

    public long getTotalExecutionTime() {
      return totalExecutionTime.get();
    }
  }

  /** 性能监控器 使用try-with-resources语法自动记录方法执行时间 */
  public static class Monitor implements AutoCloseable {
    private final String methodName;
    private final long startTime;
    private final long startMemory;

    public Monitor(String methodName) {
      this.methodName = methodName;
      this.startTime = System.nanoTime();
      this.startMemory = getUsedMemory();

      LogUtil.logBusiness(
          "PerformanceMonitor.start",
          Map.of("method", methodName, "startTime", startTime, "startMemory", startMemory));
    }

    @Override
    public void close() {
      long endTime = System.nanoTime();
      long executionTime = endTime - startTime;
      long endMemory = getUsedMemory();
      long memoryUsed = endMemory - startMemory;

      // 记录性能指标
      methodMetrics
          .computeIfAbsent(methodName, k -> new PerformanceMetrics())
          .recordExecution(executionTime);

      // 记录性能日志
      Map<String, Object> performanceData =
          Map.of(
              "method", methodName,
              "executionTimeNs", executionTime,
              "executionTimeMs", executionTime / 1_000_000,
              "memoryUsedBytes", memoryUsed,
              "memoryUsedMB", memoryUsed / (1024 * 1024));

      LogUtil.logBusiness("PerformanceMonitor.complete", performanceData);

      // 如果执行时间超过阈值，记录警告日志
      if (executionTime > 1_000_000_000) { // 1秒
        LogUtil.logError(
            "PerformanceMonitor.slowMethod",
            "",
            "Method execution time exceeded threshold",
            "",
            new RuntimeException("Method execution time exceeded threshold"));
      }
    }
  }

  /**
   * 创建性能监控器
   *
   * @param methodName 方法名称
   * @return Monitor实例
   */
  public static Monitor monitor(String methodName) {
    return new Monitor(methodName);
  }

  /**
   * 开始性能监控
   *
   * @param methodName 方法名称
   * @return Monitor实例
   */
  public static Monitor start(String methodName) {
    return new Monitor(methodName);
  }

  /**
   * 获取方法性能指标
   *
   * @param methodName 方法名称
   * @return 性能指标，如果不存在则返回null
   */
  public static PerformanceMetrics getMetrics(String methodName) {
    return methodMetrics.get(methodName);
  }

  /**
   * 获取所有方法的性能指标
   *
   * @return 所有性能指标的映射
   */
  public static Map<String, PerformanceMetrics> getAllMetrics() {
    return new ConcurrentHashMap<>(methodMetrics);
  }

  /** 清除所有性能指标 */
  public static void clearMetrics() {
    methodMetrics.clear();
    LogUtil.logBusiness("PerformanceMonitor.clearMetrics", Map.of("action", "cleared_all_metrics"));
  }

  /**
   * 获取当前内存使用情况
   *
   * @return 已使用内存字节数
   */
  public static long getUsedMemory() {
    return memoryBean.getHeapMemoryUsage().getUsed();
  }

  /**
   * 获取最大可用内存
   *
   * @return 最大可用内存字节数
   */
  public static long getMaxMemory() {
    return memoryBean.getHeapMemoryUsage().getMax();
  }

  /**
   * 获取当前线程数
   *
   * @return 当前活跃线程数
   */
  public static int getCurrentThreadCount() {
    return threadBean.getThreadCount();
  }

  /**
   * 获取系统性能摘要
   *
   * @return 系统性能信息
   */
  public static Map<String, Object> getSystemPerformanceSummary() {
    return Map.of(
        "usedMemoryMB",
        getUsedMemory() / (1024 * 1024),
        "maxMemoryMB",
        getMaxMemory() / (1024 * 1024),
        "memoryUsagePercent",
        (double) getUsedMemory() / getMaxMemory() * 100,
        "activeThreads",
        getCurrentThreadCount(),
        "monitoredMethods",
        methodMetrics.size());
  }

  /**
   * 记录自定义性能指标
   *
   * @param metricName 指标名称
   * @param value 指标值
   * @param unit 单位
   */
  public static void recordCustomMetric(String metricName, long value, String unit) {
    LogUtil.logBusiness(
        "PerformanceMonitor.customMetric",
        Map.of("metric", metricName, "value", value, "unit", unit));
  }

  /**
   * 检查内存使用是否超过阈值
   *
   * @param thresholdPercent 阈值百分比（0-100）
   * @return 是否超过阈值
   */
  public static boolean isMemoryUsageHigh(double thresholdPercent) {
    double currentUsage = (double) getUsedMemory() / getMaxMemory() * 100;
    boolean isHigh = currentUsage > thresholdPercent;

    if (isHigh) {
      LogUtil.logError(
          "PerformanceMonitor.highMemoryUsage",
          "",
          "Memory usage exceeded threshold",
          "",
          new RuntimeException("Memory usage exceeded threshold"));
    }

    return isHigh;
  }
}
