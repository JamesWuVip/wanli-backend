package com.wanli.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.backend.service.CourseService;
import com.wanli.backend.util.JwtUtil;
import com.wanli.backend.util.LogUtil;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

  @Mock private CourseService courseService;

  @Mock private JwtUtil jwtUtil;

  @InjectMocks private CourseController courseController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private UUID testUserId;
  private String validToken;
  private String authHeader;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(courseController).build();
    objectMapper = new ObjectMapper();
    testUserId = UUID.randomUUID();
    validToken = "valid-jwt-token";
    authHeader = "Bearer " + validToken;
  }

  @Test
  void createCourse_Success() throws Exception {
    // Given
    CourseController.CreateCourseRequest request = new CourseController.CreateCourseRequest();
    request.setTitle("Test Course");
    request.setDescription("Test Description");
    request.setStatus("ACTIVE");

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("message", "课程创建成功");
    successResponse.put("data", Collections.singletonMap("courseId", UUID.randomUUID()));

    when(jwtUtil.extractUserId(validToken)).thenReturn(testUserId);
    when(courseService.createCourse(eq(testUserId), anyString(), anyString(), anyString()))
        .thenReturn(successResponse);

    try (MockedStatic<LogUtil> logUtilMock = mockStatic(LogUtil.class)) {
      // When & Then
      mockMvc
          .perform(
              post("/api/courses")
                  .header("Authorization", authHeader)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("课程创建成功"));

      verify(courseService).createCourse(eq(testUserId), anyString(), anyString(), anyString());
      logUtilMock.verify(() -> LogUtil.logBusinessOperation(anyString(), anyString(), anyString()));
    }
  }

  @Test
  void createCourse_InvalidToken() throws Exception {
    // Given
    CourseController.CreateCourseRequest request = new CourseController.CreateCourseRequest();
    request.setTitle("Test Course");
    request.setDescription("Test Description");

    when(jwtUtil.extractUserId(validToken)).thenReturn(null);

    // When & Then
    mockMvc
        .perform(
            post("/api/courses")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("认证失败：无效的token"));

    verify(courseService, never()).createCourse(any(), any(), any(), any());
  }

  @Test
  void createCourse_MissingAuthHeader() throws Exception {
    // Given
    CourseController.CreateCourseRequest request = new CourseController.CreateCourseRequest();
    request.setTitle("Test Course");
    request.setDescription("Test Description");

    // When & Then
    mockMvc
        .perform(
            post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("认证失败：无效的token格式"));

    verify(courseService, never()).createCourse(any(), any(), any(), any());
  }

  @Test
  void createCourse_ValidationError() throws Exception {
    // Given
    CourseController.CreateCourseRequest request = new CourseController.CreateCourseRequest();
    request.setTitle(""); // Empty title
    request.setDescription("Test Description");

    when(jwtUtil.extractUserId(validToken)).thenReturn(testUserId);

    // When & Then
    mockMvc
        .perform(
            post("/api/courses")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("课程标题不能为空"));

    verify(courseService, never()).createCourse(any(), any(), any(), any());
  }

  @Test
  void getAllCourses_Success() throws Exception {
    // Given
    List<Map<String, Object>> courses =
        Arrays.asList(
            Collections.singletonMap("id", UUID.randomUUID()),
            Collections.singletonMap("id", UUID.randomUUID()));

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("data", courses);

    when(jwtUtil.extractUserId(validToken)).thenReturn(testUserId);
    when(courseService.getAllCourses()).thenReturn(successResponse);

    // When & Then
    mockMvc
        .perform(get("/api/courses").header("Authorization", authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());

    verify(courseService).getAllCourses();
  }

  @Test
  void getCourseById_Success() throws Exception {
    // Given
    UUID courseId = UUID.randomUUID();
    Map<String, Object> course = new HashMap<>();
    course.put("id", courseId);
    course.put("title", "Test Course");

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("data", course);

    when(jwtUtil.extractUserId(validToken)).thenReturn(testUserId);
    when(courseService.getCourseById(courseId)).thenReturn(successResponse);

    // When & Then
    mockMvc
        .perform(get("/api/courses/{id}", courseId).header("Authorization", authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(courseId.toString()));

    verify(courseService).getCourseById(courseId);
  }

  @Test
  void getCourseById_NotFound() throws Exception {
    // Given
    UUID courseId = UUID.randomUUID();
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("message", "课程不存在");

    when(jwtUtil.extractUserId(validToken)).thenReturn(testUserId);
    when(courseService.getCourseById(courseId)).thenReturn(errorResponse);

    // When & Then
    mockMvc
        .perform(get("/api/courses/{id}", courseId).header("Authorization", authHeader))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("课程不存在"));

    verify(courseService).getCourseById(courseId);
  }

  @Test
  void updateCourse_Success() throws Exception {
    // Given
    UUID courseId = UUID.randomUUID();
    CourseController.UpdateCourseRequest request = new CourseController.UpdateCourseRequest();
    request.setTitle("Updated Course");
    request.setDescription("Updated Description");

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("message", "课程更新成功");

    when(jwtUtil.extractUserId(validToken)).thenReturn(testUserId);
    when(courseService.updateCourse(
            eq(courseId),
            eq(testUserId),
            eq(request.getTitle()),
            eq(request.getDescription()),
            eq(request.getStatus())))
        .thenReturn(successResponse);

    try (MockedStatic<LogUtil> logUtilMock = mockStatic(LogUtil.class)) {
      // When & Then
      mockMvc
          .perform(
              put("/api/courses/{id}", courseId)
                  .header("Authorization", authHeader)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("课程更新成功"));

      verify(courseService)
          .updateCourse(
              eq(courseId),
              eq(testUserId),
              eq(request.getTitle()),
              eq(request.getDescription()),
              eq(request.getStatus()));
      logUtilMock.verify(() -> LogUtil.logBusinessOperation(anyString(), anyString(), anyString()));
    }
  }

  @Test
  void updateCourse_ValidationError() throws Exception {
    // Given
    UUID courseId = UUID.randomUUID();
    CourseController.UpdateCourseRequest request = new CourseController.UpdateCourseRequest();
    request.setTitle(""); // Empty title
    request.setDescription("Updated Description");

    when(jwtUtil.extractUserId(validToken)).thenReturn(testUserId);

    // When & Then
    mockMvc
        .perform(
            put("/api/courses/{id}", courseId)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("课程标题不能为空"));

    verify(courseService, never()).updateCourse(any(), any(), any(), any(), any());
  }

  @Test
  void updateCourse_PermissionDenied() throws Exception {
    // Given
    UUID courseId = UUID.randomUUID();
    CourseController.UpdateCourseRequest request = new CourseController.UpdateCourseRequest();
    request.setTitle("Updated Course");

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("message", "权限不足");

    when(jwtUtil.extractUserId(validToken)).thenReturn(testUserId);
    when(courseService.updateCourse(
            eq(courseId),
            eq(testUserId),
            eq(request.getTitle()),
            eq(request.getDescription()),
            eq(request.getStatus())))
        .thenReturn(errorResponse);

    // When & Then
    mockMvc
        .perform(
            put("/api/courses/{id}", courseId)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("权限不足"));

    verify(courseService)
        .updateCourse(
            eq(courseId),
            eq(testUserId),
            eq(request.getTitle()),
            eq(request.getDescription()),
            eq(request.getStatus()));
  }
}
