package com.wanli.backend.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.wanli.backend.util.LogUtil;

/** 应用配置管理器 集中管理应用的各种配置参数 */
@Component
public class ApplicationConfigManager {

  // 缓存配置
  @Value("${app.cache.default-expire-minutes:60}")
  private int defaultCacheExpireMinutes;

  @Value("${app.cache.max-size:10000}")
  private int cacheMaxSize;

  @Value("${app.cache.enable-statistics:true}")
  private boolean cacheStatisticsEnabled;

  // 数据库配置
  @Value("${app.database.query-timeout:30}")
  private int databaseQueryTimeoutSeconds;

  @Value("${app.database.connection-pool-size:20}")
  private int connectionPoolSize;

  @Value("${app.database.enable-slow-query-log:true}")
  private boolean slowQueryLogEnabled;

  @Value("${app.database.slow-query-threshold:1000}")
  private long slowQueryThresholdMs;

  // 安全配置
  @Value("${app.security.jwt-expire-hours:24}")
  private int jwtExpireHours;

  @Value("${app.security.max-login-attempts:5}")
  private int maxLoginAttempts;

  @Value("${app.security.login-lock-minutes:15}")
  private int loginLockMinutes;

  @Value("${app.security.password-min-length:8}")
  private int passwordMinLength;

  // 业务配置
  @Value("${app.business.max-courses-per-user:100}")
  private int maxCoursesPerUser;

  @Value("${app.business.max-lessons-per-course:500}")
  private int maxLessonsPerCourse;

  @Value("${app.business.course-title-max-length:200}")
  private int courseTitleMaxLength;

  @Value("${app.business.lesson-title-max-length:200}")
  private int lessonTitleMaxLength;

  // 性能监控配置
  @Value("${app.monitoring.enable-performance-monitoring:true}")
  private boolean performanceMonitoringEnabled;

  @Value("${app.monitoring.slow-operation-threshold:2000}")
  private long slowOperationThresholdMs;

  @Value("${app.monitoring.enable-metrics-collection:true}")
  private boolean metricsCollectionEnabled;

  // 日志配置
  @Value("${app.logging.enable-business-log:true}")
  private boolean businessLogEnabled;

  @Value("${app.logging.enable-security-log:true}")
  private boolean securityLogEnabled;

  @Value("${app.logging.log-level:INFO}")
  private String logLevel;

  // 文件上传配置
  @Value("${app.upload.max-file-size:10485760}") // 10MB
  private long maxFileSize;

  @Value("${app.upload.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx}")
  private String allowedExtensions;

  @Value("${app.upload.upload-path:/uploads}")
  private String uploadPath;

  // 邮件配置
  @Value("${app.mail.enable-email-notification:false}")
  private boolean emailNotificationEnabled;

  @Value("${app.mail.smtp-timeout:30000}")
  private int smtpTimeout;

  // 异步任务配置
  @Value("${app.async.core-pool-size:5}")
  private int asyncCorePoolSize;

  @Value("${app.async.max-pool-size:20}")
  private int asyncMaxPoolSize;

  @Value("${app.async.keep-alive-time:60}")
  private long asyncKeepAliveTime;

  @Value("${app.async.queue-capacity:1000}")
  private int asyncQueueCapacity;

  @Value("${app.async.scheduled-threads:3}")
  private int asyncScheduledThreads;

  @Value("${app.async.task-retention-minutes:60}")
  private int asyncTaskRetentionMinutes;

  // 批处理配置
  @Value("${app.batch.core-pool-size:3}")
  private int batchCorePoolSize;

  @Value("${app.batch.max-pool-size:10}")
  private int batchMaxPoolSize;

  @Value("${app.batch.keep-alive-time:60}")
  private long batchKeepAliveTime;

  @Value("${app.batch.queue-capacity:500}")
  private int batchQueueCapacity;

  @Value("${app.batch.page-size:100}")
  private int batchPageSize;

  // 运行时配置缓存
  private final ConcurrentHashMap<String, Object> runtimeConfig = new ConcurrentHashMap<>();

  /** 获取缓存配置 */
  public CacheConfig getCacheConfig() {
    return new CacheConfig(defaultCacheExpireMinutes, cacheMaxSize, cacheStatisticsEnabled);
  }

  /** 获取缓存配置（别名方法） */
  public CacheConfig getCache() {
    return getCacheConfig();
  }

  /** 获取数据库配置 */
  public DatabaseConfig getDatabaseConfig() {
    return new DatabaseConfig(
        databaseQueryTimeoutSeconds, connectionPoolSize, slowQueryLogEnabled, slowQueryThresholdMs);
  }

  /** 获取数据库配置（别名方法） */
  public DatabaseConfig getDatabase() {
    return getDatabaseConfig();
  }

  /** 获取安全配置 */
  public SecurityConfig getSecurityConfig() {
    return new SecurityConfig(
        jwtExpireHours, maxLoginAttempts, loginLockMinutes, passwordMinLength);
  }

  /** 获取安全配置（别名方法） */
  public SecurityConfig getSecurity() {
    return getSecurityConfig();
  }

  /** 获取业务配置 */
  public BusinessConfig getBusinessConfig() {
    return new BusinessConfig(
        maxCoursesPerUser, maxLessonsPerCourse, courseTitleMaxLength, lessonTitleMaxLength);
  }

  /** 获取性能监控配置 */
  public MonitoringConfig getMonitoringConfig() {
    return new MonitoringConfig(
        performanceMonitoringEnabled, slowOperationThresholdMs, metricsCollectionEnabled);
  }

  /** 获取日志配置 */
  public LoggingConfig getLoggingConfig() {
    return new LoggingConfig(businessLogEnabled, securityLogEnabled, logLevel);
  }

  /** 获取文件上传配置 */
  public UploadConfig getUploadConfig() {
    return new UploadConfig(maxFileSize, allowedExtensions.split(","), uploadPath);
  }

  /** 获取邮件配置 */
  public MailConfig getMailConfig() {
    return new MailConfig(emailNotificationEnabled, smtpTimeout);
  }

  /** 获取异步配置 */
  public AsyncConfig getAsyncConfig() {
    return new AsyncConfig(
        asyncCorePoolSize,
        asyncMaxPoolSize,
        asyncKeepAliveTime,
        asyncQueueCapacity,
        asyncScheduledThreads,
        asyncTaskRetentionMinutes);
  }

  /** 获取批处理配置 */
  public BatchConfig getBatchConfig() {
    return new BatchConfig(
        batchCorePoolSize, batchMaxPoolSize, batchKeepAliveTime, batchQueueCapacity, batchPageSize);
  }

  /** 设置运行时配置 */
  public void setRuntimeConfig(String key, Object value) {
    runtimeConfig.put(key, value);
    Map<String, Object> context = new HashMap<>();
    context.put("key", key);
    context.put("value", value);
    LogUtil.logBusiness("CONFIG_RUNTIME_SET", context);
  }

  /** 获取运行时配置 */
  @SuppressWarnings("unchecked")
  public <T> T getRuntimeConfig(String key, T defaultValue) {
    return (T) runtimeConfig.getOrDefault(key, defaultValue);
  }

  /** 移除运行时配置 */
  public void removeRuntimeConfig(String key) {
    runtimeConfig.remove(key);
    Map<String, Object> context = new HashMap<>();
    context.put("key", key);
    LogUtil.logBusiness("CONFIG_RUNTIME_REMOVE", context);
  }

  /** 检查配置是否启用 */
  public boolean isFeatureEnabled(String featureName) {
    return getRuntimeConfig("feature." + featureName + ".enabled", true);
  }

  /** 启用/禁用功能 */
  public void setFeatureEnabled(String featureName, boolean enabled) {
    setRuntimeConfig("feature." + featureName + ".enabled", enabled);
  }

  // 配置类定义
  public static class CacheConfig {
    private final int defaultExpireMinutes;
    private final int maxSize;
    private final boolean statisticsEnabled;

    public CacheConfig(int defaultExpireMinutes, int maxSize, boolean statisticsEnabled) {
      this.defaultExpireMinutes = defaultExpireMinutes;
      this.maxSize = maxSize;
      this.statisticsEnabled = statisticsEnabled;
    }

    public int getDefaultExpireMinutes() {
      return defaultExpireMinutes;
    }

    public int getMaxSize() {
      return maxSize;
    }

    public boolean isStatisticsEnabled() {
      return statisticsEnabled;
    }

    public boolean isOptimizationEnabled() {
      return true;
    } // 默认启用缓存优化

    // 分布式锁相关配置
    public int getMaxSemaphorePermits() {
      return 100; // 默认最大信号量许可数
    }

    public boolean isEnableLockRenewal() {
      return true; // 默认启用锁续期
    }

    public long getLockRenewalInterval() {
      return 10000; // 默认锁续期间隔10秒
    }

    public long getLockLeaseTime() {
      return 30000; // 默认锁租期30秒
    }
  }

  public static class DatabaseConfig {
    private final int queryTimeoutSeconds;
    private final int connectionPoolSize;
    private final boolean slowQueryLogEnabled;
    private final long slowQueryThresholdMs;

    public DatabaseConfig(
        int queryTimeoutSeconds,
        int connectionPoolSize,
        boolean slowQueryLogEnabled,
        long slowQueryThresholdMs) {
      this.queryTimeoutSeconds = queryTimeoutSeconds;
      this.connectionPoolSize = connectionPoolSize;
      this.slowQueryLogEnabled = slowQueryLogEnabled;
      this.slowQueryThresholdMs = slowQueryThresholdMs;
    }

    public int getQueryTimeoutSeconds() {
      return queryTimeoutSeconds;
    }

    public int getConnectionPoolSize() {
      return connectionPoolSize;
    }

    public boolean isSlowQueryLogEnabled() {
      return slowQueryLogEnabled;
    }

    public int getSlowQueryThresholdMs() {
      return (int) slowQueryThresholdMs;
    }

    public boolean isOptimizationEnabled() {
      return true; // 默认启用查询优化
    }

    public long getSlowQueryThreshold() {
      return slowQueryThresholdMs;
    }

    // 连接池相关配置
    public boolean isEnableReadonlyPool() {
      return true; // 默认启用只读连接池
    }

    public int getReadonlyPoolSize() {
      return Math.max(1, connectionPoolSize / 2); // 只读池大小为主池的一半
    }

    public boolean isEnableBatchPool() {
      return true; // 默认启用批处理连接池
    }

    public int getBatchPoolSize() {
      return Math.max(1, connectionPoolSize / 4); // 批处理池大小为主池的四分之一
    }

    // 数据库连接配置
    public String getUrl() {
      // 这些方法应该由Spring的DataSource配置处理，不应该在这里硬编码
      // 返回null表示使用Spring Boot的默认数据源配置
      return null;
    }

    public String getUsername() {
      return null;
    }

    public String getPassword() {
      return null;
    }

    public String getDriverClassName() {
      return null;
    }

    public int getMaxPoolSize() {
      return connectionPoolSize;
    }

    public int getMinPoolSize() {
      return Math.max(1, connectionPoolSize / 4); // 最小连接数为最大连接数的四分之一
    }

    public long getConnectionTimeout() {
      return 30000; // 30秒连接超时
    }

    public long getIdleTimeout() {
      return 600000; // 10分钟空闲超时
    }

    public long getMaxLifetime() {
      return 1800000; // 30分钟最大生命周期
    }

    public long getLeakDetectionThreshold() {
      return 60000; // 60秒泄漏检测阈值
    }

    public int getBatchSize() {
      return 1000; // 默认批处理大小
    }
  }

  public static class SecurityConfig {
    private final int jwtExpireHours;
    private final int maxLoginAttempts;
    private final int loginLockMinutes;
    private final int passwordMinLength;
    private final boolean enableThreatDetection;
    private final boolean enableRealTimeMonitoring;

    public SecurityConfig(
        int jwtExpireHours, int maxLoginAttempts, int loginLockMinutes, int passwordMinLength) {
      this.jwtExpireHours = jwtExpireHours;
      this.maxLoginAttempts = maxLoginAttempts;
      this.loginLockMinutes = loginLockMinutes;
      this.passwordMinLength = passwordMinLength;
      this.enableThreatDetection = true; // 默认启用威胁检测
      this.enableRealTimeMonitoring = true; // 默认启用实时监控
    }

    public int getJwtExpireHours() {
      return jwtExpireHours;
    }

    public int getMaxLoginAttempts() {
      return maxLoginAttempts;
    }

    public int getLoginLockMinutes() {
      return loginLockMinutes;
    }

    public int getPasswordMinLength() {
      return passwordMinLength;
    }

    public boolean isEnableThreatDetection() {
      return enableThreatDetection;
    }

    public boolean isEnableRealTimeMonitoring() {
      return enableRealTimeMonitoring;
    }
  }

  public static class BusinessConfig {
    private final int maxCoursesPerUser;
    private final int maxLessonsPerCourse;
    private final int courseTitleMaxLength;
    private final int lessonTitleMaxLength;

    public BusinessConfig(
        int maxCoursesPerUser,
        int maxLessonsPerCourse,
        int courseTitleMaxLength,
        int lessonTitleMaxLength) {
      this.maxCoursesPerUser = maxCoursesPerUser;
      this.maxLessonsPerCourse = maxLessonsPerCourse;
      this.courseTitleMaxLength = courseTitleMaxLength;
      this.lessonTitleMaxLength = lessonTitleMaxLength;
    }

    public int getMaxCoursesPerUser() {
      return maxCoursesPerUser;
    }

    public int getMaxLessonsPerCourse() {
      return maxLessonsPerCourse;
    }

    public int getCourseTitleMaxLength() {
      return courseTitleMaxLength;
    }

    public int getLessonTitleMaxLength() {
      return lessonTitleMaxLength;
    }
  }

  public static class MonitoringConfig {
    private final boolean performanceMonitoringEnabled;
    private final long slowOperationThresholdMs;
    private final boolean metricsCollectionEnabled;
    private final long healthCheckTimeoutMs;
    private final boolean healthCheckEnabled;

    public MonitoringConfig(
        boolean performanceMonitoringEnabled,
        long slowOperationThresholdMs,
        boolean metricsCollectionEnabled) {
      this.performanceMonitoringEnabled = performanceMonitoringEnabled;
      this.slowOperationThresholdMs = slowOperationThresholdMs;
      this.metricsCollectionEnabled = metricsCollectionEnabled;
      this.healthCheckTimeoutMs = 5000; // 默认5秒超时
      this.healthCheckEnabled = true; // 默认启用健康检查
    }

    public boolean isPerformanceMonitoringEnabled() {
      return performanceMonitoringEnabled;
    }

    public long getSlowOperationThresholdMs() {
      return slowOperationThresholdMs;
    }

    public boolean isMetricsCollectionEnabled() {
      return metricsCollectionEnabled;
    }

    public long getHealthCheckTimeoutMs() {
      return healthCheckTimeoutMs;
    }

    public boolean isHealthCheckEnabled() {
      return healthCheckEnabled;
    }
  }

  public static class LoggingConfig {
    private final boolean businessLogEnabled;
    private final boolean securityLogEnabled;
    private final String logLevel;

    public LoggingConfig(boolean businessLogEnabled, boolean securityLogEnabled, String logLevel) {
      this.businessLogEnabled = businessLogEnabled;
      this.securityLogEnabled = securityLogEnabled;
      this.logLevel = logLevel;
    }

    public boolean isBusinessLogEnabled() {
      return businessLogEnabled;
    }

    public boolean isSecurityLogEnabled() {
      return securityLogEnabled;
    }

    public String getLogLevel() {
      return logLevel;
    }
  }

  public static class UploadConfig {
    private final long maxFileSize;
    private final String[] allowedExtensions;
    private final String uploadPath;

    public UploadConfig(long maxFileSize, String[] allowedExtensions, String uploadPath) {
      this.maxFileSize = maxFileSize;
      this.allowedExtensions = allowedExtensions;
      this.uploadPath = uploadPath;
    }

    public long getMaxFileSize() {
      return maxFileSize;
    }

    public String[] getAllowedExtensions() {
      return allowedExtensions;
    }

    public String getUploadPath() {
      return uploadPath;
    }
  }

  public static class MailConfig {
    private final boolean emailNotificationEnabled;
    private final int smtpTimeout;

    public MailConfig(boolean emailNotificationEnabled, int smtpTimeout) {
      this.emailNotificationEnabled = emailNotificationEnabled;
      this.smtpTimeout = smtpTimeout;
    }

    public boolean isEmailNotificationEnabled() {
      return emailNotificationEnabled;
    }

    public int getSmtpTimeout() {
      return smtpTimeout;
    }
  }

  public static class AsyncConfig {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final int queueCapacity;
    private final int scheduledThreads;
    private final int taskRetentionMinutes;

    public AsyncConfig(
        int corePoolSize,
        int maxPoolSize,
        long keepAliveTime,
        int queueCapacity,
        int scheduledThreads,
        int taskRetentionMinutes) {
      this.corePoolSize = corePoolSize;
      this.maxPoolSize = maxPoolSize;
      this.keepAliveTime = keepAliveTime;
      this.queueCapacity = queueCapacity;
      this.scheduledThreads = scheduledThreads;
      this.taskRetentionMinutes = taskRetentionMinutes;
    }

    public int getCorePoolSize() {
      return corePoolSize;
    }

    public int getMaxPoolSize() {
      return maxPoolSize;
    }

    public long getKeepAliveTime() {
      return keepAliveTime;
    }

    public int getQueueCapacity() {
      return queueCapacity;
    }

    public int getScheduledThreads() {
      return scheduledThreads;
    }

    public int getTaskRetentionMinutes() {
      return taskRetentionMinutes;
    }
  }

  public static class BatchConfig {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final int queueCapacity;
    private final int pageSize;
    private final int jobRetentionMinutes;

    public BatchConfig(
        int corePoolSize, int maxPoolSize, long keepAliveTime, int queueCapacity, int pageSize) {
      this.corePoolSize = corePoolSize;
      this.maxPoolSize = maxPoolSize;
      this.keepAliveTime = keepAliveTime;
      this.queueCapacity = queueCapacity;
      this.pageSize = pageSize;
      this.jobRetentionMinutes = 60; // 默认60分钟
    }

    public int getCorePoolSize() {
      return corePoolSize;
    }

    public int getMaxPoolSize() {
      return maxPoolSize;
    }

    public long getKeepAliveTime() {
      return keepAliveTime;
    }

    public int getQueueCapacity() {
      return queueCapacity;
    }

    public int getPageSize() {
      return pageSize;
    }

    public int getJobRetentionMinutes() {
      return jobRetentionMinutes;
    }
  }
}
