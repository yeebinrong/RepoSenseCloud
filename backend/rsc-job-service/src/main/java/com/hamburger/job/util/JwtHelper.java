package com.hamburger.job.util;

import java.util.Objects;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtHelper {

    public String extractJwtFromRequest(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        System.out.println("Extracting JWT from request: " + request);
        if (Objects.nonNull(jwt) && jwt.startsWith("Bearer ")) {
            return jwt.substring(7);
        }
        return null;
    }

}
