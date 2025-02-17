package com.hamburger.user.service.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;

import java.util.Date;

public class JwtUtil {
    private static final String secretKey = System.getenv("JWT_SECRET_KEY");
    private static final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds

    public static String generateToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey())
            .compact();
    }

    public static boolean validateToken(String token) {
       try {
           Jwts.parser().verifyWith(getSigningKey()).build().parse(token);
           return true;
       } catch (SignatureException e) {
           System.out.println("Invalid JWT signature: " + e.getMessage());
       } catch (MalformedJwtException e) {
           System.out.println("Invalid JWT token: " + e.getMessage());
       } catch (ExpiredJwtException e) {
           System.out.println("JWT token is expired: " + e.getMessage());
       }
        return false;
    }

    private static SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}