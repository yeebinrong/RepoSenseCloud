package com.hamburger.user.service;

import com.hamburger.user.dao.ResetPasswordDao;
import com.hamburger.user.dao.UserDao;
import com.hamburger.user.dao.entity.ResetPassword;
import com.hamburger.user.dao.entity.User;
import com.hamburger.user.dto.RegisterReqDto;
import com.hamburger.user.service.util.JwtUtil;
import com.hamburger.user.service.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserDao userDao;
    @InjectMocks
    private UserService userService;

    @Mock
    private RegisterReqDto req;
    @Mock
    private ResetPasswordDao resetPasswordDao;
    @Mock
    private SesService sesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUserError() {
        when(req.getUserName()).thenReturn("name");
        when(userDao.findByUserName("name")).thenReturn(new User());
        userService.registerUser(req);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void testRegisterUserSuccess() {
        when(req.getUserName()).thenReturn("name");
        when(req.getEmail()).thenReturn("name@email.com");
        when(req.getHashedPassword()).thenReturn("hashedPassword");
        when(userDao.findByUserName("name")).thenReturn(null);
        userService.registerUser(req);
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void testGetUser() {
        User user = new User();
        when(userDao.findByUserName("name")).thenReturn(user);
        User result = userService.getUser("name");
        assertSame(user, result);
    }

    @Test
    void testRequestResetPassword() {
        String email = "name@email.com";
        String token = "valid_token";
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateToken(email)).thenReturn(token);
            userService = new UserService(userDao, resetPasswordDao, sesService);
            userService.requestResetPassword(email);
            verify(resetPasswordDao, times(1)).save(any(ResetPassword.class));
            verify(sesService, times(1)).sendResetPasswordEmail(eq(email), contains(token));
        }
    }

    @Test
    void testResetPasswordSuccess() {
        String email = "name@email.com";
        String token = "valid_token";
        String newPassword = "Password!1";
        long expiry = System.currentTimeMillis() + 10000;
        ResetPassword resetPassword = new ResetPassword(email, token, expiry);
        User user = new User();
        try (MockedStatic<PasswordUtil> passwordUtilMock = mockStatic(PasswordUtil.class)) {
            passwordUtilMock.when(() -> PasswordUtil.hashPassword(newPassword)).thenReturn("hashedPassword");
            when(resetPasswordDao.findByEmail(email)).thenReturn(resetPassword);
            when(userDao.findByEmail(email)).thenReturn(user);
            userService = new UserService(userDao, resetPasswordDao, sesService);
            boolean result = userService.resetPassword(email, token, newPassword);
            assertTrue(result);
        }
    }

    @Test
    void testResetPasswordUserRequestError() {
        String email = "name@email.com";
        String token = "valid_token";
        String newPassword = "Password!1";
        when(resetPasswordDao.findByEmail(email)).thenReturn(null);
        when(userDao.findByEmail(email)).thenReturn(new User());
        userService = new UserService(userDao, resetPasswordDao, sesService);
        boolean result = userService.resetPassword(email, token, newPassword);
        assertFalse(result);
    }

    @Test
    void testResetPasswordUserExistError() {
        String email = "name@email.com";
        String token = "valid_token";
        String newPassword = "Password!1";
        ResetPassword resetPassword = new ResetPassword(email, token, System.currentTimeMillis() + 10000);
        when(resetPasswordDao.findByEmail(email)).thenReturn(resetPassword);
        when(userDao.findByEmail(email)).thenReturn(null);
        userService = new UserService(userDao, resetPasswordDao, sesService);
        boolean result = userService.resetPassword(email, token, newPassword);
        assertFalse(result);
    }

    @Test
    void testResetPasswordTokenError() {
        String email = "name@email.com";
        String token = "valid_token";
        String newPassword = "Password!1";
        ResetPassword resetPassword = new ResetPassword(email, "invalid_token", System.currentTimeMillis() - 10000);
        when(resetPasswordDao.findByEmail(email)).thenReturn(resetPassword);
        when(userDao.findByEmail(email)).thenReturn(new User());
        userService = new UserService(userDao, resetPasswordDao, sesService);
        boolean result = userService.resetPassword(email, token, newPassword);
        assertFalse(result);
    }
}
