package com.hamburger.job.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class JobUserAuthTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private JobUserAuth jobUserAuth;

    private static final String MOCK_TOKEN = "mock-jwt-token";
    private static final String MOCK_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the WebClient builder chain with correct types
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.cookie(eq("JWT"), anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        jobUserAuth = new JobUserAuth(webClientBuilder);
    }

    @Test
    void authorizeAction_ValidToken_ReturnsOkResponse() {
        // Arrange
        String validResponse = "Token is valid. Username: " + MOCK_USERNAME;
        when(responseSpec.bodyToMono(String.class))
            .thenReturn(Mono.just(validResponse));

        // Act
        ResponseEntity<String> response = jobUserAuth.authorizeAction(MOCK_TOKEN);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MOCK_USERNAME, response.getBody());
    }

    @Test
    void authorizeAction_InvalidToken_ReturnsUnauthorized() {
        // Arrange
        String invalidResponse = "Token is invalid";
        when(responseSpec.bodyToMono(String.class))
            .thenReturn(Mono.just(invalidResponse));

        // Act
        ResponseEntity<String> response = jobUserAuth.authorizeAction(MOCK_TOKEN);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthenticated: Invalid Token", response.getBody());
    }

    @Test
    void authorizeAction_NullToken_ReturnsNull() {
        // Act
        ResponseEntity<String> response = jobUserAuth.authorizeAction(null);

        // Assert
        assertNull(response);
    }

}