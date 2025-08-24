package com.wanli.backend.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthUtilTest {

  @Mock private JwtUtil jwtUtil;

  @InjectMocks private AuthUtil authUtil;

  private UUID testUserId;
  private String validToken;
  private String validAuthHeader;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    validAuthHeader = "Bearer " + validToken;
  }

  @Test
  void testExtractTokenFromHeader_ValidHeader() {
    // When
    String extractedToken = AuthUtil.extractTokenFromHeader(validAuthHeader);

    // Then
    assertEquals(validToken, extractedToken);
  }

  @Test
  void testExtractTokenFromHeader_InvalidPrefix() {
    // Given
    String invalidHeader = "Basic " + validToken;

    // When
    String extractedToken = AuthUtil.extractTokenFromHeader(invalidHeader);

    // Then
    assertNull(extractedToken);
  }

  @Test
  void testExtractTokenFromHeader_NullHeader() {
    // When
    String extractedToken = AuthUtil.extractTokenFromHeader(null);

    // Then
    assertNull(extractedToken);
  }

  @Test
  void testExtractTokenFromHeader_EmptyHeader() {
    // When
    String extractedToken = AuthUtil.extractTokenFromHeader("");

    // Then
    assertNull(extractedToken);
  }

  @Test
  void testValidateTokenAndGetUserId_ValidToken() {
    // Given
    when(jwtUtil.getUserIdFromToken(validToken)).thenReturn(testUserId);

    // When
    UUID result = authUtil.validateTokenAndGetUserId(validAuthHeader);

    // Then
    assertEquals(testUserId, result);
    verify(jwtUtil).getUserIdFromToken(validToken);
  }

  @Test
  void testValidateTokenAndGetUserId_InvalidHeaderFormat() {
    // Given
    String invalidHeader = "Invalid " + validToken;

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authUtil.validateTokenAndGetUserId(invalidHeader));
    assertEquals("认证失败：无效的token格式", exception.getMessage());
  }

  @Test
  void testValidateTokenAndGetUserId_InvalidToken() {
    // Given
    when(jwtUtil.getUserIdFromToken(validToken)).thenThrow(new RuntimeException("Invalid token"));

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> authUtil.validateTokenAndGetUserId(validAuthHeader));
    assertEquals("认证失败：无效的token", exception.getMessage());
  }

  @Test
  void testValidateTokenAndGetUserIdSafely_ValidToken() {
    // Given
    when(jwtUtil.getUserIdFromToken(validToken)).thenReturn(testUserId);

    // When
    UUID result = authUtil.validateTokenAndGetUserIdSafely(validAuthHeader);

    // Then
    assertEquals(testUserId, result);
  }

  @Test
  void testValidateTokenAndGetUserIdSafely_InvalidToken() {
    // Given
    when(jwtUtil.getUserIdFromToken(validToken)).thenThrow(new RuntimeException("Invalid token"));

    // When
    UUID result = authUtil.validateTokenAndGetUserIdSafely(validAuthHeader);

    // Then
    assertNull(result);
  }

  @Test
  void testIsValidAuthHeader_ValidHeader() {
    // When
    boolean isValid = AuthUtil.isValidAuthHeader(validAuthHeader);

    // Then
    assertTrue(isValid);
  }

  @Test
  void testIsValidAuthHeader_InvalidPrefix() {
    // Given
    String invalidHeader = "Basic " + validToken;

    // When
    boolean isValid = AuthUtil.isValidAuthHeader(invalidHeader);

    // Then
    assertFalse(isValid);
  }

  @Test
  void testIsValidAuthHeader_NullHeader() {
    // When
    boolean isValid = AuthUtil.isValidAuthHeader(null);

    // Then
    assertFalse(isValid);
  }

  @Test
  void testIsValidAuthHeader_OnlyBearerPrefix() {
    // Given
    String headerWithOnlyPrefix = "Bearer ";

    // When
    boolean isValid = AuthUtil.isValidAuthHeader(headerWithOnlyPrefix);

    // Then
    assertFalse(isValid);
  }

  @Test
  void testIsValidTokenFormat_ValidToken() {
    // When
    boolean isValid = AuthUtil.isValidTokenFormat(validToken);

    // Then
    assertTrue(isValid);
  }

  @Test
  void testIsValidTokenFormat_NullToken() {
    // When
    boolean isValid = AuthUtil.isValidTokenFormat(null);

    // Then
    assertFalse(isValid);
  }

  @Test
  void testIsValidTokenFormat_EmptyToken() {
    // When
    boolean isValid = AuthUtil.isValidTokenFormat("");

    // Then
    assertFalse(isValid);
  }

  @Test
  void testIsValidTokenFormat_WhitespaceToken() {
    // When
    boolean isValid = AuthUtil.isValidTokenFormat("   ");

    // Then
    assertFalse(isValid);
  }

  @Test
  void testCreateBearerHeader_ValidToken() {
    // When
    String bearerHeader = AuthUtil.createBearerHeader(validToken);

    // Then
    assertEquals("Bearer " + validToken, bearerHeader);
  }

  @Test
  void testCreateBearerHeader_NullToken() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> AuthUtil.createBearerHeader(null));
    assertEquals("Token不能为空", exception.getMessage());
  }

  @Test
  void testCreateBearerHeader_EmptyToken() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> AuthUtil.createBearerHeader(""));
    assertEquals("Token不能为空", exception.getMessage());
  }

  @Test
  void testHasPermission_ValidParameters() {
    // When
    boolean hasPermission = AuthUtil.hasPermission(testUserId, "USER");

    // Then
    assertTrue(hasPermission); // 当前实现总是返回true
  }

  @Test
  void testHasPermission_NullUserId() {
    // When
    boolean hasPermission = AuthUtil.hasPermission(null, "USER");

    // Then
    assertFalse(hasPermission);
  }

  @Test
  void testHasPermission_NullRole() {
    // When
    boolean hasPermission = AuthUtil.hasPermission(testUserId, null);

    // Then
    assertFalse(hasPermission);
  }

  @Test
  void testIsAdmin() {
    // When
    boolean isAdmin = AuthUtil.isAdmin(testUserId);

    // Then
    assertTrue(isAdmin); // 当前实现总是返回true
  }

  @Test
  void testIsTeacher() {
    // When
    boolean isTeacher = AuthUtil.isTeacher(testUserId);

    // Then
    assertTrue(isTeacher); // 当前实现总是返回true
  }
}
