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
import com.wanli.backend.util.AuthUtil;
import com.wanli.backend.util.ControllerLogUtil;
import com.wanli.backend.util.ControllerMonitorUtil;
import com.wanli.backend.util.ControllerResponseUtil;
import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.ResponseUtil;
import com.wanli.backend.util.ServiceValidationUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

  @Autowired private LessonService lessonService;
  @Autowired private AuthUtil authUtil;

  // 辅助方法
  private void validateCreateLessonRequest(CreateLessonRequest request) {
    ServiceValidationUtil.validateNotBlank(request.getTitle(), "课时标题");
    ServiceValidationUtil.validateNotBlank(request.getContent(), "课时内容");
    ServiceValidationUtil.validateNotNull(request.getCourseId(), "课程ID");
    ServiceValidationUtil.validatePositiveInteger(request.getOrderIndex(), "课时顺序");
  }

  private void validateUpdateLessonRequest(UpdateLessonRequest request) {
    if (request.getTitle() != null) {
      ServiceValidationUtil.validateNotBlank(request.getTitle(), "课时标题");
    }
    if (request.getContent() != null) {
      ServiceValidationUtil.validateNotBlank(request.getContent(), "课时内容");
    }
    if (request.getOrderIndex() != null) {
      ServiceValidationUtil.validatePositiveInteger(request.getOrderIndex(), "课时顺序");
    }
  }

  private void logLessonOperation(
      String operation, String description, UUID userId, String lessonTitle, UUID courseId) {
    Map<String, Object> logContext = new HashMap<>();
    logContext.put("userId", userId);
    logContext.put("lessonTitle", lessonTitle);
    logContext.put("courseId", courseId);
    LogUtil.logBusinessOperation(operation, description, logContext);
  }

  private void logLessonUpdateOperation(
      String operation, String description, UUID userId, UUID lessonId, String lessonTitle) {
    Map<String, Object> logContext = new HashMap<>();
    logContext.put("userId", userId);
    logContext.put("lessonId", lessonId);
    logContext.put("lessonTitle", lessonTitle);
    LogUtil.logBusinessOperation(operation, description, logContext);
  }

  private Map<String, Object> createLogContext(String key, Object value) {
    Map<String, Object> context = new HashMap<>();
    context.put(key, value);
    return context;
  }

  private Map<String, Object> createUpdateLogContext(UUID lessonId, UpdateLessonRequest request) {
    Map<String, Object> context = new HashMap<>();
    context.put("lessonId", lessonId);
    context.put("request", request);
    return context;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createLesson(
      @RequestBody CreateLessonRequest request, @RequestHeader("Authorization") String authHeader) {

    return ControllerMonitorUtil.executeWithMonitoringAndErrorHandling(
        "createLesson",
        () -> {
          // 验证请求
          validateCreateLessonRequest(request);

          // 验证并获取用户ID
          UUID userId = authUtil.validateTokenAndGetUserId(authHeader);

          // 记录业务操作日志
          UUID courseUuid = UUID.fromString(request.getCourseId());
          ControllerLogUtil.logLessonOperation(
              "LESSON_CREATE", "创建课时", userId, request.getTitle(), courseUuid);

          // 调用服务层
          Map<String, Object> result =
              lessonService.createLesson(
                  request.getCourseId(),
                  userId,
                  request.getTitle(),
                  request.getDescription(),
                  request.getContent(),
                  request.getVideoUrl(),
                  request.getDuration(),
                  request.getStatus(),
                  request.getOrderIndex());

          // 处理结果
          return ControllerResponseUtil.fromServiceResult(result);
        },
        ControllerLogUtil.createLogContext("request", request));
  }

  @GetMapping("/course/{courseId}")
  public ResponseEntity<Map<String, Object>> getLessonsByCourse(
      @PathVariable String courseId, @RequestHeader("Authorization") String authHeader) {
    try {
      // 输入验证
      try {
        UUID.fromString(courseId);
      } catch (IllegalArgumentException e) {
        return ControllerResponseUtil.createBadRequestResponse("无效的课程ID格式");
      }

      // 验证并获取用户ID
      UUID userId = authUtil.validateTokenAndGetUserId(authHeader);
      Map<String, Object> result = lessonService.getLessonsByCourseId(courseId, userId);
      return ControllerResponseUtil.fromServiceResult(result);
    } catch (Exception e) {
      return ControllerResponseUtil.createInternalServerErrorResponse("获取课时列表失败：" + e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getLessonById(
      @PathVariable String id, @RequestHeader("Authorization") String authHeader) {
    try {
      // 输入验证
      try {
        UUID.fromString(id);
      } catch (IllegalArgumentException e) {
        return ControllerResponseUtil.createBadRequestResponse("无效的课时ID格式");
      }

      // 验证并获取用户ID
      UUID userId = authUtil.validateTokenAndGetUserId(authHeader);
      Map<String, Object> result = lessonService.getLessonById(id, userId);
      return ControllerResponseUtil.fromServiceResult(result);
    } catch (Exception e) {
      return ControllerResponseUtil.createInternalServerErrorResponse("获取课时详情失败：" + e.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> updateLesson(
      @PathVariable String id,
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody UpdateLessonRequest request) {

    return ControllerMonitorUtil.executeWithMonitoringAndErrorHandling(
        "updateLesson",
        () -> {
          // 输入验证
          try {
            UUID.fromString(id);
          } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest("无效的课时ID格式");
          }

          UUID lessonId = UUID.fromString(id);

          // 验证请求
          validateUpdateLessonRequest(request);

          // 验证并获取用户ID
          UUID userId = authUtil.validateTokenAndGetUserId(authHeader);

          // 记录业务操作日志
          logLessonUpdateOperation("LESSON_UPDATE", "更新课时", userId, lessonId, request.getTitle());

          // 调用服务层
          Map<String, Object> result =
              lessonService.updateLesson(
                  lessonId.toString(),
                  userId,
                  request.getTitle(),
                  request.getDescription(),
                  request.getContent(),
                  request.getVideoUrl(),
                  request.getDuration(),
                  request.getStatus(),
                  request.getOrderIndex());

          // 处理结果
          return ControllerResponseUtil.fromServiceResult(result);
        },
        ControllerLogUtil.createLessonUpdateLogContext(UUID.fromString(id), request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteLesson(
      @PathVariable String id, @RequestHeader("Authorization") String authHeader) {

    try {
      return ControllerMonitorUtil.executeWithMonitoring(
          "deleteLesson",
          () -> {
            UUID userId = authUtil.validateTokenAndGetUserId(authHeader);

            var result = lessonService.deleteLesson(id, userId);
            ControllerLogUtil.logLessonOperation(
                "DELETE_LESSON", "课时删除", userId, UUID.fromString(id), "课时");

            return ControllerResponseUtil.fromServiceResult(result);
          });
    } catch (Exception e) {
      ControllerLogUtil.logOperationError("删除课时失败", e, "lessonId", id);
      return ControllerResponseUtil.createErrorResponse(
          e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /** 创建课时请求体 */
  public static class CreateLessonRequest {
    @NotBlank(message = "课时标题不能为空")
    @Size(max = 200, message = "课时标题长度不能超过200个字符")
    public String title;

    @Size(max = 1000, message = "课时描述长度不能超过1000个字符")
    public String description;

    @JsonProperty("course_id")
    public String courseId;

    public String content;

    @JsonProperty("video_url")
    public String videoUrl;

    @Min(value = 0, message = "课时时长不能为负数")
    public Integer duration;

    public String status;

    @Min(value = 1, message = "排序索引必须大于0")
    @JsonProperty("order_index")
    public Integer orderIndex;

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

    public String getCourseId() {
      return courseId;
    }

    public void setCourseId(String courseId) {
      this.courseId = courseId;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getVideoUrl() {
      return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
      this.videoUrl = videoUrl;
    }

    public Integer getDuration() {
      return duration;
    }

    public void setDuration(Integer duration) {
      this.duration = duration;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
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
    public String title;

    @Size(max = 1000, message = "课时描述长度不能超过1000个字符")
    public String description;

    public String content;

    @JsonProperty("video_url")
    public String videoUrl;

    @Min(value = 0, message = "课时时长不能为负数")
    public Integer duration;

    public String status;

    @Min(value = 1, message = "排序索引必须大于0")
    @JsonProperty("order_index")
    public Integer orderIndex;

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

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getVideoUrl() {
      return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
      this.videoUrl = videoUrl;
    }

    public Integer getDuration() {
      return duration;
    }

    public void setDuration(Integer duration) {
      this.duration = duration;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public Integer getOrderIndex() {
      return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
      this.orderIndex = orderIndex;
    }
  }
}
