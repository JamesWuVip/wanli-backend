package com.wanli.backend.util;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.wanli.backend.exception.BusinessException;

/** Controller层性能监控工具类 提供统一的性能监控和异常处理功能 */
public class ControllerMonitorUtil {

  /**
   * 执行带性能监控的操作
   *
   * @param operationName 操作名称
   * @param operation 要执行的操作
   * @param <T> 返回类型
   * @return 操作结果
   */
  public static <T> T executeWithMonitoring(String operationName, MonitoredOperation<T> operation)
      throws Exception {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor(operationName)) {
      return operation.execute();
    }
  }

  /**
   * 执行带性能监控和异常处理的操作
   *
   * @param operationName 操作名称
   * @param operation 要执行的操作
   * @param logContext 日志上下文
   * @param <T> 返回类型
   * @return 操作结果
   */
  public static <T> T executeWithMonitoringAndErrorHandling(
      String operationName, MonitoredOperation<T> operation, Map<String, Object> logContext) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor(operationName)) {
      return operation.execute();
    } catch (BusinessException e) {
      LogUtil.logError(operationName, "", "BUSINESS_ERROR", e.getMessage(), e);
      throw e; // 让全局异常处理器处理
    } catch (Exception e) {
      LogUtil.logError(operationName, "", "OPERATION_ERROR", e.getMessage(), e);
      throw BusinessException.internalServerError("操作执行失败: " + operationName);
    }
  }

  /**
   * 执行带性能监控和异常处理的Controller操作，返回ResponseEntity
   *
   * @param operationName 操作名称
   * @param operation 要执行的操作
   * @param logContext 日志上下文
   * @return ResponseEntity响应
   */
  public static ResponseEntity<Map<String, Object>> executeControllerOperation(
      String operationName,
      MonitoredOperation<ResponseEntity<Map<String, Object>>> operation,
      Map<String, Object> logContext) {
    try {
      return executeWithMonitoringAndErrorHandling(operationName, operation, logContext);
    } catch (BusinessException e) {
      // 将业务异常转换为适当的HTTP响应
      return ControllerResponseUtil.createErrorResponse(e.getMessage(), e.getHttpStatus());
    } catch (Exception e) {
      // 处理其他未预期的异常
      LogUtil.logError(
          operationName + "_UNEXPECTED_ERROR", "", "UNEXPECTED_ERROR", e.getMessage(), e);
      return ControllerResponseUtil.createInternalServerErrorResponse("系统内部错误");
    }
  }

  /** 监控操作接口 */
  @FunctionalInterface
  public interface MonitoredOperation<T> {
    T execute() throws Exception;
  }
}
