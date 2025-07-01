package com.hamburger.user.middleware;

import com.hamburger.user.service.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorization extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Exclude specific URL patterns
        System.out.println("Request URI: " + requestURI);
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));
        if (requestURI.equals("/api/user/register") || requestURI.equals("/api/user/login")) {
            System.out.println("Skipping JWT validation for: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        System.out.println("Validating JWT for: " + requestURI);

        String token = null;

        // Check Authorization header for JWT token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Extract token after "Bearer "
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (!JwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT token is expired");
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }
}