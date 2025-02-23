package com.hamburger.job.util;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class JobUserAuth {

    private final WebClient webClient;
    
    public JobUserAuth(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:3001/api/user").build();
    }

    public Mono<String> authorizeAction (String jwtToken){
        System.out.println("Authenticating:" + jwtToken);
        return webClient.post()
                .uri("/authorize")
                .cookie("JWT", jwtToken)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    if (response.contains("Token is valid")) {
                        return response.split("Username: ")[1];
                    } else {
                        throw new RuntimeException("Unauthorized");
                    }
                });
    }

}
