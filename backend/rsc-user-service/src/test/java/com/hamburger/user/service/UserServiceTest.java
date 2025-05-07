package com.hamburger.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hamburger.user.dao.UserDao;
import com.hamburger.user.dao.entity.User;
import com.hamburger.user.dto.RegisterReqDto;

public class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private static final String USERNAME = "testUser";
    private static final String EMAIL = "test@example.com";
    private static final String HASHED_PASSWORD = "hashedPassword123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_WithNewUser_SavesUser() {
        // Arrange
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.getUserName()).thenReturn(USERNAME);
        when(req.getEmail()).thenReturn(EMAIL);
        when(req.getHashedPassword()).thenReturn(HASHED_PASSWORD);
        when(userDao.findByUserName(USERNAME)).thenReturn(null);

        // Act
        userService.registerUser(req);

        // Assert
        verify(userDao).findByUserName(USERNAME);
        verify(userDao).save(argThat(user -> 
            user.getUserName().equals(USERNAME) &&
            user.getEmail().equals(EMAIL) &&
            user.getHashedPassword().equals(HASHED_PASSWORD)
        ));
    }

    @Test
    void registerUser_WithExistingUsername_DoesNotSaveUser() {
        // Arrange
        RegisterReqDto req = mock(RegisterReqDto.class);
        when(req.getUserName()).thenReturn(USERNAME);
        User existingUser = User.builder().userName(USERNAME).build();
        when(userDao.findByUserName(USERNAME)).thenReturn(existingUser);

        // Act
        userService.registerUser(req);

        // Assert
        verify(userDao).findByUserName(USERNAME);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void getUser_WithExistingUsername_ReturnsUser() {
        // Arrange
        User expectedUser = User.builder()
            .userName(USERNAME)
            .email(EMAIL)
            .hashedPassword(HASHED_PASSWORD)
            .build();
        when(userDao.findByUserName(USERNAME)).thenReturn(expectedUser);

        // Act
        User result = userService.getUser(USERNAME);

        // Assert
        assertNotNull(result);
        assertEquals(USERNAME, result.getUserName());
        assertEquals(EMAIL, result.getEmail());
        assertEquals(HASHED_PASSWORD, result.getHashedPassword());
        verify(userDao).findByUserName(USERNAME);
    }

    @Test
    void getUser_WithNonExistentUsername_ReturnsNull() {
        // Arrange
        when(userDao.findByUserName(USERNAME)).thenReturn(null);

        // Act
        User result = userService.getUser(USERNAME);

        // Assert
        assertNull(result);
        verify(userDao).findByUserName(USERNAME);
    }
}
