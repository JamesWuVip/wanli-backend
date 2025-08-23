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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.wanli.backend.entity.Course;
import com.wanli.backend.entity.User;
import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.exception.PermissionDeniedException;
import com.wanli.backend.exception.ResourceNotFoundException;
import com.wanli.backend.repository.CourseRepository;
import com.wanli.backend.repository.UserRepository;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.ConfigUtil;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

  @Mock private CourseRepository courseRepository;

  @Mock private UserRepository userRepository;

  @Mock private CacheUtil cacheUtil;

  @Mock private ConfigUtil configUtil;

  @InjectMocks private CourseService courseService;

  private UUID testUserId;
  private UUID testCourseId;
  private User testUser;
  private Course testCourse;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testCourseId = UUID.randomUUID();

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
  }

  @Test
  void createCourse_Success() {
    // Given
    String title = "New Course";
    String description = "Course Description";
    String status = "DRAFT";

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

    // When
    Map<String, Object> result = courseService.createCourse(testUserId, title, description, status);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("课程创建成功", result.get("message"));

    verify(courseRepository).save(any(Course.class));
    verify(cacheUtil, atLeastOnce()).remove(anyString());
  }

  @Test
  void createCourse_InvalidTitle_ThrowsException() {
    // Given
    String title = "";
    String description = "Course Description";
    String status = "DRAFT";

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          courseService.createCourse(testUserId, title, description, status);
        });
  }

  @Test
  void createCourse_UserNotFound_ThrowsException() {
    // Given
    String title = "New Course";
    String description = "Course Description";
    String status = "DRAFT";

    when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          courseService.createCourse(testUserId, title, description, status);
        });
  }

  @Test
  void getAllCourses_Success() {
    // Given
    List<Course> courses = Arrays.asList(testCourse);
    when(cacheUtil.get(anyString(), eq(Map.class))).thenReturn(null);
    when(courseRepository.findAllNotDeleted()).thenReturn(courses);

    // When
    Map<String, Object> result = courseService.getAllCourses();

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("获取课程列表成功", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> courseList = (List<Map<String, Object>>) data.get("courses");
    assertEquals(1, courseList.size());

    verify(courseRepository).findAllNotDeleted();
    verify(cacheUtil).put(anyString(), any(), anyInt());
  }

  @Test
  void getAllCourses_Paginated_Success() {
    // Given
    int page = 0;
    int size = 10;
    String sortBy = "createdAt";
    String sortDirection = "DESC";

    List<Course> courses = Arrays.asList(testCourse);
    Page<Course> coursePage = new PageImpl<>(courses);

    when(cacheUtil.get(anyString(), eq(Map.class))).thenReturn(null);
    when(courseRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(coursePage);

    // When
    Map<String, Object> result = courseService.getAllCourses(page, size, sortBy, sortDirection);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("获取课程列表成功", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals(1L, data.get("totalElements"));
    assertEquals(0, data.get("currentPage"));

    verify(courseRepository).findAllNotDeleted(any(Pageable.class));
    verify(cacheUtil).put(anyString(), any(), anyInt());
  }

  @Test
  void getCourseById_Success() {
    // Given
    when(cacheUtil.get(anyString(), eq(Course.class))).thenReturn(null);
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

    // When
    Map<String, Object> result = courseService.getCourseById(testCourseId);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("获取课程详情成功", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    @SuppressWarnings("unchecked")
    Map<String, Object> course = (Map<String, Object>) data.get("course");
    assertEquals(testCourseId, course.get("id"));
    assertEquals("Test Course", course.get("title"));

    verify(courseRepository).findById(testCourseId);
    verify(cacheUtil).put(anyString(), any(Course.class), anyInt());
  }

  @Test
  void getCourseById_NotFound_ThrowsException() {
    // Given
    when(cacheUtil.get(anyString(), eq(Course.class))).thenReturn(null);
    when(courseRepository.findById(testCourseId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          courseService.getCourseById(testCourseId);
        });
  }

  @Test
  void updateCourse_Success() {
    // Given
    String newTitle = "Updated Course";
    String newDescription = "Updated Description";
    String newStatus = "PUBLISHED";

    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

    // When
    Map<String, Object> result =
        courseService.updateCourse(testCourseId, testUserId, newTitle, newDescription, newStatus);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("课程更新成功", result.get("message"));

    verify(courseRepository).save(any(Course.class));
    verify(cacheUtil).remove(anyString());
  }

  @Test
  void updateCourse_PermissionDenied_ThrowsException() {
    // Given
    UUID otherUserId = UUID.randomUUID();
    User otherUser = new User();
    otherUser.setId(otherUserId);
    otherUser.setRole("STUDENT");

    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

    // When & Then
    assertThrows(
        PermissionDeniedException.class,
        () -> {
          courseService.updateCourse(
              testCourseId, otherUserId, "New Title", "New Description", "PUBLISHED");
        });
  }

  @Test
  void batchCreateCourses_Success() {
    // Given
    List<Map<String, String>> courseDataList =
        Arrays.asList(
            Map.of("title", "Course 1", "description", "Description 1", "status", "DRAFT"),
            Map.of("title", "Course 2", "description", "Description 2", "status", "PUBLISHED"));

    List<Course> savedCourses = Arrays.asList(testCourse, testCourse);

    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.saveAll(anyList())).thenReturn(savedCourses);

    // When
    Map<String, Object> result = courseService.batchCreateCourses(testUserId, courseDataList);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("批量创建课程成功", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals(2, data.get("total"));

    verify(courseRepository).saveAll(anyList());
  }

  @Test
  void batchCreateCourses_TooManyCourses_ThrowsException() {
    // Given
    List<Map<String, String>> courseDataList = new ArrayList<>();
    for (int i = 0; i < 51; i++) {
      courseDataList.add(
          Map.of("title", "Course " + i, "description", "Description " + i, "status", "DRAFT"));
    }

    // When & Then
    assertThrows(
        BusinessException.class,
        () -> {
          courseService.batchCreateCourses(testUserId, courseDataList);
        });
  }

  @Test
  void batchUpdateCourses_Success() {
    // Given
    List<Map<String, Object>> updateDataList =
        Arrays.asList(
            Map.of(
                "courseId",
                testCourseId.toString(),
                "title",
                "Updated Course",
                "description",
                "Updated Description",
                "status",
                "PUBLISHED"));

    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.saveAll(anyList())).thenReturn(Arrays.asList(testCourse));

    // When
    Map<String, Object> result = courseService.batchUpdateCourses(testUserId, updateDataList);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("批量更新课程成功", result.get("message"));

    verify(courseRepository).saveAll(anyList());
  }

  @Test
  void batchDeleteCourses_Success() {
    // Given
    List<UUID> courseIds = Arrays.asList(testCourseId);

    when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(courseRepository.saveAll(anyList())).thenReturn(Arrays.asList(testCourse));

    // When
    Map<String, Object> result = courseService.batchDeleteCourses(testUserId, courseIds);

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
    assertEquals("批量删除课程成功", result.get("message"));

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    assertEquals(1, data.get("deletedCount"));

    verify(courseRepository).saveAll(anyList());
  }
}
