package com.wanli.backend.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Service层响应工具类 提供统一的响应构建方法，减少重复代码 */
public class ServiceResponseUtil {

  private static final String SUCCESS_KEY = "success";
  private static final String MESSAGE_KEY = "message";
  private static final String DATA_KEY = "data";
  private static final String ERROR_CODE_KEY = "errorCode";
  private static final String TIMESTAMP_KEY = "timestamp";

  /**
   * 创建成功响应
   *
   * @param message 成功消息
   * @return 成功响应
   */
  public static Map<String, Object> success(String message) {
    Map<String, Object> response = new HashMap<>();
    response.put(SUCCESS_KEY, true);
    response.put(MESSAGE_KEY, message);
    response.put(TIMESTAMP_KEY, System.currentTimeMillis());
    return response;
  }

  /**
   * 创建带数据的成功响应
   *
   * @param message 成功消息
   * @param data 响应数据
   * @return 成功响应
   */
  public static Map<String, Object> success(String message, Object data) {
    Map<String, Object> response = success(message);
    response.put(DATA_KEY, data);
    return response;
  }

  /**
   * 创建带自定义键值对的成功响应
   *
   * @param message 成功消息
   * @param key 数据键
   * @param value 数据值
   * @return 成功响应
   */
  public static Map<String, Object> success(String message, String key, Object value) {
    Map<String, Object> response = success(message);
    response.put(key, value);
    return response;
  }

  /**
   * 创建带多个键值对的成功响应
   *
   * @param message 成功消息
   * @param dataMap 数据映射
   * @return 成功响应
   */
  public static Map<String, Object> success(String message, Map<String, Object> dataMap) {
    Map<String, Object> response = success(message);
    if (dataMap != null) {
      response.putAll(dataMap);
    }
    return response;
  }

  /**
   * 创建错误响应
   *
   * @param message 错误消息
   * @return 错误响应
   */
  public static Map<String, Object> error(String message) {
    Map<String, Object> response = new HashMap<>();
    response.put(SUCCESS_KEY, false);
    response.put(MESSAGE_KEY, message);
    response.put(TIMESTAMP_KEY, System.currentTimeMillis());
    return response;
  }

  /**
   * 创建带错误代码的错误响应
   *
   * @param message 错误消息
   * @param errorCode 错误代码
   * @return 错误响应
   */
  public static Map<String, Object> error(String message, String errorCode) {
    Map<String, Object> response = error(message);
    response.put(ERROR_CODE_KEY, errorCode);
    return response;
  }

  /**
   * 创建带数据的错误响应
   *
   * @param message 错误消息
   * @param data 错误数据
   * @return 错误响应
   */
  public static Map<String, Object> error(String message, Object data) {
    Map<String, Object> response = error(message);
    response.put(DATA_KEY, data);
    return response;
  }

  // 常用的业务响应方法

  /**
   * 创建创建成功响应
   *
   * @param entityName 实体名称
   * @param entity 创建的实体
   * @return 创建成功响应
   */
  public static Map<String, Object> createSuccess(String entityName, Object entity) {
    return success(entityName + "创建成功", entityName, entity);
  }

  /**
   * 更新成功响应
   *
   * @param entityName 实体名称
   * @param entity 更新的实体
   * @return 更新成功响应
   */
  public static Map<String, Object> updateSuccess(String entityName, Object entity) {
    return success(entityName + "更新成功", entityName, entity);
  }

  /**
   * 删除成功响应
   *
   * @param entityName 实体名称
   * @return 删除成功响应
   */
  public static Map<String, Object> deleteSuccess(String entityName) {
    return success(entityName + "删除成功");
  }

  /**
   * 批量删除成功响应
   *
   * @param entityName 实体名称
   * @param count 删除数量
   * @return 批量删除成功响应
   */
  public static Map<String, Object> batchDeleteSuccess(String entityName, int count) {
    return success("批量删除" + entityName + "成功", "deletedCount", count);
  }

  /**
   * 获取列表成功响应
   *
   * @param entityName 实体名称
   * @param list 列表数据
   * @return 获取列表成功响应
   */
  public static Map<String, Object> listSuccess(String entityName, List<?> list) {
    return success("获取" + entityName + "列表成功", entityName + "s", list);
  }

  /**
   * 获取详情成功响应
   *
   * @param entityName 实体名称
   * @param entity 实体数据
   * @return 获取详情成功响应
   */
  public static Map<String, Object> detailSuccess(String entityName, Object entity) {
    return success("获取" + entityName + "详情成功", entityName, entity);
  }

  /**
   * 批量操作成功响应
   *
   * @param operation 操作名称
   * @param entityName 实体名称
   * @param entities 实体列表
   * @return 批量操作成功响应
   */
  public static Map<String, Object> batchSuccess(
      String operation, String entityName, List<?> entities) {
    return success("批量" + operation + entityName + "成功", entityName + "s", entities);
  }

  // 常用的错误响应方法

  /**
   * 实体未找到错误响应
   *
   * @param entityName 实体名称
   * @return 未找到错误响应
   */
  public static Map<String, Object> notFound(String entityName) {
    return error(entityName + "未找到", "NOT_FOUND");
  }

  /**
   * 权限不足错误响应
   *
   * @return 权限不足错误响应
   */
  public static Map<String, Object> permissionDenied() {
    return error("权限不足", "PERMISSION_DENIED");
  }

  /**
   * 参数验证失败错误响应
   *
   * @param message 验证失败消息
   * @return 参数验证失败错误响应
   */
  public static Map<String, Object> validationError(String message) {
    return error(message, "VALIDATION_ERROR");
  }

  /**
   * 业务逻辑错误响应
   *
   * @param message 错误消息
   * @return 业务逻辑错误响应
   */
  public static Map<String, Object> businessError(String message) {
    return error(message, "BUSINESS_ERROR");
  }
}
