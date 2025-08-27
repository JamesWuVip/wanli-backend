package com.wanli.service;

import com.wanli.dto.UserRegistrationDto;
import com.wanli.entity.User;
import com.wanli.entity.UserRole;
import com.wanli.repository.UserRepository;
import com.wanli.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private TestDataFactory testDataFactory;
    
    @BeforeEach
    void setUp() {
        testDataFactory = new TestDataFactory();
    }
    
    @Test
    void testRegisterUser_Success() {
        // Arrange
        UserRegistrationDto registrationDto = TestDataFactory.createUserRegistrationDto();
        User savedUser = TestDataFactory.createDefaultUser();
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act
        User result = userService.registerUser(registrationDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(savedUser.getUsername(), result.getUsername());
        assertEquals(savedUser.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testRegisterUser_DuplicateUsername() {
        // Arrange
        UserRegistrationDto registrationDto = TestDataFactory.createUserRegistrationDto();
        
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationDto);
        });
        
        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testRegisterUser_DuplicateEmail() {
        // Arrange
        UserRegistrationDto registrationDto = TestDataFactory.createUserRegistrationDto();
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationDto);
        });
        
        assertEquals("邮箱已存在", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testFindByUsername_Found() {
        // Arrange
        String username = "testuser";
        User user = TestDataFactory.createDefaultUser();
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        
        // Act
        Optional<User> result = userService.findByUsername(username);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(user.getUsername(), result.get().getUsername());
    }
    
    @Test
    void testFindByUsername_NotFound() {
        // Arrange
        String username = "nonexistent";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // Act
        Optional<User> result = userService.findByUsername(username);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByEmail_Found() {
        // Arrange
        String email = "test@example.com";
        User user = TestDataFactory.createDefaultUser();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        // Act
        Optional<User> result = userService.findByEmail(email);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
    }
    
    @Test
    void testExistsByUsername_True() {
        // Arrange
        String username = "testuser";
        
        when(userRepository.existsByUsername(username)).thenReturn(true);
        
        // Act
        boolean result = userService.existsByUsername(username);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void testExistsByUsername_False() {
        // Arrange
        String username = "nonexistent";
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        
        // Act
        boolean result = userService.existsByUsername(username);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testExistsByEmail_True() {
        // Arrange
        String email = "test@example.com";
        
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        // Act
        boolean result = userService.existsByEmail(email);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void testExistsByEmail_False() {
        // Arrange
        String email = "nonexistent@example.com";
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        
        // Act
        boolean result = userService.existsByEmail(email);
        
        // Assert
        assertFalse(result);
    }
}