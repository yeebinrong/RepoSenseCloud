package com.hamburger.job.util;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.hamburger.job.models.dto.AuthRespDto;

@Component
public class JobUserAuth {

    private final WebClient webClient;

    public JobUserAuth(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://user-service-app:3001/api/user").build();
        
        // this.webClient = webClientBuilder.baseUrl(System.getenv("REACT_APP_USER_SERVICE_URL")).build();
        //  + System.getenv("REACT_APP_USER_SERVICE_URL"));
    }

    // Method validate JWT and returns username in body if valid
    public ResponseEntity<String> authorizeAction (String jwtToken){
        // jwtToken = "fake fake fake";
        System.out.println("Authenticating:" + jwtToken);
        if(jwtToken == null){
            System.out.println("Missing Token");
            return null;
        }
        return webClient.post()
                .uri("/auth")
                .bodyValue(Map.of("token", jwtToken))
                .retrieve()
                .bodyToMono(AuthRespDto.class)
                .doOnNext(response -> System.out.println("Response: " + response))
                .doOnError(e -> System.err.println("Authorization error: " + e))
                .map(response -> {
                    if ("Valid token".equals(response.getMessage())) {
                        System.out.println("Token Authenticated, Username: " + response.getUsername());
                        return ResponseEntity.status(HttpStatus.OK).body(response.getUsername());
                    } else {
                        System.err.println("Token Invalid");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthenticated: Invalid Token");
                    }
                })
                .block();
    }

}
