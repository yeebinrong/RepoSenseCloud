package com.hamburger.user.service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {
    @Test
    void testHashAndCheckPassword() {
        String password = "Password!1";
        String hashed = PasswordUtil.hashPassword(password);
        assertNotNull(hashed);
        assertTrue(PasswordUtil.checkPassword(password, hashed));
        assertFalse(PasswordUtil.checkPassword("Password!2", hashed));
    }
}
