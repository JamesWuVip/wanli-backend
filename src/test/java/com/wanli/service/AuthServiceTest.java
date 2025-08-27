package com.wanli.service;

import com.wanli.dto.LoginRequestDto;
import com.wanli.dto.LoginResponseDto;
import com.wanli.dto.RegisterRequestDto;
import com.wanli.entity.User;
import com.wanli.exception.BadRequestException;
import com.wanli.repository.UserRepository;
import com.wanli.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@example.com");

        loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = authService.register(registerRequest);

        assertEquals("用户注册成功", result);
    }

    @Test
    void testRegisterUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            authService.register(registerRequest);
        });
    }

    @Test
    void testRegisterEmailExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            authService.register(registerRequest);
        });
    }

    @Test
    void testLoginSuccess() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        LoginResponseDto result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertNotNull(result.getUserInfo());
        assertEquals("testuser", result.getUserInfo().getUsername());
        assertEquals("test@example.com", result.getUserInfo().getEmail());
    }
}