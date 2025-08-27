package com.wanli.config;

import com.wanli.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForJWTTokenGenerationInTestEnvironmentThatShouldBeLongEnough",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=86400000"
})
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 手动设置测试值
        jwtUtil.setSecret("testSecretKeyForJWTTokenGenerationInTestEnvironmentThatShouldBeLongEnough");
        jwtUtil.setAccessTokenExpiration(3600000L);
        jwtUtil.setRefreshTokenExpiration(86400000L);
        jwtUtil.init();
    }
    
    @Test
    void testGenerateAccessToken() {
        String username = "testuser";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        
        String token = jwtUtil.generateAccessToken(username, authorities);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testGenerateRefreshToken() {
        String username = "testuser";
        
        String token = jwtUtil.generateRefreshToken(username);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testExtractUsername() {
        String username = "testuser";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        String token = jwtUtil.generateAccessToken(username, authorities);
        
        String extractedUsername = jwtUtil.extractUsername(token);
        
        assertEquals(username, extractedUsername);
    }
    
    @Test
    void testExtractAuthorities() {
        String username = "testuser";
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_STUDENT"),
            new SimpleGrantedAuthority("ROLE_USER")
        );
        String token = jwtUtil.generateAccessToken(username, authorities);
        
        List<SimpleGrantedAuthority> extractedAuthorities = jwtUtil.extractAuthorities(token);
        
        assertEquals(authorities.size(), extractedAuthorities.size());
        assertTrue(extractedAuthorities.containsAll(authorities));
    }
    
    @Test
    void testIsTokenExpired() {
        String username = "testuser";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        String token = jwtUtil.generateAccessToken(username, authorities);
        
        boolean isExpired = jwtUtil.isTokenExpired(token);
        
        assertFalse(isExpired);
    }
    
    @Test
    void testValidateToken() {
        String username = "testuser";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        String token = jwtUtil.generateAccessToken(username, authorities);
        
        boolean isValid = jwtUtil.validateToken(token, username);
        
        assertTrue(isValid);
    }
    
    @Test
    void testValidateTokenWithWrongUsername() {
        String username = "testuser";
        String wrongUsername = "wronguser";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        String token = jwtUtil.generateAccessToken(username, authorities);
        
        boolean isValid = jwtUtil.validateToken(token, wrongUsername);
        
        assertFalse(isValid);
    }
}