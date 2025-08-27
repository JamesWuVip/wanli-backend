package com.wanli.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = new BadCredentialsException("Bad credentials");
    }

    @Test
    void testCommenceWithUnauthorizedAccess() throws Exception {
        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertEquals("UTF-8", response.getCharacterEncoding());
        
        String responseContent = response.getContentAsString();
        assertNotNull(responseContent);
        assertFalse(responseContent.isEmpty());
        
        // Validate JSON structure by checking key fields
        assertTrue(responseContent.contains("\"success\":false"));
        assertTrue(responseContent.contains("\"code\":401"));
        assertTrue(responseContent.contains("\"message\":\"未授权访问\""));
        
        // Validate it's proper JSON format
        assertTrue(responseContent.startsWith("{"));
        assertTrue(responseContent.endsWith("}"));
    }

    @Test
    void testCommenceWithDifferentAuthException() throws Exception {
        // Given
        AuthenticationException customException = new BadCredentialsException("Custom error message");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, customException);

        // Then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        
        String responseContent = response.getContentAsString();
        assertTrue(responseContent.contains("\"success\":false"));
        assertTrue(responseContent.contains("\"code\":401"));
        assertTrue(responseContent.contains("\"message\":\"未授权访问\""));
    }

    @Test
    void testCommenceWithNullException() throws Exception {
        // When
        jwtAuthenticationEntryPoint.commence(request, response, null);

        // Then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        
        String responseContent = response.getContentAsString();
        assertTrue(responseContent.contains("\"success\":false"));
        assertTrue(responseContent.contains("\"code\":401"));
        assertTrue(responseContent.contains("\"message\":\"未授权访问\""));
    }

    @Test
    void testCommenceResponseFormat() throws Exception {
        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseContent = response.getContentAsString();
        
        // Validate JSON structure
        assertTrue(responseContent.matches(".*\\{.*\\}.*"), "Response should be valid JSON");
        assertTrue(responseContent.contains("success"), "Response should contain success field");
        assertTrue(responseContent.contains("code"), "Response should contain code field");
        assertTrue(responseContent.contains("message"), "Response should contain message field");
        assertTrue(responseContent.contains("timestamp"), "Response should contain timestamp field");
    }
}