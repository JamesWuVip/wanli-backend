package com.wanli.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanli.backend.service.LessonService;
import com.wanli.backend.util.JwtUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 课时管理控制器 处理课时相关的HTTP请求 */
@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@CrossOrigin(origins = "*")
public class LessonController {

  // 常量定义
  private static final String SUCCESS_KEY = "success";
  private static final String MESSAGE_KEY = "message";
  private static final String INVALID_TOKEN_MESSAGE = "无效的认证令牌";
  private static final String MISSING_TOKEN_MESSAGE = "缺少或无效的认证令牌";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = 7;
  private static final String INVALID_COURSE_ID_MESSAGE = "无效的课程ID格式";
  private static final String INVALID_LESSON_ID_MESSAGE = "无效的课时ID格式";
  private static final String INVALID_ID_MESSAGE = "无效的ID格式";
  private static final String LESSON_KEY = "lesson";
  private static final String LESSONS_KEY = "lessons";

  private final LessonService lessonService;
  private final JwtUtil jwtUtil;

  public LessonController(LessonService lessonService, JwtUtil jwtUtil) {
    this.lessonService = lessonService;
    this.jwtUtil = jwtUtil;
  }

  /** 创建课时 POST /api/courses/{courseId}/lessons */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createLesson(
      @PathVariable("courseId") String courseId,
      @Valid @RequestBody CreateLessonRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    try {
      UUID userId = validateTokenAndGetUserId(authHeader);
      if (userId == null) {
        return createUnauthorizedResponse(MISSING_TOKEN_MESSAGE);
      }

      UUID courseUuid = UUID.fromString(courseId);
      Map<String, Object> result =
          lessonService.createLesson(
              userId, courseUuid, request.getTitle(), request.getOrderIndex());

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, true);
        response.put(LESSON_KEY, result.get(LESSON_KEY));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
      } else {
        return handleServiceError(result);
      }

    } catch (IllegalArgumentException e) {
      return createErrorResponse(INVALID_COURSE_ID_MESSAGE, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return createErrorResponse("创建课时失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** 获取指定课程的课时列表 GET /api/courses/{courseId}/lessons */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getLessonsByCourseId(
      @PathVariable("courseId") String courseId) {

    try {
      UUID courseUuid = UUID.fromString(courseId);
      Map<String, Object> result = lessonService.getLessonsByCourseId(courseUuid);

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, true);
        response.put(LESSONS_KEY, result.get(LESSONS_KEY));
        return ResponseEntity.ok(response);
      } else {
        return handleServiceError(result);
      }

    } catch (IllegalArgumentException e) {
      return createErrorResponse(INVALID_COURSE_ID_MESSAGE, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return createErrorResponse("获取课时列表失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** 根据ID获取课时详情 GET /api/courses/{courseId}/lessons/{lessonId} */
  @GetMapping("/{lessonId}")
  public ResponseEntity<Map<String, Object>> getLessonById(
      @PathVariable("courseId") String courseId, @PathVariable("lessonId") String lessonId) {

    try {
      return getLessonByIdInternal(lessonId);
    } catch (IllegalArgumentException e) {
      return createErrorResponse(INVALID_LESSON_ID_MESSAGE, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return createErrorResponse("获取课时详情失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private ResponseEntity<Map<String, Object>> getLessonByIdInternal(String lessonId) {
    UUID lessonUuid = UUID.fromString(lessonId);
    Map<String, Object> result = lessonService.getLessonById(lessonUuid);

    Boolean success = (Boolean) result.get(SUCCESS_KEY);
    if (Boolean.TRUE.equals(success)) {
      return ResponseEntity.ok(result);
    } else {
      return handleServiceError(result);
    }
  }

  /** 更新课时 PUT /api/courses/{courseId}/lessons/{lessonId} */
  @PutMapping("/{lessonId}")
  public ResponseEntity<Map<String, Object>> updateLesson(
      @PathVariable("courseId") String courseId,
      @PathVariable("lessonId") String lessonId,
      @Valid @RequestBody UpdateLessonRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    try {
      UUID userId = validateTokenAndGetUserId(authHeader);
      if (userId == null) {
        return createUnauthorizedResponse(MISSING_TOKEN_MESSAGE);
      }

      UUID lessonUuid = UUID.fromString(lessonId);
      Map<String, Object> result =
          lessonService.updateLesson(
              lessonUuid, userId, request.getTitle(), request.getOrderIndex());

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        return ResponseEntity.ok(result);
      } else {
        return handleServiceError(result);
      }

    } catch (IllegalArgumentException e) {
      return createErrorResponse(INVALID_ID_MESSAGE, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return createErrorResponse("更新课时失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 从Authorization头中提取JWT令牌
   *
   * @param authHeader Authorization头
   * @return JWT令牌
   */
  private String extractTokenFromHeader(String authHeader) {
    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
      return authHeader.substring(BEARER_PREFIX_LENGTH);
    }
    return null;
  }

  /**
   * 验证JWT令牌并获取用户ID
   *
   * @param authHeader Authorization头
   * @return 用户ID，验证失败返回null
   */
  private UUID validateTokenAndGetUserId(String authHeader) {
    if (authHeader == null || authHeader.isEmpty()) {
      return null;
    }

    String token = extractTokenFromHeader(authHeader);
    if (token == null || !jwtUtil.validateToken(token)) {
      return null;
    }

    try {
      return jwtUtil.getUserIdFromToken(token);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 创建错误响应
   *
   * @param message 错误消息
   * @param status HTTP状态码
   * @return 错误响应
   */
  private ResponseEntity<Map<String, Object>> createErrorResponse(
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
  private ResponseEntity<Map<String, Object>> createUnauthorizedResponse(String message) {
    return createErrorResponse(message, HttpStatus.UNAUTHORIZED);
  }

  /**
   * 处理服务层错误
   *
   * @param result 服务层返回结果
   * @return 相应的HTTP响应
   */
  private ResponseEntity<Map<String, Object>> handleServiceError(Map<String, Object> result) {
    String message = (String) result.get(MESSAGE_KEY);
    if (message.contains("权限不足")) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    } else if (message.contains("不存在")) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    } else {
      return ResponseEntity.badRequest().body(result);
    }
  }

  /** 创建课时请求体 */
  public static class CreateLessonRequest {
    @NotBlank(message = "课时标题不能为空")
    @Size(max = 200, message = "课时标题长度不能超过200个字符")
    private String title;

    @Min(value = 1, message = "排序索引必须大于0")
    @JsonProperty("order_index")
    private Integer orderIndex;

    // Getters and Setters
    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public Integer getOrderIndex() {
      return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
      this.orderIndex = orderIndex;
    }
  }

  /** 更新课时请求体 */
  public static class UpdateLessonRequest {
    @Size(max = 200, message = "课时标题长度不能超过200个字符")
    private String title;

    @Min(value = 1, message = "排序索引必须大于0")
    @JsonProperty("order_index")
    private Integer orderIndex;

    // Getters and Setters
    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public Integer getOrderIndex() {
      return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
      this.orderIndex = orderIndex;
    }
  }
}
