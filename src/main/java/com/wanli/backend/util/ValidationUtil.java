package com.wanli.backend.util;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/** 验证工具类 提供统一的输入验证方法 */
public final class ValidationUtil {

  // 常量定义
  private static final int MIN_TITLE_LENGTH = 1;
  private static final int MAX_TITLE_LENGTH = 200;
  private static final int MAX_DESCRIPTION_LENGTH = 2000;
  private static final int MIN_PASSWORD_LENGTH = 6;
  private static final int MAX_PASSWORD_LENGTH = 50;

  // 邮箱正则表达式
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

  // 用户名正则表达式（字母、数字、下划线，3-20位）
  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

  // 有效的课程状态
  private static final List<String> VALID_COURSE_STATUSES =
      Arrays.asList("draft", "published", "archived");

  // 有效的用户角色
  private static final List<String> VALID_USER_ROLES = Arrays.asList("admin", "teacher", "student");

  // 私有构造函数，防止实例化
  private ValidationUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * 验证字符串是否为空或空白
   *
   * @param str 待验证字符串
   * @return 是否为空或空白
   */
  public static boolean isBlank(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * 验证字符串是否不为空且不为空白
   *
   * @param str 待验证字符串
   * @return 是否不为空且不为空白
   */
  public static boolean isNotBlank(String str) {
    return !isBlank(str);
  }

  /**
   * 验证UUID是否有效
   *
   * @param uuid UUID对象
   * @return 是否有效
   */
  public static boolean isValidUUID(UUID uuid) {
    return uuid != null;
  }

  /**
   * 验证UUID字符串是否有效
   *
   * @param uuidStr UUID字符串
   * @return 是否有效
   */
  public static boolean isValidUUID(String uuidStr) {
    if (isBlank(uuidStr)) {
      return false;
    }
    try {
      UUID.fromString(uuidStr);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 验证邮箱格式
   *
   * @param email 邮箱地址
   * @return 是否有效
   */
  public static boolean isValidEmail(String email) {
    return isNotBlank(email) && EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * 验证用户名格式
   *
   * @param username 用户名
   * @return 是否有效
   */
  public static boolean isValidUsername(String username) {
    return isNotBlank(username) && USERNAME_PATTERN.matcher(username).matches();
  }

  /**
   * 验证密码强度
   *
   * @param password 密码
   * @return 是否有效
   */
  public static boolean isValidPassword(String password) {
    return isNotBlank(password)
        && password.length() >= MIN_PASSWORD_LENGTH
        && password.length() <= MAX_PASSWORD_LENGTH;
  }

  /**
   * 验证课程标题
   *
   * @param title 课程标题
   * @return 是否有效
   */
  public static boolean isValidCourseTitle(String title) {
    return isNotBlank(title)
        && title.trim().length() >= MIN_TITLE_LENGTH
        && title.trim().length() <= MAX_TITLE_LENGTH;
  }

  /**
   * 验证课程描述
   *
   * @param description 课程描述
   * @return 是否有效
   */
  public static boolean isValidCourseDescription(String description) {
    // 描述可以为空，但如果不为空则需要检查长度
    return description == null || description.length() <= MAX_DESCRIPTION_LENGTH;
  }

  /**
   * 验证课程状态
   *
   * @param status 课程状态
   * @return 是否有效
   */
  public static boolean isValidCourseStatus(String status) {
    return isNotBlank(status) && VALID_COURSE_STATUSES.contains(status.toLowerCase());
  }

  /**
   * 验证用户角色
   *
   * @param role 用户角色
   * @return 是否有效
   */
  public static boolean isValidUserRole(String role) {
    return isNotBlank(role) && VALID_USER_ROLES.contains(role.toLowerCase());
  }

  /**
   * 验证课程创建请求
   *
   * @param title 课程标题
   * @param description 课程描述
   * @param status 课程状态
   * @return 验证结果消息，null表示验证通过
   */
  public static String validateCourseCreation(String title, String description, String status) {
    if (!isValidCourseTitle(title)) {
      return String.format("课程标题不能为空且长度必须在%d-%d字符之间", MIN_TITLE_LENGTH, MAX_TITLE_LENGTH);
    }

    if (!isValidCourseDescription(description)) {
      return String.format("课程描述长度不能超过%d字符", MAX_DESCRIPTION_LENGTH);
    }

    if (isNotBlank(status) && !isValidCourseStatus(status)) {
      return "课程状态必须是：draft（草稿）、published（已发布）或archived（已归档）";
    }

    return null; // 验证通过
  }

  /**
   * 验证用户注册请求
   *
   * @param username 用户名
   * @param email 邮箱
   * @param password 密码
   * @param role 角色
   * @return 验证结果消息，null表示验证通过
   */
  public static String validateUserRegistration(
      String username, String email, String password, String role) {
    if (!isValidUsername(username)) {
      return "用户名必须是3-20位字母、数字或下划线组合";
    }

    if (!isValidEmail(email)) {
      return "邮箱格式不正确";
    }

    if (!isValidPassword(password)) {
      return String.format("密码长度必须在%d-%d字符之间", MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
    }

    if (!isValidUserRole(role)) {
      return "用户角色必须是：admin（管理员）、teacher（教师）或student（学生）";
    }

    return null; // 验证通过
  }

  /**
   * 获取字符串的安全长度（处理null情况）
   *
   * @param str 字符串
   * @return 长度
   */
  public static int safeLength(String str) {
    return str == null ? 0 : str.length();
  }

  /**
   * 安全地修剪字符串
   *
   * @param str 字符串
   * @return 修剪后的字符串
   */
  public static String safeTrim(String str) {
    return str == null ? null : str.trim();
  }
}
