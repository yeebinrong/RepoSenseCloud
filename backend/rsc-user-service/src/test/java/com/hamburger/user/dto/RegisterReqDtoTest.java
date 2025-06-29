package com.hamburger.user.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterReqDtoTest {
    @Test
    void testIsEmailValid() {
        RegisterReqDto dto = new RegisterReqDto();
        setEmail(dto, "name@email.com");
        assertTrue(dto.isEmailValid());
        setEmail(dto, "name@email");
        assertFalse(dto.isEmailValid());
        setEmail(dto, null);
        assertFalse(dto.isEmailValid());
    }

    @Test
    void testIsPasswordValid() {
        RegisterReqDto dto = new RegisterReqDto();
        dto.setPassword("Password!1");
        assertTrue(dto.isPasswordValid());
        dto.setPassword("Password!");
        assertFalse(dto.isPasswordValid());
        dto.setPassword(null);
        assertFalse(dto.isPasswordValid());
    }

    @Test
    void testGetHashedPassword() {
        RegisterReqDto dto = new RegisterReqDto();
        dto.setPassword("Password!1");
        String hashed = dto.getHashedPassword();
        assertNotNull(hashed);
        assertNotEquals("Password!1", hashed);
    }

    private void setEmail(RegisterReqDto dto, String email) {
        try {
            java.lang.reflect.Field field = RegisterReqDto.class.getDeclaredField("email");
            field.setAccessible(true);
            field.set(dto, email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
