package com.hamburger.job.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtHelper {
    
    public String extractJwtFromRequest(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        System.out.println("Extracting JWT from request: " + request);
        if (jwt != null && jwt.startsWith("Bearer ")) {
            return jwt.substring(7);
        }
        return null;
    }

}
