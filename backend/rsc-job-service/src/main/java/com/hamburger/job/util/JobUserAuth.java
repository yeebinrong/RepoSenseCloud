package com.hamburger.job.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JobUserAuth {

    private final WebClient webClient;
    
    public JobUserAuth(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://user-service-app:3001/api/user").build();
    }

    public ResponseEntity<String> authorizeAction (String jwtToken){
        System.out.println("Authenticating:" + jwtToken);
        if(jwtToken == null){
            System.out.println("Missing Token");
            return null;
        }
        return webClient.post()
                .uri("/auth")
                .cookie("JWT", jwtToken)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Response: " + response))
                .doOnError(e -> System.err.println("Authorization error: " + e))
                .map(response -> {
                    if (response.contains("Token is valid")) {
                        System.out.println("Token Authenticated");
                        return ResponseEntity.status(HttpStatus.OK).body(response.split("Username: ")[1]);
                    } else {
                        System.err.println("Token Invalid");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthenticated: Invalid Token");
                    }
                })
                .block();
    }

}
