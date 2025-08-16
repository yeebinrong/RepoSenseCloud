package com.hamburger.user.service.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import io.jsonwebtoken.Jwts;

import java.util.Base64;

class JwtUtilTest {
    private static final String TEST_SECRET_KEY = Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
    private static final String USERNAME = "name";

    static {
        try {
            java.lang.reflect.Field field = java.lang.Class.forName("java.lang.ProcessEnvironment").getDeclaredField("theCaseInsensitiveEnvironment");
            field.setAccessible(true);
            ((java.util.Map<String, String>) field.get(null)).put("JWT_SECRET_KEY", TEST_SECRET_KEY);
        } catch (Exception e) {
            System.setProperty("JWT_SECRET_KEY", TEST_SECRET_KEY);
        }
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = JwtUtil.generateToken(USERNAME);
        assertNotNull(token);
        assertTrue(JwtUtil.validateToken(token));
    }

    @Test
    void testExtractUsername() {
        String token = JwtUtil.generateToken(USERNAME);
        String extractedUsername = JwtUtil.extractUsername(token);
        assertEquals(USERNAME, extractedUsername);
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid_token";
        assertFalse(JwtUtil.validateToken(invalidToken));
    }

    @Test
    void testExpiredToken() throws Exception {
        java.lang.reflect.Method method = JwtUtil.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true);
        javax.crypto.SecretKey key = (javax.crypto.SecretKey) method.invoke(null);
        String token = Jwts.builder()
            .subject(USERNAME)
            .issuedAt(new java.util.Date())
            .expiration(new java.util.Date(System.currentTimeMillis() + 1000)) // 1 second expiry for testing
            .signWith(key)
            .compact();
        Thread.sleep(1100); // Wait for token to expire
        assertFalse(JwtUtil.validateToken(token));
    }

    @Test
    void testInvalidSignatureToken() throws Exception {
        String otherKey = Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
        java.lang.reflect.Method method = JwtUtil.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true);
        javax.crypto.SecretKey invalidKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(otherKey));
        String token = Jwts.builder()
            .subject(USERNAME)
            .issuedAt(new java.util.Date())
            .expiration(new java.util.Date(System.currentTimeMillis() + 10000))
            .signWith(invalidKey)
            .compact();
        assertFalse(JwtUtil.validateToken(token));
    }
}
