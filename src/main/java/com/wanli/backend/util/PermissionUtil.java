package com.wanli.backend.util;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.wanli.backend.entity.User;

/** 权限验证工具类 提供统一的权限检查方法 */
public final class PermissionUtil {

  // 角色常量
  public static final String ROLE_ADMIN = "admin";
  public static final String ROLE_TEACHER = "teacher";
  public static final String ROLE_STUDENT = "student";

  // 权限组
  public static final List<String> COURSE_CREATOR_ROLES = Arrays.asList(ROLE_TEACHER, ROLE_ADMIN);
  public static final List<String> ADMIN_ROLES = Arrays.asList(ROLE_ADMIN);

  // 私有构造函数，防止实例化
  private PermissionUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * 检查用户是否有管理员权限
   *
   * @param user 用户对象
   * @return 是否有管理员权限
   */
  public static boolean isAdmin(User user) {
    return user != null && ROLE_ADMIN.equals(user.getRole());
  }

  /**
   * 检查用户是否有教师权限
   *
   * @param user 用户对象
   * @return 是否有教师权限
   */
  public static boolean isTeacher(User user) {
    return user != null && ROLE_TEACHER.equals(user.getRole());
  }

  /**
   * 检查用户是否有学生权限
   *
   * @param user 用户对象
   * @return 是否有学生权限
   */
  public static boolean isStudent(User user) {
    return user != null && ROLE_STUDENT.equals(user.getRole());
  }

  /**
   * 检查用户是否可以创建课程
   *
   * @param user 用户对象
   * @return 是否可以创建课程
   */
  public static boolean canCreateCourse(User user) {
    return user != null && COURSE_CREATOR_ROLES.contains(user.getRole());
  }

  /**
   * 检查用户是否可以编辑课程
   *
   * @param user 用户对象
   * @param creatorId 课程创建者ID
   * @return 是否可以编辑课程
   */
  public static boolean canEditCourse(User user, UUID creatorId) {
    if (user == null || creatorId == null) {
      return false;
    }
    // 管理员或课程创建者可以编辑
    return isAdmin(user) || user.getId().equals(creatorId);
  }

  /**
   * 检查用户是否可以删除课程
   *
   * @param user 用户对象
   * @param creatorId 课程创建者ID
   * @return 是否可以删除课程
   */
  public static boolean canDeleteCourse(User user, UUID creatorId) {
    // 删除权限与编辑权限相同
    return canEditCourse(user, creatorId);
  }

  /**
   * 检查用户是否可以查看课程
   *
   * @param user 用户对象
   * @return 是否可以查看课程
   */
  public static boolean canViewCourse(User user) {
    // 所有登录用户都可以查看课程
    return user != null;
  }

  /**
   * 检查用户是否可以创建课时
   *
   * @param user 用户对象
   * @param courseCreatorId 课程创建者ID
   * @return 是否可以创建课时
   */
  public static boolean canCreateLesson(User user, UUID courseCreatorId) {
    if (user == null || courseCreatorId == null) {
      return false;
    }
    // 管理员或课程创建者可以创建课时
    return isAdmin(user) || user.getId().equals(courseCreatorId);
  }

  /**
   * 检查用户是否可以编辑课时
   *
   * @param user 用户对象
   * @param courseCreatorId 课程创建者ID
   * @return 是否可以编辑课时
   */
  public static boolean canEditLesson(User user, UUID courseCreatorId) {
    // 编辑课时权限与创建课时权限相同
    return canCreateLesson(user, courseCreatorId);
  }

  /**
   * 检查用户是否可以删除课时
   *
   * @param user 用户对象
   * @param creatorId 课程创建者ID
   * @return 是否可以删除课时
   */
  public static boolean canDeleteLesson(User user, UUID creatorId) {
    return isAdmin(user) || (isTeacher(user) && user.getId().equals(creatorId));
  }

  /** 检查用户是否可以查看课程的课时列表 */
  public static boolean canViewLessons(User user, UUID courseCreatorId) {
    // 管理员可以查看所有课时，教师可以查看自己创建的课程的课时，学生可以查看已注册课程的课时
    return isAdmin(user) || isTeacher(user) || user.getId().equals(courseCreatorId);
  }

  /** 检查用户是否可以查看特定课时 */
  public static boolean canViewLesson(User user, UUID lessonCreatorId) {
    // 管理员可以查看所有课时，教师可以查看自己创建的课时，学生可以查看已注册课程的课时
    return isAdmin(user) || isTeacher(user) || user.getId().equals(lessonCreatorId);
  }

  /**
   * 检查角色是否有效
   *
   * @param role 角色字符串
   * @return 是否有效
   */
  public static boolean isValidRole(String role) {
    return role != null && Arrays.asList(ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT).contains(role);
  }

  /**
   * 获取权限错误消息
   *
   * @param action 操作名称
   * @return 权限错误消息
   */
  public static String getPermissionDeniedMessage(String action) {
    return String.format("权限不足，无法执行%s操作", action);
  }
}
