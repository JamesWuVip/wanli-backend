package com.wanli.backend.service;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanli.backend.entity.Course;
import com.wanli.backend.entity.Lesson;
import com.wanli.backend.entity.User;
import com.wanli.backend.repository.CourseRepository;
import com.wanli.backend.repository.LessonRepository;
import com.wanli.backend.repository.UserRepository;

/** 课时服务类 处理课时相关的业务逻辑 */
@Service
public class LessonService {

  @Autowired private LessonRepository lessonRepository;

  @Autowired private CourseRepository courseRepository;

  @Autowired private UserRepository userRepository;

  /**
   * 创建课时
   *
   * @param userId 操作用户ID
   * @param courseId 课程ID
   * @param title 课时标题
   * @param orderIndex 排序索引（可选）
   * @return 创建结果
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> createLesson(
      UUID userId, UUID courseId, String title, Integer orderIndex) {
    Map<String, Object> result = new HashMap<>();

    // 验证用户是否存在
    Optional<User> userOptional = userRepository.findByIdAndNotDeleted(userId);
    if (!userOptional.isPresent()) {
      result.put("success", false);
      result.put("message", "用户不存在");
      return result;
    }

    User user = userOptional.get();

    // 验证课程是否存在
    Optional<Course> courseOptional = courseRepository.findByIdAndNotDeleted(courseId);
    if (!courseOptional.isPresent()) {
      result.put("success", false);
      result.put("message", "课程不存在");
      return result;
    }

    Course course = courseOptional.get();

    // 检查权限：只有课程创建者、管理员可以创建课时
    if (!course.getCreatorId().equals(userId) && !"admin".equals(user.getRole())) {
      result.put("success", false);
      result.put("message", "权限不足，只有课程创建者和管理员可以创建课时");
      return result;
    }

    // 检查课时标题是否在该课程中已存在
    Optional<Lesson> existingLesson =
        lessonRepository.findByCourseIdAndTitleAndNotDeleted(courseId, title);
    if (existingLesson.isPresent()) {
      result.put("success", false);
      result.put("message", "该课程中已存在相同标题的课时");
      return result;
    }

    // 处理排序索引
    if (orderIndex == null) {
      // 如果未指定排序索引，则设置为最大值+1
      Integer maxOrderIndex = lessonRepository.findMaxOrderIndexByCourseId(courseId);
      orderIndex = maxOrderIndex + 1;
    } else {
      // 如果指定了排序索引，检查是否已存在
      if (lessonRepository.existsByCourseIdAndOrderIndexAndNotDeleted(courseId, orderIndex)) {
        result.put("success", false);
        result.put("message", "该排序索引已被使用");
        return result;
      }
    }

    try {
      // 创建课时
      Lesson lesson = new Lesson();
      lesson.setCourse(course); // 设置Course对象而不是courseId
      lesson.setTitle(title);
      lesson.setOrderIndex(orderIndex);
      lesson.setCreatedAt(LocalDateTime.now());
      lesson.setUpdatedAt(LocalDateTime.now());

      Lesson savedLesson = lessonRepository.save(lesson);

      result.put("success", true);
      result.put("message", "课时创建成功");
      result.put("lesson", createLessonResponse(savedLesson));

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "课时创建失败：" + e.getMessage());
      throw e; // 重新抛出异常以触发事务回滚
    }

    return result;
  }

  /**
   * 获取指定课程的课时列表
   *
   * @param courseId 课程ID
   * @return 课时列表
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getLessonsByCourseId(UUID courseId) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 验证课程是否存在
      Optional<Course> courseOptional = courseRepository.findByIdAndNotDeleted(courseId);
      if (!courseOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "课程不存在");
        return result;
      }

      List<Lesson> lessons = lessonRepository.findByCourseIdAndNotDeleted(courseId);
      List<Map<String, Object>> lessonResponses = new ArrayList<>();

      for (Lesson lesson : lessons) {
        lessonResponses.add(createLessonResponse(lesson));
      }

      result.put("success", true);
      result.put("message", "获取课时列表成功");
      result.put("lessons", lessonResponses);
      result.put("total", lessonResponses.size());

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "获取课时列表失败：" + e.getMessage());
    }

    return result;
  }

  /**
   * 根据ID获取课时详情
   *
   * @param lessonId 课时ID
   * @return 课时详情
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getLessonById(UUID lessonId) {
    Map<String, Object> result = new HashMap<>();

    try {
      Optional<Lesson> lessonOptional = lessonRepository.findByIdAndNotDeleted(lessonId);

      if (!lessonOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "课时不存在");
        return result;
      }

      Lesson lesson = lessonOptional.get();

      result.put("success", true);
      result.put("message", "获取课时详情成功");
      result.put("lesson", createLessonResponse(lesson));

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "获取课时详情失败：" + e.getMessage());
    }

    return result;
  }

  /**
   * 更新课时
   *
   * @param lessonId 课时ID
   * @param userId 操作用户ID
   * @param title 课时标题
   * @param orderIndex 排序索引
   * @return 更新结果
   */
  public Map<String, Object> updateLesson(
      UUID lessonId, UUID userId, String title, Integer orderIndex) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 验证课时是否存在
      Optional<Lesson> lessonOptional = lessonRepository.findByIdAndNotDeleted(lessonId);
      if (!lessonOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "课时不存在");
        return result;
      }

      Lesson lesson = lessonOptional.get();

      // 验证用户是否存在
      Optional<User> userOptional = userRepository.findByIdAndNotDeleted(userId);
      if (!userOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "用户不存在");
        return result;
      }

      User user = userOptional.get();

      // 验证课程是否存在
      Optional<Course> courseOptional =
          courseRepository.findByIdAndNotDeleted(lesson.getCourseId());
      if (!courseOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "关联的课程不存在");
        return result;
      }

      Course course = courseOptional.get();

      // 检查权限：只有课程创建者、管理员可以更新课时
      if (!course.getCreatorId().equals(userId) && !"admin".equals(user.getRole())) {
        result.put("success", false);
        result.put("message", "权限不足，只有课程创建者和管理员可以更新课时");
        return result;
      }

      // 更新课时信息
      boolean updated = false;

      if (title != null && !title.trim().isEmpty() && !title.equals(lesson.getTitle())) {
        // 检查新标题是否在该课程中已存在
        Optional<Lesson> existingLesson =
            lessonRepository.findByCourseIdAndTitleAndNotDeleted(lesson.getCourseId(), title);
        if (existingLesson.isPresent() && !existingLesson.get().getId().equals(lessonId)) {
          result.put("success", false);
          result.put("message", "该课程中已存在相同标题的课时");
          return result;
        }
        lesson.setTitle(title);
        updated = true;
      }

      if (orderIndex != null && !orderIndex.equals(lesson.getOrderIndex())) {
        // 检查新排序索引是否已被使用
        if (lessonRepository.existsByCourseIdAndOrderIndexAndNotDeleted(
            lesson.getCourseId(), orderIndex)) {
          result.put("success", false);
          result.put("message", "该排序索引已被使用");
          return result;
        }
        lesson.setOrderIndex(orderIndex);
        updated = true;
      }

      if (updated) {
        lesson.setUpdatedAt(LocalDateTime.now());
        Lesson updatedLesson = lessonRepository.save(lesson);

        result.put("success", true);
        result.put("message", "课时更新成功");
        result.put("lesson", createLessonResponse(updatedLesson));
      } else {
        result.put("success", true);
        result.put("message", "没有需要更新的内容");
        result.put("lesson", createLessonResponse(lesson));
      }

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "课时更新失败：" + e.getMessage());
    }

    return result;
  }

  /**
   * 创建课时响应对象
   *
   * @param lesson 课时实体
   * @return 课时响应对象
   */
  private Map<String, Object> createLessonResponse(Lesson lesson) {
    Map<String, Object> lessonResponse = new HashMap<>();
    lessonResponse.put("id", lesson.getId());
    lessonResponse.put("course_id", lesson.getCourseId());
    lessonResponse.put("title", lesson.getTitle());
    lessonResponse.put("order_index", lesson.getOrderIndex());
    lessonResponse.put("created_at", lesson.getCreatedAt());
    lessonResponse.put("updated_at", lesson.getUpdatedAt());
    return lessonResponse;
  }
}
