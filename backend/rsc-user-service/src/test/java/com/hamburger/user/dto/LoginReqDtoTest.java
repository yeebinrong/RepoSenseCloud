package com.hamburger.user.dto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.hamburger.user.service.util.JwtUtil;
import com.hamburger.user.service.util.PasswordUtil;

class LoginReqDtoTest {

    private LoginReqDto loginReqDto;
    private static final String USERNAME = "testUser";
    private static final String VALID_PASSWORD = "validPassword123";
    private static final String INVALID_PASSWORD = "short";
    private static final String HASHED_PASSWORD = "hashedPassword123";
    private static final String MOCK_TOKEN = "mock.jwt.token";

    @BeforeEach
    void setUp() {
        loginReqDto = new LoginReqDto();
        loginReqDto.setUserName(USERNAME);
        loginReqDto.setPassword(VALID_PASSWORD);
    }

    @Test
    void testGettersAndSetters() {
        // Arrange & Act
        LoginReqDto dto = new LoginReqDto();
        dto.setUserName(USERNAME);
        dto.setPassword(VALID_PASSWORD);

        // Assert
        assertEquals(USERNAME, dto.getUserName());
        assertEquals(VALID_PASSWORD, dto.getPassword());
    }

    @Test
    void isPasswordValid_WithValidPasswordAndHash_ReturnsTrue() {
        // Arrange
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = mockStatic(PasswordUtil.class)) {
            mockedPasswordUtil.when(() -> PasswordUtil.checkPassword(VALID_PASSWORD, HASHED_PASSWORD))
                    .thenReturn(true);

            // Act
            boolean result = loginReqDto.isPasswordValid(VALID_PASSWORD, HASHED_PASSWORD);

            // Assert
            assertTrue(result);
            mockedPasswordUtil.verify(() -> PasswordUtil.checkPassword(VALID_PASSWORD, HASHED_PASSWORD));
        }
    }

    @Test
    void isPasswordValid_WithInvalidPasswordLength_ReturnsFalse() {
        // Act
        boolean result = loginReqDto.isPasswordValid(INVALID_PASSWORD, HASHED_PASSWORD);

        // Assert
        assertFalse(result);
    }

    @Test
    void isPasswordValid_WithIncorrectHash_ReturnsFalse() {
        // Arrange
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = mockStatic(PasswordUtil.class)) {
            mockedPasswordUtil.when(() -> PasswordUtil.checkPassword(VALID_PASSWORD, HASHED_PASSWORD))
                    .thenReturn(false);

            // Act
            boolean result = loginReqDto.isPasswordValid(VALID_PASSWORD, HASHED_PASSWORD);

            // Assert
            assertFalse(result);
            mockedPasswordUtil.verify(() -> PasswordUtil.checkPassword(VALID_PASSWORD, HASHED_PASSWORD));
        }
    }

    @Test
    void getToken_ReturnsValidToken() {
        // Arrange
        try (MockedStatic<JwtUtil> mockedJwtUtil = mockStatic(JwtUtil.class)) {
            mockedJwtUtil.when(() -> JwtUtil.generateToken(USERNAME))
                    .thenReturn(MOCK_TOKEN);

            // Act
            String result = loginReqDto.getToken(USERNAME);

            // Assert
            assertEquals(MOCK_TOKEN, result);
            mockedJwtUtil.verify(() -> JwtUtil.generateToken(USERNAME));
        }
    }
}
