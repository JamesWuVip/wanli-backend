package com.wanli.backend.monitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.util.LogUtil;

/** 健康检查服务 监控系统各个组件的健康状态 */
@Service
@ConditionalOnBean(RedisTemplate.class)
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
      RedisTemplate<String, Object> redisTemplate,
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
    componentCheckers.put("external_api", this::checkExternalApiHealth);
  }

  /** 执行完整的健康检查 */
  public HealthCheckResult performFullHealthCheck() {
    HealthCheckResult result = new HealthCheckResult();
    result.timestamp = LocalDateTime.now();
    result.overallStatus = HealthStatus.HEALTHY;
    result.componentStatuses = new HashMap<>();
    result.details = new ArrayList<>();

    // 并行执行所有组件检查
    List<CompletableFuture<ComponentHealthResult>> futures = new ArrayList<>();

    for (Map.Entry<String, ComponentChecker> entry : componentCheckers.entrySet()) {
      String componentName = entry.getKey();
      ComponentChecker checker = entry.getValue();

      CompletableFuture<ComponentHealthResult> future =
          CompletableFuture.supplyAsync(() -> checkComponentHealth(componentName, checker))
              .orTimeout(
                  configManager.getMonitoringConfig().getHealthCheckTimeoutMs(),
                  TimeUnit.MILLISECONDS)
              .exceptionally(
                  throwable -> {
                    ComponentHealthResult errorResult = new ComponentHealthResult();
                    errorResult.componentName = componentName;
                    errorResult.status = HealthStatus.UNHEALTHY;
                    errorResult.message = "健康检查超时: " + throwable.getMessage();
                    errorResult.responseTime =
                        configManager.getMonitoringConfig().getHealthCheckTimeoutMs();
                    return errorResult;
                  });

      futures.add(future);
    }

    // 等待所有检查完成
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    // 收集结果
    for (CompletableFuture<ComponentHealthResult> future : futures) {
      try {
        ComponentHealthResult componentResult = future.get();
        result.componentStatuses.put(componentResult.componentName, componentResult.status);
        result.details.add(componentResult);

        // 更新缓存
        healthStatusCache.put(componentResult.componentName, componentResult.status);

        // 更新整体状态
        if (componentResult.status == HealthStatus.UNHEALTHY) {
          result.overallStatus = HealthStatus.UNHEALTHY;
        } else if (componentResult.status == HealthStatus.DEGRADED
            && result.overallStatus == HealthStatus.HEALTHY) {
          result.overallStatus = HealthStatus.DEGRADED;
        }

        // 记录指标
        metricsCollector.recordOperationTime(
            "health_check_" + componentResult.componentName, componentResult.responseTime);
        metricsCollector.incrementCounter(
            "health_check_"
                + componentResult.componentName
                + "_"
                + componentResult.status.name().toLowerCase());

      } catch (Exception e) {
        LogUtil.logError("健康检查结果收集", "", "HEALTH_CHECK_ERROR", "健康检查结果收集失败", e);
      }
    }

    // 记录整体健康状态
    metricsCollector.incrementCounter(
        "health_check_overall_" + result.overallStatus.name().toLowerCase());

    LogUtil.logInfo(
        "HEALTH_CHECK_COMPLETED",
        "",
        String.format("健康检查完成，整体状态: %s，检查了 %d 个组件", result.overallStatus, result.details.size()));

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
          "组件健康检查",
          "",
          "COMPONENT_HEALTH_CHECK_ERROR",
          String.format("组件 %s 健康检查失败", componentName),
          e);
    }

    result.responseTime = System.currentTimeMillis() - startTime;

    return result;
  }

  /** 检查数据库健康状态 */
  private HealthStatus checkDatabaseHealth() {
    try {
      long startTime = System.currentTimeMillis();

      // 执行简单查询
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);

      long responseTime = System.currentTimeMillis() - startTime;

      // 检查响应时间
      if (responseTime > configManager.getDatabaseConfig().getSlowQueryThresholdMs()) {
        return HealthStatus.DEGRADED;
      }

      return HealthStatus.HEALTHY;

    } catch (Exception e) {
      LogUtil.logError("数据库健康检查", "", "DATABASE_HEALTH_CHECK", "数据库健康检查失败", e);
      return HealthStatus.UNHEALTHY;
    }
  }

  /** 检查Redis健康状态 */
  private HealthStatus checkRedisHealth() {
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
      long maxMemory = runtime.maxMemory();

      double usagePercent = (double) usedMemory / maxMemory * 100;

      // 记录内存使用率
      metricsCollector.setGauge("memory_usage_percent", (long) usagePercent);

      if (usagePercent > 90) {
        return HealthStatus.UNHEALTHY;
      } else if (usagePercent > 80) {
        return HealthStatus.DEGRADED;
      }

      return HealthStatus.HEALTHY;

    } catch (Exception e) {
      LogUtil.logError("MEMORY_HEALTH_CHECK", "", "HEALTH_CHECK_FAILED", "内存健康检查失败", e);
      return HealthStatus.UNHEALTHY;
    }
  }

  /** 检查磁盘健康状态 */
  private HealthStatus checkDiskHealth() {
    try {
      java.io.File root = new java.io.File("/");

      long totalSpace = root.getTotalSpace();
      long freeSpace = root.getFreeSpace();
      long usedSpace = totalSpace - freeSpace;

      double usagePercent = (double) usedSpace / totalSpace * 100;

      // 记录磁盘使用率
      metricsCollector.setGauge("disk_usage_percent", (long) usagePercent);

      if (usagePercent > 95) {
        return HealthStatus.UNHEALTHY;
      } else if (usagePercent > 85) {
        return HealthStatus.DEGRADED;
      }

      return HealthStatus.HEALTHY;

    } catch (Exception e) {
      LogUtil.logError("DISK_HEALTH_CHECK", "", "HEALTH_CHECK_FAILED", "磁盘健康检查失败", e);
      return HealthStatus.UNHEALTHY;
    }
  }

  /** 检查外部API健康状态 */
  private HealthStatus checkExternalApiHealth() {
    // 这里可以添加对外部API的健康检查
    // 例如检查第三方服务的可用性
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

    boolean hasUnhealthy =
        healthStatusCache.values().stream().anyMatch(status -> status == HealthStatus.UNHEALTHY);

    if (hasUnhealthy) {
      return HealthStatus.UNHEALTHY;
    }

    boolean hasDegraded =
        healthStatusCache.values().stream().anyMatch(status -> status == HealthStatus.DEGRADED);

    if (hasDegraded) {
      return HealthStatus.DEGRADED;
    }

    return HealthStatus.HEALTHY;
  }

  /** 获取健康消息 */
  private String getHealthMessage(String componentName, HealthStatus status) {
    switch (status) {
      case HEALTHY:
        return componentName + " 运行正常";
      case DEGRADED:
        return componentName + " 性能下降";
      case UNHEALTHY:
        return componentName + " 不可用";
      default:
        return componentName + " 状态未知";
    }
  }

  /** 定期健康检查 */
  @Scheduled(fixedRate = 300000) // 每5分钟执行一次
  @Async
  public void scheduledHealthCheck() {
    if (!configManager.getMonitoringConfig().isHealthCheckEnabled()) {
      return;
    }

    try {
      HealthCheckResult result = performFullHealthCheck();

      // 如果整体状态不健康，发送告警
      if (result.overallStatus == HealthStatus.UNHEALTHY) {
        sendHealthAlert(result);
      }

    } catch (Exception e) {
      LogUtil.logError("SCHEDULED_HEALTH_CHECK", "", "HEALTH_CHECK_FAILED", "定期健康检查执行失败", e);
    }
  }

  /** 发送健康告警 */
  private void sendHealthAlert(HealthCheckResult result) {
    List<String> unhealthyComponents =
        result.details.stream()
            .filter(detail -> detail.status == HealthStatus.UNHEALTHY)
            .map(detail -> detail.componentName)
            .collect(java.util.stream.Collectors.toList());

    String alertMessage =
        String.format(
            "系统健康检查告警: 整体状态 %s，不健康组件: %s",
            result.overallStatus, String.join(", ", unhealthyComponents));

    LogUtil.logError("HEALTH_ALERT", "", "HEALTH_ALERT", alertMessage, null);
    metricsCollector.incrementCounter("health_alerts_sent");

    // 这里可以添加发送邮件、短信等告警逻辑
  }

  // 枚举和接口定义
  public enum HealthStatus {
    HEALTHY, // 健康
    DEGRADED, // 性能下降
    UNHEALTHY, // 不健康
    UNKNOWN // 未知
  }

  @FunctionalInterface
  private interface ComponentChecker {
    HealthStatus check() throws Exception;
  }

  // 结果类
  public static class HealthCheckResult {
    public LocalDateTime timestamp;
    public HealthStatus overallStatus;
    public Map<String, HealthStatus> componentStatuses;
    public List<ComponentHealthResult> details;
  }

  public static class ComponentHealthResult {
    public String componentName;
    public HealthStatus status;
    public String message;
    public long responseTime;
    public LocalDateTime timestamp;
  }
}
