package com.hamburger.user.dao.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testGetId() {
        User user = new User();
        user.setId("user");
        assertEquals("user", user.getId());
    }
}
