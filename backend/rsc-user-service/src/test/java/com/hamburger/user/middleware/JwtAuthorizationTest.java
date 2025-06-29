package com.hamburger.user.middleware;

import com.hamburger.user.service.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthorizationTest {
    private JwtAuthorization jwtAuthorization;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtAuthorization = new JwtAuthorization();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void testExcludeRegisterPath() throws ServletException, IOException {
        request.setRequestURI("/api/user/register");
        request.addHeader("Authorization", "Bearer invalid_token");
        jwtAuthorization.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testExcludeLoginPath() throws ServletException, IOException {
        request.setRequestURI("/api/user/login");
        request.addHeader("Authorization", "Bearer invalid_token");
        jwtAuthorization.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testValidToken() throws ServletException, IOException {
        request.setRequestURI("/api/user/reset-password");
        request.addHeader("Authorization", "Bearer valid_token");
        var jwtUtilMock = mockStatic(JwtUtil.class);
        jwtUtilMock.when(() -> JwtUtil.validateToken("valid_token")).thenReturn(true);
        jwtAuthorization.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
        jwtUtilMock.close();
    }

    @Test 
    void testNullToken() throws ServletException, IOException {
        request.setRequestURI("/api/user/reset-password");
        jwtAuthorization.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testInvalidToken() throws ServletException, IOException {
        request.setRequestURI("/api/user/reset-password");
        request.addHeader("Authorization", "Bearer invalid_token");
        var jwtUtilMock = mockStatic(JwtUtil.class);
        jwtUtilMock.when(() -> JwtUtil.validateToken("invalid_token")).thenReturn(false);
        jwtAuthorization.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid JWT token"));
        jwtUtilMock.close();
    }

    @Test
    void testExpiredToken() throws ServletException, IOException {
        request.setRequestURI("/api/user/reset-password");
        request.addHeader("Authorization", "Bearer expired_token");
        var jwtUtilMock = mockStatic(JwtUtil.class);
        jwtUtilMock.when(() -> JwtUtil.validateToken("expired_token"))
                .thenThrow(new ExpiredJwtException(null, null, "JWT expired"));
        jwtAuthorization.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("JWT token is expired"));
        jwtUtilMock.close();
    }

    @Test
    void testErrorToken() throws ServletException, IOException {
        request.setRequestURI("/api/user/reset-password");
        request.addHeader("Authorization", "Bearer invalid_token");
        var jwtUtilMock = mockStatic(JwtUtil.class);
        jwtUtilMock.when(() -> JwtUtil.validateToken("invalid_token"))
                .thenThrow(new RuntimeException("Error"));
        jwtAuthorization.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Unauthorized"));
        jwtUtilMock.close();
    }
}
