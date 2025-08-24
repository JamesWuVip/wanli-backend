package com.wanli.backend.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wanli.backend.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class ServiceValidationUtilTest {

  @Test
  void testValidateNotBlank_ValidString() {
    // Given
    String validString = "test";

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validateNotBlank(validString, "测试字段"));
  }

  @Test
  void testValidateNotBlank_BlankString() {
    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      // Given
      mockedValidationUtil.when(() -> ValidationUtil.isBlank("  ")).thenReturn(true);

      // When & Then
      BusinessException exception =
          assertThrows(
              BusinessException.class, () -> ServiceValidationUtil.validateNotBlank("  ", "测试字段"));
      assertEquals("测试字段不能为空", exception.getMessage());
    }
  }

  @Test
  void testValidateNotNull_ValidObject() {
    // Given
    Object validObject = new Object();

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validateNotNull(validObject, "测试对象"));
  }

  @Test
  void testValidateNotNull_NullObject() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> ServiceValidationUtil.validateNotNull(null, "测试对象"));
    assertEquals("测试对象不能为空", exception.getMessage());
  }

  @Test
  void testValidateNotEmpty_ValidCollection() {
    // Given
    List<String> validList = Arrays.asList("item1", "item2");

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validateNotEmpty(validList, "测试列表"));
  }

  @Test
  void testValidateNotEmpty_EmptyCollection() {
    // Given
    List<String> emptyList = new ArrayList<>();

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> ServiceValidationUtil.validateNotEmpty(emptyList, "测试列表"));
    assertEquals("测试列表不能为空", exception.getMessage());
  }

  @Test
  void testValidateNotEmpty_NullCollection() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> ServiceValidationUtil.validateNotEmpty(null, "测试列表"));
    assertEquals("测试列表不能为空", exception.getMessage());
  }

  @Test
  void testValidateUUIDFormat_ValidUUID() {
    // Given
    String validUUID = UUID.randomUUID().toString();

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validateUUIDFormat(validUUID, "用户ID"));
  }

  @Test
  void testValidateUUIDFormat_InvalidUUID() {
    // Given
    String invalidUUID = "invalid-uuid";

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> ServiceValidationUtil.validateUUIDFormat(invalidUUID, "用户ID"));
    assertEquals("无效的用户ID格式", exception.getMessage());
  }

  @Test
  void testValidateUUIDNotNull_ValidUUID() {
    // Given
    UUID validUUID = UUID.randomUUID();

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validateUUIDNotNull(validUUID, "用户ID"));
  }

  @Test
  void testValidateUUIDNotNull_NullUUID() {
    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> ServiceValidationUtil.validateUUIDNotNull(null, "用户ID"));
    assertEquals("用户ID不能为空", exception.getMessage());
  }

  @Test
  void testValidateEmail_ValidEmail() {
    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      // Given
      String validEmail = "test@example.com";
      mockedValidationUtil.when(() -> ValidationUtil.isValidEmail(validEmail)).thenReturn(true);

      // When & Then - 不应该抛出异常
      assertDoesNotThrow(() -> ServiceValidationUtil.validateEmail(validEmail));
    }
  }

  @Test
  void testValidateEmail_InvalidEmail() {
    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      // Given
      String invalidEmail = "invalid-email";
      mockedValidationUtil.when(() -> ValidationUtil.isValidEmail(invalidEmail)).thenReturn(false);

      // When & Then
      BusinessException exception =
          assertThrows(
              BusinessException.class, () -> ServiceValidationUtil.validateEmail(invalidEmail));
      assertEquals("邮箱格式不正确", exception.getMessage());
    }
  }

  @Test
  void testValidateUsername_ValidUsername() {
    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      // Given
      String validUsername = "testuser";
      mockedValidationUtil
          .when(() -> ValidationUtil.isValidUsername(validUsername))
          .thenReturn(true);

      // When & Then - 不应该抛出异常
      assertDoesNotThrow(() -> ServiceValidationUtil.validateUsername(validUsername));
    }
  }

  @Test
  void testValidateUsername_InvalidUsername() {
    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      // Given
      String invalidUsername = "invalid user";
      mockedValidationUtil
          .when(() -> ValidationUtil.isValidUsername(invalidUsername))
          .thenReturn(false);

      // When & Then
      BusinessException exception =
          assertThrows(
              BusinessException.class,
              () -> ServiceValidationUtil.validateUsername(invalidUsername));
      assertEquals("用户名格式不正确", exception.getMessage());
    }
  }

  @Test
  void testValidatePassword_ValidPassword() {
    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      // Given
      String validPassword = "ValidPass123!";
      mockedValidationUtil
          .when(() -> ValidationUtil.isValidPassword(validPassword))
          .thenReturn(true);

      // When & Then - 不应该抛出异常
      assertDoesNotThrow(() -> ServiceValidationUtil.validatePassword(validPassword));
    }
  }

  @Test
  void testValidatePassword_InvalidPassword() {
    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      // Given
      String invalidPassword = "123";
      mockedValidationUtil
          .when(() -> ValidationUtil.isValidPassword(invalidPassword))
          .thenReturn(false);

      // When & Then
      BusinessException exception =
          assertThrows(
              BusinessException.class,
              () -> ServiceValidationUtil.validatePassword(invalidPassword));
      assertEquals("密码格式不正确", exception.getMessage());
    }
  }

  @Test
  void testValidateRange_ValidValue() {
    // Given
    Integer validValue = 5;

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validateRange(validValue, 1, 10, "测试数值"));
  }

  @Test
  void testValidateRange_ValueTooSmall() {
    // Given
    Integer smallValue = 0;

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> ServiceValidationUtil.validateRange(smallValue, 1, 10, "测试数值"));
    assertEquals("测试数值必须在1到10之间", exception.getMessage());
  }

  @Test
  void testValidateRange_ValueTooLarge() {
    // Given
    Integer largeValue = 15;

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> ServiceValidationUtil.validateRange(largeValue, 1, 10, "测试数值"));
    assertEquals("测试数值必须在1到10之间", exception.getMessage());
  }

  @Test
  void testValidateRange_NullValue() {
    // When & Then - null值应该被忽略，不抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validateRange(null, 1, 10, "测试数值"));
  }

  @Test
  void testValidateMaxLength_ValidLength() {
    // Given
    String validString = "test";

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validateMaxLength(validString, 10, "测试字符串"));
  }

  @Test
  void testValidateMaxLength_TooLong() {
    // Given
    String longString = "this is a very long string";

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> ServiceValidationUtil.validateMaxLength(longString, 10, "测试字符串"));
    assertEquals("测试字符串长度不能超过10个字符", exception.getMessage());
  }

  @Test
  void testValidatePositiveInteger_ValidValue() {
    // Given
    Integer positiveValue = 5;

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(() -> ServiceValidationUtil.validatePositiveInteger(positiveValue, "测试数值"));
  }

  @Test
  void testValidatePositiveInteger_ZeroValue() {
    // Given
    Integer zeroValue = 0;

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> ServiceValidationUtil.validatePositiveInteger(zeroValue, "测试数值"));
    assertEquals("测试数值必须是正整数", exception.getMessage());
  }

  @Test
  void testValidateNonNegativeInteger_ValidValue() {
    // Given
    Integer nonNegativeValue = 0;

    // When & Then - 不应该抛出异常
    assertDoesNotThrow(
        () -> ServiceValidationUtil.validateNonNegativeInteger(nonNegativeValue, "测试数值"));
  }

  @Test
  void testValidateNonNegativeInteger_NegativeValue() {
    // Given
    Integer negativeValue = -1;

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> ServiceValidationUtil.validateNonNegativeInteger(negativeValue, "测试数值"));
    assertEquals("测试数值不能为负数", exception.getMessage());
  }

  @Test
  void testValidateLessonCreation_ValidInput() {
    // Given
    String title = "测试课时";
    String courseId = UUID.randomUUID().toString();
    Integer orderIndex = 1;
    String description = "测试描述";
    Integer duration = 60;
    String status = "PUBLISHED";

    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      mockedValidationUtil.when(() -> ValidationUtil.isBlank(title)).thenReturn(false);
      mockedValidationUtil.when(() -> ValidationUtil.isBlank(courseId)).thenReturn(false);

      // When & Then - 不应该抛出异常
      assertDoesNotThrow(
          () ->
              ServiceValidationUtil.validateLessonCreation(
                  title, courseId, orderIndex, description, duration, status));
    }
  }

  @Test
  void testValidateLessonCreation_InvalidStatus() {
    // Given
    String title = "测试课时";
    String courseId = UUID.randomUUID().toString();
    Integer orderIndex = 1;
    String description = "测试描述";
    Integer duration = 60;
    String invalidStatus = "INVALID_STATUS";

    try (MockedStatic<ValidationUtil> mockedValidationUtil = mockStatic(ValidationUtil.class)) {
      mockedValidationUtil.when(() -> ValidationUtil.isBlank(title)).thenReturn(false);
      mockedValidationUtil.when(() -> ValidationUtil.isBlank(courseId)).thenReturn(false);

      // When & Then
      BusinessException exception =
          assertThrows(
              BusinessException.class,
              () ->
                  ServiceValidationUtil.validateLessonCreation(
                      title, courseId, orderIndex, description, duration, invalidStatus));
      assertEquals("课时状态必须是DRAFT、PUBLISHED或ARCHIVED之一", exception.getMessage());
    }
  }
}
