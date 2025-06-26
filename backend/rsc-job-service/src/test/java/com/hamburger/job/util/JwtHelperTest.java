package com.hamburger.job.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class JwtHelperTest {
    
    private JwtHelper jwtHelper;
    
    @BeforeEach
    void setUp() {
        jwtHelper = new JwtHelper();
    }
    
    @Test
    void extractJwtFromRequest_withBearerToken_returnsToken() {
        // Arrange
        String expectedToken = "abc123.def456.ghi789";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expectedToken);
        
        // Act
        String result = jwtHelper.extractJwtFromRequest(request);
        
        // Assert
        assertEquals(expectedToken, result);
    }
    
    @Test
    void extractJwtFromRequest_withNonBearerAuthHeader_returnsNull() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        
        // Act
        String result = jwtHelper.extractJwtFromRequest(request);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void extractJwtFromRequest_withNoAuthHeader_returnsNull() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // Act
        String result = jwtHelper.extractJwtFromRequest(request);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void extractJwtFromRequest_withEmptyAuthHeader_returnsNull() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "");
        
        // Act
        String result = jwtHelper.extractJwtFromRequest(request);
        
        // Assert
        assertNull(result);
    }
}