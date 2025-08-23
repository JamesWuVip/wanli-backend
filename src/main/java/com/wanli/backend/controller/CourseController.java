package com.wanli.backend.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wanli.backend.service.CourseService;
import com.wanli.backend.util.AuthUtil;
import com.wanli.backend.util.ControllerLogUtil;
import com.wanli.backend.util.ControllerMonitorUtil;
import com.wanli.backend.util.ControllerResponseUtil;
import com.wanli.backend.util.ResponseUtil;
import com.wanli.backend.util.ServiceValidationUtil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

  @Autowired private CourseService courseService;

  @Autowired private AuthUtil authUtil;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createCourse(
      @RequestBody CreateCourseRequest request, @RequestHeader("Authorization") String authHeader) {

    return ControllerMonitorUtil.executeWithMonitoringAndErrorHandling(
        "createCourse",
        () -> {
          // 验证请求
          validateCreateCourseRequest(request);

          // 验证并获取用户ID
          UUID userId = authUtil.validateTokenAndGetUserId(authHeader);

          // 记录业务操作日志
          ControllerLogUtil.logCourseOperation("COURSE_CREATE", "创建课程", userId, request.getTitle());

          // 调用服务层
          Map<String, Object> result =
              courseService.createCourse(
                  userId, request.getTitle(), request.getDescription(), request.getStatus());

          // 处理结果
          return ControllerResponseUtil.fromServiceResult(result);
        },
        ControllerLogUtil.createLogContext("request", request));
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllCourses() {
    try {
      Map<String, Object> result = courseService.getAllCourses();
      return ResponseUtil.fromServiceResult(result);
    } catch (Exception e) {
      return ResponseUtil.internalServerError("获取课程列表失败：" + e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable UUID id) {
    try {
      Map<String, Object> result = courseService.getCourseById(id);
      return ResponseUtil.fromServiceResult(result);
    } catch (Exception e) {
      return ResponseUtil.internalServerError("获取课程详情失败：" + e.getMessage());
    }
  }

  @PutMapping("/{courseId}")
  public ResponseEntity<Map<String, Object>> updateCourse(
      @PathVariable UUID courseId,
      @RequestBody UpdateCourseRequest request,
      @RequestHeader("Authorization") String authHeader) {

    return ControllerMonitorUtil.executeWithMonitoringAndErrorHandling(
        "updateCourse",
        () -> {
          // 验证请求
          validateUpdateCourseRequest(request);

          // 验证并获取用户ID
          UUID userId = authUtil.validateTokenAndGetUserId(authHeader);

          // 记录业务操作日志
          ControllerLogUtil.logCourseOperation(
              "COURSE_UPDATE", "更新课程", userId, courseId, request.getTitle());

          // 调用服务层
          Map<String, Object> result =
              courseService.updateCourse(
                  courseId,
                  userId,
                  request.getTitle(),
                  request.getDescription(),
                  request.getStatus());

          // 处理结果
          return ControllerResponseUtil.fromServiceResult(result);
        },
        ControllerLogUtil.createCourseUpdateLogContext(courseId, request));
  }

  @DeleteMapping("/{courseId}")
  public ResponseEntity<Map<String, Object>> deleteCourse(
      @PathVariable UUID courseId, @RequestHeader("Authorization") String authHeader) {

    return ControllerMonitorUtil.executeWithMonitoringAndErrorHandling(
        "deleteCourse",
        () -> {
          // 验证并获取用户ID
          UUID userId = authUtil.validateTokenAndGetUserId(authHeader);

          // 调用服务层
          Map<String, Object> result = courseService.deleteCourse(courseId, userId);

          // 处理结果
          return ControllerResponseUtil.fromServiceResult(result);
        },
        ControllerLogUtil.createLogContext("courseId", courseId));
  }

  // 辅助方法
  private void validateCreateCourseRequest(CreateCourseRequest request) {
    ServiceValidationUtil.validateNotBlank(request.getTitle(), "课程标题");
    ServiceValidationUtil.validateNotBlank(request.getDescription(), "课程描述");
  }

  private void validateUpdateCourseRequest(UpdateCourseRequest request) {
    if (request.getTitle() != null) {
      ServiceValidationUtil.validateNotBlank(request.getTitle(), "课程标题");
    }
    if (request.getDescription() != null) {
      ServiceValidationUtil.validateNotBlank(request.getDescription(), "课程描述");
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
