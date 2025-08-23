package com.wanli.backend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/** 日志工具类 提供结构化日志记录功能，包括性能监控和安全日志 */
public class LogUtil {

  private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  // MDC键常量
  private static final String USER_ID = "userId";
  private static final String REQUEST_ID = "requestId";
  private static final String OPERATION = "operation";
  private static final String DURATION = "duration";
  private static final String IP_ADDRESS = "ipAddress";

  /** 设置用户上下文 */
  public static void setUserContext(String userId) {
    MDC.put(USER_ID, userId);
  }

  /** 设置请求上下文 */
  public static void setRequestContext(String requestId, String ipAddress) {
    MDC.put(REQUEST_ID, requestId);
    MDC.put(IP_ADDRESS, ipAddress);
  }

  /** 清除上下文 */
  public static void clearContext() {
    MDC.clear();
  }

  /** 记录业务操作日志 */
  public static void logBusinessOperation(String operation, String userId, String details) {
    Map<String, Object> logData = new HashMap<>();
    logData.put("type", "BUSINESS");
    logData.put("operation", operation);
    logData.put("userId", userId);
    logData.put("details", details);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    logger.info("业务操作: {}", formatLogData(logData));
  }

  /**
   * 记录业务操作日志（重载方法）
   *
   * @param operation 操作名称
   * @param description 操作描述
   * @param context 上下文数据
   */
  public static void logBusinessOperation(
      String operation, String description, Map<String, Object> context) {
    Map<String, Object> logData = new HashMap<>(context);
    logData.put("type", "BUSINESS");
    logData.put("operation", operation);
    logData.put("description", description);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    logger.info("业务操作: {}", formatLogData(logData));
  }

  /**
   * 记录通用业务日志
   *
   * @param operation 操作名称
   * @param context 上下文数据
   */
  public static void logBusiness(String operation, Map<String, Object> context) {
    Map<String, Object> logData = new HashMap<>(context);
    logData.put("operation", operation);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    logger.info("业务日志: {}", formatLogData(logData));
  }

  /** 记录性能监控日志 */
  public static void logPerformance(String operation, long duration, String details) {
    Map<String, Object> logData = new HashMap<>();
    logData.put("type", "PERFORMANCE");
    logData.put("operation", operation);
    logData.put("duration", duration + "ms");
    logData.put("details", details);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    if (duration > 1000) {
      logger.warn("性能警告: {}", formatLogData(logData));
    } else {
      logger.info("性能监控: {}", formatLogData(logData));
    }
  }

  /** 记录性能监控日志（重载方法） */
  public static void logPerformance(
      String operation, Object duration, Map<String, Object> context) {
    Map<String, Object> logData = new HashMap<>(context != null ? context : new HashMap<>());
    logData.put("type", "PERFORMANCE");
    logData.put("operation", operation);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    // 从context中提取duration信息来判断是否为慢操作
    String durationStr = (String) logData.get("duration");
    boolean isSlowOperation = false;
    if (durationStr != null && durationStr.endsWith("ms")) {
      try {
        long durationMs = Long.parseLong(durationStr.replace("ms", ""));
        isSlowOperation = durationMs > 1000;
      } catch (NumberFormatException e) {
        // 忽略解析错误
      }
    }

    if (isSlowOperation) {
      logger.warn("性能警告: {}", formatLogData(logData));
    } else {
      logger.info("性能监控: {}", formatLogData(logData));
    }
  }

  /** 记录数据库操作日志 */
  public static void logDatabaseOperation(
      String operation, String table, String userId, long duration) {
    Map<String, Object> logData = new HashMap<>();
    logData.put("type", "DATABASE");
    logData.put("operation", operation);
    logData.put("table", table);
    logData.put("userId", userId);
    logData.put("duration", duration + "ms");
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    if (duration > 500) {
      logger.warn("数据库慢查询: {}", formatLogData(logData));
    } else {
      logger.debug("数据库操作: {}", formatLogData(logData));
    }
  }

  /** 记录API调用日志 */
  public static void logApiCall(
      String method, String path, String userId, int statusCode, long duration) {
    Map<String, Object> logData = new HashMap<>();
    logData.put("type", "API");
    logData.put("method", method);
    logData.put("path", path);
    logData.put("userId", userId);
    logData.put("statusCode", statusCode);
    logData.put("duration", duration + "ms");
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    if (statusCode >= 400) {
      logger.warn("API错误: {}", formatLogData(logData));
    } else {
      logger.info("API调用: {}", formatLogData(logData));
    }
  }

  /** 记录错误日志 */
  public static void logError(
      String operation, String userId, String errorCode, String errorMessage, Exception ex) {
    Map<String, Object> logData = new HashMap<>();
    logData.put("type", "ERROR");
    logData.put("operation", operation);
    logData.put("userId", userId);
    logData.put("errorCode", errorCode);
    logData.put("errorMessage", errorMessage);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    if (ex != null) {
      logger.error("系统错误: {}", formatLogData(logData), ex);
    } else {
      logger.error("系统错误: {}", formatLogData(logData));
    }
  }

  /** 记录警告日志 */
  public static void logWarn(String operation, String errorCode, String message) {
    Map<String, Object> logData = new HashMap<>();
    logData.put("type", "WARNING");
    logData.put("operation", operation);
    logData.put("errorCode", errorCode);
    logData.put("message", message);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    logger.warn("系统警告: {}", formatLogData(logData));
  }

  /** 记录信息日志 */
  public static void logInfo(String operation, String userId, String message) {
    Map<String, Object> logData = new HashMap<>();
    logData.put("type", "INFO");
    logData.put("operation", operation);
    logData.put("userId", userId);
    logData.put("message", message);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    logger.info("系统信息: {}", formatLogData(logData));
  }

  /** 记录调试日志 */
  public static void logDebug(String operation, String userId, String message) {
    Map<String, Object> logData = new HashMap<>();
    logData.put("type", "DEBUG");
    logData.put("operation", operation);
    logData.put("userId", userId);
    logData.put("message", message);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));

    logger.debug("系统调试: {}", formatLogData(logData));
  }

  /** 性能监控器 */
  public static class PerformanceMonitor {
    private final String operation;
    private final long startTime;

    public PerformanceMonitor(String operation) {
      this.operation = operation;
      this.startTime = System.currentTimeMillis();
    }

    public void end() {
      long duration = System.currentTimeMillis() - startTime;
      Map<String, Object> context = new HashMap<>();
      context.put("duration", duration + "ms");
      logPerformance(operation, null, context);
    }
  }

  /** 开始性能监控 */
  public static PerformanceMonitor startPerformanceMonitor(String operation) {
    return new PerformanceMonitor(operation);
  }

  /**
   * 格式化日志数据
   *
   * @param logData 数据映射
   * @return 格式化后的字符串
   */
  private static String formatLogData(Map<String, Object> logData) {
    if (logData == null || logData.isEmpty()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder();
    logData.forEach(
        (key, value) -> {
          if (sb.length() > 0) {
            sb.append(", ");
          }

          // 敏感信息脱敏
          if (isSensitiveField(key)) {
            sb.append(key).append("=***");
          } else {
            String valueStr = value != null ? value.toString() : "null";
            // 限制值的长度，避免日志过长
            if (valueStr.length() > 200) {
              valueStr = valueStr.substring(0, 200) + "...";
            }
            sb.append(key).append("=").append(valueStr);
          }
        });
    return sb.toString();
  }

  /**
   * 检查是否为敏感字段
   *
   * @param fieldName 字段名
   * @return 是否为敏感字段
   */
  private static boolean isSensitiveField(String fieldName) {
    if (fieldName == null) {
      return false;
    }

    String lowerFieldName = fieldName.toLowerCase();
    return lowerFieldName.contains("password")
        || lowerFieldName.contains("token")
        || lowerFieldName.contains("secret")
        || lowerFieldName.contains("key")
        || lowerFieldName.contains("credential");
  }

  /**
   * 记录安全相关日志
   *
   * @param action 安全操作
   * @param data 相关数据
   */
  public static void logSecurity(String action, Map<String, Object> data) {
    Map<String, Object> logData = new HashMap<>(data);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));
    logData.put("logType", "SECURITY");
    logData.put("action", action);

    logger.warn("SECURITY: {} - {}", action, formatLogData(logData));
  }

  /**
   * 记录安全相关日志（重载方法）
   *
   * @param event 安全事件
   * @param userId 用户ID
   * @param ipAddress IP地址
   * @param details 详细信息
   */
  public static void logSecurity(String event, String userId, String ipAddress, String details) {
    Map<String, Object> data = new HashMap<>();
    data.put("userId", userId);
    data.put("ipAddress", ipAddress);
    data.put("details", details);
    logSecurity(event, data);
  }

  /**
   * 记录审计日志
   *
   * @param action 审计操作
   * @param userId 用户ID
   * @param data 相关数据
   */
  public static void logAudit(String action, String userId, Map<String, Object> data) {
    Map<String, Object> logData = new HashMap<>(data);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));
    logData.put("logType", "AUDIT");
    logData.put("action", action);
    logData.put("userId", userId);

    logger.info("AUDIT: {} by {} - {}", action, userId, formatLogData(logData));
  }

  /**
   * 记录数据库操作日志
   *
   * @param operation 数据库操作
   * @param table 表名
   * @param data 相关数据
   */
  public static void logDatabase(String operation, String table, Map<String, Object> data) {
    Map<String, Object> logData = new HashMap<>(data);
    logData.put("timestamp", LocalDateTime.now().format(FORMATTER));
    logData.put("logType", "DATABASE");
    logData.put("operation", operation);
    logData.put("table", table);

    logger.debug("DATABASE: {} on {} - {}", operation, table, formatLogData(logData));
  }
}
