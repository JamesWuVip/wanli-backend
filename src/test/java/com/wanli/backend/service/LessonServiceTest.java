package com.wanli.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
import com.wanli.backend.util.DatabaseUtil;
import com.wanli.backend.util.PermissionUtil;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

  @Mock private LessonRepository lessonRepository;

  @Mock private CourseRepository courseRepository;

  @Mock private UserRepository userRepository;

  @Mock private CacheUtil cacheUtil;

  @Mock private ConfigUtil configUtil;

  @InjectMocks private LessonService lessonService;

  private User testUser;
  private Course testCourse;
  private Lesson testLesson;
  private UUID testUserId;
  private UUID testCourseId;
  private UUID testLessonId;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testCourseId = UUID.randomUUID();
    testLessonId = UUID.randomUUID();

    testUser = new User();
    testUser.setId(testUserId);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setRole("teacher");

    testCourse = new Course();
    testCourse.setId(testCourseId);
    testCourse.setCreatorId(testUserId);
    testCourse.setTitle("Test Course");
    testCourse.setDescription("Test Description");
    testCourse.setStatus("active");
    testCourse.setCreatedAt(LocalDateTime.now());
    testCourse.setUpdatedAt(LocalDateTime.now());

    testLesson = new Lesson();
    testLesson.setId(testLessonId);
    testLesson.setCourse(testCourse);
    testLesson.setCreator(testUser);
    testLesson.setTitle("Test Lesson");
    testLesson.setDescription("Test Lesson Description");
    testLesson.setContent("Test Content");
    testLesson.setVideoUrl("https://example.com/video.mp4");
    testLesson.setDuration(3600);
    testLesson.setStatus(LessonStatus.PUBLISHED);
    testLesson.setOrderIndex(1);
    testLesson.setCreatedAt(LocalDateTime.now());
    testLesson.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void testCreateLesson_Success() {
    // Given
    String courseId = testCourseId.toString();
    String title = "New Lesson";
    String description = "Lesson Description";
    String content = "Lesson Content";
    String videoUrl = "https://example.com/video.mp4";
    Integer duration = 3600;
    String status = "PUBLISHED";
    Integer orderIndex = 1;

    // Mock DatabaseUtil and PermissionUtil
    try (MockedStatic<DatabaseUtil> mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class)) {

      // Mock findUserById - uses findByIdAndNotDeleted
      when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));

      // Mock findCourseById - uses findByIdAndNotDeleted
      when(courseRepository.findByIdAndNotDeleted(testCourseId))
          .thenReturn(Optional.of(testCourse));

      // Mock permission check
      mockedPermissionUtil
          .when(() -> PermissionUtil.canCreateLesson(testUser, testCourse.getCreatorId()))
          .thenReturn(true);

      // Mock lesson uniqueness checks
      when(lessonRepository.findByCourseIdAndTitleAndNotDeleted(testCourseId, title))
          .thenReturn(Optional.empty());
      when(lessonRepository.existsByCourseIdAndOrderIndexAndNotDeleted(testCourseId, orderIndex))
          .thenReturn(false);

      // Mock DatabaseUtil.saveSafely
      mockedDatabaseUtil
          .when(
              () ->
                  DatabaseUtil.saveSafely(
                      eq(lessonRepository),
                      any(Lesson.class),
                      eq("课时创建"),
                      eq(testUserId.toString())))
          .thenReturn(testLesson);

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
      assertEquals(true, result.get("success"));
      assertEquals("课时创建成功", result.get("message"));

      assertNotNull(result.get("lesson"));

      verify(userRepository).findByIdAndNotDeleted(testUserId);
      verify(courseRepository).findByIdAndNotDeleted(testCourseId);
    }
  }

  @Test
  void testCreateLesson_InvalidInput() {
    // When & Then - Test null course ID
    BusinessException exception1 =
        assertThrows(
            BusinessException.class,
            () -> {
              lessonService.createLesson(
                  null, testUserId, "title", "desc", "content", "url", 3600, "PUBLISHED", 1);
            });
    assertTrue(exception1.getMessage().contains("课程ID不能为空"));

    // When & Then - Test empty title
    BusinessException exception2 =
        assertThrows(
            BusinessException.class,
            () -> {
              lessonService.createLesson(
                  testCourseId.toString(),
                  testUserId,
                  "",
                  "desc",
                  "content",
                  "url",
                  3600,
                  "PUBLISHED",
                  1);
            });
    assertTrue(exception2.getMessage().contains("课时标题不能为空"));

    // When & Then - Test null order index
    BusinessException exception3 =
        assertThrows(
            BusinessException.class,
            () -> {
              lessonService.createLesson(
                  testCourseId.toString(),
                  testUserId,
                  "title",
                  "desc",
                  "content",
                  "url",
                  3600,
                  "PUBLISHED",
                  null);
            });
    assertTrue(exception3.getMessage().contains("排序索引不能为空"));
  }

  @Test
  void testCreateLesson_UserNotFound() {
    // Given
    when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.empty());

    // When & Then
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> {
              lessonService.createLesson(
                  testCourseId.toString(),
                  testUserId,
                  "title",
                  "desc",
                  "content",
                  "url",
                  3600,
                  "PUBLISHED",
                  1);
            });

    assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
    verify(userRepository).findByIdAndNotDeleted(testUserId);
  }

  @Test
  void testCreateLesson_CourseNotFound() {
    // Given
    when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.findByIdAndNotDeleted(testCourseId)).thenReturn(Optional.empty());

    // When & Then
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> {
              lessonService.createLesson(
                  testCourseId.toString(),
                  testUserId,
                  "title",
                  "desc",
                  "content",
                  "url",
                  3600,
                  "PUBLISHED",
                  1);
            });

    assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
    verify(userRepository).findByIdAndNotDeleted(testUserId);
    verify(courseRepository).findByIdAndNotDeleted(testCourseId);
  }

  @Test
  void testCreateLesson_PermissionDenied() {
    // Given
    UUID anotherUserId = UUID.randomUUID();
    testCourse.setCreatorId(anotherUserId); // Different creator

    // Mock DatabaseUtil and PermissionUtil
    try (MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class)) {

      when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));
      when(courseRepository.findByIdAndNotDeleted(testCourseId))
          .thenReturn(Optional.of(testCourse));

      // Mock permission check to return false
      mockedPermissionUtil
          .when(() -> PermissionUtil.canCreateLesson(testUser, testCourse.getCreatorId()))
          .thenReturn(false);

      // When & Then
      PermissionDeniedException exception =
          assertThrows(
              PermissionDeniedException.class,
              () -> {
                lessonService.createLesson(
                    testCourseId.toString(),
                    testUserId,
                    "title",
                    "desc",
                    "content",
                    "url",
                    3600,
                    "PUBLISHED",
                    1);
              });

      assertTrue(exception.getMessage().contains("无权限创建课时"));
      verify(userRepository).findByIdAndNotDeleted(testUserId);
      verify(courseRepository).findByIdAndNotDeleted(testCourseId);
    }
  }

  @Test
  void testGetLessonsByCourseId_Paginated_Success() {
    // Given
    String courseId = testCourseId.toString();
    Integer page = 0;
    Integer size = 10;
    String sortBy = "orderIndex";
    String sortDir = "asc";

    List<Lesson> lessons = Arrays.asList(testLesson);
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "orderIndex"));
    Page<Lesson> lessonPage = new PageImpl<>(lessons, pageable, lessons.size());

    // Mock DatabaseUtil and PermissionUtil
    try (MockedStatic<DatabaseUtil> mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class)) {

      String cacheKey = "lesson:list:page:" + testCourseId + ":0:10:orderIndex:asc";
      when(cacheUtil.get(cacheKey)).thenReturn(null);
      when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));
      when(courseRepository.findByIdAndNotDeleted(testCourseId))
          .thenReturn(Optional.of(testCourse));

      // Mock permission check
      mockedPermissionUtil
          .when(() -> PermissionUtil.canViewLessons(testUser, testCourse.getCreatorId()))
          .thenReturn(true);

      // Mock DatabaseUtil.findByIdSafely for paginated query
      mockedDatabaseUtil
          .when(() -> DatabaseUtil.findByIdSafely(any(Supplier.class), eq("课时分页查询")))
          .thenReturn(lessonPage);

      // When
      Map<String, Object> result =
          lessonService.getLessonsByCourseId(courseId, testUserId, page, size, sortBy, sortDir);

      // Then
      assertNotNull(result);
      assertEquals(true, result.get("success"));
      assertEquals("获取课时列表成功", result.get("message"));

      // 验证分页信息 - 从data中获取
      Map<String, Object> data = (Map<String, Object>) result.get("data");
      assertEquals(1L, data.get("totalElements")); // totalElements是Long类型
      assertEquals(1, data.get("totalPages"));
      assertEquals(0, data.get("currentPage"));
      assertEquals(10, data.get("pageSize"));
      assertEquals(false, data.get("hasNext"));
      assertEquals(false, data.get("hasPrevious"));

      // Verify DatabaseUtil was called instead of direct repository call
      mockedDatabaseUtil.verify(
          () -> DatabaseUtil.findByIdSafely(any(Supplier.class), eq("课时分页查询")));
      verify(cacheUtil).put(eq(cacheKey), any(Map.class), eq(10));
    }
  }

  @Test
  void testGetLessonsByCourseId_Paginated_FromCache() {
    // Given
    String courseId = testCourseId.toString();
    Integer page = 0;
    Integer size = 10;
    String sortBy = "orderIndex";
    String sortDir = "asc";

    String cacheKey = "lesson:list:page:" + testCourseId + ":0:10:orderIndex:asc";
    Map<String, Object> cachedResult =
        Map.of(
            "lessons",
            Arrays.asList(),
            "totalElements",
            0L,
            "totalPages",
            0,
            "currentPage",
            0,
            "pageSize",
            10,
            "hasNext",
            false,
            "hasPrevious",
            false);
    when(cacheUtil.get(cacheKey)).thenReturn(cachedResult);

    // When
    Map<String, Object> result =
        lessonService.getLessonsByCourseId(courseId, testUserId, page, size, sortBy, sortDir);

    // Then
    assertEquals(true, result.get("success"));
    verify(cacheUtil).get(cacheKey);
    verify(lessonRepository, never()).findByCourseIdAndNotDeleted(any(), any(Pageable.class));
  }

  @Test
  void testGetLessonsByCourseId_Paginated_InvalidParams() {
    // When & Then - Test negative page
    BusinessException exception1 =
        assertThrows(
            BusinessException.class,
            () -> {
              lessonService.getLessonsByCourseId(
                  testCourseId.toString(), testUserId, -1, 10, "orderIndex", "asc");
            });
    assertTrue(exception1.getMessage().contains("页码不能小于0"));

    // When & Then - Test invalid size
    BusinessException exception2 =
        assertThrows(
            BusinessException.class,
            () -> {
              lessonService.getLessonsByCourseId(
                  testCourseId.toString(), testUserId, 0, 0, "orderIndex", "asc");
            });
    assertTrue(exception2.getMessage().contains("每页大小必须在1-100之间"));

    // When & Then - Test size too large
    BusinessException exception3 =
        assertThrows(
            BusinessException.class,
            () -> {
              lessonService.getLessonsByCourseId(
                  testCourseId.toString(), testUserId, 0, 101, "orderIndex", "asc");
            });
    assertTrue(exception3.getMessage().contains("每页大小必须在1-100之间"));
  }

  @Test
  void testGetLessonsByCourseId_NonPaginated_Success() {
    // Given
    String courseId = testCourseId.toString();
    List<Lesson> lessons = Arrays.asList(testLesson);

    // Mock DatabaseUtil and PermissionUtil
    try (MockedStatic<DatabaseUtil> mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class)) {

      String cacheKey = "lesson:course:" + testCourseId;
      when(cacheUtil.get(cacheKey)).thenReturn(null);
      when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));
      when(courseRepository.findByIdAndNotDeleted(testCourseId))
          .thenReturn(Optional.of(testCourse));

      // Mock permission check
      mockedPermissionUtil
          .when(() -> PermissionUtil.canViewLessons(testUser, testCourse.getCreatorId()))
          .thenReturn(true);

      // Mock DatabaseUtil.findByIdSafely for non-paginated query
      mockedDatabaseUtil
          .when(() -> DatabaseUtil.findByIdSafely(any(Supplier.class), eq("课时列表查询")))
          .thenReturn(lessons);

      // When
      Map<String, Object> result = lessonService.getLessonsByCourseId(courseId, testUserId);

      // Then
      assertNotNull(result);
      assertEquals(true, result.get("success"));
      assertEquals("获取课时列表成功", result.get("message"));

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> lessonList = (List<Map<String, Object>>) result.get("lessons");
      assertEquals(1, lessonList.size());

      // Verify DatabaseUtil was called
      mockedDatabaseUtil.verify(
          () -> DatabaseUtil.findByIdSafely(any(Supplier.class), eq("课时列表查询")));
      verify(cacheUtil).put(eq(cacheKey), any(List.class), eq(15));
    }
  }

  @Test
  void testGetLessonsByCourseId_NonPaginated_FromCache() {
    // Given
    String courseId = testCourseId.toString();
    String cacheKey = "lesson:course:" + testCourseId;
    List<Map<String, Object>> cachedLessons = Arrays.asList();
    when(cacheUtil.get(cacheKey)).thenReturn(cachedLessons);

    // When
    Map<String, Object> result = lessonService.getLessonsByCourseId(courseId, testUserId);

    // Then
    assertEquals(true, result.get("success"));
    assertEquals("获取课时列表成功", result.get("message"));
    assertEquals(cachedLessons, result.get("lessons"));

    verify(cacheUtil).get(cacheKey);
    // No DatabaseUtil call should be made when using cache
  }

  @Test
  void testGetLessonById_Success() {
    // Given
    String lessonId = testLessonId.toString();
    String cacheKey = "lesson:detail:" + testLessonId;

    // Mock DatabaseUtil and PermissionUtil
    try (MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class)) {

      when(cacheUtil.get(cacheKey)).thenReturn(null);
      when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));
      when(lessonRepository.findByIdAndNotDeleted(testLessonId))
          .thenReturn(Optional.of(testLesson));

      // Mock permission check
      mockedPermissionUtil
          .when(() -> PermissionUtil.canViewLesson(testUser, testCourse.getCreatorId()))
          .thenReturn(true);

      // When
      Map<String, Object> result = lessonService.getLessonById(lessonId, testUserId);

      // Then
      assertNotNull(result);
      assertEquals(true, result.get("success"));
      assertEquals("获取课时详情成功", result.get("message"));

      @SuppressWarnings("unchecked")
      Map<String, Object> lesson = (Map<String, Object>) result.get("lesson");
      assertNotNull(lesson);

      verify(lessonRepository).findByIdAndNotDeleted(testLessonId);
      verify(cacheUtil).put(eq(cacheKey), any(Map.class));
    }
  }

  @Test
  void testGetLessonById_FromCache() {
    // Given
    String lessonId = testLessonId.toString();
    String cacheKey = "lesson:detail:" + testLessonId;
    Map<String, Object> cachedLesson =
        Map.of("id", testLessonId.toString(), "title", "Test Lesson");

    when(cacheUtil.get(cacheKey)).thenReturn(cachedLesson);

    // When
    Map<String, Object> result = lessonService.getLessonById(lessonId, testUserId);

    // Then
    assertEquals(true, result.get("success"));
    assertEquals("获取课时详情成功", result.get("message"));
    assertEquals(cachedLesson, result.get("lesson"));

    verify(cacheUtil).get(cacheKey);
    verify(lessonRepository, never()).findById(any());
  }

  @Test
  void testGetLessonById_NotFound() {
    // Given
    String lessonId = testLessonId.toString();
    String cacheKey = "lesson:detail:" + testLessonId;

    when(cacheUtil.get(cacheKey)).thenReturn(null);
    when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));
    when(lessonRepository.findByIdAndNotDeleted(testLessonId)).thenReturn(Optional.empty());

    // When & Then
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> {
              lessonService.getLessonById(lessonId, testUserId);
            });

    assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
    verify(lessonRepository).findByIdAndNotDeleted(testLessonId);
  }

  @Test
  void testGetLessonById_InvalidId() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              lessonService.getLessonById("invalid-uuid", testUserId);
            });

    assertTrue(exception.getMessage().contains("无效的课时ID格式"));
    verify(lessonRepository, never()).findById(any());
  }

  @Test
  void testUpdateLesson_Success() {
    // Given
    String lessonId = testLessonId.toString();
    String newTitle = "Updated Lesson";
    String newDescription = "Updated Description";
    String newContent = "Updated Content";
    String newVideoUrl = "https://example.com/new-video.mp4";
    Integer newDuration = 7200;
    String newStatus = "DRAFT";
    Integer newOrderIndex = 2;

    // Mock DatabaseUtil and PermissionUtil
    try (MockedStatic<DatabaseUtil> mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class)) {

      when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));
      when(lessonRepository.findByIdAndNotDeleted(testLessonId))
          .thenReturn(Optional.of(testLesson));

      // Mock permission check
      mockedPermissionUtil
          .when(() -> PermissionUtil.canEditLesson(testUser, testCourse.getCreatorId()))
          .thenReturn(true);

      // Mock DatabaseUtil.saveSafely
      mockedDatabaseUtil
          .when(
              () ->
                  DatabaseUtil.saveSafely(
                      eq(lessonRepository),
                      any(Lesson.class),
                      eq("Lesson"),
                      eq(testUserId.toString())))
          .thenReturn(testLesson);

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
      assertEquals(true, result.get("success"));
      assertEquals("课时更新成功", result.get("message"));

      assertNotNull(result.get("lesson"));

      verify(userRepository).findByIdAndNotDeleted(testUserId);
      verify(lessonRepository).findByIdAndNotDeleted(testLessonId);
    }
  }

  @Test
  void testUpdateLesson_PermissionDenied() {
    // Given
    UUID anotherUserId = UUID.randomUUID();
    testCourse.setCreatorId(anotherUserId); // Different creator

    // Mock DatabaseUtil and PermissionUtil
    try (MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class)) {

      when(userRepository.findByIdAndNotDeleted(testUserId)).thenReturn(Optional.of(testUser));
      when(lessonRepository.findByIdAndNotDeleted(testLessonId))
          .thenReturn(Optional.of(testLesson));

      // Mock permission check to return false
      mockedPermissionUtil
          .when(() -> PermissionUtil.canEditLesson(testUser, testCourse.getCreatorId()))
          .thenReturn(false);

      // When & Then
      PermissionDeniedException exception =
          assertThrows(
              PermissionDeniedException.class,
              () -> {
                lessonService.updateLesson(
                    testLessonId.toString(),
                    testUserId,
                    "title",
                    "desc",
                    "content",
                    "url",
                    3600,
                    "PUBLISHED",
                    1);
              });

      assertTrue(exception.getMessage().contains("无权限更新课时"));
    }
  }

  @Test
  void testUpdateLesson_InvalidId() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              lessonService.updateLesson(
                  "invalid-uuid",
                  testUserId,
                  "title",
                  "desc",
                  "content",
                  "url",
                  3600,
                  "PUBLISHED",
                  1);
            });

    assertTrue(exception.getMessage().contains("无效的课时ID格式"));
    verify(lessonRepository, never()).save(any(Lesson.class));
  }
}
