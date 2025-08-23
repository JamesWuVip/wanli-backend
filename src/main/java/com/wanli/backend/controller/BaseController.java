package com.wanli.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.util.JwtUtil;

/** 基础Controller类，提供公共的认证逻辑和响应处理方法 */
public abstract class BaseController {

  protected static final String BEARER_PREFIX = "Bearer ";
  protected static final String SUCCESS_KEY = "success";
  protected static final String MESSAGE_KEY = "message";
  protected static final String PERMISSION_DENIED_MESSAGE = "权限不足";
  protected static final String NOT_FOUND_MESSAGE = "未找到";

  @Autowired protected JwtUtil jwtUtil;

  /**
   * 从Authorization头中提取JWT令牌
   *
   * @param authHeader Authorization头
   * @return JWT令牌
   */
  protected String extractTokenFromHeader(String authHeader) {
    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
      return authHeader.substring(7);
    }
    return null;
  }

  /**
   * 验证JWT令牌并获取用户ID
   *
   * @param authHeader Authorization头
   * @return 用户ID
   * @throws BusinessException 认证失败时抛出异常
   */
  protected UUID validateTokenAndGetUserId(String authHeader) {
    String token = extractTokenFromHeader(authHeader);
    if (token == null) {
      throw BusinessException.unauthorized("认证失败：无效的token格式");
    }

    UUID userId = jwtUtil.extractUserId(token);
    if (userId == null) {
      throw BusinessException.unauthorized("认证失败：无效的token");
    }

    return userId;
  }

  /**
   * 创建错误响应
   *
   * @param message 错误消息
   * @param status HTTP状态码
   * @return 错误响应
   */
  protected ResponseEntity<Map<String, Object>> createErrorResponse(
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
  protected ResponseEntity<Map<String, Object>> createUnauthorizedResponse(String message) {
    return createErrorResponse(message, HttpStatus.UNAUTHORIZED);
  }

  /**
   * 处理服务层错误
   *
   * @param result 服务层返回结果
   * @return 相应的HTTP响应
   */
  protected ResponseEntity<Map<String, Object>> handleServiceError(Map<String, Object> result) {
    String message = (String) result.get(MESSAGE_KEY);
    if (message.contains(PERMISSION_DENIED_MESSAGE)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    } else if (message.contains(NOT_FOUND_MESSAGE)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    } else {
      return ResponseEntity.badRequest().body(result);
    }
  }
}
