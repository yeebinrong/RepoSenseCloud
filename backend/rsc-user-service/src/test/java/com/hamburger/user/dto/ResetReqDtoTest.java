package com.hamburger.user.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResetReqDtoTest {
    @Test
    void testIsEmailValid() {
        ResetReqDto valid = new ResetReqDto();
        valid.setEmail("name@email.com");
        valid.setNewPassword("Password1!");
        assertTrue(valid.isEmailValid());

        ResetReqDto invalid = new ResetReqDto();
        invalid.setEmail("name@email");
        invalid.setNewPassword("Password1!");
        assertFalse(invalid.isEmailValid());

        ResetReqDto nullEmail = new ResetReqDto();
        nullEmail.setEmail(null);
        nullEmail.setNewPassword("Password1!");
        assertFalse(nullEmail.isEmailValid());
    }

    @Test
    void testIsPasswordValid() {
        ResetReqDto valid = new ResetReqDto();
        valid.setEmail("name@email.com");
        valid.setNewPassword("Password1!");
        assertTrue(valid.isPasswordValid());

        ResetReqDto invalid = new ResetReqDto();
        invalid.setEmail("name@email.com");
        invalid.setNewPassword("password");
        assertFalse(invalid.isPasswordValid());

        ResetReqDto nullPassword = new ResetReqDto();
        nullPassword.setEmail("name@email.com");
        nullPassword.setNewPassword(null);
        assertFalse(nullPassword.isPasswordValid());

        ResetReqDto shortPassword = new ResetReqDto();
        shortPassword.setEmail("name@email.com");
        shortPassword.setNewPassword("Pwsd1!");
        assertFalse(shortPassword.isPasswordValid());

        ResetReqDto noSpecialChar = new ResetReqDto();
        noSpecialChar.setEmail("name@email.com");
        noSpecialChar.setNewPassword("Password1");
        assertFalse(noSpecialChar.isPasswordValid());
    }
}
