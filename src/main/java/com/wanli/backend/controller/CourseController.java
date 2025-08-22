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

  @Autowired private CourseService courseService;

  @Autowired private JwtUtil jwtUtil;

  /** 创建课程 POST /api/courses */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createCourse(
      @Valid @RequestBody CreateCourseRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    try {
      // 检查是否提供了Authorization头部
      if (authHeader == null || authHeader.isEmpty()) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "缺少认证令牌");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
      }

      // 验证和解析JWT令牌
      String token = extractTokenFromHeader(authHeader);
      if (token == null || !jwtUtil.validateToken(token)) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "无效的认证令牌");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
      }

      UUID creatorId;
      try {
        creatorId = jwtUtil.getUserIdFromToken(token);
      } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "无效的认证令牌");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
      }
      // 调用服务层创建课程
      Map<String, Object> result =
          courseService.createCourse(
              creatorId, request.getTitle(), request.getDescription(), request.getStatus());

      if ((Boolean) result.get("success")) {
        // 直接返回课程对象，符合测试期望的响应格式
        @SuppressWarnings("unchecked")
        Map<String, Object> courseData = (Map<String, Object>) result.get("course");
        return ResponseEntity.status(HttpStatus.CREATED).body(courseData);
      } else {
        return ResponseEntity.badRequest().body(result);
      }

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "创建课程失败：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** 获取所有课程列表 GET /api/courses */
  @GetMapping
  public ResponseEntity<?> getAllCourses() {
    try {
      Map<String, Object> result = courseService.getAllCourses();

      if ((Boolean) result.get("success")) {
        // 直接返回课程数组，符合测试期望
        return ResponseEntity.ok(result.get("courses"));
      } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
      }

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "获取课程列表失败：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** 根据ID获取课程详情 GET /api/courses/{id} */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable("id") String courseId) {
    try {
      UUID id = UUID.fromString(courseId);
      Map<String, Object> result = courseService.getCourseById(id);

      if ((Boolean) result.get("success")) {
        return ResponseEntity.ok(result);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
      }

    } catch (IllegalArgumentException e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "无效的课程ID格式");
      return ResponseEntity.badRequest().body(errorResponse);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "获取课程详情失败：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** 更新课程 PUT /api/courses/{id} */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateCourse(
      @PathVariable("id") String courseId,
      @Valid @RequestBody UpdateCourseRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    try {
      // 验证和解析JWT令牌
      String token = extractTokenFromHeader(authHeader);
      if (token == null || !jwtUtil.validateToken(token)) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "无效的认证令牌");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
      }

      UUID id = UUID.fromString(courseId);
      UUID userId;
      try {
        userId = jwtUtil.getUserIdFromToken(token);
      } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "无效的认证令牌");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
      }

      Map<String, Object> result =
          courseService.updateCourse(
              id, userId, request.getTitle(), request.getDescription(), request.getStatus());

      if ((Boolean) result.get("success")) {
        // 直接返回课程对象，符合测试期望
        return ResponseEntity.ok(result.get("course"));
      } else {
        String message = (String) result.get("message");
        if (message.contains("权限不足")) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        } else if (message.contains("不存在")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } else {
          return ResponseEntity.badRequest().body(result);
        }
      }

    } catch (IllegalArgumentException e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "无效的课程ID格式");
      return ResponseEntity.badRequest().body(errorResponse);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "更新课程失败：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /**
   * 从Authorization头中提取JWT令牌
   *
   * @param authHeader Authorization头
   * @return JWT令牌
   */
  private String extractTokenFromHeader(String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
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
