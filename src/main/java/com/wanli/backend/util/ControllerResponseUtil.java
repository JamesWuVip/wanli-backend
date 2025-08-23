package com.wanli.backend.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Controller响应处理工具类 统一处理Controller层的响应格式和错误处理 */
public class ControllerResponseUtil {

  private static final String SUCCESS_KEY = "success";
  private static final String MESSAGE_KEY = "message";
  private static final String DATA_KEY = "data";
  private static final String PERMISSION_DENIED_MESSAGE = "权限不足";
  private static final String NOT_FOUND_MESSAGE = "未找到";

  /**
   * 创建成功响应
   *
   * @param message 成功消息
   * @param data 响应数据
   * @return 成功响应
   */
  public static ResponseEntity<Map<String, Object>> createSuccessResponse(
      String message, Object data) {
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
  public static ResponseEntity<Map<String, Object>> createSuccessResponse(String message) {
    return createSuccessResponse(message, null);
  }

  /**
   * 创建错误响应
   *
   * @param message 错误消息
   * @param status HTTP状态码
   * @return 错误响应
   */
  public static ResponseEntity<Map<String, Object>> createErrorResponse(
      String message, HttpStatus status) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put(SUCCESS_KEY, false);
    errorResponse.put(MESSAGE_KEY, message);
    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * 创建未授权响应
   *
   * @param message 错误消息
   * @return 未授权响应
   */
  public static ResponseEntity<Map<String, Object>> createUnauthorizedResponse(String message) {
    return createErrorResponse(message, HttpStatus.UNAUTHORIZED);
  }

  /**
   * 创建禁止访问响应
   *
   * @param message 错误消息
   * @return 禁止访问响应
   */
  public static ResponseEntity<Map<String, Object>> createForbiddenResponse(String message) {
    return createErrorResponse(message, HttpStatus.FORBIDDEN);
  }

  /**
   * 创建未找到响应
   *
   * @param message 错误消息
   * @return 未找到响应
   */
  public static ResponseEntity<Map<String, Object>> createNotFoundResponse(String message) {
    return createErrorResponse(message, HttpStatus.NOT_FOUND);
  }

  /**
   * 创建请求错误响应
   *
   * @param message 错误消息
   * @return 请求错误响应
   */
  public static ResponseEntity<Map<String, Object>> createBadRequestResponse(String message) {
    return createErrorResponse(message, HttpStatus.BAD_REQUEST);
  }

  /**
   * 创建内部服务器错误响应
   *
   * @param message 错误消息
   * @return 内部服务器错误响应
   */
  public static ResponseEntity<Map<String, Object>> createInternalServerErrorResponse(
      String message) {
    return createErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * 处理服务层错误 根据服务层返回的结果自动判断HTTP状态码
   *
   * @param result 服务层返回结果
   * @return 相应的HTTP响应
   */
  public static ResponseEntity<Map<String, Object>> handleServiceError(Map<String, Object> result) {
    String message = (String) result.get(MESSAGE_KEY);
    if (message == null) {
      return createInternalServerErrorResponse("未知错误");
    }

    if (message.contains(PERMISSION_DENIED_MESSAGE)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    } else if (message.contains(NOT_FOUND_MESSAGE)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    } else if (message.contains("认证失败") || message.contains("token")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    } else {
      return ResponseEntity.badRequest().body(result);
    }
  }

  /**
   * 从服务层结果创建响应 根据服务层返回的success字段自动判断是成功还是失败响应
   *
   * @param result 服务层返回结果
   * @return HTTP响应
   */
  public static ResponseEntity<Map<String, Object>> fromServiceResult(Map<String, Object> result) {
    Boolean success = (Boolean) result.get(SUCCESS_KEY);
    if (success != null && success) {
      return ResponseEntity.ok(result);
    } else {
      return handleServiceError(result);
    }
  }

  /**
   * 验证UUID格式并创建错误响应
   *
   * @param id UUID字符串
   * @param fieldName 字段名称
   * @return 如果格式无效返回错误响应，否则返回null
   */
  public static ResponseEntity<Map<String, Object>> validateUUIDFormat(
      String id, String fieldName) {
    try {
      java.util.UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      return createBadRequestResponse("无效的" + fieldName + "格式");
    }
    return null;
  }

  /**
   * 验证必填字段并创建错误响应
   *
   * @param value 字段值
   * @param fieldName 字段名称
   * @return 如果字段为空返回错误响应，否则返回null
   */
  public static ResponseEntity<Map<String, Object>> validateRequiredField(
      String value, String fieldName) {
    if (ValidationUtil.isBlank(value)) {
      return createBadRequestResponse(fieldName + "不能为空");
    }
    return null;
  }
}
