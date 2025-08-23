package com.wanli.backend.monitor;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wanli.backend.config.ApplicationConfigManager;
import com.wanli.backend.util.LogUtil;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

/** 数据库连接池监控器 监控数据库连接池的使用情况和性能 */
@Component
public class DatabaseConnectionMonitor {

  private final ApplicationConfigManager configManager;
  private final MetricsCollector metricsCollector;
  private final DataSource dataSource;

  // 连接池统计信息
  private final ConcurrentHashMap<String, AtomicLong> connectionStats = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Long> lastMetricValues = new ConcurrentHashMap<>();

  // HikariCP MXBean
  private HikariPoolMXBean hikariPoolMXBean;

  @Autowired
  public DatabaseConnectionMonitor(
      ApplicationConfigManager configManager,
      MetricsCollector metricsCollector,
      DataSource dataSource) {
    this.configManager = configManager;
    this.metricsCollector = metricsCollector;
    this.dataSource = dataSource;

    initializeMonitoring();
  }

  /** 初始化监控 */
  private void initializeMonitoring() {
    // 初始化统计计数器
    connectionStats.put("connections_created", new AtomicLong(0));
    connectionStats.put("connections_closed", new AtomicLong(0));
    connectionStats.put("connections_timeout", new AtomicLong(0));
    connectionStats.put("connections_leaked", new AtomicLong(0));
    connectionStats.put("slow_queries", new AtomicLong(0));

    // 如果使用HikariCP，获取MXBean
    if (dataSource instanceof HikariDataSource) {
      HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
      try {
        this.hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
        LogUtil.logInfo("DB_MONITOR_INIT", "", "HikariCP连接池监控初始化成功");
      } catch (Exception e) {
        LogUtil.logWarn("DB_MONITOR_INIT", "", "HikariCP MXBean获取失败: " + e.getMessage());
      }
    }
  }

  /** 记录连接创建 */
  public void recordConnectionCreated() {
    connectionStats.get("connections_created").incrementAndGet();
    metricsCollector.incrementCounter("db_connections_created");
  }

  /** 记录连接关闭 */
  public void recordConnectionClosed() {
    connectionStats.get("connections_closed").incrementAndGet();
    metricsCollector.incrementCounter("db_connections_closed");
  }

  /** 记录连接超时 */
  public void recordConnectionTimeout() {
    connectionStats.get("connections_timeout").incrementAndGet();
    metricsCollector.incrementCounter("db_connections_timeout");

    LogUtil.logWarn("DB_CONNECTION_TIMEOUT", "", "数据库连接获取超时");
  }

  /** 记录连接泄漏 */
  public void recordConnectionLeak() {
    connectionStats.get("connections_leaked").incrementAndGet();
    metricsCollector.incrementCounter("db_connections_leaked");

    LogUtil.logError("DB_CONNECTION_LEAK", "", "CONNECTION_LEAK", "检测到数据库连接泄漏", null);
  }

  /** 记录慢查询 */
  public void recordSlowQuery(String sql, long executionTime) {
    connectionStats.get("slow_queries").incrementAndGet();
    metricsCollector.incrementCounter("db_slow_queries");
    metricsCollector.recordOperationTime("db_slow_query", executionTime);

    LogUtil.logWarn(
        "DB_SLOW_QUERY",
        "",
        String.format("慢查询检测: 执行时间 %dms, SQL: %s", executionTime, truncateSql(sql)));
  }

  /** 获取连接池状态 */
  public ConnectionPoolStatus getConnectionPoolStatus() {
    ConnectionPoolStatus status = new ConnectionPoolStatus();
    status.timestamp = LocalDateTime.now();

    if (hikariPoolMXBean != null) {
      // HikariCP连接池信息
      status.totalConnections = hikariPoolMXBean.getTotalConnections();
      status.activeConnections = hikariPoolMXBean.getActiveConnections();
      status.idleConnections = hikariPoolMXBean.getIdleConnections();
      status.threadsAwaitingConnection = hikariPoolMXBean.getThreadsAwaitingConnection();

      // 计算使用率
      if (status.totalConnections > 0) {
        status.utilizationPercent =
            (double) status.activeConnections / status.totalConnections * 100;
      }

      // 检查连接池健康状态
      status.healthStatus = evaluatePoolHealth(status);

    } else {
      // 如果不是HikariCP，设置默认值
      status.healthStatus = PoolHealthStatus.UNKNOWN;
    }

    // 添加统计信息
    status.connectionsCreated = connectionStats.get("connections_created").get();
    status.connectionsClosed = connectionStats.get("connections_closed").get();
    status.connectionsTimeout = connectionStats.get("connections_timeout").get();
    status.connectionsLeaked = connectionStats.get("connections_leaked").get();
    status.slowQueries = connectionStats.get("slow_queries").get();

    return status;
  }

  /** 评估连接池健康状态 */
  private PoolHealthStatus evaluatePoolHealth(ConnectionPoolStatus status) {
    // 检查连接池使用率
    if (status.utilizationPercent > 90) {
      return PoolHealthStatus.CRITICAL;
    } else if (status.utilizationPercent > 80) {
      return PoolHealthStatus.WARNING;
    }

    // 检查等待连接的线程数
    if (status.threadsAwaitingConnection > 5) {
      return PoolHealthStatus.WARNING;
    } else if (status.threadsAwaitingConnection > 10) {
      return PoolHealthStatus.CRITICAL;
    }

    // 检查连接泄漏
    long leakCount = connectionStats.get("connections_leaked").get();
    Long lastLeakCount = lastMetricValues.get("connections_leaked");
    if (lastLeakCount != null && leakCount > lastLeakCount) {
      return PoolHealthStatus.WARNING;
    }

    return PoolHealthStatus.HEALTHY;
  }

  /** 获取连接池配置信息 */
  public ConnectionPoolConfig getConnectionPoolConfig() {
    ConnectionPoolConfig config = new ConnectionPoolConfig();

    if (dataSource instanceof HikariDataSource) {
      HikariDataSource hikariDataSource = (HikariDataSource) dataSource;

      config.maximumPoolSize = hikariDataSource.getMaximumPoolSize();
      config.minimumIdle = hikariDataSource.getMinimumIdle();
      config.connectionTimeout = hikariDataSource.getConnectionTimeout();
      config.idleTimeout = hikariDataSource.getIdleTimeout();
      config.maxLifetime = hikariDataSource.getMaxLifetime();
      config.leakDetectionThreshold = hikariDataSource.getLeakDetectionThreshold();
      config.validationTimeout = hikariDataSource.getValidationTimeout();
    }

    return config;
  }

  /** 优化连接池配置建议 */
  public ConnectionPoolOptimizationSuggestions getOptimizationSuggestions() {
    ConnectionPoolOptimizationSuggestions suggestions = new ConnectionPoolOptimizationSuggestions();
    ConnectionPoolStatus status = getConnectionPoolStatus();
    ConnectionPoolConfig config = getConnectionPoolConfig();

    // 基于当前状态提供优化建议
    if (status.utilizationPercent > 80) {
      suggestions.addSuggestion(
          "连接池使用率过高 (" + String.format("%.1f", status.utilizationPercent) + "%)，建议增加最大连接数");
    }

    if (status.threadsAwaitingConnection > 0) {
      suggestions.addSuggestion(
          "有 " + status.threadsAwaitingConnection + " 个线程在等待连接，建议检查连接池大小或查询性能");
    }

    if (status.connectionsLeaked > 0) {
      suggestions.addSuggestion("检测到 " + status.connectionsLeaked + " 个连接泄漏，建议检查代码中的连接管理");
    }

    if (status.slowQueries > 0) {
      suggestions.addSuggestion("检测到 " + status.slowQueries + " 个慢查询，建议优化SQL或添加索引");
    }

    // 配置优化建议
    if (config.maximumPoolSize > 0) {
      int recommendedMaxSize = calculateRecommendedMaxPoolSize();
      if (Math.abs(config.maximumPoolSize - recommendedMaxSize) > 5) {
        suggestions.addSuggestion("建议将最大连接数调整为 " + recommendedMaxSize);
      }
    }

    return suggestions;
  }

  /** 计算推荐的最大连接池大小 */
  private int calculateRecommendedMaxPoolSize() {
    // 基于CPU核心数和应用特性计算推荐值
    int cpuCores = Runtime.getRuntime().availableProcessors();

    // 对于IO密集型应用，推荐连接数 = CPU核心数 * 2
    // 对于CPU密集型应用，推荐连接数 = CPU核心数 + 1
    // 这里假设是IO密集型应用
    return cpuCores * 2;
  }

  /** 截断SQL语句用于日志记录 */
  private String truncateSql(String sql) {
    if (sql == null) {
      return "null";
    }

    String cleanSql = sql.replaceAll("\\s+", " ").trim();
    if (cleanSql.length() > 200) {
      return cleanSql.substring(0, 200) + "...";
    }

    return cleanSql;
  }

  /** 定期收集连接池指标 */
  @Scheduled(fixedRate = 60000) // 每分钟执行一次
  public void collectConnectionPoolMetrics() {
    if (!configManager.getMonitoringConfig().isMetricsCollectionEnabled()) {
      return;
    }

    try {
      ConnectionPoolStatus status = getConnectionPoolStatus();

      // 记录连接池指标
      metricsCollector.setGauge("db_pool_total_connections", status.totalConnections);
      metricsCollector.setGauge("db_pool_active_connections", status.activeConnections);
      metricsCollector.setGauge("db_pool_idle_connections", status.idleConnections);
      metricsCollector.setGauge("db_pool_threads_awaiting", status.threadsAwaitingConnection);
      metricsCollector.setGauge("db_pool_utilization_percent", (long) status.utilizationPercent);

      // 记录健康状态
      metricsCollector.incrementCounter(
          "db_pool_health_" + status.healthStatus.name().toLowerCase());

      // 检查是否需要告警
      if (status.healthStatus == PoolHealthStatus.CRITICAL) {
        LogUtil.logError(
            "DB_POOL_CRITICAL",
            "",
            "POOL_CRITICAL",
            String.format(
                "数据库连接池状态严重: 使用率 %.1f%%, 等待线程 %d",
                status.utilizationPercent, status.threadsAwaitingConnection),
            null);
      } else if (status.healthStatus == PoolHealthStatus.WARNING) {
        LogUtil.logWarn(
            "DB_POOL_WARNING",
            "",
            String.format(
                "数据库连接池状态警告: 使用率 %.1f%%, 等待线程 %d",
                status.utilizationPercent, status.threadsAwaitingConnection));
      }

      // 更新上次指标值
      lastMetricValues.put("connections_leaked", status.connectionsLeaked);

    } catch (Exception e) {
      LogUtil.logError("DB_POOL_METRICS_ERROR", "", "METRICS_ERROR", "连接池指标收集失败", e);
    }
  }

  // 枚举定义
  public enum PoolHealthStatus {
    HEALTHY, // 健康
    WARNING, // 警告
    CRITICAL, // 严重
    UNKNOWN // 未知
  }

  // 数据类定义
  public static class ConnectionPoolStatus {
    public LocalDateTime timestamp;
    public int totalConnections;
    public int activeConnections;
    public int idleConnections;
    public int threadsAwaitingConnection;
    public double utilizationPercent;
    public PoolHealthStatus healthStatus;

    // 统计信息
    public long connectionsCreated;
    public long connectionsClosed;
    public long connectionsTimeout;
    public long connectionsLeaked;
    public long slowQueries;
  }

  public static class ConnectionPoolConfig {
    public int maximumPoolSize;
    public int minimumIdle;
    public long connectionTimeout;
    public long idleTimeout;
    public long maxLifetime;
    public long leakDetectionThreshold;
    public long validationTimeout;
  }

  public static class ConnectionPoolOptimizationSuggestions {
    private final java.util.List<String> suggestions = new java.util.ArrayList<>();

    public void addSuggestion(String suggestion) {
      suggestions.add(suggestion);
    }

    public java.util.List<String> getSuggestions() {
      return new java.util.ArrayList<>(suggestions);
    }

    public boolean hasSuggestions() {
      return !suggestions.isEmpty();
    }
  }
}
