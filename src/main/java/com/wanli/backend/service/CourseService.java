package com.wanli.backend.service;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanli.backend.entity.Course;
import com.wanli.backend.entity.User;
import com.wanli.backend.repository.CourseRepository;
import com.wanli.backend.repository.UserRepository;

/** 课程服务类 处理课程相关的业务逻辑 */
@Service
public class CourseService {

  private final CourseRepository courseRepository;
  private final UserRepository userRepository;

  public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
    this.courseRepository = courseRepository;
    this.userRepository = userRepository;
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
    Map<String, Object> result = new HashMap<>();

    try {
      // 验证创建者是否存在
      Optional<User> creatorOptional = userRepository.findByIdAndNotDeleted(creatorId);
      if (!creatorOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "创建者不存在");
        return result;
      }

      User creator = creatorOptional.get();

      // 检查权限：只有教师和管理员可以创建课程
      if (!"teacher".equals(creator.getRole()) && !"admin".equals(creator.getRole())) {
        result.put("success", false);
        result.put("message", "权限不足，只有教师和管理员可以创建课程");
        return result;
      }

      // 创建课程
      Course course = new Course();
      course.setCreatorId(creatorId);
      course.setTitle(title);
      course.setDescription(description);
      course.setStatus(status != null ? status.toUpperCase() : "DRAFT"); // 使用传入的状态，默认为草稿
      // 不需要手动设置时间戳，@CreationTimestamp和@UpdateTimestamp会自动处理

      Course savedCourse = courseRepository.save(course);
      courseRepository.flush(); // 强制刷新到数据库，确保时间戳被设置

      // 重新查询以获取完整的时间戳信息
      Course refreshedCourse = courseRepository.findById(savedCourse.getId()).orElse(savedCourse);

      result.put("success", true);
      result.put("message", "课程创建成功");
      result.put("course", createCourseResponse(refreshedCourse));

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "课程创建失败：" + e.getMessage());
    }

    return result;
  }

  /**
   * 获取所有课程列表
   *
   * @return 课程列表
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getAllCourses() {
    Map<String, Object> result = new HashMap<>();

    try {
      List<Course> courses = courseRepository.findAllNotDeleted();
      List<Map<String, Object>> courseResponses = new ArrayList<>();

      for (Course course : courses) {
        courseResponses.add(createCourseResponse(course));
      }

      result.put("success", true);
      result.put("message", "获取课程列表成功");
      result.put("courses", courseResponses);
      result.put("total", courseResponses.size());

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "获取课程列表失败：" + e.getMessage());
    }

    return result;
  }

  /**
   * 根据ID获取课程详情
   *
   * @param courseId 课程ID
   * @return 课程详情
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getCourseById(UUID courseId) {
    Map<String, Object> result = new HashMap<>();

    try {
      Optional<Course> courseOptional = courseRepository.findByIdAndNotDeleted(courseId);

      if (!courseOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "课程不存在");
        return result;
      }

      Course course = courseOptional.get();

      result.put("success", true);
      result.put("message", "获取课程详情成功");
      result.put("course", createCourseResponse(course));

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "获取课程详情失败：" + e.getMessage());
    }

    return result;
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
  public Map<String, Object> updateCourse(
      UUID courseId, UUID userId, String title, String description, String status) {
    Map<String, Object> result = new HashMap<>();

    try {
      // 验证课程是否存在
      Optional<Course> courseOptional = courseRepository.findByIdAndNotDeleted(courseId);
      if (!courseOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "课程不存在");
        return result;
      }

      Course course = courseOptional.get();

      // 验证用户是否存在
      Optional<User> userOptional = userRepository.findByIdAndNotDeleted(userId);
      if (!userOptional.isPresent()) {
        result.put("success", false);
        result.put("message", "用户不存在");
        return result;
      }

      User user = userOptional.get();

      // 检查权限：只有课程创建者、管理员可以更新课程
      if (!course.getCreatorId().equals(userId) && !"admin".equals(user.getRole())) {
        result.put("success", false);
        result.put("message", "权限不足，只有课程创建者和管理员可以更新课程");
        return result;
      }

      // 更新课程信息
      if (title != null && !title.trim().isEmpty()) {
        course.setTitle(title);
      }
      if (description != null) {
        course.setDescription(description);
      }
      if (status != null && !status.trim().isEmpty()) {
        // 验证状态值（不区分大小写，但保持原始格式）
        String normalizedStatus = status.toLowerCase();
        if (Arrays.asList("draft", "published", "archived").contains(normalizedStatus)) {
          course.setStatus(status); // 保持原始大小写格式
        } else {
          result.put("success", false);
          result.put("message", "无效的课程状态");
          return result;
        }
      }

      Course updatedCourse = courseRepository.save(course);
      // 强制刷新到数据库并重新查询以获取正确的时间戳
      courseRepository.flush();
      updatedCourse = courseRepository.findById(course.getId()).orElse(updatedCourse);

      result.put("success", true);
      result.put("message", "课程更新成功");
      result.put("course", createCourseResponse(updatedCourse));

    } catch (Exception e) {
      result.put("success", false);
      result.put("message", "课程更新失败：" + e.getMessage());
    }

    return result;
  }

  /**
   * 创建课程响应对象
   *
   * @param course 课程实体
   * @return 课程响应对象
   */
  private Map<String, Object> createCourseResponse(Course course) {
    Map<String, Object> courseResponse = new HashMap<>();
    courseResponse.put("id", course.getId());
    courseResponse.put("creatorId", course.getCreatorId());
    courseResponse.put("title", course.getTitle());
    courseResponse.put("description", course.getDescription());
    courseResponse.put("status", course.getStatus());
    courseResponse.put("created_at", course.getCreatedAt());
    courseResponse.put("updated_at", course.getUpdatedAt());
    return courseResponse;
  }
}
