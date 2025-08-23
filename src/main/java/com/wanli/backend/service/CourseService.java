package com.wanli.backend.service;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanli.backend.entity.Course;
import com.wanli.backend.entity.User;
import com.wanli.backend.event.CourseDeletedEvent;
import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.exception.PermissionDeniedException;
import com.wanli.backend.exception.ResourceNotFoundException;
import com.wanli.backend.repository.CourseRepository;
import com.wanli.backend.repository.UserRepository;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.ConfigUtil;
import com.wanli.backend.util.DatabaseUtil;
import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.PerformanceMonitor;
import com.wanli.backend.util.PermissionUtil;
import com.wanli.backend.util.ServiceResponseUtil;
import com.wanli.backend.util.ServiceValidationUtil;

/** 课程服务类 处理课程相关的业务逻辑 应用了缓存、日志记录、配置管理等最佳实践 */
@Service
public class CourseService {

  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final CacheUtil cacheUtil;
  private final ConfigUtil configUtil;
  private final ApplicationEventPublisher eventPublisher;

  // 缓存键前缀和策略
  private static final String COURSE_CACHE_PREFIX = "course:detail:";
  private static final String COURSE_LIST_CACHE_PREFIX = "course:list:";
  private static final String COURSE_LIST_CACHE_KEY = "course:list:all";
  private static final String COURSE_LIST_PAGINATED_PREFIX = "course:list:page:";
  private static final String COURSE_USER_LIST_PREFIX = "course:user:";

  // 缓存TTL配置（分钟）
  private static final int COURSE_DETAIL_TTL = 30; // 课程详情缓存30分钟
  private static final int COURSE_LIST_TTL = 15; // 课程列表缓存15分钟
  private static final int COURSE_PAGINATED_TTL = 10; // 分页列表缓存10分钟

  public CourseService(
      CourseRepository courseRepository,
      UserRepository userRepository,
      CacheUtil cacheUtil,
      ConfigUtil configUtil,
      ApplicationEventPublisher eventPublisher) {
    this.courseRepository = courseRepository;
    this.userRepository = userRepository;
    this.cacheUtil = cacheUtil;
    this.configUtil = configUtil;
    this.eventPublisher = eventPublisher;
  }

  /**
   * 创建课程
   *
   * @param creatorId 创建者ID
   * @param title 课程标题
   * @param description 课程描述
   * @param status 课程状态
   * @return 创建结果
   */
  @Transactional
  public Map<String, Object> createCourse(
      UUID creatorId, String title, String description, String status) {

    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("COURSE_CREATE")) {

      try {
        // 输入验证
        validateCourseCreationInput(creatorId, title, description, status);

        // 验证创建者并检查权限
        User creator = validateCreatorAndPermission(creatorId);

        // 创建并保存课程
        Course course = buildCourse(creatorId, title, description, status);
        Course savedCourse = DatabaseUtil.saveSafely(courseRepository, course, "Course", null);

        // 清除课程列表缓存
        clearCourseListCache();

        // 缓存新创建的课程
        cacheCourse(savedCourse);

        // 记录日志
        LogUtil.logBusinessOperation(
            "COURSE_CREATE", savedCourse.getId().toString(), "课程创建成功: " + title);

        return ServiceResponseUtil.success(
            "课程创建成功", Map.of("course", createCourseResponse(savedCourse)));

      } catch (BusinessException e) {
        throw e;
      } catch (Exception e) {
        LogUtil.logError(
            "COURSE_CREATE",
            creatorId != null ? creatorId.toString() : null,
            "COURSE_CREATE_ERROR",
            "创建课程失败",
            e);
        throw new BusinessException("COURSE_CREATE_FAILED", "课程创建失败，请稍后重试");
      }
    }
  }

  /**
   * 获取所有课程列表（分页）
   *
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @param sortBy 排序字段
   * @param sortDirection 排序方向
   * @return 分页的课程列表
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getAllCourses(
      int page, int size, String sortBy, String sortDirection) {
    try (PerformanceMonitor.Monitor monitor =
        PerformanceMonitor.monitor("COURSE_GET_ALL_PAGINATED")) {

      try {
        // 输入验证
        validatePaginationParams(page, size);

        // 构建分页参数
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        // 构建缓存键
        String cacheKey =
            COURSE_LIST_PAGINATED_PREFIX + page + ":" + size + ":" + sortBy + ":" + sortDirection;

        // 优先从缓存获取
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedResult = cacheUtil.get(cacheKey, Map.class);
        if (cachedResult != null) {
          LogUtil.logBusinessOperation("COURSE_GET_ALL_PAGINATED", "", "从缓存获取分页课程列表");
          return cachedResult;
        }

        // 从数据库查询分页数据
        Page<Course> coursePage = courseRepository.findAllNotDeleted(Pageable.ofSize(1000));
        List<Course> courses = coursePage.getContent();

        // 手动分页处理
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), courses.size());
        List<Course> pageContent = courses.subList(start, end);

        List<Map<String, Object>> courseResponses = convertCoursesToResponses(pageContent);

        // 计算分页信息
        int totalElements = courses.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        Map<String, Object> data =
            Map.of(
                "courses", courseResponses,
                "totalElements", totalElements,
                "totalPages", totalPages,
                "currentPage", page,
                "pageSize", size,
                "hasNext", hasNext,
                "hasPrevious", hasPrevious);

        Map<String, Object> result = ServiceResponseUtil.success("获取课程列表成功", data);

        // 缓存结果
        cacheUtil.put(cacheKey, result, COURSE_PAGINATED_TTL);

        LogUtil.logBusinessOperation(
            "COURSE_GET_ALL_PAGINATED",
            "",
            String.format("获取分页课程列表成功，第%d页，共%d门课程", page + 1, totalElements));

        return result;

      } catch (Exception e) {
        LogUtil.logError(
            "COURSE_GET_ALL_PAGINATED", "", "GET_ALL_PAGINATED_ERROR", e.getMessage(), e);
        throw new BusinessException("COURSE_GET_ALL_FAILED", "获取课程列表失败，请稍后重试");
      }
    }
  }

  /**
   * 获取所有课程列表（不分页，保持向后兼容）
   *
   * @return 课程列表
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getAllCourses() {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("COURSE_GET_ALL")) {

      try {
        // 优先从缓存获取
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedResult = cacheUtil.get(COURSE_LIST_CACHE_KEY, Map.class);
        if (cachedResult != null) {
          LogUtil.logBusinessOperation("COURSE_GET_ALL", "", "从缓存获取课程列表");
          return cachedResult;
        }

        // 从数据库查询
        Page<Course> coursePage =
            DatabaseUtil.executeQuery(
                "FIND_ALL_COURSES",
                "Course",
                null,
                () -> courseRepository.findAllNotDeleted(Pageable.ofSize(1000)));
        List<Course> courses = coursePage.getContent();

        List<Map<String, Object>> courseResponses = convertCoursesToResponses(courses);

        Map<String, Object> data =
            Map.of("courses", courseResponses, "total", courseResponses.size());

        Map<String, Object> result = ServiceResponseUtil.success("获取课程列表成功", data);

        // 缓存结果
        cacheUtil.put(COURSE_LIST_CACHE_KEY, result, COURSE_LIST_TTL);

        LogUtil.logBusinessOperation("COURSE_GET_ALL", "", "获取课程列表成功，共" + courses.size() + "门课程");

        return result;

      } catch (Exception e) {
        LogUtil.logError("COURSE_GET_ALL", "", "GET_ALL_ERROR", e.getMessage(), e);
        throw new BusinessException("COURSE_GET_ALL_FAILED", "获取课程列表失败，请稍后重试");
      }
    }
  }

  /**
   * 根据ID获取课程详情
   *
   * @param courseId 课程ID
   * @return 课程详情
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getCourseById(UUID courseId) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("COURSE_GET_BY_ID")) {

      try {
        // 输入验证
        ServiceValidationUtil.validateNotNull(courseId, "课程ID不能为空");

        // 优先从缓存获取
        String cacheKey = COURSE_CACHE_PREFIX + courseId.toString();
        Course cachedCourse = cacheUtil.get(cacheKey, Course.class);
        if (cachedCourse != null && cachedCourse.getDeletedAt() == null) {
          LogUtil.logBusinessOperation("COURSE_GET_BY_ID", courseId.toString(), "从缓存获取课程详情");
          return ServiceResponseUtil.success(
              "获取课程详情成功", Map.of("course", createCourseResponse(cachedCourse)));
        }

        // 从数据库查询
        Optional<Course> courseOptional =
            DatabaseUtil.findByIdSafely(courseRepository, courseId, "Course", courseId.toString())
                .filter(course -> course.getDeletedAt() == null);

        if (!courseOptional.isPresent()) {
          throw new ResourceNotFoundException("COURSE_NOT_FOUND", "Course", courseId.toString());
        }

        Course course = courseOptional.get();

        // 缓存查询结果
        cacheCourse(course);

        LogUtil.logBusinessOperation("COURSE_GET_BY_ID", courseId.toString(), "获取课程详情成功");

        return ServiceResponseUtil.success(
            "获取课程详情成功", Map.of("course", createCourseResponse(course)));

      } catch (Exception e) {
        LogUtil.logError(
            "COURSE_GET_BY_ID",
            courseId != null ? courseId.toString() : null,
            "COURSE_QUERY_ERROR",
            "查询课程失败",
            e);
        throw new BusinessException("COURSE_GET_FAILED", "获取课程详情失败，请稍后重试");
      }
    }
  }

  /**
   * 批量创建课程
   *
   * @param creatorId 创建者ID
   * @param courseDataList 课程数据列表
   * @return 批量创建结果
   */
  @Transactional
  public Map<String, Object> batchCreateCourses(
      UUID creatorId, List<Map<String, String>> courseDataList) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("COURSE_BATCH_CREATE")) {

      try {
        // 输入验证
        ServiceValidationUtil.validateNotNull(creatorId, "创建者ID不能为空");
        ServiceValidationUtil.validateNotNull(courseDataList, "课程数据列表不能为空");

        if (courseDataList.isEmpty()) {
          throw new BusinessException("EMPTY_COURSE_LIST", "课程数据列表不能为空");
        }

        if (courseDataList.size() > 50) {
          throw new BusinessException("TOO_MANY_COURSES", "单次最多只能创建50门课程");
        }

        // 验证创建者并检查权限
        User creator = validateCreatorAndPermission(creatorId);

        List<Course> coursesToCreate = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 验证并构建课程对象
        for (int i = 0; i < courseDataList.size(); i++) {
          Map<String, String> courseData = courseDataList.get(i);
          try {
            String title = courseData.get("title");
            String description = courseData.get("description");
            String status = courseData.get("status");

            validateCourseCreationInput(creatorId, title, description, status);
            Course course = buildCourse(creatorId, title, description, status);
            coursesToCreate.add(course);
          } catch (Exception e) {
            errors.add("第" + (i + 1) + "门课程: " + e.getMessage());
          }
        }

        if (!errors.isEmpty()) {
          throw new BusinessException(
              "BATCH_VALIDATION_FAILED", "批量验证失败: " + String.join("; ", errors));
        }

        // 批量保存课程
        List<Course> savedCourses = courseRepository.saveAll(coursesToCreate);

        // 批量缓存课程
        savedCourses.forEach(this::cacheCourse);

        // 清除课程列表缓存
        clearCourseListCache();

        // 记录日志
        LogUtil.logBusinessOperation(
            "COURSE_BATCH_CREATE",
            creatorId.toString(),
            "批量创建课程成功，共" + savedCourses.size() + "门课程");

        return ServiceResponseUtil.success(
            "批量创建课程成功",
            Map.of(
                "courses", convertCoursesToResponses(savedCourses),
                "total", savedCourses.size()));

      } catch (BusinessException e) {
        throw e;
      } catch (Exception e) {
        LogUtil.logError(
            "COURSE_BATCH_CREATE",
            creatorId != null ? creatorId.toString() : null,
            "BATCH_CREATE_ERROR",
            e.getMessage(),
            e);
        throw new BusinessException("COURSE_BATCH_CREATE_FAILED", "批量创建课程失败，请稍后重试");
      }
    }
  }

  /**
   * 批量更新课程
   *
   * @param userId 操作用户ID
   * @param updateDataList 更新数据列表
   * @return 批量更新结果
   */
  @Transactional
  public Map<String, Object> batchUpdateCourses(
      UUID userId, List<Map<String, Object>> updateDataList) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("COURSE_BATCH_UPDATE")) {

      try {
        // 输入验证
        ServiceValidationUtil.validateNotNull(userId, "用户ID不能为空");
        ServiceValidationUtil.validateNotNull(updateDataList, "更新数据列表不能为空");

        if (updateDataList.isEmpty()) {
          throw new BusinessException("EMPTY_UPDATE_LIST", "更新数据列表不能为空");
        }

        if (updateDataList.size() > 50) {
          throw new BusinessException("TOO_MANY_UPDATES", "单次最多只能更新50门课程");
        }

        List<Course> coursesToUpdate = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 验证并准备更新数据
        for (int i = 0; i < updateDataList.size(); i++) {
          Map<String, Object> updateData = updateDataList.get(i);
          try {
            UUID courseId = UUID.fromString(updateData.get("courseId").toString());
            String title = (String) updateData.get("title");
            String description = (String) updateData.get("description");
            String status = (String) updateData.get("status");

            // 验证课程是否存在
            Course course = findCourseById(courseId);

            // 验证用户权限
            validateUserAndPermission(userId, course.getCreatorId(), "批量更新课程");

            // 验证更新数据
            validateUpdateData(title, description, status);

            // 更新课程字段
            updateCourseFields(course, title, description, status);
            coursesToUpdate.add(course);

          } catch (Exception e) {
            errors.add("第" + (i + 1) + "门课程: " + e.getMessage());
          }
        }

        if (!errors.isEmpty()) {
          throw new BusinessException(
              "BATCH_UPDATE_VALIDATION_FAILED", "批量更新验证失败: " + String.join("; ", errors));
        }

        // 批量保存更新
        List<Course> updatedCourses = courseRepository.saveAll(coursesToUpdate);

        // 批量清除和更新缓存
        updatedCourses.forEach(
            course -> {
              clearCourseCache(course.getId());
              cacheCourse(course);
            });
        clearCourseListCache();

        // 记录日志
        LogUtil.logBusinessOperation(
            "COURSE_BATCH_UPDATE", userId.toString(), "批量更新课程成功，共" + updatedCourses.size() + "门课程");

        return ServiceResponseUtil.success(
            "批量更新课程成功",
            Map.of(
                "courses", convertCoursesToResponses(updatedCourses),
                "total", updatedCourses.size()));

      } catch (BusinessException e) {
        throw e;
      } catch (Exception e) {
        LogUtil.logError(
            "COURSE_BATCH_UPDATE",
            userId != null ? userId.toString() : null,
            "BATCH_UPDATE_ERROR",
            e.getMessage(),
            e);
        throw new BusinessException("COURSE_BATCH_UPDATE_FAILED", "批量更新课程失败，请稍后重试");
      }
    }
  }

  /**
   * 批量删除课程（软删除）
   *
   * @param userId 操作用户ID
   * @param courseIds 课程ID列表
   * @return 批量删除结果
   */
  @Transactional
  public Map<String, Object> batchDeleteCourses(UUID userId, List<UUID> courseIds) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("COURSE_BATCH_DELETE")) {

      try {
        // 输入验证
        ServiceValidationUtil.validateNotNull(userId, "用户ID不能为空");
        ServiceValidationUtil.validateNotNull(courseIds, "课程ID列表不能为空");

        if (courseIds.isEmpty()) {
          throw new BusinessException("EMPTY_COURSE_ID_LIST", "课程ID列表不能为空");
        }

        if (courseIds.size() > 50) {
          throw new BusinessException("TOO_MANY_DELETES", "单次最多只能删除50门课程");
        }

        List<Course> coursesToDelete = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 验证并准备删除数据
        for (int i = 0; i < courseIds.size(); i++) {
          UUID courseId = courseIds.get(i);
          try {
            // 验证课程是否存在
            Course course = findCourseById(courseId);

            // 验证用户权限
            validateUserAndPermission(userId, course.getCreatorId(), "批量删除课程");

            // 标记为删除
            course.setDeletedAt(java.time.LocalDateTime.now());
            course.setUpdatedAt(java.time.LocalDateTime.now());
            coursesToDelete.add(course);

          } catch (Exception e) {
            errors.add("课程ID " + courseId + ": " + e.getMessage());
          }
        }

        if (!errors.isEmpty()) {
          throw new BusinessException(
              "BATCH_DELETE_VALIDATION_FAILED", "批量删除验证失败: " + String.join("; ", errors));
        }

        // 批量保存删除标记
        List<Course> deletedCourses = courseRepository.saveAll(coursesToDelete);

        // 批量清除缓存
        deletedCourses.forEach(course -> clearCourseCache(course.getId()));
        clearCourseListCache();

        // 记录日志
        LogUtil.logBusinessOperation(
            "COURSE_BATCH_DELETE", userId.toString(), "批量删除课程成功，共" + deletedCourses.size() + "门课程");

        return ServiceResponseUtil.success(
            "批量删除课程成功",
            Map.of(
                "deletedCount", deletedCourses.size(),
                "deletedCourseIds",
                    deletedCourses.stream()
                        .map(Course::getId)
                        .collect(java.util.stream.Collectors.toList())));

      } catch (BusinessException e) {
        throw e;
      } catch (Exception e) {
        LogUtil.logError(
            "COURSE_BATCH_DELETE",
            userId != null ? userId.toString() : null,
            "BATCH_DELETE_ERROR",
            e.getMessage(),
            e);
        throw new BusinessException("COURSE_BATCH_DELETE_FAILED", "批量删除课程失败，请稍后重试");
      }
    }
  }

  /**
   * 更新课程
   *
   * @param courseId 课程ID
   * @param userId 操作用户ID
   * @param title 课程标题
   * @param description 课程描述
   * @param status 课程状态
   * @return 更新结果
   */
  @Transactional
  public Map<String, Object> updateCourse(
      UUID courseId, UUID userId, String title, String description, String status) {

    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("COURSE_UPDATE")) {

      try {
        // 输入验证
        ServiceValidationUtil.validateNotNull(courseId, "课程ID不能为空");
        ServiceValidationUtil.validateNotNull(userId, "用户ID不能为空");

        // 验证课程是否存在
        Course course = findCourseById(courseId);

        // 验证用户并检查权限
        User user = validateUserAndPermission(userId, course.getCreatorId(), "更新课程");

        // 验证更新数据
        validateUpdateData(title, description, status);

        // 更新课程信息
        updateCourseFields(course, title, description, status);
        Course updatedCourse =
            DatabaseUtil.saveSafely(courseRepository, course, "Course", courseId.toString());

        // 清除相关缓存
        clearCourseCache(courseId);
        clearCourseListCache();

        // 缓存更新后的课程
        cacheCourse(updatedCourse);

        // 记录日志
        LogUtil.logBusinessOperation(
            "COURSE_UPDATE", courseId.toString(), "课程更新成功: " + updatedCourse.getTitle());

        return ServiceResponseUtil.success(
            "课程更新成功", Map.of("course", createCourseResponse(updatedCourse)));

      } catch (BusinessException e) {
        throw e;
      } catch (Exception e) {
        LogUtil.logError(
            "COURSE_UPDATE",
            courseId != null ? courseId.toString() : null,
            "COURSE_UPDATE_ERROR",
            "更新课程失败",
            e);
        throw new BusinessException("COURSE_UPDATE_FAILED", "课程更新失败，请稍后重试");
      }
    }
  }

  /**
   * 删除课程（软删除）
   *
   * @param courseId 课程ID
   * @param userId 操作用户ID
   * @return 删除结果
   */
  @Transactional
  public Map<String, Object> deleteCourse(UUID courseId, UUID userId) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.monitor("COURSE_DELETE")) {

      try {
        // 输入验证
        ServiceValidationUtil.validateNotNull(courseId, "课程ID不能为空");
        ServiceValidationUtil.validateNotNull(userId, "用户ID不能为空");

        // 验证课程是否存在
        Course course = findCourseById(courseId);

        // 验证用户并检查权限
        User user = validateUserAndPermission(userId, course.getCreatorId(), "删除课程");

        // 标记为删除
        course.setDeletedAt(java.time.LocalDateTime.now());
        course.setUpdatedAt(java.time.LocalDateTime.now());
        Course deletedCourse =
            DatabaseUtil.saveSafely(courseRepository, course, "Course", courseId.toString());

        // 清除相关缓存
        clearCourseCache(courseId);
        clearCourseListCache();
        clearUserCourseCache(userId);

        // 发布课程删除事件
        eventPublisher.publishEvent(new CourseDeletedEvent(courseId, course.getTitle(), userId));

        // 记录日志
        LogUtil.logBusinessOperation(
            "COURSE_DELETE", courseId.toString(), "课程删除成功: " + course.getTitle());

        return ServiceResponseUtil.success("课程删除成功", Map.of("courseId", courseId));

      } catch (BusinessException e) {
        throw e;
      } catch (Exception e) {
        LogUtil.logError(
            "COURSE_DELETE",
            courseId != null ? courseId.toString() : null,
            "COURSE_DELETE_ERROR",
            "删除课程失败",
            e);
        throw new BusinessException("COURSE_DELETE_FAILED", "课程删除失败，请稍后重试");
      }
    }
  }

  // ==================== 私有辅助方法 ====================

  /** 验证课程创建输入 */
  private void validateCourseCreationInput(
      UUID creatorId, String title, String description, String status) {
    ServiceValidationUtil.validateNotNull(creatorId, "创建者ID不能为空");
    ServiceValidationUtil.validateNotBlank(title, "课程标题不能为空");
    ServiceValidationUtil.validateNotBlank(description, "课程描述不能为空");
    ServiceValidationUtil.validateNotBlank(status, "课程状态不能为空");

    if (title.length() > 100) {
      throw new BusinessException("INVALID_TITLE_LENGTH", "课程标题长度不能超过100个字符");
    }

    if (description.length() > 500) {
      throw new BusinessException("INVALID_DESCRIPTION_LENGTH", "课程描述长度不能超过500个字符");
    }

    if (!Arrays.asList("DRAFT", "PUBLISHED", "ARCHIVED").contains(status)) {
      throw new BusinessException("INVALID_STATUS", "无效的课程状态");
    }
  }

  /** 验证创建者并检查权限 */
  private User validateCreatorAndPermission(UUID creatorId) {
    Optional<User> creatorOptional =
        DatabaseUtil.findByIdSafely(userRepository, creatorId, "User", creatorId.toString())
            .filter(user -> !user.isDeleted());

    if (!creatorOptional.isPresent()) {
      throw new ResourceNotFoundException("USER_NOT_FOUND", "User", creatorId.toString());
    }

    User creator = creatorOptional.get();

    if (!PermissionUtil.canCreateCourse(creator)) {
      throw new PermissionDeniedException("权限不足，无法创建课程");
    }

    return creator;
  }

  /** 根据ID查找课程 */
  private Course findCourseById(UUID courseId) {
    Optional<Course> courseOptional =
        DatabaseUtil.findByIdSafely(courseRepository, courseId, "Course", courseId.toString())
            .filter(course -> !course.isDeleted());

    if (!courseOptional.isPresent()) {
      throw new ResourceNotFoundException("COURSE_NOT_FOUND", "Course", courseId.toString());
    }

    return courseOptional.get();
  }

  /** 验证用户并检查权限 */
  private User validateUserAndPermission(UUID userId, UUID creatorId, String operation) {
    Optional<User> userOptional =
        DatabaseUtil.findByIdSafely(userRepository, userId, "User", userId.toString())
            .filter(user -> !user.isDeleted());

    if (!userOptional.isPresent()) {
      throw new ResourceNotFoundException("USER_NOT_FOUND", "User", userId.toString());
    }

    User user = userOptional.get();

    if (!PermissionUtil.canEditCourse(user, creatorId)) {
      throw new PermissionDeniedException("权限不足，无法" + operation);
    }

    return user;
  }

  /** 缓存课程 */
  private void cacheCourse(Course course) {
    if (course != null && course.getId() != null) {
      String cacheKey = COURSE_CACHE_PREFIX + course.getId().toString();
      cacheUtil.put(cacheKey, course, COURSE_DETAIL_TTL);
    }
  }

  /** 清除课程缓存 */
  private void clearCourseCache(UUID courseId) {
    if (courseId != null) {
      String cacheKey = COURSE_CACHE_PREFIX + courseId.toString();
      cacheUtil.remove(cacheKey);
    }
  }

  /** 清除课程列表缓存 */
  private void clearCourseListCache() {
    // 清除全量列表缓存
    cacheUtil.remove(COURSE_LIST_CACHE_KEY);

    // 清除分页列表缓存（使用模式匹配）
    cacheUtil.removeByPattern(COURSE_LIST_PAGINATED_PREFIX + "*");
  }

  /** 清除用户相关的课程缓存 */
  private void clearUserCourseCache(UUID userId) {
    if (userId != null) {
      String pattern = COURSE_USER_LIST_PREFIX + userId + ":*";
      cacheUtil.removeByPattern(pattern);
    }
  }

  /** 批量清除课程缓存 */
  private void batchClearCourseCache(List<UUID> courseIds) {
    if (courseIds != null && !courseIds.isEmpty()) {
      List<String> cacheKeys =
          courseIds.stream()
              .map(id -> COURSE_CACHE_PREFIX + id.toString())
              .collect(java.util.stream.Collectors.toList());

      // 使用批量删除提高性能
      for (String key : cacheKeys) {
        cacheUtil.remove(key);
      }
    }
  }

  /**
   * 构建课程对象
   *
   * @param creatorId 创建者ID
   * @param title 标题
   * @param description 描述
   * @param status 状态
   * @return 课程对象
   */
  private Course buildCourse(UUID creatorId, String title, String description, String status) {
    Course course = new Course();
    course.setCreatorId(creatorId);
    course.setTitle(title.trim());
    course.setDescription(description.trim());
    course.setStatus(determineStatus(status));
    course.setCreatedAt(LocalDateTime.now());
    course.setUpdatedAt(LocalDateTime.now());
    course.setDeletedAt(null);
    return course;
  }

  /**
   * 确定课程状态
   *
   * @param status 状态字符串
   * @return 课程状态
   */
  private String determineStatus(String status) {
    if (status == null || status.trim().isEmpty()) {
      return "DRAFT";
    }
    return status.trim().toUpperCase();
  }

  /**
   * 转换课程列表为响应格式
   *
   * @param courses 课程列表
   * @return 响应格式的课程列表
   */
  private List<Map<String, Object>> convertCoursesToResponses(List<Course> courses) {
    return courses.stream()
        .map(this::createCourseResponse)
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * 验证更新数据
   *
   * @param title 标题
   * @param description 描述
   * @param status 状态
   */
  private void validateUpdateData(String title, String description, String status) {
    if (title != null) {
      if (title.trim().isEmpty()) {
        throw new BusinessException("INVALID_TITLE", "课程标题不能为空");
      }
      if (title.length() > 100) {
        throw new BusinessException("INVALID_TITLE_LENGTH", "课程标题长度不能超过100个字符");
      }
    }

    if (description != null) {
      if (description.trim().isEmpty()) {
        throw new BusinessException("INVALID_DESCRIPTION", "课程描述不能为空");
      }
      if (description.length() > 500) {
        throw new BusinessException("INVALID_DESCRIPTION_LENGTH", "课程描述长度不能超过500个字符");
      }
    }

    if (status != null && !Arrays.asList("DRAFT", "PUBLISHED", "ARCHIVED").contains(status)) {
      throw new BusinessException("INVALID_STATUS", "无效的课程状态");
    }
  }

  /**
   * 更新课程字段
   *
   * @param course 课程对象
   * @param title 新标题
   * @param description 新描述
   * @param status 新状态
   */
  private void updateCourseFields(Course course, String title, String description, String status) {
    boolean hasChanges = false;

    if (title != null && !title.trim().isEmpty()) {
      course.setTitle(title.trim());
      hasChanges = true;
    }

    if (description != null && !description.trim().isEmpty()) {
      course.setDescription(description.trim());
      hasChanges = true;
    }

    if (status != null) {
      course.setStatus(determineStatus(status));
      hasChanges = true;
    }

    if (hasChanges) {
      course.setUpdatedAt(java.time.LocalDateTime.now());
    }
  }

  /**
   * 验证分页参数
   *
   * @param page 页码
   * @param size 每页大小
   */
  private void validatePaginationParams(int page, int size) {
    if (page < 0) {
      throw new BusinessException("INVALID_PAGE", "页码不能小于0");
    }
    if (size <= 0) {
      throw new BusinessException("INVALID_SIZE", "每页大小必须大于0");
    }
    if (size > 100) {
      throw new BusinessException("SIZE_TOO_LARGE", "每页大小不能超过100");
    }
  }

  /**
   * 创建分页对象
   *
   * @param page 页码
   * @param size 每页大小
   * @param sortBy 排序字段
   * @param sortDirection 排序方向
   * @return 分页对象
   */
  private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
    // 默认排序字段和方向
    String actualSortBy =
        (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy.trim() : "createdAt";
    String actualSortDirection =
        (sortDirection != null && !sortDirection.trim().isEmpty())
            ? sortDirection.trim().toUpperCase()
            : "DESC";

    // 验证排序字段
    if (!Arrays.asList("id", "title", "status", "createdAt", "updatedAt").contains(actualSortBy)) {
      actualSortBy = "createdAt";
    }

    // 验证排序方向
    if (!Arrays.asList("ASC", "DESC").contains(actualSortDirection)) {
      actualSortDirection = "DESC";
    }

    Sort.Direction direction = Sort.Direction.fromString(actualSortDirection);
    Sort sort = Sort.by(direction, actualSortBy);

    return PageRequest.of(page, size, sort);
  }

  /**
   * 创建课程响应对象
   *
   * @param course 课程对象
   * @return 课程响应
   */
  private Map<String, Object> createCourseResponse(Course course) {
    if (course == null) {
      return Collections.emptyMap();
    }

    return Map.of(
        "id", course.getId(),
        "creatorId", course.getCreatorId(),
        "title", course.getTitle(),
        "description", course.getDescription(),
        "status", course.getStatus(),
        "createdAt", course.getCreatedAt(),
        "updatedAt", course.getUpdatedAt());
  }
}
