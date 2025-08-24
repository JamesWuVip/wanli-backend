package com.wanli.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wanli.backend.entity.User;
import com.wanli.backend.exception.BusinessException;
import com.wanli.backend.repository.UserRepository;
import com.wanli.backend.util.CacheUtil;
import com.wanli.backend.util.ConfigUtil;
import com.wanli.backend.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtUtil jwtUtil;

  @Mock private CacheUtil cacheUtil;

  @Mock private ConfigUtil configUtil;

  @InjectMocks private AuthService authService;

  private User testUser;
  private UUID testUserId;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testUser = new User();
    testUser.setId(testUserId);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("encodedPassword");
    testUser.setRole("student");
    testUser.setFranchiseId(UUID.randomUUID());
  }

  @Test
  void testRegister_Success() {
    // Arrange
    String username = "testuser";
    String password = "password123";
    String email = "test@example.com";
    String role = "student";

    User savedUser = new User();
    savedUser.setId(UUID.randomUUID());
    savedUser.setUsername(username);
    savedUser.setEmail(email);
    savedUser.setRole(role);

    when(configUtil.getPasswordMinLength()).thenReturn(8);
    when(userRepository.existsByUsername(username)).thenReturn(false);
    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(configUtil.getCacheDefaultExpireMinutes()).thenReturn(60);
    when(jwtUtil.generateToken(any(UUID.class), eq(username), eq(role)))
        .thenReturn("mock-jwt-token");

    // Act
    Map<String, Object> result = authService.register(username, password, email, role);

    // Assert
    assertNotNull(result);
    assertEquals(true, result.get("success"));
    verify(userRepository).save(any(User.class));
    verify(cacheUtil).put(anyString(), any(User.class), eq(60));
  }

  @Test
  void testRegister_UsernameExists() {
    // Arrange
    String username = "existinguser";
    String password = "password123";
    String email = "test@example.com";
    String role = "student";

    when(configUtil.getPasswordMinLength()).thenReturn(8);
    when(userRepository.existsByUsername(username)).thenReturn(true);

    // Act & Assert
    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> authService.register(username, password, email, role));
    assertEquals("USERNAME_EXISTS", exception.getErrorCode());
    assertEquals("用户名已存在", exception.getMessage());
  }

  @Test
  void testRegister_EmailExists() {
    // Arrange
    String username = "testuser";
    String password = "password123";
    String email = "existing@example.com";
    String role = "student";

    when(configUtil.getPasswordMinLength()).thenReturn(8);
    when(userRepository.existsByUsername(username)).thenReturn(false);
    when(userRepository.existsByEmail(email)).thenReturn(true);

    // Act & Assert
    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> authService.register(username, password, email, role));
    assertEquals("EMAIL_EXISTS", exception.getErrorCode());
    assertEquals("邮箱已被注册", exception.getMessage());
  }

  @Test
  void testRegister_WeakPassword() {
    // Given
    String username = "newuser";
    String password = "123";
    String email = "new@example.com";
    String role = "student";

    when(configUtil.getPasswordMinLength()).thenReturn(6);

    // When & Then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              authService.register(username, password, email, role);
            });

    assertEquals("WEAK_PASSWORD", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("密码长度不能少于"));
  }

  @Test
  void testLogin_Success() {
    // Arrange
    String username = "testuser";
    String password = "password123";
    String encodedPassword = "encodedPassword";

    User user = new User();
    user.setId(UUID.randomUUID());
    user.setUsername(username);
    user.setPassword(encodedPassword);
    user.setRole("student");

    when(configUtil.getMaxLoginAttempts()).thenReturn(5);
    when(cacheUtil.get(anyString(), eq(Integer.class))).thenReturn(null);
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
    when(jwtUtil.generateToken(user.getId(), username, "student")).thenReturn("mock-jwt-token");
    when(configUtil.getCacheDefaultExpireMinutes()).thenReturn(60);
    doNothing().when(cacheUtil).put(anyString(), eq(user), eq(60));
    doNothing().when(cacheUtil).remove(anyString());

    // Act
    Map<String, Object> result = authService.login(username, password);

    // Assert
    assertNotNull(result);
    assertEquals(true, result.get("success"));
    assertEquals("mock-jwt-token", result.get("token"));
    verify(cacheUtil).remove(anyString()); // Clear login attempts
  }

  @Test
  void testLogin_UserNotFound() {
    // Arrange
    String username = "nonexistent";
    String password = "password123";

    when(configUtil.getMaxLoginAttempts()).thenReturn(5);
    when(cacheUtil.get(anyString(), eq(Integer.class))).thenReturn(null);
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    // Act & Assert
    BusinessException exception =
        assertThrows(BusinessException.class, () -> authService.login(username, password));

    assertEquals("LOGIN_FAILED", exception.getErrorCode());
    assertEquals("用户名或密码错误", exception.getMessage());
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  void testLogin_InvalidPassword() {
    // Arrange
    String username = "testuser";
    String password = "wrongpassword";
    String encodedPassword = "encodedPassword";

    User user = new User();
    user.setId(UUID.randomUUID());
    user.setUsername(username);
    user.setPassword(encodedPassword);
    user.setRole("student");

    when(configUtil.getMaxLoginAttempts()).thenReturn(5);
    when(cacheUtil.get(anyString(), eq(Integer.class))).thenReturn(null);
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

    // Act & Assert
    BusinessException exception =
        assertThrows(BusinessException.class, () -> authService.login(username, password));

    assertEquals("LOGIN_FAILED", exception.getErrorCode());
    assertEquals("用户名或密码错误", exception.getMessage());
    verify(cacheUtil).put(anyString(), eq(1), eq(30));
  }

  @Test
  void testLogin_AccountLocked() {
    // Arrange
    String username = "testuser";
    String password = "password123";

    when(configUtil.getMaxLoginAttempts()).thenReturn(5);
    when(cacheUtil.get(anyString(), eq(Integer.class))).thenReturn(5);

    // Act & Assert
    BusinessException exception =
        assertThrows(BusinessException.class, () -> authService.login(username, password));

    assertEquals("ACCOUNT_LOCKED", exception.getErrorCode());
    assertEquals("登录尝试次数过多，账户已被锁定", exception.getMessage());
    verify(userRepository, never()).findByUsername(anyString());
  }

  @Test
  void testGetUserById_Success() {
    // Arrange
    UUID userId = UUID.randomUUID();
    User user = new User();
    user.setId(userId);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole("student");

    when(cacheUtil.get(anyString(), eq(User.class))).thenReturn(null);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(configUtil.getCacheDefaultExpireMinutes()).thenReturn(1); // 1分钟 = 60秒

    // Act
    Optional<User> result = authService.getUserById(userId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(user, result.get());
    verify(cacheUtil).put(anyString(), eq(user), eq(1)); // 验证缓存时间为1分钟
  }

  @Test
  void testGetUserById_FromCache() {
    // Arrange
    UUID userId = UUID.randomUUID();
    User user = new User();
    user.setId(userId);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole("student");

    when(cacheUtil.get(anyString(), eq(User.class))).thenReturn(user);

    // Act
    Optional<User> result = authService.getUserById(userId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(user, result.get());
    verify(userRepository, never()).findById(any());
  }

  @Test
  void testGetUserById_NotFound() {
    // Arrange
    UUID userId = UUID.randomUUID();

    when(cacheUtil.get(anyString(), eq(User.class))).thenReturn(null);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act
    Optional<User> result = authService.getUserById(userId);

    // Assert
    assertFalse(result.isPresent());
    verify(cacheUtil, never()).put(anyString(), any(), anyInt());
  }

  @Test
  void testGetUserById_NullUserId() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              authService.getUserById(null);
            });

    assertEquals("用户ID不能为空", exception.getMessage());
    verify(cacheUtil, never()).get(anyString(), any());
    verify(userRepository, never()).findById(any());
  }

  @Test
  void testRegister_NullInputs() {
    // When & Then - Test null username
    BusinessException exception1 =
        assertThrows(
            BusinessException.class,
            () -> {
              authService.register(null, "password", "email@test.com", "student");
            });
    assertTrue(exception1.getMessage().contains("用户名不能为空"));

    // When & Then - Test null password
    BusinessException exception2 =
        assertThrows(
            BusinessException.class,
            () -> {
              authService.register("username", null, "email@test.com", "student");
            });
    assertTrue(exception2.getMessage().contains("密码不能为空"));

    // When & Then - Test null email
    BusinessException exception3 =
        assertThrows(
            BusinessException.class,
            () -> {
              authService.register("username", "password", null, "student");
            });
    assertTrue(exception3.getMessage().contains("邮箱不能为空"));
  }

  @Test
  void testLogin_NullInputs() {
    // When & Then - Test null username
    BusinessException exception1 =
        assertThrows(
            BusinessException.class,
            () -> {
              authService.login(null, "password");
            });
    assertTrue(exception1.getMessage().contains("用户名不能为空"));

    // When & Then - Test null password
    BusinessException exception2 =
        assertThrows(
            BusinessException.class,
            () -> {
              authService.login("username", null);
            });
    assertTrue(exception2.getMessage().contains("密码不能为空"));
  }
}
