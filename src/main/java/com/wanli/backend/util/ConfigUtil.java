package com.wanli.backend.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 配置管理工具类 统一管理应用配置，支持动态配置更新 */
@Component
public class ConfigUtil {

  // 缓存配置值
  private final Map<String, Object> configCache = new ConcurrentHashMap<>();

  // JWT相关配置
  @Value("${jwt.secret:wanli-secret-key}")
  private String jwtSecret;

  @Value("${jwt.expiration:86400}")
  private Long jwtExpiration;

  // 数据库相关配置
  @Value("${spring.datasource.url:}")
  private String databaseUrl;

  @Value("${spring.jpa.show-sql:false}")
  private Boolean showSql;

  // 缓存相关配置
  @Value("${cache.enabled:true}")
  private Boolean cacheEnabled;

  @Value("${cache.default-expire-minutes:30}")
  private Integer cacheDefaultExpireMinutes;

  @Value("${cache.max-size:1000}")
  private Integer cacheMaxSize;

  // 日志相关配置
  @Value("${logging.level.com.wanli:INFO}")
  private String logLevel;

  @Value("${logging.performance.enabled:true}")
  private Boolean performanceLoggingEnabled;

  // 安全相关配置
  @Value("${security.password.min-length:8}")
  private Integer passwordMinLength;

  @Value("${security.login.max-attempts:5}")
  private Integer maxLoginAttempts;

  @Value("${security.session.timeout:3600}")
  private Integer sessionTimeout;

  // 业务相关配置
  @Value("${business.course.max-lessons:100}")
  private Integer maxLessonsPerCourse;

  @Value("${business.user.max-courses:50}")
  private Integer maxCoursesPerUser;

  // 文件上传配置
  @Value("${file.upload.max-size:10485760}")
  private Long maxFileSize;

  @Value("${file.upload.allowed-types:jpg,jpeg,png,pdf,doc,docx}")
  private String allowedFileTypes;

  /** 获取JWT密钥 */
  public String getJwtSecret() {
    return getCachedConfig("jwt.secret", jwtSecret, String.class);
  }

  /** 获取JWT过期时间（秒） */
  public Long getJwtExpiration() {
    return getCachedConfig("jwt.expiration", jwtExpiration, Long.class);
  }

  /** 获取数据库URL */
  public String getDatabaseUrl() {
    return getCachedConfig("database.url", databaseUrl, String.class);
  }

  /** 是否显示SQL */
  public Boolean isShowSql() {
    return getCachedConfig("database.show-sql", showSql, Boolean.class);
  }

  /** 是否启用缓存 */
  public Boolean isCacheEnabled() {
    return getCachedConfig("cache.enabled", cacheEnabled, Boolean.class);
  }

  /** 获取缓存默认过期时间（分钟） */
  public Integer getCacheDefaultExpireMinutes() {
    return getCachedConfig(
        "cache.default-expire-minutes", cacheDefaultExpireMinutes, Integer.class);
  }

  /** 获取缓存最大大小 */
  public Integer getCacheMaxSize() {
    return getCachedConfig("cache.max-size", cacheMaxSize, Integer.class);
  }

  /** 获取日志级别 */
  public String getLogLevel() {
    return getCachedConfig("log.level", logLevel, String.class);
  }

  /** 是否启用性能日志 */
  public Boolean isPerformanceLoggingEnabled() {
    return getCachedConfig("log.performance.enabled", performanceLoggingEnabled, Boolean.class);
  }

  /** 获取密码最小长度 */
  public Integer getPasswordMinLength() {
    return getCachedConfig("security.password.min-length", passwordMinLength, Integer.class);
  }

  /** 获取最大登录尝试次数 */
  public Integer getMaxLoginAttempts() {
    return getCachedConfig("security.login.max-attempts", maxLoginAttempts, Integer.class);
  }

  /** 获取会话超时时间（秒） */
  public Integer getSessionTimeout() {
    return getCachedConfig("security.session.timeout", sessionTimeout, Integer.class);
  }

  /** 获取每个课程最大课时数 */
  public Integer getMaxLessonsPerCourse() {
    return getCachedConfig("business.course.max-lessons", maxLessonsPerCourse, Integer.class);
  }

  /** 获取每个用户最大课程数 */
  public Integer getMaxCoursesPerUser() {
    return getCachedConfig("business.user.max-courses", maxCoursesPerUser, Integer.class);
  }

  /** 获取最大文件大小（字节） */
  public Long getMaxFileSize() {
    return getCachedConfig("file.upload.max-size", maxFileSize, Long.class);
  }

  /** 获取允许的文件类型 */
  public String[] getAllowedFileTypes() {
    String types = getCachedConfig("file.upload.allowed-types", allowedFileTypes, String.class);
    return types != null ? types.split(",") : new String[0];
  }

  /** 动态设置配置值 */
  public void setConfig(String key, Object value) {
    configCache.put(key, value);
    LogUtil.logBusinessOperation("CONFIG_UPDATE", "", "key=" + key + ", value=" + value);
  }

  /** 获取配置值 */
  @SuppressWarnings("unchecked")
  public <T> T getConfig(String key, T defaultValue, Class<T> type) {
    Object cachedValue = configCache.get(key);
    if (cachedValue != null && type.isInstance(cachedValue)) {
      return (T) cachedValue;
    }
    return defaultValue;
  }

  /** 获取缓存的配置值 */
  @SuppressWarnings("unchecked")
  private <T> T getCachedConfig(String key, T defaultValue, Class<T> type) {
    Object cachedValue = configCache.get(key);
    if (cachedValue != null && type.isInstance(cachedValue)) {
      return (T) cachedValue;
    }

    // 如果缓存中没有，使用默认值并缓存
    if (defaultValue != null) {
      configCache.put(key, defaultValue);
    }

    return defaultValue;
  }

  /** 移除配置 */
  public void removeConfig(String key) {
    Object removed = configCache.remove(key);
    if (removed != null) {
      LogUtil.logBusinessOperation("CONFIG_REMOVE", "", "key=" + key);
    }
  }

  /** 清空所有动态配置 */
  public void clearDynamicConfig() {
    int size = configCache.size();
    configCache.clear();
    LogUtil.logBusinessOperation("CONFIG_CLEAR", "", "cleared=" + size + " configs");
  }

  /** 获取所有配置 */
  public Map<String, Object> getAllConfigs() {
    Map<String, Object> allConfigs = new ConcurrentHashMap<>();

    // 添加静态配置
    allConfigs.put("jwt.secret", getJwtSecret());
    allConfigs.put("jwt.expiration", getJwtExpiration());
    allConfigs.put("database.url", getDatabaseUrl());
    allConfigs.put("database.show-sql", isShowSql());
    allConfigs.put("cache.default-expire-minutes", getCacheDefaultExpireMinutes());
    allConfigs.put("cache.max-size", getCacheMaxSize());
    allConfigs.put("log.level", getLogLevel());
    allConfigs.put("log.performance.enabled", isPerformanceLoggingEnabled());
    allConfigs.put("security.password.min-length", getPasswordMinLength());
    allConfigs.put("security.login.max-attempts", getMaxLoginAttempts());
    allConfigs.put("security.session.timeout", getSessionTimeout());
    allConfigs.put("business.course.max-lessons", getMaxLessonsPerCourse());
    allConfigs.put("business.user.max-courses", getMaxCoursesPerUser());
    allConfigs.put("file.upload.max-size", getMaxFileSize());
    allConfigs.put("file.upload.allowed-types", String.join(",", getAllowedFileTypes()));

    // 添加动态配置
    allConfigs.putAll(configCache);

    return allConfigs;
  }

  /** 验证配置完整性 */
  public boolean validateConfig() {
    try {
      // 验证必要配置
      if (getJwtSecret() == null || getJwtSecret().trim().isEmpty()) {
        LogUtil.logError("CONFIG_VALIDATION", "", "MISSING_CONFIG", "JWT secret is missing", null);
        return false;
      }

      if (getJwtExpiration() == null || getJwtExpiration() <= 0) {
        LogUtil.logError(
            "CONFIG_VALIDATION", "", "INVALID_CONFIG", "JWT expiration is invalid", null);
        return false;
      }

      if (getPasswordMinLength() == null || getPasswordMinLength() < 6) {
        LogUtil.logError(
            "CONFIG_VALIDATION", "", "INVALID_CONFIG", "Password min length is too short", null);
        return false;
      }

      LogUtil.logBusinessOperation("CONFIG_VALIDATION", "", "配置验证通过");
      return true;
    } catch (Exception e) {
      LogUtil.logError("CONFIG_VALIDATION", "", "VALIDATION_ERROR", e.getMessage(), e);
      return false;
    }
  }

  /** 重新加载配置 */
  public void reloadConfig() {
    configCache.clear();
    LogUtil.logBusinessOperation("CONFIG_RELOAD", "", "配置已重新加载");
  }
}
