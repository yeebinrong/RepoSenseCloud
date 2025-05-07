package com.hamburger.job.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

class JwtHelperTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtHelper jwtHelper;

    private static final String MOCK_JWT = "mock-jwt-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void extractJwtFromRequest_WithValidJwtCookie_ReturnsToken() {
        // Arrange
        Cookie jwtCookie = new Cookie("JWT", MOCK_JWT);
        Cookie[] cookies = new Cookie[]{jwtCookie};
        when(request.getCookies()).thenReturn(cookies);

        // Act
        String result = jwtHelper.extractJwtFromRequest(request);

        // Assert
        assertEquals(MOCK_JWT, result);
        verify(request).getCookies();
    }

    @Test
    void extractJwtFromRequest_WithMultipleCookies_ReturnsToken() {
        // Arrange
        Cookie otherCookie = new Cookie("other", "value");
        Cookie jwtCookie = new Cookie("JWT", MOCK_JWT);
        Cookie[] cookies = new Cookie[]{otherCookie, jwtCookie};
        when(request.getCookies()).thenReturn(cookies);

        // Act
        String result = jwtHelper.extractJwtFromRequest(request);

        // Assert
        assertEquals(MOCK_JWT, result);
        verify(request).getCookies();
    }

    @Test
    void extractJwtFromRequest_WithoutJwtCookie_ReturnsNull() {
        // Arrange
        Cookie otherCookie = new Cookie("other", "value");
        Cookie[] cookies = new Cookie[]{otherCookie};
        when(request.getCookies()).thenReturn(cookies);

        // Act
        String result = jwtHelper.extractJwtFromRequest(request);

        // Assert
        assertNull(result);
        verify(request).getCookies();
    }

    @Test
    void extractJwtFromRequest_WithNoCookies_ReturnsNull() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        String result = jwtHelper.extractJwtFromRequest(request);

        // Assert
        assertNull(result);
        verify(request).getCookies();
    }
}