// package com.hamburger.job.util;

// import com.hamburger.job.models.dto.AuthRespDto;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.reactive.function.client.WebClient;
// import reactor.core.publisher.Mono;

// import java.util.Map;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// class JobUserAuthTest {

//     @Mock
//     private WebClient.Builder webClientBuilder;
    
//     @Mock 
//     private WebClient webClient;
    
//     @Mock
//     private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    
//     @Mock
//     private WebClient.RequestBodySpec requestBodySpec;
    
//     @Mock
//     private WebClient.RequestHeadersSpec requestHeadersSpec;
    
//     @Mock
//     private WebClient.ResponseSpec responseSpec;
    
//     private JobUserAuth jobUserAuth;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
        
//         // Setup WebClient mock chain
//         when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
//         when(webClientBuilder.build()).thenReturn(webClient);
//         when(webClient.post()).thenReturn(requestBodyUriSpec);
//         when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
//         when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
//         when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
//         jobUserAuth = new JobUserAuth(webClientBuilder);
//     }

//     @Test
//     void authorizeAction_withNullToken_returnsNull() {
//         // Act
//         ResponseEntity<String> result = jobUserAuth.authorizeAction(null);
        
//         // Assert
//         assertNull(result);
//     }

//     @Test
//     void authorizeAction_withValidToken_returnsOkResponse() {
//         // Arrange
//         AuthRespDto mockResponse = mock(AuthRespDto.class);
//         when(mockResponse.getMessage()).thenReturn("Valid token");
//         when(mockResponse.getUsername()).thenReturn("testUser");
//         when(responseSpec.bodyToMono(AuthRespDto.class)).thenReturn(Mono.just(mockResponse));
        
//         // Act
//         ResponseEntity<String> result = jobUserAuth.authorizeAction("valid-token");
        
//         // Assert
//         assertNotNull(result);
//         assertEquals(HttpStatus.OK, result.getStatusCode());
//         assertEquals("testUser", result.getBody());
        
//         // Verify WebClient calls
//         verify(requestBodyUriSpec).uri("/auth");
//         verify(requestBodySpec).bodyValue(Map.of("token", "valid-token"));
//     }

//     @Test
//     void authorizeAction_withInvalidToken_returnsUnauthorizedResponse() {
//         // Arrange
//         AuthRespDto mockResponse = mock(AuthRespDto.class);
//         when(mockResponse.getMessage()).thenReturn("Invalid token");
//         when(mockResponse.getUsername()).thenReturn("testUser");
//         when(responseSpec.bodyToMono(AuthRespDto.class)).thenReturn(Mono.just(mockResponse));
        
//         // Act
//         ResponseEntity<String> result = jobUserAuth.authorizeAction("invalid-token");
        
//         // Assert
//         assertNotNull(result);
//         assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
//         assertEquals("Unauthenticated: Invalid Token", result.getBody());
//     }

//     @Test
//     void authorizeAction_withNetworkError_handlesError() {
//         // Arrange
//         when(responseSpec.bodyToMono(AuthRespDto.class))
//             .thenReturn(Mono.error(new RuntimeException("Network error")));
        
//         // Act & Assert
//         assertThrows(RuntimeException.class, () -> {
//             jobUserAuth.authorizeAction("token");
//         });
//     }
// }