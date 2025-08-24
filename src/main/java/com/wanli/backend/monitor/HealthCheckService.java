package com.wanli.backend.monitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.util.LogUtil;

/** 健康检查服务 监控系统各个组件的健康状态 */
@Service
public class HealthCheckService {

  private final ApplicationConfigManager configManager;
  private final MetricsCollector metricsCollector;
  private final RedisTemplate<String, Object> redisTemplate;
  private final JdbcTemplate jdbcTemplate;

  // 健康状态缓存
  private final ConcurrentHashMap<String, HealthStatus> healthStatusCache =
      new ConcurrentHashMap<>();

  // 组件检查器映射
  private final Map<String, ComponentChecker> componentCheckers = new HashMap<>();

  public HealthCheckService(
      ApplicationConfigManager configManager,
      MetricsCollector metricsCollector,
      @Autowired(required = false) RedisTemplate<String, Object> redisTemplate,
      JdbcTemplate jdbcTemplate) {
    this.configManager = configManager;
    this.metricsCollector = metricsCollector;
    this.redisTemplate = redisTemplate;
    this.jdbcTemplate = jdbcTemplate;

    initializeComponentCheckers();
  }

  /** 初始化组件检查器 */
  private void initializeComponentCheckers() {
    componentCheckers.put("database", this::checkDatabaseHealth);
    componentCheckers.put("redis", this::checkRedisHealth);
    componentCheckers.put("memory", this::checkMemoryHealth);
    componentCheckers.put("disk", this::checkDiskHealth);
    componentCheckers.put("external-api", this::checkExternalApiHealth);
  }

  /** 执行完整的健康检查 */
  public HealthCheckResult performFullHealthCheck() {
    HealthCheckResult result = new HealthCheckResult();
    result.timestamp = LocalDateTime.now();
    result.componentStatuses = new HashMap<>();
    result.details = new ArrayList<>();

    List<CompletableFuture<ComponentHealthResult>> futures = new ArrayList<>();

    // 异步检查各个组件
    for (Map.Entry<String, ComponentChecker> entry : componentCheckers.entrySet()) {
      String componentName = entry.getKey();
      ComponentChecker checker = entry.getValue();

      CompletableFuture<ComponentHealthResult> future =
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return checkComponentHealth(componentName, checker);
                } catch (Exception e) {
                  LogUtil.logError(
                      "HEALTH_CHECK",
                      "",
                      "COMPONENT_CHECK_FAILED",
                      "组件健康检查失败: " + componentName,
                      e);
                  ComponentHealthResult errorResult = new ComponentHealthResult();
                  errorResult.componentName = componentName;
                  errorResult.status = HealthStatus.UNHEALTHY;
                  errorResult.message = "检查失败: " + e.getMessage();
                  errorResult.timestamp = LocalDateTime.now();
                  return errorResult;
                }
              });

      futures.add(future);
    }

    // 等待所有检查完成
    try {
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
          .get(30, TimeUnit.SECONDS);

      for (CompletableFuture<ComponentHealthResult> future : futures) {
        ComponentHealthResult componentResult = future.get();
        result.details.add(componentResult);
        result.componentStatuses.put(componentResult.componentName, componentResult.status);

        // 更新缓存
        healthStatusCache.put(componentResult.componentName, componentResult.status);
      }

    } catch (Exception e) {
      LogUtil.logError(
          "HEALTH_CHECK", "", "HEALTH_CHECK_TIMEOUT", "健康检查超时或失败", e);
      // 如果检查超时，使用缓存的状态
      for (String componentName : componentCheckers.keySet()) {
        if (!result.componentStatuses.containsKey(componentName)) {
          HealthStatus cachedStatus =
              healthStatusCache.getOrDefault(componentName, HealthStatus.UNKNOWN);
          result.componentStatuses.put(componentName, cachedStatus);
        }
      }
    }

    // 计算整体健康状态
    result.overallStatus = calculateOverallHealth(result.componentStatuses);

    return result;
  }

  /** 检查单个组件健康状态 */
  private ComponentHealthResult checkComponentHealth(
      String componentName, ComponentChecker checker) {
    ComponentHealthResult result = new ComponentHealthResult();
    result.componentName = componentName;
    result.timestamp = LocalDateTime.now();

    long startTime = System.currentTimeMillis();
    try {
      result.status = checker.check();
      result.message = getHealthMessage(componentName, result.status);
    } catch (Exception e) {
      result.status = HealthStatus.UNHEALTHY;
      result.message = "检查失败: " + e.getMessage();
      LogUtil.logError(
          "COMPONENT_HEALTH_CHECK",
          "",
          "CHECK_FAILED",
          "组件健康检查失败: " + componentName,
          e);
    } finally {
      result.responseTime = System.currentTimeMillis() - startTime;
    }

    return result;
  }

  /** 检查数据库健康状态 */
  private HealthStatus checkDatabaseHealth() {
    try {
      long startTime = System.currentTimeMillis();
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);
      long responseTime = System.currentTimeMillis() - startTime;

      if (responseTime > 1000) { // 1秒阈值
        return HealthStatus.DEGRADED;
      }

      return HealthStatus.HEALTHY;

    } catch (Exception e) {
      LogUtil.logError(
          "DATABASE_HEALTH_CHECK", "", "HEALTH_CHECK_FAILED", "数据库健康检查失败", e);
      return HealthStatus.UNHEALTHY;
    }
  }

  /** 检查Redis健康状态 */
  private HealthStatus checkRedisHealth() {
    // 如果Redis未配置或不可用，返回UNKNOWN状态
    if (redisTemplate == null) {
      return HealthStatus.UNKNOWN;
    }

    try {
      long startTime = System.currentTimeMillis();

      // 执行ping命令
      String response = redisTemplate.getConnectionFactory().getConnection().ping();

      long responseTime = System.currentTimeMillis() - startTime;

      if (!"PONG".equals(response)) {
        return HealthStatus.UNHEALTHY;
      }

      // 检查响应时间
      if (responseTime > 100) { // 100ms阈值
        return HealthStatus.DEGRADED;
      }

      return HealthStatus.HEALTHY;

    } catch (Exception e) {
      LogUtil.logError("REDIS_HEALTH_CHECK", "", "HEALTH_CHECK_FAILED", "Redis健康检查失败", e);
      return HealthStatus.UNHEALTHY;
    }
  }

  /** 检查内存健康状态 */
  private HealthStatus checkMemoryHealth() {
    try {
      Runtime runtime = Runtime.getRuntime();
      long totalMemory = runtime.totalMemory();
      long freeMemory = runtime.freeMemory();
      long usedMemory = totalMemory - freeMemory;
      double memoryUsage = (double) usedMemory / totalMemory;

      if (memoryUsage > 0.9) { // 90%阈值
        return HealthStatus.UNHEALTHY;
      } else if (memoryUsage > 0.8) { // 80%阈值
        return HealthStatus.DEGRADED;
      }

      return HealthStatus.HEALTHY;

    } catch (Exception e) {
      LogUtil.logError(
          "MEMORY_HEALTH_CHECK", "", "HEALTH_CHECK_FAILED", "内存健康检查失败", e);
      return HealthStatus.UNHEALTHY;
    }
  }

  /** 检查磁盘健康状态 */
  private HealthStatus checkDiskHealth() {
    try {
      java.io.File root = new java.io.File("/");
      long totalSpace = root.getTotalSpace();
      long freeSpace = root.getFreeSpace();
      double diskUsage = 1.0 - ((double) freeSpace / totalSpace);

      if (diskUsage > 0.95) { // 95%阈值
        return HealthStatus.UNHEALTHY;
      } else if (diskUsage > 0.85) { // 85%阈值
        return HealthStatus.DEGRADED;
      }

      return HealthStatus.HEALTHY;

    } catch (Exception e) {
      LogUtil.logError(
          "DISK_HEALTH_CHECK", "", "HEALTH_CHECK_FAILED", "磁盘健康检查失败", e);
      return HealthStatus.UNHEALTHY;
    }
  }

  /** 检查外部API健康状态 */
  private HealthStatus checkExternalApiHealth() {
    // 这里可以添加对外部API的健康检查
    return HealthStatus.HEALTHY;
  }

  /** 获取组件健康状态 */
  public HealthStatus getComponentHealth(String componentName) {
    return healthStatusCache.getOrDefault(componentName, HealthStatus.UNKNOWN);
  }

  /** 获取整体健康状态 */
  public HealthStatus getOverallHealth() {
    if (healthStatusCache.isEmpty()) {
      return HealthStatus.UNKNOWN;
    }

    boolean hasUnhealthy = healthStatusCache.values().contains(HealthStatus.UNHEALTHY);
    boolean hasDegraded = healthStatusCache.values().contains(HealthStatus.DEGRADED);

    if (hasUnhealthy) {
      return HealthStatus.UNHEALTHY;
    } else if (hasDegraded) {
      return HealthStatus.DEGRADED;
    } else {
      return HealthStatus.HEALTHY;
    }
  }

  /** 计算整体健康状态 */
  private HealthStatus calculateOverallHealth(Map<String, HealthStatus> componentStatuses) {
    return getOverallHealth();
  }

  /** 获取健康状态消息 */
  private String getHealthMessage(String componentName, HealthStatus status) {
    switch (status) {
      case HEALTHY:
        return componentName + "运行正常";
      case DEGRADED:
        return componentName + "性能下降";
      case UNHEALTHY:
        return componentName + "运行异常";
      default:
        return componentName + "状态未知";
    }
  }

  /** 定时健康检查 */
  @Scheduled(fixedRate = 300000) // 每5分钟执行一次
  @Async
  public void scheduledHealthCheck() {
    try {
      HealthCheckResult result = performFullHealthCheck();

      // 记录健康检查结果
      LogUtil.logInfo(
          "SCHEDULED_HEALTH_CHECK",
          "",
          "HEALTH_CHECK_COMPLETED",
          "定时健康检查完成，整体状态: " + result.overallStatus);

      // 如果发现问题，发送告警
      if (result.overallStatus == HealthStatus.UNHEALTHY
          || result.overallStatus == HealthStatus.DEGRADED) {
        sendHealthAlert(result);
      }

    } catch (Exception e) {
      LogUtil.logError(
          "SCHEDULED_HEALTH_CHECK", "", "HEALTH_CHECK_FAILED", "定时健康检查失败", e);
    }
  }

  /** 发送健康告警 */
  private void sendHealthAlert(HealthCheckResult result) {
    try {
      // 这里可以集成邮件、短信、钉钉等告警方式
      String alertMessage =
          String.format(
              "系统健康告警 - 整体状态: %s, 时间: %s",
              result.overallStatus, result.timestamp);

      LogUtil.logWarn(
          "HEALTH_ALERT", "", "SYSTEM_HEALTH_ALERT", alertMessage);

      // 记录详细的组件状态
      for (ComponentHealthResult detail : result.details) {
        if (detail.status != HealthStatus.HEALTHY) {
          LogUtil.logWarn(
              "COMPONENT_ALERT",
              "",
              "COMPONENT_UNHEALTHY",
              String.format(
                  "组件异常 - %s: %s (%s)",
                  detail.componentName, detail.status, detail.message));
        }
      }

    } catch (Exception e) {
      LogUtil.logError("HEALTH_ALERT", "", "ALERT_SEND_FAILED", "健康告警发送失败", e);
    }
  }

  /** 健康状态枚举 */
  public enum HealthStatus {
    HEALTHY, // 健康
    DEGRADED, // 性能下降
    UNHEALTHY, // 不健康
    UNKNOWN // 未知
  }

  /** 组件检查器接口 */
  @FunctionalInterface
  private interface ComponentChecker {
    HealthStatus check() throws Exception;
  }

  /** 健康检查结果 */
  public static class HealthCheckResult {
    public LocalDateTime timestamp;
    public HealthStatus overallStatus;
    public Map<String, HealthStatus> componentStatuses;
    public List<ComponentHealthResult> details;
  }

  /** 组件健康检查结果 */
  public static class ComponentHealthResult {
    public String componentName;
    public HealthStatus status;
    public String message;
    public long responseTime;
    public LocalDateTime timestamp;
  }
}