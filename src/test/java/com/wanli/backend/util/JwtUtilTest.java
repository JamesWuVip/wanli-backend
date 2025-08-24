package com.wanli.backend.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

  @InjectMocks private JwtUtil jwtUtil;

  private UUID testUserId;
  private String testUsername;
  private String testRole;
  private String validToken;

  @BeforeEach
  void setUp() {
    // 设置测试用的JWT配置
    ReflectionTestUtils.setField(
        jwtUtil, "jwtSecret", "test-secret-key-for-jwt-token-generation-and-validation-testing");
    ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400000L); // 24小时

    testUserId = UUID.randomUUID();
    testUsername = "testuser";
    testRole = "USER";

    // 生成一个有效的测试令牌
    validToken = jwtUtil.generateToken(testUserId, testUsername, testRole);
  }

  @Test
  void testGenerateToken() {
    // When
    String token = jwtUtil.generateToken(testUserId, testUsername, testRole);

    // Then
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains("."));
  }

  @Test
  void testGetUserIdFromToken() {
    // When
    UUID extractedUserId = jwtUtil.getUserIdFromToken(validToken);

    // Then
    assertEquals(testUserId, extractedUserId);
  }

  @Test
  void testGetUsernameFromToken() {
    // When
    String extractedUsername = jwtUtil.getUsernameFromToken(validToken);

    // Then
    assertEquals(testUsername, extractedUsername);
  }

  @Test
  void testGetRoleFromToken() {
    // When
    String extractedRole = jwtUtil.getRoleFromToken(validToken);

    // Then
    assertEquals(testRole, extractedRole);
  }

  @Test
  void testGetExpirationDateFromToken() {
    // When
    Date expirationDate = jwtUtil.getExpirationDateFromToken(validToken);

    // Then
    assertNotNull(expirationDate);
    assertTrue(expirationDate.after(new Date()));
  }

  @Test
  void testValidateToken_ValidToken() {
    // When
    boolean isValid = jwtUtil.validateToken(validToken);

    // Then
    assertTrue(isValid);
  }

  @Test
  void testValidateToken_InvalidToken() {
    // Given
    String invalidToken = "invalid.token.here";

    // When
    boolean isValid = jwtUtil.validateToken(invalidToken);

    // Then
    assertFalse(isValid);
  }

  @Test
  void testValidateToken_NullToken() {
    // When
    boolean isValid = jwtUtil.validateToken(null);

    // Then
    assertFalse(isValid);
  }

  @Test
  void testIsTokenExpired_ValidToken() {
    // When
    boolean isExpired = jwtUtil.isTokenExpired(validToken);

    // Then
    assertFalse(isExpired);
  }

  @Test
  void testIsTokenExpired_ExpiredToken() {
    // Given - 创建一个已过期的令牌
    ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1000L); // 负数表示已过期
    String expiredToken = jwtUtil.generateToken(testUserId, testUsername, testRole);

    // 恢复正常的过期时间
    ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400000L);

    // When
    boolean isExpired = jwtUtil.isTokenExpired(expiredToken);

    // Then
    assertTrue(isExpired);
  }

  @Test
  void testIsTokenExpired_InvalidToken() {
    // Given
    String invalidToken = "invalid.token.here";

    // When
    boolean isExpired = jwtUtil.isTokenExpired(invalidToken);

    // Then
    assertTrue(isExpired); // 无效令牌被视为已过期
  }

  @Test
  void testExtractUserId_ValidAuthHeader() {
    // Given
    String authHeader = "Bearer " + validToken;

    // When
    UUID extractedUserId = jwtUtil.extractUserId(authHeader);

    // Then
    assertEquals(testUserId, extractedUserId);
  }

  @Test
  void testExtractUserId_InvalidAuthHeaderFormat() {
    // Given
    String invalidAuthHeader = "InvalidFormat " + validToken;

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> jwtUtil.extractUserId(invalidAuthHeader));
    assertEquals("无效的Authorization头格式", exception.getMessage());
  }

  @Test
  void testExtractUserId_NullAuthHeader() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.extractUserId(null));
    assertEquals("无效的Authorization头格式", exception.getMessage());
  }

  @Test
  void testExtractUserId_InvalidToken() {
    // Given
    String authHeaderWithInvalidToken = "Bearer invalid.token.here";

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> jwtUtil.extractUserId(authHeaderWithInvalidToken));
    assertEquals("无效的JWT令牌", exception.getMessage());
  }

  @Test
  void testExtractUserId_EmptyBearerToken() {
    // Given
    String authHeaderWithEmptyToken = "Bearer ";

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> jwtUtil.extractUserId(authHeaderWithEmptyToken));
    assertEquals("无效的JWT令牌", exception.getMessage());
  }
}
