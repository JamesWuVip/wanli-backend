package com.wanli.backend.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** 响应工具类 提供统一的响应格式和常用响应方法 */
public final class ResponseUtil {

  // 常量定义
  public static final String SUCCESS_KEY = "success";
  public static final String MESSAGE_KEY = "message";
  public static final String DATA_KEY = "data";

  // 私有构造函数，防止实例化
  private ResponseUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * 创建成功响应
   *
   * @param message 成功消息
   * @param data 响应数据
   * @return 成功响应
   */
  public static ResponseEntity<Map<String, Object>> success(String message, Object data) {
    Map<String, Object> response = new HashMap<>();
    response.put(SUCCESS_KEY, true);
    response.put(MESSAGE_KEY, message);
    if (data != null) {
      response.put(DATA_KEY, data);
    }
    return ResponseEntity.ok(response);
  }

  /**
   * 创建成功响应（无数据）
   *
   * @param message 成功消息
   * @return 成功响应
   */
  public static ResponseEntity<Map<String, Object>> success(String message) {
    return success(message, null);
  }

  /**
   * 创建创建成功响应
   *
   * @param message 成功消息
   * @param data 响应数据
   * @return 创建成功响应
   */
  public static ResponseEntity<Map<String, Object>> created(String message, Object data) {
    Map<String, Object> response = new HashMap<>();
    response.put(SUCCESS_KEY, true);
    response.put(MESSAGE_KEY, message);
    if (data != null) {
      response.put(DATA_KEY, data);
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 创建错误响应
   *
   * @param message 错误消息
   * @param status HTTP状态码
   * @return 错误响应
   */
  public static ResponseEntity<Map<String, Object>> error(String message, HttpStatus status) {
    Map<String, Object> response = new HashMap<>();
    response.put(SUCCESS_KEY, false);
    response.put(MESSAGE_KEY, message);
    return ResponseEntity.status(status).body(response);
  }

  /**
   * 创建带错误代码的错误响应
   *
   * @param errorCode 错误代码
   * @param message 错误消息
   * @return 错误响应
   */
  public static Map<String, Object> error(String errorCode, String message) {
    Map<String, Object> response = new HashMap<>();
    response.put(SUCCESS_KEY, false);
    response.put(MESSAGE_KEY, message);
    response.put("errorCode", errorCode);
    return response;
  }

  /**
   * 创建错误响应（默认400状态码）
   *
   * @param message 错误消息
   * @return 错误响应
   */
  public static ResponseEntity<Map<String, Object>> badRequest(String message) {
    return error(message, HttpStatus.BAD_REQUEST);
  }

  /**
   * 创建未授权响应
   *
   * @param message 错误消息
   * @return 未授权响应
   */
  public static ResponseEntity<Map<String, Object>> unauthorized(String message) {
    return error(message, HttpStatus.UNAUTHORIZED);
  }

  /**
   * 创建禁止访问响应
   *
   * @param message 错误消息
   * @return 禁止访问响应
   */
  public static ResponseEntity<Map<String, Object>> forbidden(String message) {
    return error(message, HttpStatus.FORBIDDEN);
  }

  /**
   * 创建未找到响应
   *
   * @param message 错误消息
   * @return 未找到响应
   */
  public static ResponseEntity<Map<String, Object>> notFound(String message) {
    return error(message, HttpStatus.NOT_FOUND);
  }

  /**
   * 创建内部服务器错误响应
   *
   * @param message 错误消息
   * @return 内部服务器错误响应
   */
  public static ResponseEntity<Map<String, Object>> internalServerError(String message) {
    return error(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * 根据服务层结果创建响应
   *
   * @param result 服务层返回结果
   * @return HTTP响应
   */
  public static ResponseEntity<Map<String, Object>> fromServiceResult(Map<String, Object> result) {
    Boolean success = (Boolean) result.get(SUCCESS_KEY);
    String message = (String) result.get(MESSAGE_KEY);

    if (Boolean.TRUE.equals(success)) {
      return ResponseEntity.ok(result);
    } else {
      // 根据错误消息判断状态码
      if (message.contains("权限不足") || message.contains("权限")) {
        return forbidden(message);
      } else if (message.contains("不存在") || message.contains("未找到")) {
        return notFound(message);
      } else if (message.contains("认证") || message.contains("令牌")) {
        return unauthorized(message);
      } else {
        return badRequest(message);
      }
    }
  }
}
