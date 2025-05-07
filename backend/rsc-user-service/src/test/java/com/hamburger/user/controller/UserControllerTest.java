package com.hamburger.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hamburger.user.dao.entity.User;
import com.hamburger.user.dto.LoginReqDto;
import com.hamburger.user.dto.RegisterReqDto;
import com.hamburger.user.service.UserService;
import com.hamburger.user.service.util.PasswordUtil;
import com.hamburger.user.service.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    
    @Mock
    private UserService userService;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private UserController userController;
    
    private static final String VALID_USERNAME = "testUser";
    private static final String VALID_PASSWORD = "Password123";
    
    @Test
    void registerUser_ValidInput_ReturnsSuccess() {
        // Arrange
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.getUserName()).thenReturn(VALID_USERNAME);
        when(req.isEmailValid()).thenReturn(true);
        when(req.isPasswordValid()).thenReturn(true);
        when(userService.getUser(VALID_USERNAME)).thenReturn(null);

        // Act
        ResponseEntity<String> response = userController.registerUser(req);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered!", response.getBody());
        verify(userService).registerUser(req);
    }

    @Test
    void registerUser_InvalidEmail_ReturnsBadRequest() {
        // Arrange
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.isEmailValid()).thenReturn(false);

        // Act
        ResponseEntity<String> response = userController.registerUser(req);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid email format", response.getBody());
        verify(userService, never()).registerUser(req);
    }

    @Test
    void registerUser_InvalidPassword_ReturnsBadRequest() {
        // Arrange
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.isEmailValid()).thenReturn(true);
        when(req.isPasswordValid()).thenReturn(false);

        // Act
        ResponseEntity<String> response = userController.registerUser(req);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid password format", response.getBody());
        verify(userService, never()).registerUser(req);
    }

    @Test
    void registerUser_UserAlreadyExists_ReturnsConflict() {
        // Arrange
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.getUserName()).thenReturn(VALID_USERNAME);
        when(req.isEmailValid()).thenReturn(true);
        when(req.isPasswordValid()).thenReturn(true);
        when(userService.getUser(VALID_USERNAME)).thenReturn(new User());

        // Act
        ResponseEntity<String> response = userController.registerUser(req);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
        verify(userService, never()).registerUser(req);
    }
    
    @Test
    void loginUser_ValidCredentials_ReturnsSuccess() {
        // Arrange
        LoginReqDto req = mock(LoginReqDto.class);
        when(req.getUserName()).thenReturn(VALID_USERNAME);
        when(req.getPassword()).thenReturn(VALID_PASSWORD);
        // Update to match actual method signature
        when(req.isPasswordValid(eq(VALID_PASSWORD), any())).thenReturn(true);
        // Update to match actual method signature
        when(req.getToken(VALID_USERNAME)).thenReturn("mock.jwt.token");

        User user = new User();
        user.setUserName(VALID_USERNAME);
        user.setHashedPassword(PasswordUtil.hashPassword(VALID_PASSWORD));

        when(userService.getUser(VALID_USERNAME)).thenReturn(user);

        // Act
        ResponseEntity<String> response = userController.loginUser(req, this.response);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody());
        verify(this.response).addCookie(any(Cookie.class));
    }

    @Test
    void loginUser_InvalidCredentials_ReturnsBadRequest() {
        // Arrange
        LoginReqDto req = mock(LoginReqDto.class);
        when(req.getUserName()).thenReturn(VALID_USERNAME);
        when(req.getPassword()).thenReturn(VALID_PASSWORD);
        // Mock invalid password verification
        when(req.isPasswordValid(eq(VALID_PASSWORD), any())).thenReturn(false);

        User user = new User();
        user.setUserName(VALID_USERNAME);
        user.setHashedPassword(PasswordUtil.hashPassword("different_password"));

        when(userService.getUser(VALID_USERNAME)).thenReturn(user);

        // Act
        ResponseEntity<String> response = userController.loginUser(req, this.response);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
        verify(this.response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void validateToken_ValidToken_ReturnsSuccess() {
        // Arrange
        String testToken = "valid.test.token";
        Cookie cookie = new Cookie("JWT", testToken);
        Cookie[] cookies = new Cookie[] { cookie };
        when(request.getCookies()).thenReturn(cookies);

        // Use try-with-resources to set environment variable for test
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            // Mock static methods
            jwtUtilMock.when(() -> JwtUtil.validateToken(testToken)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.extractUsername(testToken)).thenReturn(VALID_USERNAME);

            // Act
            ResponseEntity<String> response = userController.validateToken(request);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("Token is valid"));
            assertTrue(response.getBody().contains(VALID_USERNAME));
        }
    }

    @Test
    void validateToken_InvalidToken_ReturnsUnauthorized() {
        // Arrange
        String testToken = "invalid.test.token";
        Cookie cookie = new Cookie("JWT", testToken);
        Cookie[] cookies = new Cookie[] { cookie };
        when(request.getCookies()).thenReturn(cookies);

        // Use try-with-resources to set environment variable for test
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            // Mock static methods
            jwtUtilMock.when(() -> JwtUtil.validateToken(testToken)).thenReturn(false);

            // Act
            ResponseEntity<String> response = userController.validateToken(request);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Invalid or expired token", response.getBody());
        }
    }

    @Test
    void validateToken_NoCookies_ReturnsUnauthorized() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        ResponseEntity<String> response = userController.validateToken(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid or expired token", response.getBody());
    }

    
    @Test
    void getUser_ExistingUser_ReturnsUser() {
        // Arrange
        User expectedUser = new User();
        expectedUser.setUserName(VALID_USERNAME);
        when(userService.getUser(VALID_USERNAME)).thenReturn(expectedUser);
        
        // Act
        ResponseEntity<User> response = userController.getUser(VALID_USERNAME);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUser, response.getBody());
    }
    
    @Test
    void getUser_NonExistingUser_ReturnsNotFound() {
        // Arrange
        when(userService.getUser(VALID_USERNAME)).thenReturn(null);
        
        // Act
        ResponseEntity<User> response = userController.getUser(VALID_USERNAME);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}