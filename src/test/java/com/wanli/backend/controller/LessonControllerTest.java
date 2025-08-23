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
import com.wanli.backend.service.LessonService;
import com.wanli.backend.util.LogUtil;

@ExtendWith(MockitoExtension.class)
class LessonControllerTest {

  @Mock private LessonService lessonService;

  @InjectMocks private LessonController lessonController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private UUID testUserId;
  private UUID testCourseId;
  private UUID testLessonId;
  private String validToken;
  private String authHeader;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(lessonController).build();
    objectMapper = new ObjectMapper();
    testUserId = UUID.randomUUID();
    testCourseId = UUID.randomUUID();
    testLessonId = UUID.randomUUID();
    validToken = "valid-jwt-token";
    authHeader = "Bearer " + validToken;
  }

  @Test
  void createLesson_Success() throws Exception {
    // Given
    LessonController.CreateLessonRequest request = new LessonController.CreateLessonRequest();
    request.setTitle("Test Lesson");
    request.setDescription("Test Description");
    request.setCourseId(testCourseId.toString());
    request.setContent("Test Content");
    request.setVideoUrl("http://example.com/video.mp4");
    request.setDuration(3600);
    request.setStatus("PUBLISHED");
    request.setOrderIndex(1);

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("message", "课时创建成功");
    successResponse.put("data", Collections.singletonMap("lessonId", testLessonId));

    when(lessonService.createLesson(
            eq(request.getTitle()),
            eq(request.getDescription()),
            eq(request.getCourseId()),
            eq(request.getContent()),
            eq(request.getVideoUrl()),
            eq(request.getDuration()),
            eq(request.getStatus()),
            eq(request.getOrderIndex()),
            any(UUID.class)))
        .thenReturn(successResponse);

    try (MockedStatic<LogUtil> logUtilMock = mockStatic(LogUtil.class)) {
      // When & Then
      mockMvc
          .perform(
              post("/api/lessons")
                  .header("Authorization", authHeader)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("课时创建成功"));

      verify(lessonService)
          .createLesson(
              eq(request.getTitle()),
              eq(request.getDescription()),
              eq(request.getCourseId()),
              eq(request.getContent()),
              eq(request.getVideoUrl()),
              eq(request.getDuration()),
              eq(request.getStatus()),
              eq(request.getOrderIndex()),
              any(UUID.class));
      logUtilMock.verify(() -> LogUtil.logBusinessOperation(anyString(), anyString(), any()));
    }
  }

  @Test
  void createLesson_ValidationError_EmptyTitle() throws Exception {
    // Given
    LessonController.CreateLessonRequest request = new LessonController.CreateLessonRequest();
    request.setTitle(""); // Empty title
    request.setDescription("Test Description");
    request.setCourseId(testCourseId.toString());
    request.setOrderIndex(1);

    // When & Then
    mockMvc
        .perform(
            post("/api/lessons")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("课时标题不能为空"));

    verify(lessonService, never())
        .createLesson(any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void createLesson_ValidationError_InvalidCourseId() throws Exception {
    // Given
    LessonController.CreateLessonRequest request = new LessonController.CreateLessonRequest();
    request.setTitle("Test Lesson");
    request.setDescription("Test Description");
    request.setCourseId(""); // Empty course ID
    request.setOrderIndex(1);

    // When & Then
    mockMvc
        .perform(
            post("/api/lessons")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("课程ID不能为空"));

    verify(lessonService, never())
        .createLesson(any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void createLesson_ValidationError_InvalidOrderIndex() throws Exception {
    // Given
    LessonController.CreateLessonRequest request = new LessonController.CreateLessonRequest();
    request.setTitle("Test Lesson");
    request.setDescription("Test Description");
    request.setCourseId(testCourseId.toString());
    request.setOrderIndex(0); // Invalid order index

    // When & Then
    mockMvc
        .perform(
            post("/api/lessons")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("排序索引必须大于0"));

    verify(lessonService, never())
        .createLesson(any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void getLessonsByCourse_Success() throws Exception {
    // Given
    List<Map<String, Object>> lessons =
        Arrays.asList(
            Collections.singletonMap("id", testLessonId),
            Collections.singletonMap("id", UUID.randomUUID()));

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("data", lessons);

    when(lessonService.getLessonsByCourse(testCourseId)).thenReturn(successResponse);

    // When & Then
    mockMvc
        .perform(get("/api/lessons/course/{courseId}", testCourseId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());

    verify(lessonService).getLessonsByCourse(testCourseId);
  }

  @Test
  void getLessonsByCourse_InvalidUUID() throws Exception {
    // Given
    String invalidCourseId = "invalid-uuid";

    // When & Then
    mockMvc
        .perform(get("/api/lessons/course/{courseId}", invalidCourseId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("无效的课程ID格式"));

    verify(lessonService, never()).getLessonsByCourse(any());
  }

  @Test
  void getLessonById_Success() throws Exception {
    // Given
    Map<String, Object> lesson = new HashMap<>();
    lesson.put("id", testLessonId);
    lesson.put("title", "Test Lesson");

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("data", lesson);

    when(lessonService.getLessonById(testLessonId)).thenReturn(successResponse);

    // When & Then
    mockMvc
        .perform(get("/api/lessons/{id}", testLessonId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(testLessonId.toString()));

    verify(lessonService).getLessonById(testLessonId);
  }

  @Test
  void getLessonById_InvalidUUID() throws Exception {
    // Given
    String invalidLessonId = "invalid-uuid";

    // When & Then
    mockMvc
        .perform(get("/api/lessons/{id}", invalidLessonId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("无效的课时ID格式"));

    verify(lessonService, never()).getLessonById(any());
  }

  @Test
  void updateLesson_Success() throws Exception {
    // Given
    LessonController.UpdateLessonRequest request = new LessonController.UpdateLessonRequest();
    request.setTitle("Updated Lesson");
    request.setDescription("Updated Description");
    request.setContent("Updated Content");
    request.setDuration(7200);
    request.setStatus("PUBLISHED");
    request.setOrderIndex(2);

    Map<String, Object> successResponse = new HashMap<>();
    successResponse.put("success", true);
    successResponse.put("message", "课时更新成功");

    when(lessonService.updateLesson(
            eq(testLessonId),
            eq(request.getTitle()),
            eq(request.getDescription()),
            eq(request.getContent()),
            eq(request.getVideoUrl()),
            eq(request.getDuration()),
            eq(request.getStatus()),
            eq(request.getOrderIndex()),
            any(UUID.class)))
        .thenReturn(successResponse);

    try (MockedStatic<LogUtil> logUtilMock = mockStatic(LogUtil.class)) {
      // When & Then
      mockMvc
          .perform(
              put("/api/lessons/{id}", testLessonId)
                  .header("Authorization", authHeader)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("课时更新成功"));

      verify(lessonService)
          .updateLesson(
              eq(testLessonId),
              eq(request.getTitle()),
              eq(request.getDescription()),
              eq(request.getContent()),
              eq(request.getVideoUrl()),
              eq(request.getDuration()),
              eq(request.getStatus()),
              eq(request.getOrderIndex()),
              any(UUID.class));
      logUtilMock.verify(() -> LogUtil.logBusinessOperation(anyString(), anyString(), any()));
    }
  }

  @Test
  void updateLesson_InvalidUUID() throws Exception {
    // Given
    String invalidLessonId = "invalid-uuid";
    LessonController.UpdateLessonRequest request = new LessonController.UpdateLessonRequest();
    request.setTitle("Updated Lesson");

    // When & Then
    mockMvc
        .perform(
            put("/api/lessons/{id}", invalidLessonId)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("无效的课时ID格式"));

    verify(lessonService, never())
        .updateLesson(any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void updateLesson_ValidationError_EmptyTitle() throws Exception {
    // Given
    LessonController.UpdateLessonRequest request = new LessonController.UpdateLessonRequest();
    request.setTitle(""); // Empty title
    request.setDescription("Updated Description");

    // When & Then
    mockMvc
        .perform(
            put("/api/lessons/{id}", testLessonId)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("课时标题不能为空"));

    verify(lessonService, never())
        .updateLesson(any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void updateLesson_ValidationError_InvalidOrderIndex() throws Exception {
    // Given
    LessonController.UpdateLessonRequest request = new LessonController.UpdateLessonRequest();
    request.setTitle("Updated Lesson");
    request.setOrderIndex(0); // Invalid order index

    // When & Then
    mockMvc
        .perform(
            put("/api/lessons/{id}", testLessonId)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("排序索引必须大于0"));

    verify(lessonService, never())
        .updateLesson(any(), any(), any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void updateLesson_ServiceError() throws Exception {
    // Given
    LessonController.UpdateLessonRequest request = new LessonController.UpdateLessonRequest();
    request.setTitle("Updated Lesson");

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("message", "课时不存在");

    when(lessonService.updateLesson(
            eq(testLessonId),
            eq(request.getTitle()),
            eq(request.getDescription()),
            eq(request.getContent()),
            eq(request.getVideoUrl()),
            eq(request.getDuration()),
            eq(request.getStatus()),
            eq(request.getOrderIndex()),
            any(UUID.class)))
        .thenReturn(errorResponse);

    // When & Then
    mockMvc
        .perform(
            put("/api/lessons/{id}", testLessonId)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("课时不存在"));

    verify(lessonService)
        .updateLesson(
            eq(testLessonId),
            eq(request.getTitle()),
            eq(request.getDescription()),
            eq(request.getContent()),
            eq(request.getVideoUrl()),
            eq(request.getDuration()),
            eq(request.getStatus()),
            eq(request.getOrderIndex()),
            any(UUID.class));
  }
}
