package com.hamburger.user.dto;

import com.hamburger.user.service.util.JwtUtil;
import com.hamburger.user.service.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class LoginReqDtoTest {
    private LoginReqDto loginReqDto;

    @BeforeEach
    void setUp() {
        loginReqDto = new LoginReqDto();
    }

    @Test
    void testValidPasswordWithValidHash() {
        String password = "Password!1";
        String hashedPassword = "hashedPassword";
        try (MockedStatic<PasswordUtil> passwordUtilMock = Mockito.mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.checkPassword(password, hashedPassword)).thenReturn(true);
            assertTrue(loginReqDto.isPasswordValid(password, hashedPassword));
        }
    }

    @Test
    void testValidPasswordWithInvalidHash() {
        String password = "Password!";
        String hashedPassword = "invalid_hash";
        try (MockedStatic<PasswordUtil> passwordUtilMock = Mockito.mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.checkPassword(password, hashedPassword)).thenReturn(false);
            assertFalse(loginReqDto.isPasswordValid(password, hashedPassword));
        }
    }

    @Test
    void testInvalidPasswordWithValidHash() {
        String password = "Pswd!1";
        String hashedPassword = "hashedPassword";
        try (MockedStatic<PasswordUtil> passwordUtilMock = Mockito.mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.checkPassword(password, hashedPassword)).thenReturn(true);
            assertFalse(loginReqDto.isPasswordValid(password, hashedPassword));
        }
    }

    @Test
    void testInvalidPasswordWithInvalidHash() {
        String password = "Pswd!1";
        String hashedPassword = "invalid_hash";
        try (MockedStatic<PasswordUtil> passwordUtilMock = Mockito.mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.checkPassword(password, hashedPassword)).thenReturn(false);
            assertFalse(loginReqDto.isPasswordValid(password, hashedPassword));
        }
    }

    @Test
    void testGetToken() {
        String userName = "name";
        String token = "valid_token";
        try (MockedStatic<JwtUtil> jwtUtilMock = Mockito.mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateToken(userName)).thenReturn(token);
            assertEquals(token, loginReqDto.getToken(userName));
        }
    }
}
