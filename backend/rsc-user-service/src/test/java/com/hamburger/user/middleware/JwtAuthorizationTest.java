package com.hamburger.user.middleware;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import com.hamburger.user.service.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthorizationTest {

    private JwtAuthorization jwtAuthorization;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String EXPIRED_TOKEN = "expired.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthorization = new JwtAuthorization();
    }

    @Test
    void doFilterInternal_WithNoCookies_ContinuesChain() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        jwtAuthorization.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void doFilterInternal_WithNoJwtCookie_ContinuesChain() throws ServletException, IOException {
        // Arrange
        Cookie[] cookies = new Cookie[] { new Cookie("OTHER", "value") };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        jwtAuthorization.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void doFilterInternal_WithValidToken_ContinuesChain() throws ServletException, IOException {
        // Arrange
        Cookie[] cookies = new Cookie[] { new Cookie("JWT", VALID_TOKEN) };
        when(request.getCookies()).thenReturn(cookies);

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);

            // Act
            jwtAuthorization.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(writer);
        }
    }

    @Test
    void doFilterInternal_WithInvalidToken_ReturnsUnauthorized() throws ServletException, IOException {
        // Arrange
        Cookie[] cookies = new Cookie[] { new Cookie("JWT", INVALID_TOKEN) };
        when(request.getCookies()).thenReturn(cookies);
        when(response.getWriter()).thenReturn(writer);

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(INVALID_TOKEN)).thenReturn(false);

            // Act
            jwtAuthorization.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(writer).write("Invalid JWT token");
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_WithExpiredToken_ReturnsUnauthorized() throws ServletException, IOException {
        // Arrange
        Cookie[] cookies = new Cookie[] { new Cookie("JWT", EXPIRED_TOKEN) };
        when(request.getCookies()).thenReturn(cookies);
        when(response.getWriter()).thenReturn(writer);

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(EXPIRED_TOKEN))
                    .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

            // Act
            jwtAuthorization.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(writer).write("JWT token is expired");
            verifyNoMoreInteractions(filterChain);
        }
    }

    @Test
    void doFilterInternal_WithGenericError_ReturnsUnauthorized() throws ServletException, IOException {
        // Arrange
        Cookie[] cookies = new Cookie[] { new Cookie("JWT", INVALID_TOKEN) };
        when(request.getCookies()).thenReturn(cookies);
        when(response.getWriter()).thenReturn(writer);

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(INVALID_TOKEN))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // Act
            jwtAuthorization.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(writer).write("Unauthorized");
            verifyNoMoreInteractions(filterChain);
        }
    }
}