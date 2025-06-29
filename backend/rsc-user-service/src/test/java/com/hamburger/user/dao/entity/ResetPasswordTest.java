package com.hamburger.user.dao.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordTest {
    @Test
    void testGetEmail() {
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setEmail("name@email.com");
        assertEquals("name@email.com", resetPassword.getEmail());
    }
}
