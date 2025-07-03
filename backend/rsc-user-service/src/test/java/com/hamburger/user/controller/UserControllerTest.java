package com.hamburger.user.controller;

import com.hamburger.user.dao.entity.User;
import com.hamburger.user.dto.*;
import com.hamburger.user.service.UserService;
import com.hamburger.user.service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUserSuccess() {
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.isEmailValid()).thenReturn(true);
        when(req.isPasswordValid()).thenReturn(true);
        when(req.getUserName()).thenReturn("name");
        when(userService.getUser("name")).thenReturn(null);

        ResponseEntity<String> response = userController.registerUser(req);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered!", response.getBody());
    }

    @Test
    void testRegisterUserInvalidEmail() {
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.isEmailValid()).thenReturn(false);
        ResponseEntity<String> response = userController.registerUser(req);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid email format", response.getBody());
    }

    @Test
    void testRegisterUserInvalidPassword() {
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.isEmailValid()).thenReturn(true);
        when(req.isPasswordValid()).thenReturn(false);
        ResponseEntity<String> response = userController.registerUser(req);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid password format", response.getBody());
    }

    @Test
    void testRegisterUserExists() {
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.isEmailValid()).thenReturn(true);
        when(req.isPasswordValid()).thenReturn(true);
        when(req.getUserName()).thenReturn("name");
        when(userService.getUser("name")).thenReturn(new User());
        ResponseEntity<String> response = userController.registerUser(req);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("User already exists", response.getBody());
    }

    @Test
    void testLoginUserSuccess() {
        LoginReqDto req = mock(LoginReqDto.class);
        User user = mock(User.class);
        when(req.getUserName()).thenReturn("name");
        when(userService.getUser("name")).thenReturn(user);
        when(req.getPassword()).thenReturn("Password!1");
        when(user.getHashedPassword()).thenReturn("hashedPassword");
        when(req.isPasswordValid("Password!1", "hashedPassword")).thenReturn(true);
        when(req.getToken("name")).thenReturn("valid_token");
        when(user.getUserName()).thenReturn("name");

        ResponseEntity<Map<String, Object>> response = userController.loginUser(req, null);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Login successful", response.getBody().get("message"));
        assertEquals("valid_token", response.getBody().get("token"));
        assertEquals("name", ((Map<?, ?>)response.getBody().get("userInfo")).get("userName"));
    }

    @Test
    void testLoginUserInvalidCredentials() {
        LoginReqDto req = mock(LoginReqDto.class);
        when(req.getUserName()).thenReturn("name");
        // Simulate user not found
        when(userService.getUser("name")).thenReturn(null);
        ResponseEntity<Map<String, Object>> response = userController.loginUser(req, null);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid username or password", response.getBody().get("error"));

        // Simulate invalid username or password
        when(req.getUserName()).thenReturn("name");
        User user = mock(User.class);
        when(userService.getUser("name")).thenReturn(user);
        when(req.getPassword()).thenReturn("Password!1");
        when(user.getHashedPassword()).thenReturn("hashedPassword");
        when(req.isPasswordValid("Password!1", "hashedPassword")).thenReturn(false);
        response = userController.loginUser(req, null);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid username or password", response.getBody().get("error"));
    }

    @Test
    void testValidateTokenSuccess() {
        AuthReqDto req = mock(AuthReqDto.class);
        String token = "valid_token";
        String username = "name";
        when(req.getToken()).thenReturn(token);
        try (var jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.extractUsername(token)).thenReturn(username);
            ResponseEntity<Map<String, Object>> response = userController.validateToken(req);
            assertEquals(200, response.getStatusCodeValue());
            assertEquals("Valid token", response.getBody().get("message"));
            assertEquals(username, response.getBody().get("username"));
        }
    }

    @Test
    void testValidateTokenError() {
        AuthReqDto req = mock(AuthReqDto.class);
        // Simulate no token
        when(req.getToken()).thenReturn(null);
        ResponseEntity<Map<String, Object>> response = userController.validateToken(req);
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid or expired token", response.getBody().get("error"));

        // Simulate invalid token
        when(req.getToken()).thenReturn("invalid_token");
        try (var jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken("invalid_token")).thenReturn(false);
            response = userController.validateToken(req);
            assertEquals(401, response.getStatusCodeValue());
            assertEquals("Invalid or expired token", response.getBody().get("error"));
        }
    }

    @Test
    void testGetUserSuccess() {
        User user = mock(User.class);
        when(userService.getUser("name")).thenReturn(user);
        ResponseEntity<User> response = userController.getUser("name");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserError() {
        when(userService.getUser("name")).thenReturn(null);
        ResponseEntity<User> response = userController.getUser("name");
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testForgotPasswordSuccess() {
        ResetReqDto req = mock(ResetReqDto.class);
        when(req.isEmailValid()).thenReturn(true);
        when(req.getEmail()).thenReturn("name@email.com");
        ResponseEntity<Map<String, Object>> response = userController.forgotPassword(req);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Reset password email is sent", response.getBody().get("message"));
    }

    @Test
    void testForgotPasswordError() {
        ResetReqDto req = mock(ResetReqDto.class);
        when(req.isEmailValid()).thenReturn(false);
        ResponseEntity<Map<String, Object>> response = userController.forgotPassword(req);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid email format", response.getBody().get("error"));
    }

    @Test
    void testResetPasswordSuccess() {
        ResetReqDto req = mock(ResetReqDto.class);
        when(req.getEmail()).thenReturn("name@email.com");
        when(req.getToken()).thenReturn("valid_token");
        when(req.getNewPassword()).thenReturn("Password!2");
        when(userService.resetPassword("name@email.com", "valid_token", "Password!2")).thenReturn(true);
        ResponseEntity<Map<String, Object>> response = userController.resetPassword(req);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password has been reset successfully", response.getBody().get("message"));
    }

    @Test
    void testResetPasswordError() {
        ResetReqDto req = mock(ResetReqDto.class);
        when(req.getEmail()).thenReturn("name@email.com");
        when(req.getToken()).thenReturn("invalid_token");
        when(req.getNewPassword()).thenReturn("Password!2");
        when(userService.resetPassword("name@email.com", "invalid_token", "Password!2")).thenReturn(false);
        ResponseEntity<Map<String, Object>> response = userController.resetPassword(req);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid url or token expired", response.getBody().get("error"));
    }
}
