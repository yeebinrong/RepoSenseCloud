package com.hamburger.user.dto;

import com.hamburger.user.service.util.PasswordUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "password")
public class RegisterReqDto {

    @Getter
    private String userName;

    @Getter
    // Email must be a well formatted non-null email address
    private String email;

    @Setter
    private String password;

    public String getHashedPassword() {
        System.out.println("Password hashed!");
        return PasswordUtil.hashPassword(password);
    }
}