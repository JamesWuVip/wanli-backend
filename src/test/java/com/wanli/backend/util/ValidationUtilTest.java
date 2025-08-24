package com.wanli.backend.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/** ValidationUtil 单元测试 */
@ExtendWith(MockitoExtension.class)
class ValidationUtilTest {

  @Test
  void testIsBlank() {
    // 测试空白字符串检查
    assertTrue(ValidationUtil.isBlank(null));
    assertTrue(ValidationUtil.isBlank(""));
    assertTrue(ValidationUtil.isBlank(" "));
    assertTrue(ValidationUtil.isBlank("\t"));
    assertTrue(ValidationUtil.isBlank("\n"));
    assertTrue(ValidationUtil.isBlank("  \t\n  "));

    assertFalse(ValidationUtil.isBlank("test"));
    assertFalse(ValidationUtil.isBlank(" test "));
    assertFalse(ValidationUtil.isBlank("a"));
  }

  @Test
  void testIsNotBlank() {
    // 测试非空白字符串检查
    assertFalse(ValidationUtil.isNotBlank(null));
    assertFalse(ValidationUtil.isNotBlank(""));
    assertFalse(ValidationUtil.isNotBlank(" "));
    assertFalse(ValidationUtil.isNotBlank("\t"));
    assertFalse(ValidationUtil.isNotBlank("\n"));
    assertFalse(ValidationUtil.isNotBlank("  \t\n  "));

    assertTrue(ValidationUtil.isNotBlank("test"));
    assertTrue(ValidationUtil.isNotBlank(" test "));
    assertTrue(ValidationUtil.isNotBlank("a"));
  }

  @Test
  void testIsValidUUID() {
    // 测试UUID验证
    UUID validUUID = UUID.randomUUID();
    assertTrue(ValidationUtil.isValidUUID(validUUID));

    try {
      UUID uuid1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
      assertTrue(ValidationUtil.isValidUUID(uuid1));

      UUID uuid2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
      assertTrue(ValidationUtil.isValidUUID(uuid2));
    } catch (IllegalArgumentException e) {
      fail("Valid UUID strings should be parseable");
    }

    assertFalse(ValidationUtil.isValidUUID((String) null));
  }

  @Test
  void testIsValidEmail() {
    // 测试邮箱验证
    assertTrue(ValidationUtil.isValidEmail("test@example.com"));
    assertTrue(ValidationUtil.isValidEmail("user.name@domain.co.uk"));
    assertTrue(ValidationUtil.isValidEmail("user+tag@example.org"));
    assertTrue(ValidationUtil.isValidEmail("123@test.com"));
    assertTrue(ValidationUtil.isValidEmail("a@b.co"));

    assertFalse(ValidationUtil.isValidEmail(null));
    assertFalse(ValidationUtil.isValidEmail(""));
    assertFalse(ValidationUtil.isValidEmail("invalid-email"));
    assertFalse(ValidationUtil.isValidEmail("@example.com"));
    assertFalse(ValidationUtil.isValidEmail("test@"));
    assertFalse(ValidationUtil.isValidEmail("test.example.com"));
    assertFalse(ValidationUtil.isValidEmail("test@.com"));
    assertFalse(ValidationUtil.isValidEmail("test@com"));
    assertFalse(ValidationUtil.isValidEmail("test..test@example.com"));
  }

  @Test
  void testIsValidUsername() {
    // 测试用户名验证
    assertTrue(ValidationUtil.isValidUsername("user123"));
    assertTrue(ValidationUtil.isValidUsername("test_user"));
    assertTrue(ValidationUtil.isValidUsername("user-name"));
    assertTrue(ValidationUtil.isValidUsername("User123"));
    assertTrue(ValidationUtil.isValidUsername("abc"));
    assertTrue(ValidationUtil.isValidUsername("a".repeat(20))); // 20个字符

    assertFalse(ValidationUtil.isValidUsername(null));
    assertFalse(ValidationUtil.isValidUsername(""));
    assertFalse(ValidationUtil.isValidUsername("ab")); // 太短
    assertFalse(ValidationUtil.isValidUsername("a".repeat(21))); // 太长
    assertFalse(ValidationUtil.isValidUsername("user@name")); // 包含@
    assertFalse(ValidationUtil.isValidUsername("user name")); // 包含空格
    assertFalse(ValidationUtil.isValidUsername("user#name")); // 包含特殊字符
    assertFalse(ValidationUtil.isValidUsername("123")); // 纯数字
    assertFalse(ValidationUtil.isValidUsername("_user")); // 以下划线开头
    assertFalse(ValidationUtil.isValidUsername("-user")); // 以连字符开头
  }

  @Test
  void testIsValidPassword() {
    // 测试密码验证
    assertTrue(ValidationUtil.isValidPassword("Password123!"));
    assertTrue(ValidationUtil.isValidPassword("MyPass@123"));
    assertTrue(ValidationUtil.isValidPassword("Secure#Pass1"));
    assertTrue(ValidationUtil.isValidPassword("Test$123"));

    assertFalse(ValidationUtil.isValidPassword(null));
    assertFalse(ValidationUtil.isValidPassword(""));
    assertFalse(ValidationUtil.isValidPassword("12345")); // 太短
    assertFalse(ValidationUtil.isValidPassword("password")); // 没有大写字母和数字
    assertFalse(ValidationUtil.isValidPassword("PASSWORD")); // 没有小写字母和数字
    assertFalse(ValidationUtil.isValidPassword("Password")); // 没有数字
    assertFalse(ValidationUtil.isValidPassword("password123")); // 没有大写字母
    assertFalse(ValidationUtil.isValidPassword("PASSWORD123")); // 没有小写字母
    assertFalse(ValidationUtil.isValidPassword("a".repeat(129))); // 太长
  }

  @Test
  void testIsValidCourseTitle() {
    // 测试课程标题验证
    assertTrue(ValidationUtil.isValidCourseTitle("Java编程基础"));
    assertTrue(ValidationUtil.isValidCourseTitle("Spring Boot实战"));
    assertTrue(ValidationUtil.isValidCourseTitle("数据结构与算法"));
    assertTrue(ValidationUtil.isValidCourseTitle("A"));
    assertTrue(ValidationUtil.isValidCourseTitle("a".repeat(100))); // 100个字符

    assertFalse(ValidationUtil.isValidCourseTitle(null));
    assertFalse(ValidationUtil.isValidCourseTitle(""));
    assertFalse(ValidationUtil.isValidCourseTitle(" "));
    assertFalse(ValidationUtil.isValidCourseTitle("a".repeat(101))); // 太长
  }

  @Test
  void testIsValidCourseDescription() {
    // 测试课程描述验证
    assertTrue(ValidationUtil.isValidCourseDescription("这是一个很好的课程"));
    assertTrue(ValidationUtil.isValidCourseDescription("Course description"));
    assertTrue(ValidationUtil.isValidCourseDescription("a".repeat(1000))); // 1000个字符

    assertFalse(ValidationUtil.isValidCourseDescription(null));
    assertFalse(ValidationUtil.isValidCourseDescription(""));
    assertFalse(ValidationUtil.isValidCourseDescription(" "));
    assertFalse(ValidationUtil.isValidCourseDescription("a".repeat(1001))); // 太长
  }

  @Test
  void testIsValidCourseStatus() {
    // 测试课程状态验证
    assertTrue(ValidationUtil.isValidCourseStatus("DRAFT"));
    assertTrue(ValidationUtil.isValidCourseStatus("PUBLISHED"));
    assertTrue(ValidationUtil.isValidCourseStatus("ARCHIVED"));

    assertFalse(ValidationUtil.isValidCourseStatus(null));
    assertFalse(ValidationUtil.isValidCourseStatus(""));
    assertFalse(ValidationUtil.isValidCourseStatus("INVALID"));
    assertFalse(ValidationUtil.isValidCourseStatus("draft")); // 小写
    assertFalse(ValidationUtil.isValidCourseStatus("Published")); // 混合大小写
  }

  @Test
  void testIsValidUserRole() {
    // 测试用户角色验证
    assertTrue(ValidationUtil.isValidUserRole("STUDENT"));
    assertTrue(ValidationUtil.isValidUserRole("TEACHER"));
    assertTrue(ValidationUtil.isValidUserRole("ADMIN"));

    assertFalse(ValidationUtil.isValidUserRole(null));
    assertFalse(ValidationUtil.isValidUserRole(""));
    assertFalse(ValidationUtil.isValidUserRole("INVALID"));
    assertFalse(ValidationUtil.isValidUserRole("student")); // 小写
    assertFalse(ValidationUtil.isValidUserRole("Teacher")); // 混合大小写
  }

  @Test
  void testValidateCourseCreation() {
    // 测试课程创建验证
    String validTitle = "Java编程基础";
    String validDescription = "这是一个Java编程基础课程";
    String validCreatorId = UUID.randomUUID().toString();

    // 有效的课程创建数据
    String result = ValidationUtil.validateCourseCreation(validTitle, validDescription, "DRAFT");
    assertNull(result);

    // 无效的标题
    String emptyTitleResult = ValidationUtil.validateCourseCreation("", validDescription, "DRAFT");
    assertNotNull(emptyTitleResult);
    assertTrue(emptyTitleResult.contains("标题"));

    String nullTitleResult = ValidationUtil.validateCourseCreation(null, validDescription, "DRAFT");
    assertNotNull(nullTitleResult);
    assertTrue(nullTitleResult.contains("标题"));

    // 无效的描述 - 空描述实际上是允许的，所以测试超长描述
    String longDescription = "a".repeat(2001);
    String longDescResult =
        ValidationUtil.validateCourseCreation(validTitle, longDescription, "DRAFT");
    assertNotNull(longDescResult);
    assertTrue(longDescResult.contains("描述"));

    // null描述是允许的
    String nullDescResult = ValidationUtil.validateCourseCreation(validTitle, null, "DRAFT");
    assertNull(nullDescResult);

    // 无效的状态
    String invalidStatusResult =
        ValidationUtil.validateCourseCreation(validTitle, validDescription, "INVALID");
    assertNotNull(invalidStatusResult);
    assertTrue(invalidStatusResult.contains("状态"));

    String nullStatusResult =
        ValidationUtil.validateCourseCreation(validTitle, validDescription, null);
    assertNotNull(nullStatusResult);
    assertTrue(nullStatusResult.contains("状态"));
  }

  @Test
  void testValidateUserRegistration() {
    // 测试用户注册验证
    String validUsername = "testuser";
    String validEmail = "test@example.com";
    String validPassword = "Password123!";
    String validRole = "student";

    // 有效的用户注册数据
    String result =
        ValidationUtil.validateUserRegistration(
            validUsername, validEmail, validPassword, validRole);
    assertNull(result);

    // 无效的用户名
    String invalidUsernameResult =
        ValidationUtil.validateUserRegistration("ab", validEmail, validPassword, validRole);
    assertNotNull(invalidUsernameResult);
    assertTrue(invalidUsernameResult.contains("用户名"));

    String nullUsernameResult =
        ValidationUtil.validateUserRegistration(null, validEmail, validPassword, validRole);
    assertNotNull(nullUsernameResult);

    // 无效的邮箱
    String invalidEmailResult =
        ValidationUtil.validateUserRegistration(
            validUsername, "invalid-email", validPassword, validRole);
    assertNotNull(invalidEmailResult);
    assertTrue(invalidEmailResult.contains("邮箱"));

    String nullEmailResult =
        ValidationUtil.validateUserRegistration(validUsername, null, validPassword, validRole);
    assertNotNull(nullEmailResult);

    // 无效的密码
    String weakPasswordResult =
        ValidationUtil.validateUserRegistration(validUsername, validEmail, "weak", validRole);
    assertNotNull(weakPasswordResult);
    assertTrue(weakPasswordResult.contains("密码"));

    String nullPasswordResult =
        ValidationUtil.validateUserRegistration(validUsername, validEmail, null, validRole);
    assertNotNull(nullPasswordResult);
  }

  @Test
  void testSafeLength() {
    // 测试安全长度检查
    assertEquals(0, ValidationUtil.safeLength(null));
    assertEquals(0, ValidationUtil.safeLength(""));
    assertEquals(5, ValidationUtil.safeLength("hello"));
    assertEquals(10, ValidationUtil.safeLength("hello world"));
  }

  @Test
  void testSafeTrim() {
    // 测试安全trim操作
    assertNull(ValidationUtil.safeTrim(null));
    assertEquals("", ValidationUtil.safeTrim(""));
    assertEquals("hello", ValidationUtil.safeTrim("  hello  "));
    assertEquals("hello world", ValidationUtil.safeTrim("\thello world\n"));
    assertEquals("test", ValidationUtil.safeTrim("test"));
  }

  @Test
  void testEdgeCases() {
    // 测试边界情况

    // 测试最大长度的用户名
    String maxUsername = "a".repeat(20);
    assertTrue(ValidationUtil.isValidUsername(maxUsername));

    // 测试最大长度的课程标题
    String maxTitle = "a".repeat(100);
    assertTrue(ValidationUtil.isValidCourseTitle(maxTitle));

    // 测试最大长度的课程描述
    String maxDescription = "a".repeat(1000);
    assertTrue(ValidationUtil.isValidCourseDescription(maxDescription));

    // 测试最短有效密码
    String minPassword = "Aa1!";
    assertFalse(ValidationUtil.isValidPassword(minPassword)); // 仍然太短

    String validMinPassword = "Aa1!ab";
    assertTrue(ValidationUtil.isValidPassword(validMinPassword));
  }

  @Test
  void testSpecialCharacters() {
    // 测试特殊字符处理

    // 课程标题中的特殊字符
    assertTrue(ValidationUtil.isValidCourseTitle("C++编程"));
    assertTrue(ValidationUtil.isValidCourseTitle("Java & Spring"));
    assertTrue(ValidationUtil.isValidCourseTitle("数据结构(第二版)"));

    // 课程描述中的特殊字符
    assertTrue(ValidationUtil.isValidCourseDescription("这是一个关于C++的课程，包含指针&引用等内容。"));

    // 邮箱中的特殊字符
    assertTrue(ValidationUtil.isValidEmail("user+tag@example.com"));
    assertTrue(ValidationUtil.isValidEmail("user.name@example.com"));

    // 用户名中不允许的特殊字符
    assertFalse(ValidationUtil.isValidUsername("user@name"));
    assertFalse(ValidationUtil.isValidUsername("user name"));
    assertFalse(ValidationUtil.isValidUsername("user#name"));
  }
}
