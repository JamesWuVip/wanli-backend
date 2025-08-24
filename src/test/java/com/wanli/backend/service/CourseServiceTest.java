package com.wanli.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.wanli.backend.entity.Course;
import com.wanli.backend.entity.User;
import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.exception.ResourceNotFoundException;
import com.wanli.backend.repository.CourseRepository;
import com.wanli.backend.repository.UserRepository;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.ConfigUtil;
import com.wanli.backend.util.DatabaseUtil;
import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.PermissionUtil;
import com.wanli.backend.util.ServiceResponseUtil;
import com.wanli.backend.util.ServiceValidationUtil;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

  @Mock private CourseRepository courseRepository;

  @Mock private UserRepository userRepository;

  @Mock private CacheUtil cacheUtil;

  @Mock private ConfigUtil configUtil;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private CourseService courseService;

  private User testUser;
  private Course testCourse;
  private UUID testUserId;
  private UUID testCourseId;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testCourseId = UUID.randomUUID();

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
  }

  @Test
  void testCreateCourse_Success() {
    // Given
    UUID creatorId = UUID.randomUUID();
    String title = "Test Course";
    String description = "Test Description";
    String status = "DRAFT";

    User creator = new User();
    creator.setId(creatorId);
    creator.setUsername("testuser");
    creator.setRole("teacher"); // 设置角色为teacher，使其有创建课程的权限
    creator.setDeletedAt(null); // 确保用户未被删除

    Course savedCourse = new Course();
    savedCourse.setId(UUID.randomUUID());
    savedCourse.setCreatorId(creatorId);
    savedCourse.setTitle(title);
    savedCourse.setDescription(description);
    savedCourse.setStatus(status);
    savedCourse.setCreatedAt(LocalDateTime.now());
    savedCourse.setUpdatedAt(LocalDateTime.now());

    // Mock DatabaseUtil.findByIdSafely for user
    try (MockedStatic<DatabaseUtil> mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class);
        MockedStatic<LogUtil> mockedLogUtil = mockStatic(LogUtil.class);
        MockedStatic<ServiceResponseUtil> mockedServiceResponseUtil =
            mockStatic(ServiceResponseUtil.class)) {

      // Mock DatabaseUtil.findByIdSafely to return user, then filter will be applied
      mockedDatabaseUtil
          .when(
              () ->
                  DatabaseUtil.findByIdSafely(
                      userRepository, creatorId, "User", creatorId.toString()))
          .thenReturn(Optional.of(creator));

      // Since validateCreatorAndPermission calls filter(user -> !user.isDeleted())
      // and our creator has deletedAt = null, so !user.isDeleted() = true
      // The filter should pass through our creator object
      mockedPermissionUtil.when(() -> PermissionUtil.canCreateCourse(creator)).thenReturn(true);

      // Mock DatabaseUtil.findByIdSafely for user lookup
      mockedDatabaseUtil
          .when(
              () ->
                  DatabaseUtil.findByIdSafely(
                      eq(userRepository), eq(creatorId), eq("User"), eq(creatorId.toString())))
          .thenReturn(Optional.of(creator));

      // Mock DatabaseUtil.saveSafely for course creation
      mockedDatabaseUtil
          .when(
              () ->
                  DatabaseUtil.saveSafely(
                      eq(courseRepository), any(Course.class), eq("Course"), isNull()))
          .thenReturn(savedCourse);

      // Mock cacheUtil methods
      doNothing().when(cacheUtil).remove(anyString());
      doNothing().when(cacheUtil).removeByPattern(anyString());
      doNothing().when(cacheUtil).put(anyString(), any(), anyInt());

      // Mock LogUtil methods
      mockedLogUtil
          .when(() -> LogUtil.logBusinessOperation(anyString(), anyString(), anyString()))
          .thenAnswer(invocation -> null);
      mockedLogUtil
          .when(
              () ->
                  LogUtil.logError(
                      anyString(), anyString(), anyString(), anyString(), any(Exception.class)))
          .thenAnswer(invocation -> null);

      // Mock ServiceValidationUtil methods
      try (MockedStatic<ServiceValidationUtil> mockedValidationUtil =
          mockStatic(ServiceValidationUtil.class)) {
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateNotNull(eq(creatorId), eq("创建者ID不能为空")))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateNotNull(any(), anyString()))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateNotBlank(eq(title), eq("课程标题不能为空")))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateNotBlank(eq(description), eq("课程描述不能为空")))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateNotBlank(eq(status), eq("课程状态不能为空")))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateNotBlank(anyString(), anyString()))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateUUIDNotNull(any(UUID.class), anyString()))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateCourseTitle(anyString()))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateCourseDescription(anyString()))
            .thenAnswer(invocation -> null);
        mockedValidationUtil
            .when(() -> ServiceValidationUtil.validateCourseStatus(anyString()))
            .thenAnswer(invocation -> null);

        // Mock ServiceResponseUtil methods
        Map<String, Object> mockResponse =
            Map.of(
                "success",
                true,
                "message",
                "课程创建成功",
                "course",
                Map.of(
                    "id", savedCourse.getId().toString(),
                    "title", title,
                    "description", description,
                    "status", status));
        mockedServiceResponseUtil
            .when(() -> ServiceResponseUtil.success(anyString(), any(Map.class)))
            .thenReturn(mockResponse);

        // When
        Map<String, Object> result;
        try {
          System.out.println("=== Starting createCourse test ===");
          System.out.println("Creator: " + creator);
          System.out.println("Creator.isDeleted(): " + creator.isDeleted());
          System.out.println("Creator.getDeletedAt(): " + creator.getDeletedAt());
          System.out.println("Creator.getRole(): " + creator.getRole());

          // Test the mocked methods directly
          System.out.println("=== Testing mocked methods ===");
          Optional<User> findResult =
              DatabaseUtil.findByIdSafely(userRepository, creatorId, "User", creatorId.toString());
          System.out.println("DatabaseUtil.findByIdSafely result: " + findResult);
          if (findResult.isPresent()) {
            User foundUser = findResult.get();
            System.out.println("Found user: " + foundUser);
            System.out.println("Found user.isDeleted(): " + foundUser.isDeleted());
            boolean canCreate = PermissionUtil.canCreateCourse(foundUser);
            System.out.println("PermissionUtil.canCreateCourse result: " + canCreate);
          }

          System.out.println("=== About to call createCourse ===");
          System.out.println(
              "Parameters: creatorId="
                  + creatorId
                  + ", title="
                  + title
                  + ", description="
                  + description
                  + ", status="
                  + status);

          result = courseService.createCourse(creatorId, title, description, status);
          System.out.println("Test passed successfully: " + result);
        } catch (Exception e) {
          System.out.println("=== Test failed with exception ===");
          System.out.println("Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());

          // Print the cause chain
          Throwable cause = e.getCause();
          while (cause != null) {
            System.out.println(
                "Caused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
            cause = cause.getCause();
          }

          e.printStackTrace();
          throw e;
        }

        // Then
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals("课程创建成功", result.get("message"));

        @SuppressWarnings("unchecked")
        Map<String, Object> course = (Map<String, Object>) result.get("course");
        assertNotNull(course);
        assertEquals(savedCourse.getId().toString(), course.get("id"));
        assertEquals(title, course.get("title"));
        assertEquals(description, course.get("description"));
        assertEquals(status, course.get("status"));

        // Verify DatabaseUtil.saveSafely was called
        mockedDatabaseUtil.verify(
            () ->
                DatabaseUtil.saveSafely(
                    eq(courseRepository), any(Course.class), eq("Course"), isNull()));
      }
    }
  }

  @Test
  void testCreateCourse_InvalidInput() {
    // When & Then - Test null title
    BusinessException exception1 =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.createCourse(testUserId, null, "description", "active");
            });
    assertTrue(exception1.getMessage().contains("标题不能为空"));

    // When & Then - Test empty title
    BusinessException exception2 =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.createCourse(testUserId, "", "description", "active");
            });
    assertTrue(exception2.getMessage().contains("标题不能为空"));

    // When & Then - Test null creator ID
    BusinessException exception3 =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.createCourse(null, "title", "description", "active");
            });
    assertTrue(exception3.getMessage().contains("创建者ID不能为空"));
  }

  @Test
  void testCreateCourse_UserNotFound() {
    // Given
    UUID creatorId = UUID.randomUUID();
    String title = "Test Course";
    String description = "Test Description";
    String status = "DRAFT";

    // Mock DatabaseUtil.findByIdSafely to return empty
    try (MockedStatic<DatabaseUtil> mockedDatabaseUtil = mockStatic(DatabaseUtil.class)) {
      mockedDatabaseUtil
          .when(
              () ->
                  DatabaseUtil.findByIdSafely(
                      userRepository, creatorId, "User", creatorId.toString()))
          .thenReturn(Optional.empty());

      // When & Then
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> courseService.createCourse(creatorId, title, description, status));

      assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
      // Verify DatabaseUtil.saveSafely was never called
      mockedDatabaseUtil.verify(
          () -> DatabaseUtil.saveSafely(any(), any(Course.class), any(), any()), never());
    }
  }

  @Test
  void testGetAllCourses_Success() {
    // Given
    List<Course> courses = Arrays.asList(testCourse);
    Page<Course> coursePage = new PageImpl<>(courses);

    when(cacheUtil.get("course:list:all", Map.class)).thenReturn(null);
    when(courseRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(coursePage);

    // When
    Map<String, Object> result = courseService.getAllCourses();

    // Then
    assertNotNull(result);
    assertEquals(true, result.get("success"));
    assertEquals("获取课程列表成功", result.get("message"));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> courseList = (List<Map<String, Object>>) result.get("courses");
    assertEquals(1, courseList.size());
    assertEquals(1, result.get("total"));

    verify(courseRepository).findAllNotDeleted(any(Pageable.class));
    verify(cacheUtil).put(eq("course:list:all"), any(Map.class), eq(15));
  }

  @Test
  void testGetAllCourses_FromCache() {
    // Given
    Map<String, Object> cachedResult =
        Map.of("success", true, "message", "获取课程列表成功", "courses", Arrays.asList(), "total", 0);
    when(cacheUtil.get("course:list:all", Map.class)).thenReturn(cachedResult);

    // When
    Map<String, Object> result = courseService.getAllCourses();

    // Then
    assertEquals(cachedResult, result);
    verify(cacheUtil).get("course:list:all", Map.class);
    verify(courseRepository, never()).findAllNotDeleted(any(Pageable.class));
  }

  @Test
  void testGetAllCourses_Paginated_Success() {
    // Given
    int page = 0;
    int size = 10;
    String sortBy = "title";
    String sortDirection = "asc";

    List<Course> courses = Arrays.asList(testCourse);
    Page<Course> coursePage = new PageImpl<>(courses);

    String cacheKey = "course:list:page:0:10:title:asc";
    when(cacheUtil.get(cacheKey, Map.class)).thenReturn(null);
    when(courseRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(coursePage);

    // When
    Map<String, Object> result = courseService.getAllCourses(page, size, sortBy, sortDirection);

    // Then
    assertNotNull(result);
    assertEquals(true, result.get("success"));
    assertEquals("获取课程列表成功", result.get("message"));

    assertEquals(1, result.get("totalElements"));
    assertEquals(1, result.get("totalPages"));
    assertEquals(0, result.get("currentPage"));
    assertEquals(10, result.get("pageSize"));
    assertEquals(false, result.get("hasNext"));
    assertEquals(false, result.get("hasPrevious"));

    verify(courseRepository).findAllNotDeleted(any(Pageable.class));
    verify(cacheUtil).put(eq(cacheKey), any(Map.class), eq(10));
  }

  @Test
  void testGetAllCourses_Paginated_InvalidParams() {
    // When & Then - Test negative page
    BusinessException exception1 =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.getAllCourses(-1, 10, "title", "asc");
            });
    System.out.println("DEBUG: exception1.getMessage() = " + exception1.getMessage());
    assertTrue(exception1.getMessage().contains("页码不能小于0"));

    // When & Then - Test invalid size
    BusinessException exception2 =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.getAllCourses(0, 0, "title", "asc");
            });
    assertTrue(exception2.getMessage().contains("每页大小必须大于0"));

    // When & Then - Test size too large
    BusinessException exception3 =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.getAllCourses(0, 101, "title", "asc");
            });
    assertTrue(exception3.getMessage().contains("每页大小不能超过100"));
  }

  @Test
  void testGetCourseById_Success() {
    // Given
    String cacheKey = "course:detail:" + testCourseId.toString();
    when(cacheUtil.get(cacheKey, Course.class)).thenReturn(null);
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

    // When
    Map<String, Object> result = courseService.getCourseById(testCourseId);

    // Then
    assertNotNull(result);
    assertEquals(true, result.get("success"));
    assertEquals("获取课程详情成功", result.get("message"));

    assertNotNull(result.get("course"));

    verify(courseRepository).findById(testCourseId);
    verify(cacheUtil).put(eq(cacheKey), eq(testCourse), eq(30));
  }

  @Test
  void testGetCourseById_FromCache() {
    // Given
    String cacheKey = "course:detail:" + testCourseId.toString();
    when(cacheUtil.get(cacheKey, Course.class)).thenReturn(testCourse);

    // When
    Map<String, Object> result = courseService.getCourseById(testCourseId);

    // Then
    assertNotNull(result);
    assertEquals(true, result.get("success"));
    verify(cacheUtil).get(cacheKey, Course.class);
    verify(courseRepository, never()).findById(testCourseId);
  }

  @Test
  void testGetCourseById_NotFound() {
    // Given
    String cacheKey = "course:detail:" + testCourseId.toString();
    when(cacheUtil.get(cacheKey, Course.class)).thenReturn(null);
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.empty());

    // When & Then
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> {
              courseService.getCourseById(testCourseId);
            });

    assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
    verify(courseRepository).findById(testCourseId);
  }

  @Test
  void testGetCourseById_NullId() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.getCourseById(null);
            });

    assertTrue(exception.getMessage().contains("课程ID不能为空"));
    verify(courseRepository, never()).findById(any());
  }

  @Test
  void testBatchCreateCourses_Success() {
    // Given
    UUID creatorId = UUID.randomUUID();
    List<Map<String, String>> courseDataList =
        Arrays.asList(
            Map.of("title", "Course 1", "description", "Description 1", "status", "DRAFT"),
            Map.of("title", "Course 2", "description", "Description 2", "status", "DRAFT"));

    User creator = new User();
    creator.setId(creatorId);
    creator.setUsername("testuser");
    creator.setDeletedAt(null);

    List<Course> savedCourses = Arrays.asList(testCourse, testCourse);

    // Mock DatabaseUtil and PermissionUtil
    try (MockedStatic<DatabaseUtil> mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        MockedStatic<PermissionUtil> mockedPermissionUtil = mockStatic(PermissionUtil.class)) {

      mockedDatabaseUtil
          .when(
              () ->
                  DatabaseUtil.findByIdSafely(
                      userRepository, creatorId, "User", creatorId.toString()))
          .thenReturn(Optional.of(creator));
      mockedPermissionUtil.when(() -> PermissionUtil.canCreateCourse(creator)).thenReturn(true);

      when(courseRepository.saveAll(anyList())).thenReturn(savedCourses);

      // When
      Map<String, Object> result = courseService.batchCreateCourses(creatorId, courseDataList);

      // Then
      assertNotNull(result);
      assertEquals(true, result.get("success"));
      assertEquals("批量创建课程成功", result.get("message"));

      assertEquals(2, result.get("total"));
      assertNotNull(result.get("courses"));

      verify(courseRepository).saveAll(anyList());
    }
  }

  @Test
  void testBatchCreateCourses_EmptyList() {
    // Given
    List<Map<String, String>> courseDataList = Arrays.asList();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.batchCreateCourses(testUserId, courseDataList);
            });

    assertEquals("EMPTY_COURSE_LIST", exception.getErrorCode());
    assertEquals("课程数据列表不能为空", exception.getMessage());
    verify(courseRepository, never()).saveAll(anyList());
  }

  @Test
  void testBatchCreateCourses_TooManyCourses() {
    // Given
    List<Map<String, String>> courseDataList = new ArrayList<>();
    for (int i = 0; i < 51; i++) {
      courseDataList.add(
          Map.of("title", "Course " + i, "description", "Description", "status", "active"));
    }

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.batchCreateCourses(testUserId, courseDataList);
            });

    assertEquals("TOO_MANY_COURSES", exception.getErrorCode());
    assertEquals("单次最多只能创建50门课程", exception.getMessage());
    verify(courseRepository, never()).saveAll(anyList());
  }

  @Test
  void testBatchCreateCourses_NullInputs() {
    // When & Then - Test null creator ID
    BusinessException exception1 =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.batchCreateCourses(null, Arrays.asList());
            });
    assertTrue(exception1.getMessage().contains("创建者ID不能为空"));

    // When & Then - Test null course data list
    BusinessException exception2 =
        assertThrows(
            BusinessException.class,
            () -> {
              courseService.batchCreateCourses(testUserId, null);
            });
    assertTrue(exception2.getMessage().contains("课程数据列表不能为空"));
  }
}
