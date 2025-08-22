package com.wanli.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired private LessonService lessonService;

  @Autowired private JwtUtil jwtUtil;

  /** 创建课时 POST /api/courses/{courseId}/lessons */
  @PostMapping
  public ResponseEntity<?> createLesson(
      @PathVariable("courseId") String courseId,
      @Valid @RequestBody CreateLessonRequest request,
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

      UUID courseUuid = UUID.fromString(courseId);
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
          lessonService.createLesson(
              userId, courseUuid, request.getTitle(), request.getOrderIndex());

      if ((Boolean) result.get("success")) {
        return ResponseEntity.status(HttpStatus.CREATED).body(result.get("lesson"));
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
      errorResponse.put("message", "创建课时失败：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** 获取指定课程的课时列表 GET /api/courses/{courseId}/lessons */
  @GetMapping
  public ResponseEntity<?> getLessonsByCourseId(@PathVariable("courseId") String courseId) {

    try {
      UUID courseUuid = UUID.fromString(courseId);
      Map<String, Object> result = lessonService.getLessonsByCourseId(courseUuid);

      if ((Boolean) result.get("success")) {
        return ResponseEntity.ok(result.get("lessons"));
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
      errorResponse.put("message", "获取课时列表失败：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** 根据ID获取课时详情 GET /api/courses/{courseId}/lessons/{lessonId} */
  @GetMapping("/{lessonId}")
  public ResponseEntity<Map<String, Object>> getLessonById(
      @PathVariable("courseId") String courseId, @PathVariable("lessonId") String lessonId) {

    try {
      UUID lessonUuid = UUID.fromString(lessonId);
      Map<String, Object> result = lessonService.getLessonById(lessonUuid);

      if ((Boolean) result.get("success")) {
        return ResponseEntity.ok(result);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
      }

    } catch (IllegalArgumentException e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "无效的课时ID格式");
      return ResponseEntity.badRequest().body(errorResponse);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "获取课时详情失败：" + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

      UUID lessonUuid = UUID.fromString(lessonId);
      UUID userId = jwtUtil.getUserIdFromToken(token);

      Map<String, Object> result =
          lessonService.updateLesson(
              lessonUuid, userId, request.getTitle(), request.getOrderIndex());

      if ((Boolean) result.get("success")) {
        return ResponseEntity.ok(result);
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
      errorResponse.put("message", "无效的ID格式");
      return ResponseEntity.badRequest().body(errorResponse);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "更新课时失败：" + e.getMessage());
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
