package com.wanli.backend.util;

import java.util.Collection;
import java.util.UUID;

import com.wanli.backend.exception.BusinessException;

/** Service层验证工具类 统一处理Service层的输入验证和业务规则验证 */
public class ServiceValidationUtil {

  /**
   * 验证字符串不为空
   *
   * @param value 待验证的值
   * @param fieldName 字段名称
   * @throws BusinessException 如果值为空
   */
  public static void validateNotBlank(String value, String fieldName) {
    if (ValidationUtil.isBlank(value)) {
      throw BusinessException.badRequest(fieldName + "不能为空");
    }
  }

  /**
   * 验证对象不为null
   *
   * @param value 待验证的值
   * @param fieldName 字段名称
   * @throws BusinessException 如果值为null
   */
  public static void validateNotNull(Object value, String fieldName) {
    if (value == null) {
      throw BusinessException.badRequest(fieldName + "不能为空");
    }
  }

  /**
   * 验证集合不为空
   *
   * @param collection 待验证的集合
   * @param fieldName 字段名称
   * @throws BusinessException 如果集合为null或空
   */
  public static void validateNotEmpty(Collection<?> collection, String fieldName) {
    if (collection == null || collection.isEmpty()) {
      throw BusinessException.badRequest(fieldName + "不能为空");
    }
  }

  /**
   * 验证UUID格式
   *
   * @param uuidStr UUID字符串
   * @param fieldName 字段名称
   * @throws BusinessException 如果UUID格式无效
   */
  public static void validateUUIDFormat(String uuidStr, String fieldName) {
    try {
      UUID.fromString(uuidStr);
    } catch (IllegalArgumentException e) {
      throw BusinessException.badRequest("无效的" + fieldName + "格式");
    }
  }

  /**
   * 验证UUID不为null
   *
   * @param uuid UUID对象
   * @param fieldName 字段名称
   * @throws BusinessException 如果UUID为null
   */
  public static void validateUUIDNotNull(UUID uuid, String fieldName) {
    if (uuid == null) {
      throw BusinessException.badRequest(fieldName + "不能为空");
    }
  }

  /**
   * 验证邮箱格式
   *
   * @param email 邮箱地址
   * @throws BusinessException 如果邮箱格式无效
   */
  public static void validateEmail(String email) {
    if (!ValidationUtil.isValidEmail(email)) {
      throw BusinessException.badRequest("邮箱格式不正确");
    }
  }

  /**
   * 验证用户名格式
   *
   * @param username 用户名
   * @throws BusinessException 如果用户名格式无效
   */
  public static void validateUsername(String username) {
    if (!ValidationUtil.isValidUsername(username)) {
      throw BusinessException.badRequest("用户名格式不正确");
    }
  }

  /**
   * 验证密码格式
   *
   * @param password 密码
   * @throws BusinessException 如果密码格式无效
   */
  public static void validatePassword(String password) {
    if (!ValidationUtil.isValidPassword(password)) {
      throw BusinessException.badRequest("密码格式不正确");
    }
  }

  /**
   * 验证课程标题
   *
   * @param title 课程标题
   * @throws BusinessException 如果标题格式无效
   */
  public static void validateCourseTitle(String title) {
    if (!ValidationUtil.isValidCourseTitle(title)) {
      throw BusinessException.badRequest("课程标题格式不正确");
    }
  }

  /**
   * 验证课程描述
   *
   * @param description 课程描述
   * @throws BusinessException 如果描述格式无效
   */
  public static void validateCourseDescription(String description) {
    if (!ValidationUtil.isValidCourseDescription(description)) {
      throw BusinessException.badRequest("课程描述格式不正确");
    }
  }

  /**
   * 验证课程状态
   *
   * @param status 课程状态
   * @throws BusinessException 如果状态无效
   */
  public static void validateCourseStatus(String status) {
    if (!ValidationUtil.isValidCourseStatus(status)) {
      throw BusinessException.badRequest("课程状态无效");
    }
  }

  /**
   * 验证用户角色
   *
   * @param role 用户角色
   * @throws BusinessException 如果角色无效
   */
  public static void validateUserRole(String role) {
    if (!ValidationUtil.isValidUserRole(role)) {
      throw BusinessException.badRequest("用户角色无效");
    }
  }

  /**
   * 验证数值范围
   *
   * @param value 数值
   * @param min 最小值
   * @param max 最大值
   * @param fieldName 字段名称
   * @throws BusinessException 如果数值超出范围
   */
  public static void validateRange(Integer value, int min, int max, String fieldName) {
    if (value != null && (value < min || value > max)) {
      throw BusinessException.badRequest(fieldName + "必须在" + min + "到" + max + "之间");
    }
  }

  /**
   * 验证字符串长度
   *
   * @param value 字符串值
   * @param maxLength 最大长度
   * @param fieldName 字段名称
   * @throws BusinessException 如果长度超出限制
   */
  public static void validateMaxLength(String value, int maxLength, String fieldName) {
    if (value != null && value.length() > maxLength) {
      throw BusinessException.badRequest(fieldName + "长度不能超过" + maxLength + "个字符");
    }
  }

  /**
   * 验证正整数
   *
   * @param value 数值
   * @param fieldName 字段名称
   * @throws BusinessException 如果不是正整数
   */
  public static void validatePositiveInteger(Integer value, String fieldName) {
    if (value != null && value <= 0) {
      throw BusinessException.badRequest(fieldName + "必须是正整数");
    }
  }

  /**
   * 验证非负整数
   *
   * @param value 数值
   * @param fieldName 字段名称
   * @throws BusinessException 如果是负数
   */
  public static void validateNonNegativeInteger(Integer value, String fieldName) {
    if (value != null && value < 0) {
      throw BusinessException.badRequest(fieldName + "不能为负数");
    }
  }

  /**
   * 组合验证：课程创建
   *
   * @param title 课程标题
   * @param description 课程描述
   * @param status 课程状态
   * @param creatorId 创建者ID
   */
  public static void validateCourseCreation(
      String title, String description, String status, UUID creatorId) {
    validateNotBlank(title, "课程标题");
    validateCourseTitle(title);
    validateNotBlank(description, "课程描述");
    validateCourseDescription(description);
    validateNotBlank(status, "课程状态");
    validateCourseStatus(status);
    validateUUIDNotNull(creatorId, "创建者ID");
  }

  /**
   * 组合验证：用户注册
   *
   * @param username 用户名
   * @param email 邮箱
   * @param password 密码
   * @param role 角色
   */
  public static void validateUserRegistration(
      String username, String email, String password, String role) {
    validateNotBlank(username, "用户名");
    validateUsername(username);
    validateNotBlank(email, "邮箱");
    validateEmail(email);
    validateNotBlank(password, "密码");
    validatePassword(password);
    if (role != null) {
      validateUserRole(role);
    }
  }

  /**
   * 组合验证：课时创建
   *
   * @param title 课时标题
   * @param courseId 课程ID
   * @param orderIndex 排序索引
   * @param description 课时描述
   * @param duration 时长
   * @param status 状态
   */
  public static void validateLessonCreation(
      String title,
      String courseId,
      Integer orderIndex,
      String description,
      Integer duration,
      String status) {
    validateNotBlank(title, "课时标题");
    validateMaxLength(title, 200, "课时标题");
    validateNotBlank(courseId, "课程ID");
    validateUUIDFormat(courseId, "课程ID");
    validateNotNull(orderIndex, "排序索引");
    validatePositiveInteger(orderIndex, "排序索引");
    validateMaxLength(description, 1000, "课时描述");
    validateNonNegativeInteger(duration, "课时时长");
    if (status != null && !status.matches("^(DRAFT|PUBLISHED|ARCHIVED)$")) {
      throw BusinessException.badRequest("课时状态必须是DRAFT、PUBLISHED或ARCHIVED之一");
    }
  }
}
