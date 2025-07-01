package com.hamburger.job.util;

import com.hamburger.job.models.dto.AuthRespDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Component
public class JobUserAuth extends OncePerRequestFilter {

    private final WebClient webClient;

    public JobUserAuth(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("http://rsc-user-service:" + System.getenv("USER_SVC_PORT") + "/api/user")
            .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        try {
            String token = extractJwtFromRequest(request);
            System.out.println("Extracted JWT token: " + token);
            if (Objects.isNull(token)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing JWT token");
                return;
            }

            AuthRespDto authResponse = webClient.post()
                .uri("/auth")
                .bodyValue(Map.of("token", token))
                .retrieve()
                .bodyToMono(AuthRespDto.class)
                .doOnError(e -> System.err.println("Auth error: " + e.getMessage()))
                .block();

            if (authResponse == null || !"Valid token".equals(authResponse.getMessage())) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token");
                return;
            }

            request.setAttribute("owner", authResponse.getUsername());
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.err.println("JWT middleware error: " + e.getMessage());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: " + e.getMessage());
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (Objects.nonNull(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
