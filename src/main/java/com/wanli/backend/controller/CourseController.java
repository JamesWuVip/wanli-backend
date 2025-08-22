package com.wanli.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wanli.backend.service.CourseService;
import com.wanli.backend.util.JwtUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 课程管理控制器 处理课程相关的HTTP请求 */
@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

  // 常量定义
  private static final String SUCCESS_KEY = "success";
  private static final String MESSAGE_KEY = "message";
  private static final String COURSE_KEY = "course";
  private static final String COURSES_KEY = "courses";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String AUTH_FAILED_MESSAGE = "认证失败";
  private static final String INVALID_COURSE_ID_MESSAGE = "无效的课程ID格式";
  private static final String PERMISSION_DENIED_MESSAGE = "权限不足";
  private static final String NOT_FOUND_MESSAGE = "不存在";

  private final CourseService courseService;
  private final JwtUtil jwtUtil;

  @Autowired
  public CourseController(CourseService courseService, JwtUtil jwtUtil) {
    this.courseService = courseService;
    this.jwtUtil = jwtUtil;
  }

  /** 创建课程 POST /api/courses */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createCourse(
      @Valid @RequestBody CreateCourseRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    try {
      UUID creatorId = validateTokenAndGetUserId(authHeader);
      if (creatorId == null) {
        return createUnauthorizedResponse(AUTH_FAILED_MESSAGE);
      }
      Map<String, Object> result =
          courseService.createCourse(
              creatorId, request.getTitle(), request.getDescription(), request.getStatus());

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> courseData = (Map<String, Object>) result.get(COURSE_KEY);
        return ResponseEntity.status(HttpStatus.CREATED).body(courseData);
      } else {
        return ResponseEntity.badRequest().body(result);
      }

    } catch (Exception e) {
      return createErrorResponse("创建课程失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** 获取所有课程列表 GET /api/courses */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllCourses() {
    try {
      Map<String, Object> result = courseService.getAllCourses();

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        // 直接返回课程数组，符合测试期望
        return ResponseEntity.ok(result);
      } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
      }

    } catch (Exception e) {
      return createErrorResponse("获取课程列表失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** 根据ID获取课程详情 GET /api/courses/{id} */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable("id") String courseId) {
    try {
      UUID id = UUID.fromString(courseId);
      Map<String, Object> result = courseService.getCourseById(id);

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        return ResponseEntity.ok(result);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
      }

    } catch (IllegalArgumentException e) {
      return createErrorResponse(INVALID_COURSE_ID_MESSAGE, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return createErrorResponse("获取课程详情失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** 更新课程 PUT /api/courses/{id} */
  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> updateCourse(
      @PathVariable("id") String courseId,
      @Valid @RequestBody UpdateCourseRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    try {
      UUID userId = validateTokenAndGetUserId(authHeader);
      if (userId == null) {
        return createUnauthorizedResponse(AUTH_FAILED_MESSAGE);
      }

      UUID id = UUID.fromString(courseId);

      Map<String, Object> result =
          courseService.updateCourse(
              id, userId, request.getTitle(), request.getDescription(), request.getStatus());

      Boolean success = (Boolean) result.get(SUCCESS_KEY);
      if (Boolean.TRUE.equals(success)) {
        return ResponseEntity.ok(result);
      } else {
        return handleServiceError(result);
      }

    } catch (IllegalArgumentException e) {
      return createErrorResponse(INVALID_COURSE_ID_MESSAGE, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return createErrorResponse("更新课程失败：" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
      return authHeader.substring(7);
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
    if (message.contains(PERMISSION_DENIED_MESSAGE)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    } else if (message.contains(NOT_FOUND_MESSAGE)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    } else {
      return ResponseEntity.badRequest().body(result);
    }
  }

  /** 创建课程请求体 */
  public static class CreateCourseRequest {
    @NotBlank(message = "课程标题不能为空")
    @Size(max = 200, message = "课程标题长度不能超过200个字符")
    private String title;

    @Size(max = 1000, message = "课程描述长度不能超过1000个字符")
    private String description;

    private String status;

    // Getters and Setters
    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }

  /** 更新课程请求体 */
  public static class UpdateCourseRequest {
    @Size(max = 200, message = "课程标题长度不能超过200个字符")
    private String title;

    @Size(max = 1000, message = "课程描述长度不能超过1000个字符")
    private String description;

    private String status;

    // Getters and Setters
    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }
}
