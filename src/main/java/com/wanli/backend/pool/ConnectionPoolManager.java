package com.wanli.backend.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.monitor.MetricsCollector;
import com.wanli.backend.util.LogUtil;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

/** 连接池管理器 提供数据库连接池的监控、优化和管理功能 */
@Component
public class ConnectionPoolManager {

  private final ApplicationConfigManager configManager;
  private final MetricsCollector metricsCollector;
  private final DataSource dataSource;
  private final HikariPoolMXBean poolMXBean;

  // 连接池监控数据
  private final ConcurrentHashMap<String, ConnectionMetrics> connectionMetrics =
      new ConcurrentHashMap<>();
  private final ConcurrentLinkedQueue<ConnectionEvent> connectionEvents =
      new ConcurrentLinkedQueue<>();

  // 连接池统计
  private final AtomicLong totalConnectionsCreated = new AtomicLong(0);
  private final AtomicLong totalConnectionsClosed = new AtomicLong(0);
  private final AtomicLong totalConnectionTimeouts = new AtomicLong(0);
  private final AtomicLong totalConnectionLeaks = new AtomicLong(0);
  private final AtomicLong totalSlowQueries = new AtomicLong(0);

  // 性能监控
  private final ConcurrentHashMap<String, QueryPerformance> queryPerformanceMap =
      new ConcurrentHashMap<>();
  private final ReentrantReadWriteLock performanceLock = new ReentrantReadWriteLock();

  // 连接池配置
  private volatile PoolConfiguration currentConfig;
  private volatile PoolHealthStatus healthStatus = PoolHealthStatus.HEALTHY;

  public ConnectionPoolManager(
      ApplicationConfigManager configManager,
      MetricsCollector metricsCollector,
      DataSource dataSource) {
    this.configManager = configManager;
    this.metricsCollector = metricsCollector;
    this.dataSource = dataSource;

    // 获取HikariCP MXBean
    if (dataSource instanceof HikariDataSource) {
      this.poolMXBean = ((HikariDataSource) dataSource).getHikariPoolMXBean();
    } else {
      this.poolMXBean = null;
      LogUtil.logWarn("CONNECTION_POOL_MXBEAN_UNAVAILABLE", "", "HikariCP MXBean不可用，某些监控功能将受限");
    }

    // 初始化配置
    this.currentConfig = loadPoolConfiguration();

    // 启动监控
    startMonitoring();
  }

  /** 加载连接池配置 */
  private PoolConfiguration loadPoolConfiguration() {
    PoolConfiguration config = new PoolConfiguration();

    if (dataSource instanceof HikariDataSource) {
      HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
      config.maximumPoolSize = hikariDataSource.getMaximumPoolSize();
      config.minimumIdle = hikariDataSource.getMinimumIdle();
      config.connectionTimeout = hikariDataSource.getConnectionTimeout();
      config.idleTimeout = hikariDataSource.getIdleTimeout();
      config.maxLifetime = hikariDataSource.getMaxLifetime();
    }

    return config;
  }

  /** 启动监控 */
  private void startMonitoring() {
    // 定期收集连接池指标
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    scheduler.scheduleAtFixedRate(this::collectPoolMetrics, 0, 30, TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(this::analyzePerformance, 0, 5, TimeUnit.MINUTES);
    scheduler.scheduleAtFixedRate(this::cleanupOldEvents, 0, 1, TimeUnit.HOURS);
  }

  /** 获取连接（带监控） */
  public Connection getConnection() throws SQLException {
    long startTime = System.currentTimeMillis();
    String connectionId = generateConnectionId();

    try {
      Connection connection = dataSource.getConnection();
      long acquisitionTime = System.currentTimeMillis() - startTime;

      // 记录连接获取事件
      recordConnectionEvent(ConnectionEventType.ACQUIRED, connectionId, acquisitionTime);

      // 创建连接指标
      ConnectionMetrics metrics = new ConnectionMetrics(connectionId, LocalDateTime.now());
      connectionMetrics.put(connectionId, metrics);

      // 包装连接以监控使用情况
      return new MonitoredConnection(connection, connectionId, this);

    } catch (SQLException e) {
      long acquisitionTime = System.currentTimeMillis() - startTime;
      recordConnectionEvent(ConnectionEventType.ACQUISITION_FAILED, connectionId, acquisitionTime);

      // 检查是否是超时异常
      if (isTimeoutException(e)) {
        totalConnectionTimeouts.incrementAndGet();
        metricsCollector.incrementCounter("connection_timeouts");
      }

      throw e;
    }
  }

  /** 记录连接事件 */
  public void recordConnectionEvent(
      ConnectionEventType eventType, String connectionId, long duration) {
    ConnectionEvent event =
        new ConnectionEvent(eventType, connectionId, duration, LocalDateTime.now());
    connectionEvents.offer(event);

    // 更新统计
    switch (eventType) {
      case ACQUIRED:
        totalConnectionsCreated.incrementAndGet();
        metricsCollector.incrementCounter("connections_acquired");
        metricsCollector.recordOperationTime("connection_acquisition", duration);
        break;
      case CLOSED:
        totalConnectionsClosed.incrementAndGet();
        metricsCollector.incrementCounter("connections_closed");
        break;
      case LEAKED:
        totalConnectionLeaks.incrementAndGet();
        metricsCollector.incrementCounter("connection_leaks");
        LogUtil.logWarn("CONNECTION_LEAK_DETECTED", "", String.format("检测到连接泄漏: %s", connectionId));
        break;
      case SLOW_QUERY:
        totalSlowQueries.incrementAndGet();
        metricsCollector.incrementCounter("slow_queries");
        break;
    }

    // 限制事件队列大小
    while (connectionEvents.size() > 10000) {
      connectionEvents.poll();
    }
  }

  /** 记录查询性能 */
  public void recordQueryPerformance(String sql, long executionTime, boolean success) {
    String normalizedSql = normalizeSql(sql);

    performanceLock.writeLock().lock();
    try {
      QueryPerformance performance =
          queryPerformanceMap.computeIfAbsent(normalizedSql, k -> new QueryPerformance(k));

      performance.recordExecution(executionTime, success);

      // 检查是否是慢查询
      if (executionTime > configManager.getDatabaseConfig().getSlowQueryThreshold()) {
        recordConnectionEvent(
            ConnectionEventType.SLOW_QUERY, Thread.currentThread().getName(), executionTime);

        LogUtil.logWarn(
            "SLOW_QUERY_DETECTED",
            "",
            String.format("检测到慢查询: %s, 执行时间: %dms", normalizedSql, executionTime));
      }

    } finally {
      performanceLock.writeLock().unlock();
    }
  }

  /** 连接关闭回调 */
  public void onConnectionClosed(String connectionId) {
    ConnectionMetrics metrics = connectionMetrics.remove(connectionId);
    if (metrics != null) {
      long connectionLifetime =
          Duration.between(metrics.getCreationTime(), LocalDateTime.now()).toMillis();
      recordConnectionEvent(ConnectionEventType.CLOSED, connectionId, connectionLifetime);

      metricsCollector.recordOperationTime("connection_lifetime", connectionLifetime);
    }
  }

  /** 收集连接池指标 */
  private void collectPoolMetrics() {
    try {
      if (poolMXBean == null) {
        return;
      }

      // 基本连接池指标
      int activeConnections = poolMXBean.getActiveConnections();
      int idleConnections = poolMXBean.getIdleConnections();
      int totalConnections = poolMXBean.getTotalConnections();
      int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();

      // 记录指标
      metricsCollector.recordGauge("connection_pool_active", activeConnections);
      metricsCollector.recordGauge("connection_pool_idle", idleConnections);
      metricsCollector.recordGauge("connection_pool_total", totalConnections);
      metricsCollector.recordGauge("connection_pool_waiting_threads", threadsAwaitingConnection);

      // 计算使用率
      double utilizationRate =
          currentConfig.maximumPoolSize > 0
              ? (double) activeConnections / currentConfig.maximumPoolSize
              : 0;
      metricsCollector.recordGauge("connection_pool_utilization", utilizationRate * 100);

      // 健康状态评估
      evaluatePoolHealth(
          activeConnections,
          idleConnections,
          totalConnections,
          threadsAwaitingConnection,
          utilizationRate);

      LogUtil.logInfo(
          "CONNECTION_POOL_METRICS",
          "",
          String.format(
              "连接池指标 - 活跃: %d, 空闲: %d, 总计: %d, 等待: %d, 使用率: %.1f%%",
              activeConnections,
              idleConnections,
              totalConnections,
              threadsAwaitingConnection,
              utilizationRate * 100));

    } catch (Exception e) {
      LogUtil.logError("CONNECTION_POOL_METRICS_ERROR", "", "METRICS_ERROR", "收集连接池指标失败", e);
    }
  }

  /** 评估连接池健康状态 */
  private void evaluatePoolHealth(
      int activeConnections,
      int idleConnections,
      int totalConnections,
      int threadsAwaitingConnection,
      double utilizationRate) {

    PoolHealthStatus previousStatus = healthStatus;

    // 评估健康状态
    if (threadsAwaitingConnection > 10) {
      healthStatus = PoolHealthStatus.CRITICAL;
    } else if (utilizationRate > 0.9) {
      healthStatus = PoolHealthStatus.WARNING;
    } else if (utilizationRate > 0.7) {
      healthStatus = PoolHealthStatus.CAUTION;
    } else {
      healthStatus = PoolHealthStatus.HEALTHY;
    }

    // 状态变化时记录日志
    if (previousStatus != healthStatus) {
      LogUtil.logInfo(
          "CONNECTION_POOL_HEALTH_CHANGE",
          "",
          String.format("连接池健康状态变化: %s -> %s", previousStatus, healthStatus));

      metricsCollector.recordBusinessEvent(
          "connection_pool_health_change",
          String.format("previous=%s,current=%s", previousStatus.name(), healthStatus.name()));
    }
  }

  /** 分析性能 */
  private void analyzePerformance() {
    try {
      performanceLock.readLock().lock();

      // 分析查询性能
      List<QueryPerformance> slowQueries =
          queryPerformanceMap.values().stream()
              .filter(
                  qp ->
                      qp.getAverageExecutionTime()
                          > configManager.getDatabaseConfig().getSlowQueryThreshold())
              .sorted(
                  (a, b) -> Long.compare(b.getAverageExecutionTime(), a.getAverageExecutionTime()))
              .limit(10)
              .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);

      if (!slowQueries.isEmpty()) {
        LogUtil.logWarn(
            "SLOW_QUERIES_ANALYSIS", "", String.format("发现 %d 个慢查询模式", slowQueries.size()));

        for (QueryPerformance qp : slowQueries) {
          LogUtil.logWarn(
              "SLOW_QUERY_PATTERN",
              "",
              String.format(
                  "慢查询: %s, 平均耗时: %dms, 执行次数: %d",
                  qp.getSql(), qp.getAverageExecutionTime(), qp.getExecutionCount()));
        }
      }

      // 生成性能报告
      PerformanceReport report = generatePerformanceReport();
      metricsCollector.recordBusinessEvent(
          "connection_pool_performance_analysis", report.toString());

    } finally {
      performanceLock.readLock().unlock();
    }
  }

  /** 生成性能报告 */
  public PerformanceReport generatePerformanceReport() {
    PerformanceReport report = new PerformanceReport();

    // 基本统计
    report.totalConnectionsCreated = totalConnectionsCreated.get();
    report.totalConnectionsClosed = totalConnectionsClosed.get();
    report.totalConnectionTimeouts = totalConnectionTimeouts.get();
    report.totalConnectionLeaks = totalConnectionLeaks.get();
    report.totalSlowQueries = totalSlowQueries.get();
    report.activeConnections = connectionMetrics.size();
    report.healthStatus = healthStatus;

    // 连接池配置
    report.poolConfiguration = currentConfig;

    // 性能统计
    performanceLock.readLock().lock();
    try {
      report.totalQueries =
          queryPerformanceMap.values().stream()
              .mapToLong(QueryPerformance::getExecutionCount)
              .sum();

      report.averageQueryTime =
          queryPerformanceMap.values().stream()
              .mapToLong(QueryPerformance::getAverageExecutionTime)
              .average()
              .orElse(0.0);

      report.slowQueryCount =
          queryPerformanceMap.values().stream()
              .filter(
                  qp ->
                      qp.getAverageExecutionTime()
                          > configManager.getDatabaseConfig().getSlowQueryThreshold())
              .count();

    } finally {
      performanceLock.readLock().unlock();
    }

    // 连接事件统计
    Map<ConnectionEventType, Long> eventCounts = new EnumMap<>(ConnectionEventType.class);
    for (ConnectionEvent event : connectionEvents) {
      eventCounts.merge(event.getEventType(), 1L, Long::sum);
    }
    report.eventCounts = eventCounts;

    return report;
  }

  /** 获取连接池优化建议 */
  public List<OptimizationSuggestion> getOptimizationSuggestions() {
    List<OptimizationSuggestion> suggestions = new ArrayList<>();

    if (poolMXBean == null) {
      return suggestions;
    }

    int activeConnections = poolMXBean.getActiveConnections();
    int totalConnections = poolMXBean.getTotalConnections();
    int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();

    double utilizationRate =
        currentConfig.maximumPoolSize > 0
            ? (double) activeConnections / currentConfig.maximumPoolSize
            : 0;

    // 连接池大小建议
    if (utilizationRate > 0.9 && threadsAwaitingConnection > 0) {
      suggestions.add(
          new OptimizationSuggestion(
              SuggestionType.INCREASE_POOL_SIZE,
              SuggestionPriority.HIGH,
              String.format(
                  "连接池使用率过高(%.1f%%)，建议增加最大连接数从 %d 到 %d",
                  utilizationRate * 100,
                  currentConfig.maximumPoolSize,
                  currentConfig.maximumPoolSize + 10),
              Map.of(
                  "current_max",
                  currentConfig.maximumPoolSize,
                  "suggested_max",
                  currentConfig.maximumPoolSize + 10)));
    }

    if (utilizationRate < 0.3 && currentConfig.maximumPoolSize > 10) {
      suggestions.add(
          new OptimizationSuggestion(
              SuggestionType.DECREASE_POOL_SIZE,
              SuggestionPriority.MEDIUM,
              String.format(
                  "连接池使用率较低(%.1f%%)，建议减少最大连接数从 %d 到 %d",
                  utilizationRate * 100,
                  currentConfig.maximumPoolSize,
                  Math.max(10, currentConfig.maximumPoolSize - 5)),
              Map.of(
                  "current_max",
                  currentConfig.maximumPoolSize,
                  "suggested_max",
                  Math.max(10, currentConfig.maximumPoolSize - 5))));
    }

    // 连接超时建议
    if (totalConnectionTimeouts.get() > 0) {
      suggestions.add(
          new OptimizationSuggestion(
              SuggestionType.INCREASE_CONNECTION_TIMEOUT,
              SuggestionPriority.MEDIUM,
              String.format("检测到 %d 次连接超时，建议增加连接超时时间", totalConnectionTimeouts.get()),
              Map.of("timeout_count", totalConnectionTimeouts.get())));
    }

    // 连接泄漏建议
    if (totalConnectionLeaks.get() > 0) {
      suggestions.add(
          new OptimizationSuggestion(
              SuggestionType.FIX_CONNECTION_LEAKS,
              SuggestionPriority.HIGH,
              String.format("检测到 %d 次连接泄漏，需要检查代码中的连接关闭逻辑", totalConnectionLeaks.get()),
              Map.of("leak_count", totalConnectionLeaks.get())));
    }

    // 慢查询建议
    if (totalSlowQueries.get() > 0) {
      suggestions.add(
          new OptimizationSuggestion(
              SuggestionType.OPTIMIZE_SLOW_QUERIES,
              SuggestionPriority.HIGH,
              String.format("检测到 %d 个慢查询，建议优化SQL语句或添加索引", totalSlowQueries.get()),
              Map.of("slow_query_count", totalSlowQueries.get())));
    }

    return suggestions;
  }

  /** 应用优化建议 */
  public boolean applyOptimizationSuggestion(OptimizationSuggestion suggestion) {
    try {
      switch (suggestion.getType()) {
        case INCREASE_POOL_SIZE:
        case DECREASE_POOL_SIZE:
          // 注意：HikariCP在运行时不支持动态修改连接池大小
          // 这里只是记录建议，实际修改需要重启应用
          LogUtil.logInfo(
              "POOL_SIZE_SUGGESTION",
              "",
              String.format("连接池大小优化建议: %s", suggestion.getDescription()));
          return false; // 需要重启应用

        case INCREASE_CONNECTION_TIMEOUT:
          // 同样，超时设置通常需要重启应用
          LogUtil.logInfo(
              "CONNECTION_TIMEOUT_SUGGESTION",
              "",
              String.format("连接超时优化建议: %s", suggestion.getDescription()));
          return false;

        case FIX_CONNECTION_LEAKS:
        case OPTIMIZE_SLOW_QUERIES:
          // 这些建议需要代码修改，无法自动应用
          LogUtil.logWarn(
              "MANUAL_OPTIMIZATION_REQUIRED",
              "",
              String.format("需要手动优化: %s", suggestion.getDescription()));
          return false;

        default:
          return false;
      }
    } catch (Exception e) {
      LogUtil.logError(
          "OPTIMIZATION_APPLICATION_ERROR",
          "",
          "OPTIMIZATION_ERROR",
          String.format("应用优化建议失败: %s", suggestion.getDescription()),
          e);
      return false;
    }
  }

  /** 清理旧事件 */
  private void cleanupOldEvents() {
    try {
      LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

      connectionEvents.removeIf(event -> event.getTimestamp().isBefore(cutoffTime));

      // 清理旧的查询性能数据
      performanceLock.writeLock().lock();
      try {
        queryPerformanceMap
            .entrySet()
            .removeIf(
                entry -> {
                  QueryPerformance qp = entry.getValue();
                  return qp.getLastExecutionTime().isBefore(cutoffTime);
                });
      } finally {
        performanceLock.writeLock().unlock();
      }

    } catch (Exception e) {
      LogUtil.logError("CONNECTION_POOL_CLEANUP_ERROR", "", "CLEANUP_ERROR", "清理连接池事件失败", e);
    }
  }

  /** 获取连接池健康状态 */
  public PoolHealthStatus getHealthStatus() {
    return healthStatus;
  }

  /** 获取连接池详细信息 */
  public Map<String, Object> getPoolDetails() {
    Map<String, Object> details = new HashMap<>();

    try {
      if (poolMXBean != null) {
        details.put("activeConnections", poolMXBean.getActiveConnections());
        details.put("totalConnections", poolMXBean.getTotalConnections());
        details.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());
      }

      details.put("healthStatus", healthStatus.toString());
      details.put("totalConnectionsCreated", totalConnectionsCreated.get());
      details.put("totalConnectionTimeouts", totalConnectionTimeouts.get());
      details.put("totalConnectionLeaks", totalConnectionLeaks.get());

    } catch (Exception e) {
      LogUtil.logError("POOL_DETAILS_ERROR", "", "POOL_DETAILS_ERROR", "获取连接池详细信息失败", e);
      details.put("error", e.getMessage());
    }

    return details;
  }

  /** 规范化SQL */
  private String normalizeSql(String sql) {
    if (sql == null) {
      return "unknown";
    }

    // 简单的SQL规范化：移除参数值，保留结构
    return sql.replaceAll("'[^']*'", "'?'")
        .replaceAll("\\b\\d+\\b", "?")
        .replaceAll("\\s+", " ")
        .trim();
  }

  /** 检查是否是超时异常 */
  private boolean isTimeoutException(SQLException e) {
    String message = e.getMessage();
    return message != null
        && (message.contains("timeout")
            || message.contains("Connection is not available")
            || message.contains("Unable to acquire JDBC Connection"));
  }

  /** 生成连接ID */
  private String generateConnectionId() {
    return "conn_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
  }

  // 枚举定义
  public enum ConnectionEventType {
    ACQUIRED,
    CLOSED,
    LEAKED,
    SLOW_QUERY,
    ACQUISITION_FAILED
  }

  public enum PoolHealthStatus {
    HEALTHY,
    CAUTION,
    WARNING,
    CRITICAL
  }

  public enum SuggestionType {
    INCREASE_POOL_SIZE,
    DECREASE_POOL_SIZE,
    INCREASE_CONNECTION_TIMEOUT,
    FIX_CONNECTION_LEAKS,
    OPTIMIZE_SLOW_QUERIES
  }

  public enum SuggestionPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  // 数据类定义
  public static class ConnectionMetrics {
    private final String connectionId;
    private final LocalDateTime creationTime;
    private volatile LocalDateTime lastUsedTime;
    private volatile long totalQueries = 0;
    private volatile long totalQueryTime = 0;

    public ConnectionMetrics(String connectionId, LocalDateTime creationTime) {
      this.connectionId = connectionId;
      this.creationTime = creationTime;
      this.lastUsedTime = creationTime;
    }

    public void recordQuery(long executionTime) {
      this.lastUsedTime = LocalDateTime.now();
      this.totalQueries++;
      this.totalQueryTime += executionTime;
    }

    // Getters
    public String getConnectionId() {
      return connectionId;
    }

    public LocalDateTime getCreationTime() {
      return creationTime;
    }

    public LocalDateTime getLastUsedTime() {
      return lastUsedTime;
    }

    public long getTotalQueries() {
      return totalQueries;
    }

    public long getTotalQueryTime() {
      return totalQueryTime;
    }

    public long getAverageQueryTime() {
      return totalQueries > 0 ? totalQueryTime / totalQueries : 0;
    }
  }

  public static class ConnectionEvent {
    private final ConnectionEventType eventType;
    private final String connectionId;
    private final long duration;
    private final LocalDateTime timestamp;

    public ConnectionEvent(
        ConnectionEventType eventType,
        String connectionId,
        long duration,
        LocalDateTime timestamp) {
      this.eventType = eventType;
      this.connectionId = connectionId;
      this.duration = duration;
      this.timestamp = timestamp;
    }

    // Getters
    public ConnectionEventType getEventType() {
      return eventType;
    }

    public String getConnectionId() {
      return connectionId;
    }

    public long getDuration() {
      return duration;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }
  }

  public static class QueryPerformance {
    private final String sql;
    private volatile long executionCount = 0;
    private volatile long totalExecutionTime = 0;
    private volatile long minExecutionTime = Long.MAX_VALUE;
    private volatile long maxExecutionTime = 0;
    private volatile long successCount = 0;
    private volatile long failureCount = 0;
    private volatile LocalDateTime lastExecutionTime;

    public QueryPerformance(String sql) {
      this.sql = sql;
      this.lastExecutionTime = LocalDateTime.now();
    }

    public synchronized void recordExecution(long executionTime, boolean success) {
      this.executionCount++;
      this.totalExecutionTime += executionTime;
      this.minExecutionTime = Math.min(this.minExecutionTime, executionTime);
      this.maxExecutionTime = Math.max(this.maxExecutionTime, executionTime);
      this.lastExecutionTime = LocalDateTime.now();

      if (success) {
        this.successCount++;
      } else {
        this.failureCount++;
      }
    }

    // Getters
    public String getSql() {
      return sql;
    }

    public long getExecutionCount() {
      return executionCount;
    }

    public long getAverageExecutionTime() {
      return executionCount > 0 ? totalExecutionTime / executionCount : 0;
    }

    public long getMinExecutionTime() {
      return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
    }

    public long getMaxExecutionTime() {
      return maxExecutionTime;
    }

    public double getSuccessRate() {
      return executionCount > 0 ? (double) successCount / executionCount : 0;
    }

    public LocalDateTime getLastExecutionTime() {
      return lastExecutionTime;
    }
  }

  public static class PoolConfiguration {
    public int maximumPoolSize;
    public int minimumIdle;
    public long connectionTimeout;
    public long idleTimeout;
    public long maxLifetime;

    @Override
    public String toString() {
      return String.format(
          "PoolConfiguration{maxSize=%d, minIdle=%d, connTimeout=%d, idleTimeout=%d, maxLifetime=%d}",
          maximumPoolSize, minimumIdle, connectionTimeout, idleTimeout, maxLifetime);
    }
  }

  public static class PerformanceReport {
    public long totalConnectionsCreated;
    public long totalConnectionsClosed;
    public long totalConnectionTimeouts;
    public long totalConnectionLeaks;
    public long totalSlowQueries;
    public int activeConnections;
    public PoolHealthStatus healthStatus;
    public PoolConfiguration poolConfiguration;
    public long totalQueries;
    public double averageQueryTime;
    public long slowQueryCount;
    public Map<ConnectionEventType, Long> eventCounts;

    @Override
    public String toString() {
      return String.format(
          "PerformanceReport{created=%d, closed=%d, timeouts=%d, leaks=%d, slowQueries=%d, active=%d, health=%s}",
          totalConnectionsCreated,
          totalConnectionsClosed,
          totalConnectionTimeouts,
          totalConnectionLeaks,
          totalSlowQueries,
          activeConnections,
          healthStatus);
    }
  }

  public static class OptimizationSuggestion {
    private final SuggestionType type;
    private final SuggestionPriority priority;
    private final String description;
    private final Map<String, Object> parameters;
    private final LocalDateTime createdTime;

    public OptimizationSuggestion(
        SuggestionType type,
        SuggestionPriority priority,
        String description,
        Map<String, Object> parameters) {
      this.type = type;
      this.priority = priority;
      this.description = description;
      this.parameters = parameters;
      this.createdTime = LocalDateTime.now();
    }

    // Getters
    public SuggestionType getType() {
      return type;
    }

    public SuggestionPriority getPriority() {
      return priority;
    }

    public String getDescription() {
      return description;
    }

    public Map<String, Object> getParameters() {
      return parameters;
    }

    public LocalDateTime getCreatedTime() {
      return createdTime;
    }
  }

  /** 注册数据源到连接池管理器 */
  public void registerDataSource(String poolName, DataSource dataSource) {
    LogUtil.logInfo("CONNECTION_POOL_MANAGER", "", String.format("注册数据源到连接池管理器: %s", poolName));
    // 这里可以添加数据源注册的具体逻辑
    // 例如：将数据源添加到监控列表中
  }

  /** 从连接池管理器注销数据源 */
  public void unregisterDataSource(String poolName) {
    LogUtil.logInfo("CONNECTION_POOL_MANAGER", "", String.format("从连接池管理器注销数据源: %s", poolName));
    // 这里可以添加数据源注销的具体逻辑
    // 例如：从监控列表中移除数据源
  }
}
