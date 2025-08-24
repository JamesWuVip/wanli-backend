package com.wanli.backend.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.wanli.backend.util.LogUtil;
import com.wanli.backend.util.PerformanceMonitor;
import com.wanli.backend.util.PermissionUtil;
import com.wanli.backend.util.ServiceResponseUtil;
import com.wanli.backend.util.ValidationUtil;

/** 课时服务类 处理课时相关的业务逻辑 */
@Service
public class LessonService {

  private final LessonRepository lessonRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final CacheUtil cacheUtil;
  private final ConfigUtil configUtil;

  // 缓存键前缀和策略
  private static final String LESSON_CACHE_PREFIX = "lesson:detail:";
  private static final String LESSON_LIST_CACHE_PREFIX = "lesson:list:";
  private static final String LESSON_LIST_PAGINATED_PREFIX = "lesson:list:page:";
  private static final String LESSON_COURSE_LIST_PREFIX = "lesson:course:";

  // 缓存TTL配置（分钟）
  private static final int LESSON_DETAIL_TTL = 30; // 课时详情缓存30分钟
  private static final int LESSON_LIST_TTL = 15; // 课时列表缓存15分钟
  private static final int LESSON_PAGINATED_TTL = 10; // 分页列表缓存10分钟

  public LessonService(
      LessonRepository lessonRepository,
      CourseRepository courseRepository,
      UserRepository userRepository,
      CacheUtil cacheUtil,
      ConfigUtil configUtil) {
    this.lessonRepository = lessonRepository;
    this.courseRepository = courseRepository;
    this.userRepository = userRepository;
    this.cacheUtil = cacheUtil;
    this.configUtil = configUtil;
  }

  /**
   * 创建课时
   *
   * @param courseId 课程ID字符串
   * @param userId 操作用户ID
   * @param title 课时标题
   * @param description 课时描述
   * @param content 课时内容
   * @param videoUrl 视频URL
   * @param duration 课时时长（秒）
   * @param status 课时状态
   * @param orderIndex 排序索引
   * @return 创建结果
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> createLesson(
      String courseId,
      UUID userId,
      String title,
      String description,
      String content,
      String videoUrl,
      Integer duration,
      String status,
      Integer orderIndex) {

    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.start("createLesson")) {
      // 输入验证
      validateLessonCreationInput(courseId, title, orderIndex);

      UUID courseUuid = UUID.fromString(courseId);

      // 验证用户和课程，检查权限
      User user = findUserById(userId);
      Course course = findCourseById(courseUuid);
      validateCreatorAndPermission(user, course, userId);

      // 验证课时唯一性
      validateLessonUniqueness(courseUuid, title, orderIndex);

      // 创建并保存课时
      LessonStatus lessonStatus =
          status != null ? LessonStatus.fromCode(status) : LessonStatus.DRAFT;
      Lesson lesson =
          buildLesson(
              title,
              description,
              content,
              videoUrl,
              duration,
              lessonStatus,
              orderIndex,
              course,
              user);
      Lesson savedLesson =
          DatabaseUtil.saveSafely(lessonRepository, lesson, "课时创建", userId.toString());

      // 清除相关缓存
      clearLessonListCache(courseUuid);

      // 清除课时详情缓存
      clearLessonDetailCache(savedLesson.getId());

      // 记录操作日志
      LogUtil.logBusinessOperation(
          "课时创建",
          userId.toString(),
          Map.of("lessonId", savedLesson.getId(), "courseId", courseUuid, "title", title));

      return ServiceResponseUtil.success("课时创建成功", "lesson", createLessonResponse(savedLesson));
    }
  }

  /**
   * 根据课程ID获取课时列表（分页）
   *
   * @param courseId 课程ID字符串
   * @param userId 操作用户ID
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @param sortBy 排序字段
   * @param sortDir 排序方向
   * @return 分页的课时列表
   */
  public Map<String, Object> getLessonsByCourseId(
      String courseId, UUID userId, Integer page, Integer size, String sortBy, String sortDir) {
    try (PerformanceMonitor.Monitor monitor =
        PerformanceMonitor.start("getLessonsByCourseIdPaged")) {
      // 输入验证
      if (!ValidationUtil.isValidUUID(courseId)) {
        throw new BusinessException("无效的课程ID格式", "INVALID_COURSE_ID");
      }

      UUID courseUuid = UUID.fromString(courseId);

      // 验证分页参数
      validatePaginationParams(page, size);

      // 创建分页对象
      Pageable pageable = createPageable(page, size, sortBy, sortDir);

      String cacheKey =
          LESSON_LIST_PAGINATED_PREFIX
              + courseUuid
              + ":"
              + page
              + ":"
              + size
              + ":"
              + sortBy
              + ":"
              + sortDir;

      // 尝试从缓存获取
      Map<String, Object> cachedResult = cacheUtil.get(cacheKey);
      if (cachedResult != null) {
        LogUtil.logBusinessOperation(
            "课时列表分页缓存命中",
            userId.toString(),
            Map.of("courseId", courseUuid, "page", page, "size", size));
        return ServiceResponseUtil.success("获取课时列表成功", "data", cachedResult);
      }

      // 验证用户和课程，检查权限
      User user = findUserById(userId);
      Course course = findCourseById(courseUuid);

      if (!PermissionUtil.canViewLessons(user, course.getCreatorId())) {
        throw new PermissionDeniedException("无权限查看课时列表");
      }

      // 从数据库获取分页课时列表
      Page<Lesson> lessonPage =
          DatabaseUtil.findByIdSafely(
              () -> lessonRepository.findByCourseIdAndNotDeleted(courseUuid, pageable), "课时分页查询");

      List<Map<String, Object>> lessonResponses =
          convertLessonsToResponses(lessonPage.getContent());

      // 构建分页响应
      Map<String, Object> result =
          Map.of(
              "lessons", lessonResponses,
              "totalElements", lessonPage.getTotalElements(),
              "totalPages", lessonPage.getTotalPages(),
              "currentPage", lessonPage.getNumber(),
              "pageSize", lessonPage.getSize(),
              "hasNext", lessonPage.hasNext(),
              "hasPrevious", lessonPage.hasPrevious());

      // 缓存结果
      cacheUtil.put(cacheKey, result, LESSON_PAGINATED_TTL);

      // 记录操作日志
      LogUtil.logBusinessOperation(
          "获取课时分页列表",
          userId.toString(),
          Map.of(
              "courseId",
              courseUuid,
              "page",
              page,
              "size",
              size,
              "totalElements",
              lessonPage.getTotalElements()));

      return ServiceResponseUtil.success("获取课时列表成功", "data", result);
    }
  }

  /**
   * 根据课程ID获取课时列表（无分页，保持向后兼容）
   *
   * @param courseId 课程ID字符串
   * @param userId 操作用户ID
   * @return 课时列表
   */
  public Map<String, Object> getLessonsByCourseId(String courseId, UUID userId) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.start("getLessonsByCourseId")) {
      // 输入验证
      if (!ValidationUtil.isValidUUID(courseId)) {
        throw new BusinessException("无效的课程ID格式", "INVALID_COURSE_ID");
      }

      UUID courseUuid = UUID.fromString(courseId);
      String cacheKey = LESSON_COURSE_LIST_PREFIX + courseUuid;

      // 尝试从缓存获取
      List<Map<String, Object>> cachedLessons = cacheUtil.get(cacheKey);
      if (cachedLessons != null) {
        LogUtil.logBusinessOperation("课时列表缓存命中", userId.toString(), Map.of("courseId", courseUuid));
        return ServiceResponseUtil.success("获取课时列表成功", "lessons", cachedLessons);
      }

      // 验证用户和课程，检查权限
      User user = findUserById(userId);
      Course course = findCourseById(courseUuid);

      if (!PermissionUtil.canViewLessons(user, course.getCreatorId())) {
        throw new PermissionDeniedException("无权限查看课时列表");
      }

      // 从数据库获取课时列表
      List<Lesson> lessons =
          DatabaseUtil.findByIdSafely(
              () -> lessonRepository.findByCourseIdAndNotDeletedOrderByOrderIndex(courseUuid),
              "课时列表查询");

      List<Map<String, Object>> lessonResponses = convertLessonsToResponses(lessons);

      // 缓存结果
      cacheUtil.put(cacheKey, lessonResponses, LESSON_LIST_TTL);

      // 记录操作日志
      LogUtil.logBusinessOperation(
          "获取课时列表",
          userId.toString(),
          Map.of("courseId", courseUuid, "lessonCount", lessons.size()));

      return ServiceResponseUtil.success("获取课时列表成功", "lessons", lessonResponses);
    }
  }

  /**
   * 根据ID获取课时详情
   *
   * @param lessonId 课时ID字符串
   * @param userId 操作用户ID
   * @return 课时详情
   */
  public Map<String, Object> getLessonById(String lessonId, UUID userId) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.start("getLessonById")) {
      // 输入验证
      if (!ValidationUtil.isValidUUID(lessonId)) {
        throw new BusinessException("无效的课时ID格式", "INVALID_LESSON_ID");
      }

      UUID lessonUuid = UUID.fromString(lessonId);
      String cacheKey = LESSON_CACHE_PREFIX + lessonUuid;

      // 尝试从缓存获取
      Map<String, Object> cachedLesson = cacheUtil.get(cacheKey);
      if (cachedLesson != null) {
        LogUtil.logBusinessOperation("课时详情缓存命中", userId.toString(), Map.of("lessonId", lessonUuid));
        return ServiceResponseUtil.success("获取课时详情成功", "lesson", cachedLesson);
      }

      // 验证用户
      User user = findUserById(userId);

      // 验证课时是否存在
      Lesson lesson = findLessonById(lessonUuid);
      Course course = lesson.getCourse();

      // 权限检查
      if (!PermissionUtil.canViewLesson(user, course.getCreatorId())) {
        throw new PermissionDeniedException("无权限查看课时详情");
      }

      Map<String, Object> lessonResponse = createLessonResponse(lesson);

      // 缓存结果
      cacheUtil.put(cacheKey, lessonResponse);

      // 记录操作日志
      LogUtil.logBusinessOperation(
          "获取课时详情", userId.toString(), Map.of("lessonId", lessonUuid, "courseId", course.getId()));

      return ServiceResponseUtil.success("获取课时详情成功", "lesson", lessonResponse);
    }
  }

  /**
   * 更新课时
   *
   * @param lessonId 课时ID字符串
   * @param userId 操作用户ID
   * @param title 新标题
   * @param description 新描述
   * @param content 新内容
   * @param videoUrl 新视频URL
   * @param duration 新时长
   * @param status 新状态
   * @param orderIndex 新排序索引
   * @return 更新结果
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> updateLesson(
      String lessonId,
      UUID userId,
      String title,
      String description,
      String content,
      String videoUrl,
      Integer duration,
      String status,
      Integer orderIndex) {

    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.start("updateLesson")) {
      // 输入验证
      if (!ValidationUtil.isValidUUID(lessonId)) {
        throw new BusinessException("无效的课时ID格式", "INVALID_LESSON_ID");
      }

      UUID lessonUuid = UUID.fromString(lessonId);

      // 验证用户和课时
      User user = findUserById(userId);
      Lesson lesson = findLessonById(lessonUuid);
      Course course = lesson.getCourse();

      // 权限检查
      if (!PermissionUtil.canEditLesson(user, course.getCreatorId())) {
        throw new PermissionDeniedException("无权限更新课时");
      }

      // 验证更新数据
      validateUpdateData(title, description, content, videoUrl, duration, status, orderIndex);

      updateLessonFields(
          lesson, title, description, content, videoUrl, duration, status, orderIndex);

      Lesson savedLesson =
          DatabaseUtil.saveSafely(lessonRepository, lesson, "课时", userId.toString());

      // 清除相关缓存
      clearLessonDetailCache(lessonUuid);
      clearLessonListCache(course.getId());

      // 记录操作日志
      LogUtil.logBusinessOperation(
          "课时更新", userId.toString(), Map.of("lessonId", lessonUuid, "courseId", course.getId()));

      return ServiceResponseUtil.success("课时更新成功", "lesson", createLessonResponse(savedLesson));
    }
  }

  /**
   * 批量创建课时
   *
   * @param courseId 课程ID字符串
   * @param userId 操作用户ID
   * @param lessonDataList 课时数据列表
   * @return 批量创建结果
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> batchCreateLessons(
      String courseId, UUID userId, List<Map<String, Object>> lessonDataList) {

    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.start("batchCreateLessons")) {
      // 输入验证
      if (!ValidationUtil.isValidUUID(courseId)) {
        throw new BusinessException("无效的课程ID格式", "INVALID_COURSE_ID");
      }
      if (lessonDataList == null || lessonDataList.isEmpty()) {
        throw new BusinessException("课时列表不能为空", "EMPTY_LESSON_LIST");
      }
      if (lessonDataList.size() > 50) { // 限制批量操作数量
        throw new BusinessException("单次批量创建课时数量不能超过50个", "TOO_MANY_LESSONS");
      }

      UUID courseUuid = UUID.fromString(courseId);

      // 验证用户和课程，检查权限
      User user = findUserById(userId);
      Course course = findCourseById(courseUuid);
      validateCreatorAndPermission(user, course, userId);

      List<Lesson> lessonsToSave = new ArrayList<>();
      List<String> titles = new ArrayList<>();
      List<Integer> orderIndexes = new ArrayList<>();

      // 验证所有课时数据
      for (Map<String, Object> lessonData : lessonDataList) {
        String title = (String) lessonData.get("title");
        String description = (String) lessonData.get("description");
        String content = (String) lessonData.get("content");
        String videoUrl = (String) lessonData.get("videoUrl");
        Integer duration = (Integer) lessonData.get("duration");
        String statusStr = (String) lessonData.get("status");
        Integer orderIndex = (Integer) lessonData.get("orderIndex");

        validateLessonCreationInput(courseId, title, orderIndex);

        // 检查重复标题和排序索引
        if (titles.contains(title)) {
          throw new BusinessException("批量创建中存在重复的课时标题: " + title, "DUPLICATE_TITLE_IN_BATCH");
        }
        if (orderIndexes.contains(orderIndex)) {
          throw new BusinessException(
              "DUPLICATE_ORDER_INDEX_IN_BATCH", "批量创建中存在重复的排序索引: " + orderIndex);
        }

        titles.add(title);
        orderIndexes.add(orderIndex);

        LessonStatus lessonStatus =
            statusStr != null ? LessonStatus.fromCode(statusStr) : LessonStatus.DRAFT;
        Lesson lesson =
            buildLesson(
                title,
                description,
                content,
                videoUrl,
                duration,
                lessonStatus,
                orderIndex,
                course,
                user);
        lessonsToSave.add(lesson);
      }

      // 检查数据库中的重复性
      for (String title : titles) {
        if (lessonRepository.findByCourseIdAndTitleAndNotDeleted(courseUuid, title).isPresent()) {
          throw new BusinessException("该课程中已存在相同标题的课时: " + title, "DUPLICATE_LESSON_TITLE");
        }
      }

      for (Integer orderIndex : orderIndexes) {
        if (lessonRepository.existsByCourseIdAndOrderIndexAndNotDeleted(courseUuid, orderIndex)) {
          throw new BusinessException("该排序索引已被使用: " + orderIndex, "DUPLICATE_ORDER_INDEX");
        }
      }

      // 批量保存
      List<Lesson> savedLessons = lessonRepository.saveAll(lessonsToSave);

      // 清除相关缓存
      clearLessonListCache(courseUuid);

      // 记录操作日志
      LogUtil.logBusinessOperation(
          "批量创建课时",
          userId.toString(),
          Map.of("courseId", courseUuid, "lessonCount", savedLessons.size()));

      List<Map<String, Object>> lessonResponses = convertLessonsToResponses(savedLessons);
      return ServiceResponseUtil.success("批量创建课时成功", "lessons", lessonResponses);
    }
  }

  /**
   * 批量更新课时
   *
   * @param userId 操作用户ID
   * @param updateDataList 更新数据列表
   * @return 批量更新结果
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> batchUpdateLessons(
      UUID userId, List<Map<String, Object>> updateDataList) {

    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.start("batchUpdateLessons")) {
      // 输入验证
      if (updateDataList == null || updateDataList.isEmpty()) {
        throw new BusinessException("更新列表不能为空", "EMPTY_UPDATE_LIST");
      }
      if (updateDataList.size() > 50) { // 限制批量操作数量
        throw new BusinessException("单次批量更新课时数量不能超过50个", "TOO_MANY_UPDATES");
      }

      // 验证用户
      User user = findUserById(userId);

      List<UUID> lessonIds = new ArrayList<>();
      List<Lesson> lessonsToUpdate = new ArrayList<>();
      Set<UUID> courseIds = new HashSet<>();

      // 收集所有课时ID并验证
      for (Map<String, Object> updateData : updateDataList) {
        String lessonIdStr = (String) updateData.get("lessonId");
        if (!ValidationUtil.isValidUUID(lessonIdStr)) {
          throw new BusinessException("无效的课时ID格式: " + lessonIdStr, "INVALID_LESSON_ID");
        }
        UUID lessonId = UUID.fromString(lessonIdStr);
        lessonIds.add(lessonId);
      }

      // 批量查询课时
      List<Lesson> lessons = lessonRepository.findByIdsAndNotDeleted(lessonIds, Pageable.unpaged());
      if (lessons.size() != lessonIds.size()) {
        throw new BusinessException("部分课时不存在或已被删除", "LESSON_NOT_FOUND");
      }

      // 创建课时ID到课时对象的映射
      Map<UUID, Lesson> lessonMap =
          lessons.stream().collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

      // 验证权限并更新数据
      for (Map<String, Object> updateData : updateDataList) {
        String lessonIdStr = (String) updateData.get("lessonId");
        UUID lessonId = UUID.fromString(lessonIdStr);
        Lesson lesson = lessonMap.get(lessonId);
        Course course = lesson.getCourse();

        // 权限检查
        if (!PermissionUtil.canEditLesson(user, course.getCreatorId())) {
          throw new PermissionDeniedException("无权限更新课时: " + lesson.getTitle());
        }

        String title = (String) updateData.get("title");
        String description = (String) updateData.get("description");
        String content = (String) updateData.get("content");
        String videoUrl = (String) updateData.get("videoUrl");
        Integer duration = (Integer) updateData.get("duration");
        String status = (String) updateData.get("status");
        Integer orderIndex = (Integer) updateData.get("orderIndex");

        // 验证更新数据
        validateUpdateData(title, description, content, videoUrl, duration, status, orderIndex);

        // 更新字段
        updateLessonFields(
            lesson, title, description, content, videoUrl, duration, status, orderIndex);
        lessonsToUpdate.add(lesson);
        courseIds.add(course.getId());
      }

      // 批量保存
      List<Lesson> savedLessons = lessonRepository.saveAll(lessonsToUpdate);

      // 清除相关缓存
      for (UUID lessonId : lessonIds) {
        clearLessonDetailCache(lessonId);
      }
      for (UUID courseId : courseIds) {
        clearLessonListCache(courseId);
      }

      // 记录操作日志
      LogUtil.logBusinessOperation(
          "批量更新课时",
          userId.toString(),
          Map.of("lessonCount", savedLessons.size(), "courseIds", courseIds));

      List<Map<String, Object>> lessonResponses = convertLessonsToResponses(savedLessons);
      return ServiceResponseUtil.success("批量更新课时成功", "lessons", lessonResponses);
    }
  }

  /**
   * 批量删除课时
   *
   * @param lessonIds 课时ID列表
   * @param userId 操作用户ID
   * @return 批量删除结果
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> batchDeleteLessons(List<String> lessonIds, UUID userId) {
    try (PerformanceMonitor.Monitor monitor = PerformanceMonitor.start("batchDeleteLessons")) {
      // 输入验证
      if (lessonIds == null || lessonIds.isEmpty()) {
        throw new BusinessException("课时ID列表不能为空", "EMPTY_LESSON_IDS");
      }
      if (lessonIds.size() > 50) { // 限制批量操作数量
        throw new BusinessException("单次批量删除课时数量不能超过50个", "TOO_MANY_DELETIONS");
      }

      // 验证用户
      User user = findUserById(userId);

      List<UUID> lessonUuids = new ArrayList<>();
      for (String lessonIdStr : lessonIds) {
        if (!ValidationUtil.isValidUUID(lessonIdStr)) {
          throw new BusinessException("无效的课时ID格式: " + lessonIdStr, "INVALID_LESSON_ID");
        }
        lessonUuids.add(UUID.fromString(lessonIdStr));
      }

      // 批量查询课时
      List<Lesson> lessons =
          lessonRepository.findByIdsAndNotDeleted(lessonUuids, Pageable.unpaged());
      if (lessons.size() != lessonUuids.size()) {
        throw new BusinessException("部分课时不存在或已被删除", "LESSON_NOT_FOUND");
      }

      Set<UUID> courseIds = new HashSet<>();

      // 验证权限
      for (Lesson lesson : lessons) {
        Course course = lesson.getCourse();
        if (!PermissionUtil.canDeleteLesson(user, course.getCreatorId())) {
          throw new PermissionDeniedException("无权限删除课时: " + lesson.getTitle());
        }
        courseIds.add(course.getId());
      }

      // 批量软删除
      LocalDateTime now = LocalDateTime.now();
      for (Lesson lesson : lessons) {
        lesson.setDeletedAt(now);
        lesson.setUpdatedAt(now);
      }

      lessonRepository.saveAll(lessons);

      // 清除相关缓存
      for (UUID lessonId : lessonUuids) {
        clearLessonDetailCache(lessonId);
      }
      for (UUID courseId : courseIds) {
        clearLessonListCache(courseId);
      }

      // 记录操作日志
      LogUtil.logBusinessOperation(
          "批量删除课时",
          userId.toString(),
          Map.of("lessonCount", lessons.size(), "courseIds", courseIds));

      return ServiceResponseUtil.success("批量删除课时成功", "deletedCount", lessons.size());
    }
  }

  /**
   * 创建课时响应对象
   *
   * @param lesson 课时实体
   * @return 课时响应对象
   */
  private Map<String, Object> createLessonResponse(Lesson lesson) {
    if (lesson == null) {
      return Collections.emptyMap();
    }

    Map<String, Object> response = new HashMap<>();
    response.put("id", lesson.getId());
    response.put("title", lesson.getTitle());
    response.put("description", lesson.getDescription() != null ? lesson.getDescription() : "");
    response.put("content", lesson.getContent() != null ? lesson.getContent() : "");
    response.put("videoUrl", lesson.getVideoUrl() != null ? lesson.getVideoUrl() : "");
    response.put("duration", lesson.getDuration());
    response.put(
        "status",
        lesson.getStatus() != null ? lesson.getStatus().getCode() : LessonStatus.DRAFT.getCode());
    response.put(
        "statusDescription",
        lesson.getStatus() != null
            ? lesson.getStatus().getDescription()
            : LessonStatus.DRAFT.getDescription());
    response.put("orderIndex", lesson.getOrderIndex());
    response.put("courseId", lesson.getCourse().getId());
    response.put("creatorId", lesson.getCreator().getId());
    response.put("createdAt", lesson.getCreatedAt());
    response.put("updatedAt", lesson.getUpdatedAt());
    return response;
  }

  /** 验证课时创建输入 */
  private void validateLessonCreationInput(String courseId, String title, Integer orderIndex) {
    if (courseId == null) {
      throw new BusinessException("课程ID不能为空", "EMPTY_COURSE_ID");
    }
    if (!ValidationUtil.isValidUUID(courseId)) {
      throw new BusinessException("无效的课程ID格式", "INVALID_COURSE_ID");
    }
    if (ValidationUtil.isBlank(title)) {
      throw new BusinessException("课时标题不能为空", "EMPTY_TITLE");
    }
    if (orderIndex == null) {
      throw new BusinessException("排序索引不能为空", "EMPTY_ORDER_INDEX");
    }
    if (orderIndex < 1) {
      throw new BusinessException("课时顺序必须大于0", "INVALID_ORDER_INDEX");
    }
  }

  private void validateLessonCreationInput(UUID courseId, String title, Integer orderIndex) {
    validateLessonCreationInput(courseId.toString(), title, orderIndex);
  }

  /** 根据ID查找用户 */
  private User findUserById(UUID userId) {
    return userRepository
        .findByIdAndNotDeleted(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("用户不存在: " + userId, "User", userId.toString()));
  }

  /** 根据ID查找课程 */
  private Course findCourseById(UUID courseId) {
    return courseRepository
        .findByIdAndNotDeleted(courseId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("课程不存在: " + courseId, "Course", courseId.toString()));
  }

  /** 根据ID查找课时 */
  private Lesson findLessonById(UUID lessonId) {
    return lessonRepository
        .findByIdAndNotDeleted(lessonId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("课时不存在: " + lessonId, "Lesson", lessonId.toString()));
  }

  /** 验证创建者和权限 */
  private void validateCreatorAndPermission(User user, Course course, UUID userId) {
    if (!PermissionUtil.canCreateLesson(user, course.getCreatorId())) {
      throw new PermissionDeniedException("无权限创建课时");
    }
  }

  /** 验证课时唯一性 */
  private void validateLessonUniqueness(UUID courseId, String title, Integer orderIndex) {
    // 检查课时标题是否在该课程中已存在
    Optional<Lesson> existingLesson =
        lessonRepository.findByCourseIdAndTitleAndNotDeleted(courseId, title);
    if (existingLesson.isPresent()) {
      throw new BusinessException("该课程中已存在相同标题的课时", "DUPLICATE_LESSON_TITLE");
    }

    // 处理排序索引
    if (lessonRepository.existsByCourseIdAndOrderIndexAndNotDeleted(courseId, orderIndex)) {
      throw new BusinessException("该排序索引已被使用", "DUPLICATE_ORDER_INDEX");
    }
  }

  /** 清除课时列表缓存 */
  private void clearLessonListCache(UUID courseId) {
    if (courseId != null) {
      // 清除课程相关的课时列表缓存
      String cacheKey = LESSON_COURSE_LIST_PREFIX + courseId.toString();
      cacheUtil.remove(cacheKey);

      // 清除分页列表缓存（使用模式匹配）
      String pagePattern = LESSON_LIST_PAGINATED_PREFIX + courseId + ":*";
      cacheUtil.removeByPattern(pagePattern);
    }
  }

  /** 清除单个课时详情缓存 */
  private void clearLessonDetailCache(UUID lessonId) {
    if (lessonId != null) {
      String cacheKey = LESSON_CACHE_PREFIX + lessonId.toString();
      cacheUtil.remove(cacheKey);
    }
  }

  /** 批量清除课时缓存 */
  private void batchClearLessonCache(List<UUID> courseIds) {
    if (courseIds != null && !courseIds.isEmpty()) {
      for (UUID courseId : courseIds) {
        clearLessonListCache(courseId);
      }
    }
  }

  /** 清除所有课时相关缓存 */
  private void clearAllLessonCache() {
    // 清除所有课时详情缓存
    cacheUtil.removeByPattern(LESSON_CACHE_PREFIX + "*");

    // 清除所有课时列表缓存
    cacheUtil.removeByPattern(LESSON_COURSE_LIST_PREFIX + "*");
    cacheUtil.removeByPattern(LESSON_LIST_PAGINATED_PREFIX + "*");
  }

  /** 构建课时对象 */
  private Lesson buildLesson(
      String title,
      String description,
      String content,
      String videoUrl,
      Integer duration,
      LessonStatus status,
      Integer orderIndex,
      Course course,
      User creator) {
    Lesson lesson = new Lesson();
    lesson.setTitle(title != null ? title.trim() : null);
    lesson.setDescription(description != null ? description.trim() : null);
    lesson.setContent(content != null ? content.trim() : null);
    lesson.setVideoUrl(videoUrl != null ? videoUrl.trim() : null);
    lesson.setDuration(duration);
    lesson.setStatus(status != null ? status : LessonStatus.DRAFT);
    lesson.setOrderIndex(orderIndex);
    lesson.setCourse(course);
    lesson.setCreator(creator);
    lesson.setCreatedAt(LocalDateTime.now());
    lesson.setUpdatedAt(LocalDateTime.now());
    lesson.setDeletedAt(null);
    return lesson;
  }

  /** 转换课时列表为响应格式 */
  private List<Map<String, Object>> convertLessonsToResponses(List<Lesson> lessons) {
    return lessons.stream().map(this::createLessonResponse).collect(Collectors.toList());
  }

  /** 验证更新数据 */
  private void validateUpdateData(
      String title,
      String description,
      String content,
      String videoUrl,
      Integer duration,
      String status,
      Integer orderIndex) {
    if (title != null && ValidationUtil.isBlank(title)) {
      throw new BusinessException("EMPTY_TITLE", "课时标题不能为空");
    }
    if (description != null && description.length() > 1000) {
      throw new BusinessException("课时描述不能超过1000个字符", "DESCRIPTION_TOO_LONG");
    }
    if (duration != null && duration < 0) {
      throw new BusinessException("课时时长不能为负数", "INVALID_DURATION");
    }
    if (status != null) {
      try {
        LessonStatus.fromCode(status);
      } catch (IllegalArgumentException e) {
        throw new BusinessException("无效的课时状态: " + status, "INVALID_STATUS");
      }
    }
    if (orderIndex != null && orderIndex < 1) {
      throw new BusinessException("课时顺序必须大于0", "INVALID_ORDER_INDEX");
    }
  }

  /** 更新课时字段 */
  private void updateLessonFields(
      Lesson lesson,
      String title,
      String description,
      String content,
      String videoUrl,
      Integer duration,
      String status,
      Integer orderIndex) {
    boolean hasChanges = false;

    if (title != null && !title.trim().equals(lesson.getTitle())) {
      lesson.setTitle(title.trim());
      hasChanges = true;
    }
    if (description != null && !description.trim().equals(lesson.getDescription())) {
      lesson.setDescription(description.trim());
      hasChanges = true;
    }
    if (content != null && !content.trim().equals(lesson.getContent())) {
      lesson.setContent(content.trim());
      hasChanges = true;
    }
    if (videoUrl != null && !videoUrl.trim().equals(lesson.getVideoUrl())) {
      lesson.setVideoUrl(videoUrl.trim());
      hasChanges = true;
    }
    if (duration != null && !duration.equals(lesson.getDuration())) {
      lesson.setDuration(duration);
      hasChanges = true;
    }
    if (status != null) {
      LessonStatus newStatus = LessonStatus.fromCode(status);
      if (!newStatus.equals(lesson.getStatus())) {
        lesson.setStatus(newStatus);
        hasChanges = true;
      }
    }
    if (orderIndex != null && !orderIndex.equals(lesson.getOrderIndex())) {
      lesson.setOrderIndex(orderIndex);
      hasChanges = true;
    }

    if (hasChanges) {
      lesson.setUpdatedAt(LocalDateTime.now());
    }
  }

  /**
   * 验证分页参数
   *
   * @param page 页码
   * @param size 每页大小
   */
  private void validatePaginationParams(Integer page, Integer size) {
    if (page != null && page < 0) {
      throw new BusinessException("页码不能小于0", "INVALID_PAGE");
    }
    if (size != null && (size < 1 || size > 100)) {
      throw new BusinessException("每页大小必须在1-100之间", "INVALID_PAGE_SIZE");
    }
  }

  /**
   * 创建分页对象
   *
   * @param page 页码
   * @param size 每页大小
   * @param sortBy 排序字段
   * @param sortDir 排序方向
   * @return Pageable对象
   */
  private Pageable createPageable(Integer page, Integer size, String sortBy, String sortDir) {
    // 设置默认值
    int pageNumber = page != null ? page : 0;
    int pageSize = size != null ? size : 20;
    String sortField = sortBy != null ? sortBy : "orderIndex";
    String sortDirection = sortDir != null ? sortDir : "asc";

    // 验证排序字段
    Set<String> allowedSortFields = Set.of("orderIndex", "title", "createdAt", "updatedAt");
    if (!allowedSortFields.contains(sortField)) {
      sortField = "orderIndex";
    }

    // 验证排序方向
    Sort.Direction direction =
        "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;

    return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
  }
}
