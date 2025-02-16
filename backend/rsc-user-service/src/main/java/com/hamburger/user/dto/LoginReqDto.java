package com.hamburger.user.dto;

import com.hamburger.user.service.util.PasswordUtil;
import com.hamburger.user.service.util.JwtUtil;

import lombok.Getter;
import lombok.Setter;

public class LoginReqDto {

    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    private String password;

    public boolean isPasswordValid(String password, String hashedPassword) {
        return PasswordUtil.checkPassword(password, hashedPassword) && password.length() >= 8;
    }

    public String getToken(String userName) {
        return JwtUtil.generateToken(userName);
    }
}