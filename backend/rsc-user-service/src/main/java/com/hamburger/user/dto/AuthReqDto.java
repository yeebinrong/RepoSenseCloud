package com.hamburger.user.dto;

import lombok.Getter;
import lombok.Setter;

public class AuthReqDto {

    @Getter
    @Setter
    private String token;

    public boolean isTokenValid() {
        return token != null && !token.isEmpty();
    }
}