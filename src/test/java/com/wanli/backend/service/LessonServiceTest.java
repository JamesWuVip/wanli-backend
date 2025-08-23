package com.wanli.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wanli.backend.entity.Course;
import com.wanli.backend.entity.Lesson;
import com.wanli.backend.entity.User;
import com.wanli.backend.enums.LessonStatus;
import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.exception.PermissionDeniedException;
import com.wanli.backend.exception.ResourceNotFoundException;
import com.wanli.backend.repository.CourseRepository;
import com.wanli.backend.repository.LessonRepository;
import com.wanli.backend.repository.UserRepository;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.ConfigUtil;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

  @Mock private LessonRepository lessonRepository;

  @Mock private CourseRepository courseRepository;

  @Mock private UserRepository userRepository;

  @Mock private CacheUtil cacheUtil;

  @Mock private ConfigUtil configUtil;

  @InjectMocks private LessonService lessonService;

  private UUID testUserId;
  private UUID testCourseId;
  private UUID testLessonId;
  private User testUser;
  private Course testCourse;
  private Lesson testLesson;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testCourseId = UUID.randomUUID();
    testLessonId = UUID.randomUUID();

    testUser = new User();
    testUser.setId(testUserId);
    testUser.setUsername("testuser");
    testUser.setRole("TEACHER");
    testUser.setDeletedAt(null);

    testCourse = new Course();
    testCourse.setId(testCourseId);
    testCourse.setCreatorId(testUserId);
    testCourse.setTitle("Test Course");
    testCourse.setDescription("Test Description");
    testCourse.setStatus("PUBLISHED");
    testCourse.setDeletedAt(null);
    testCourse.setCreatedAt(LocalDateTime.now());
    testCourse.setUpdatedAt(LocalDateTime.now());

    testLesson = new Lesson();
    testLesson.setId(testLessonId);
    testLesson.setCourse(testCourse);
    testLesson.setTitle("Test Lesson");
    testLesson.setDescription("Test Lesson Description");
    testLesson.setContent("Test Content");
    testLesson.setVideoUrl("https://example.com/video.mp4");
    testLesson.setDuration(1800); // 30 minutes
    testLesson.setStatus(LessonStatus.PUBLISHED);
    testLesson.setOrderIndex(1);
    testLesson.setDeletedAt(null);
    testLesson.setCreatedAt(LocalDateTime.now());
    testLesson.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void createLesson_Success() {
    // Given
    String courseId = testCourseId.toString();
    String title = "New Lesson";
    String description = "Lesson Description";
    String content = "Lesson Content";
    String videoUrl = "https://example.com/video.mp4";
    Integer duration = 1800;
    String status = "DRAFT";
    Integer orderIndex = 1;

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(lessonRepository.findByCourseIdAndTitleAndNotDeleted(testCourseId, title))
        .thenReturn(Optional.empty());
    when(lessonRepository.existsByCourseIdAndOrderIndexAndNotDeleted(testCourseId, orderIndex))
        .thenReturn(false);
    when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

    // When
    Map<String, Object> result =
        lessonService.createLesson(
            courseId,
            testUserId,
            title,
            description,
            content,
            videoUrl,
            duration,
            status,
            orderIndex);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("课时创建成功", result.get("message"));

    verify(lessonRepository).save(any(Lesson.class));
    verify(cacheUtil, atLeastOnce()).remove(anyString());
  }

  @Test
  void createLesson_InvalidCourseId_ThrowsException() {
    // Given
    String invalidCourseId = "invalid-uuid";

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          lessonService.createLesson(
              invalidCourseId,
              testUserId,
              "Title",
              "Description",
              "Content",
              "https://example.com/video.mp4",
              1800,
              "DRAFT",
              1);
        });
  }

  @Test
  void createLesson_EmptyTitle_ThrowsException() {
    // Given
    String courseId = testCourseId.toString();
    String emptyTitle = "";

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          lessonService.createLesson(
              courseId,
              testUserId,
              emptyTitle,
              "Description",
              "Content",
              "https://example.com/video.mp4",
              1800,
              "DRAFT",
              1);
        });
  }

  @Test
  void createLesson_DuplicateTitle_ThrowsException() {
    // Given
    String courseId = testCourseId.toString();
    String title = "Existing Lesson";

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(lessonRepository.findByCourseIdAndTitleAndNotDeleted(testCourseId, title))
        .thenReturn(Optional.of(testLesson));

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          lessonService.createLesson(
              courseId,
              testUserId,
              title,
              "Description",
              "Content",
              "https://example.com/video.mp4",
              1800,
              "DRAFT",
              1);
        });
  }

  @Test
  void createLesson_DuplicateOrderIndex_ThrowsException() {
    // Given
    String courseId = testCourseId.toString();
    String title = "New Lesson";
    Integer orderIndex = 1;

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(lessonRepository.findByCourseIdAndTitleAndNotDeleted(testCourseId, title))
        .thenReturn(Optional.empty());
    when(lessonRepository.existsByCourseIdAndOrderIndexAndNotDeleted(testCourseId, orderIndex))
        .thenReturn(true);

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          lessonService.createLesson(
              courseId,
              testUserId,
              title,
              "Description",
              "Content",
              "https://example.com/video.mp4",
              1800,
              "DRAFT",
              orderIndex);
        });
  }

  @Test
  void getLessonsByCourseId_Success() {
    // Given
    String courseId = testCourseId.toString();
    List<Lesson> lessons = Arrays.asList(testLesson);

    when(cacheUtil.get(anyString())).thenReturn(null);
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(lessonRepository.findByCourseIdAndNotDeletedOrderByOrderIndex(testCourseId))
        .thenReturn(lessons);

    // When
    Map<String, Object> result = lessonService.getLessonsByCourseId(courseId, testUserId);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("获取课时列表成功", result.get("message"));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> lessonList = (List<Map<String, Object>>) result.get("lessons");
    assertEquals(1, lessonList.size());

    verify(lessonRepository).findByCourseIdAndNotDeletedOrderByOrderIndex(testCourseId);
    verify(cacheUtil).put(anyString(), any(), anyInt());
  }

  @Test
  void getLessonsByCourseId_InvalidCourseId_ThrowsException() {
    // Given
    String invalidCourseId = "invalid-uuid";

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          lessonService.getLessonsByCourseId(invalidCourseId, testUserId);
        });
  }

  @Test
  void getLessonById_Success() {
    // Given
    String lessonId = testLessonId.toString();

    when(cacheUtil.get(anyString())).thenReturn(null);
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(lessonRepository.findById(testLessonId)).thenReturn(Optional.of(testLesson));

    // When
    Map<String, Object> result = lessonService.getLessonById(lessonId, testUserId);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("获取课时详情成功", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> lesson = (Map<String, Object>) result.get("lesson");
    assertEquals(testLessonId, lesson.get("id"));
    assertEquals("Test Lesson", lesson.get("title"));

    verify(lessonRepository).findById(testLessonId);
    verify(cacheUtil).put(anyString(), any());
  }

  @Test
  void getLessonById_InvalidLessonId_ThrowsException() {
    // Given
    String invalidLessonId = "invalid-uuid";

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          lessonService.getLessonById(invalidLessonId, testUserId);
        });
  }

  @Test
  void getLessonById_NotFound_ThrowsException() {
    // Given
    String lessonId = testLessonId.toString();

    when(cacheUtil.get(anyString())).thenReturn(null);
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(lessonRepository.findById(testLessonId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          lessonService.getLessonById(lessonId, testUserId);
        });
  }

  @Test
  void updateLesson_Success() {
    // Given
    String lessonId = testLessonId.toString();
    String newTitle = "Updated Lesson";
    String newDescription = "Updated Description";
    String newContent = "Updated Content";
    String newVideoUrl = "https://example.com/new-video.mp4";
    Integer newDuration = 2400;
    String newStatus = "PUBLISHED";
    Integer newOrderIndex = 2;

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(lessonRepository.findById(testLessonId)).thenReturn(Optional.of(testLesson));
    when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

    // When
    Map<String, Object> result =
        lessonService.updateLesson(
            lessonId,
            testUserId,
            newTitle,
            newDescription,
            newContent,
            newVideoUrl,
            newDuration,
            newStatus,
            newOrderIndex);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("课时更新成功", result.get("message"));

    verify(lessonRepository).save(any(Lesson.class));
    verify(cacheUtil, atLeastOnce()).remove(anyString());
  }

  @Test
  void updateLesson_PermissionDenied_ThrowsException() {
    // Given
    String lessonId = testLessonId.toString();
    UUID otherUserId = UUID.randomUUID();
    User otherUser = new User();
    otherUser.setId(otherUserId);
    otherUser.setRole("STUDENT");

    when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
    when(lessonRepository.findById(testLessonId)).thenReturn(Optional.of(testLesson));

    // When & Then
    assertThrows(
        PermissionDeniedException.class,
        () -> {
          lessonService.updateLesson(
              lessonId,
              otherUserId,
              "New Title",
              "New Description",
              "New Content",
              "https://example.com/video.mp4",
              1800,
              "PUBLISHED",
              1);
        });
  }

  @Test
  void batchCreateLessons_Success() {
    // Given
    String courseId = testCourseId.toString();
    List<Map<String, Object>> lessonDataList =
        Arrays.asList(
            Map.of(
                "title",
                "Lesson 1",
                "description",
                "Description 1",
                "content",
                "Content 1",
                "videoUrl",
                "https://example.com/video1.mp4",
                "duration",
                1800,
                "status",
                "DRAFT",
                "orderIndex",
                1),
            Map.of(
                "title",
                "Lesson 2",
                "description",
                "Description 2",
                "content",
                "Content 2",
                "videoUrl",
                "https://example.com/video2.mp4",
                "duration",
                2400,
                "status",
                "PUBLISHED",
                "orderIndex",
                2));

    List<Lesson> savedLessons = Arrays.asList(testLesson, testLesson);

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(lessonRepository.findByCourseIdAndTitleAndNotDeleted(eq(testCourseId), anyString()))
        .thenReturn(Optional.empty());
    when(lessonRepository.existsByCourseIdAndOrderIndexAndNotDeleted(eq(testCourseId), anyInt()))
        .thenReturn(false);
    when(lessonRepository.saveAll(anyList())).thenReturn(savedLessons);

    // When
    Map<String, Object> result =
        lessonService.batchCreateLessons(courseId, testUserId, lessonDataList);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("批量创建课时成功", result.get("message"));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> lessons = (List<Map<String, Object>>) result.get("lessons");
    assertEquals(2, lessons.size());

    verify(lessonRepository).saveAll(anyList());
  }

  @Test
  void batchCreateLessons_TooManyLessons_ThrowsException() {
    // Given
    String courseId = testCourseId.toString();
    List<Map<String, Object>> lessonDataList = new ArrayList<>();
    for (int i = 0; i < 51; i++) {
      lessonDataList.add(
          Map.of(
              "title",
              "Lesson " + i,
              "description",
              "Description " + i,
              "content",
              "Content " + i,
              "orderIndex",
              i + 1));
    }

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          lessonService.batchCreateLessons(courseId, testUserId, lessonDataList);
        });
  }

  @Test
  void batchCreateLessons_DuplicateTitleInBatch_ThrowsException() {
    // Given
    String courseId = testCourseId.toString();
    List<Map<String, Object>> lessonDataList =
        Arrays.asList(
            Map.of(
                "title",
                "Duplicate Title",
                "description",
                "Description 1",
                "content",
                "Content 1",
                "orderIndex",
                1),
            Map.of(
                "title",
                "Duplicate Title",
                "description",
                "Description 2",
                "content",
                "Content 2",
                "orderIndex",
                2));

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          lessonService.batchCreateLessons(courseId, testUserId, lessonDataList);
        });
  }
}
