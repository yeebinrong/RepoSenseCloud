package com.hamburger.user.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthReqDtoTest {
    @Test
    void testIsTokenValid() {
        AuthReqDto dtoWithToken = new AuthReqDto();
        dtoWithToken.setToken("valid_token");
        assertTrue(dtoWithToken.isTokenValid());

        AuthReqDto dtoWithEmptyToken = new AuthReqDto();
        dtoWithEmptyToken.setToken("");
        assertFalse(dtoWithEmptyToken.isTokenValid());

        AuthReqDto dtoWithNullToken = new AuthReqDto();
        dtoWithNullToken.setToken(null);
        assertFalse(dtoWithNullToken.isTokenValid());
    }
}
