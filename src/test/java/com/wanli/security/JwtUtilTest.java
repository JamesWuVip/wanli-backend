package com.wanli.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "test-secret-key-for-jwt-testing-purposes-only";
    private final long testExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void testExtractUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractExpiration() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testExtractClaim() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        String subject = jwtUtil.extractClaim(token, Claims::getSubject);

        assertEquals(username, subject);
    }

    @Test
    void testIsTokenExpired() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        boolean isExpired = jwtUtil.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testValidateTokenValid() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token, username);

        assertTrue(isValid);
    }

    @Test
    void testValidateTokenInvalidUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token, "differentuser");

        assertFalse(isValid);
    }

    @Test
    void testValidateTokenWithNullToken() {
        assertThrows(Exception.class, () -> {
            jwtUtil.validateToken(null, "testuser");
        });
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        assertThrows(Exception.class, () -> {
            jwtUtil.validateToken("invalid.token.here", "testuser");
        });
    }

    @Test
    void testExtractAllClaims() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // This is testing the private method indirectly through public methods
        String extractedUsername = jwtUtil.extractUsername(token);
        Date extractedExpiration = jwtUtil.extractExpiration(token);

        assertEquals(username, extractedUsername);
        assertNotNull(extractedExpiration);
    }
}